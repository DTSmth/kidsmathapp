package org.example.kidsmathapp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.kidsmathapp.dto.inventory.*;
import org.example.kidsmathapp.entity.*;
import org.example.kidsmathapp.entity.enums.*;
import org.example.kidsmathapp.exception.ApiException;
import org.example.kidsmathapp.repository.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final ChildInventoryRepository childInventoryRepository;
    private final AvatarItemRepository avatarItemRepository;
    private final ChildRepository childRepository;
    private final StreakRepository streakRepository;

    @Transactional(readOnly = true)
    public InventoryDto getInventory(Long childId) {
        if (!childRepository.existsById(childId)) {
            throw ApiException.notFound("Child not found");
        }
        List<ChildInventory> allItems = childInventoryRepository.findAllByChildIdWithItem(childId);
        List<ChildInventory> equipped = childInventoryRepository.findEquippedByChildId(childId);
        Map<String, InventoryItemDto> equippedMap = equipped.stream()
                .collect(Collectors.toMap(
                        ci -> ci.getEquippedSlot().name(),
                        this::toDto
                ));
        List<InventoryItemDto> items = allItems.stream().map(this::toDto).collect(Collectors.toList());
        return InventoryDto.builder()
                .childId(childId)
                .items(items)
                .equipped(equippedMap)
                .totalItems(items.size())
                .build();
    }

    /**
     * Equip an item to a slot. Unequips any existing item in that slot first.
     * MANDATORY propagation — must be called within an existing transaction.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public InventoryItemDto equipItem(Long childId, Long itemId, ItemType slot) {
        ChildInventory inv = childInventoryRepository.findByChildIdAndItemId(childId, itemId)
                .orElseThrow(() -> ApiException.forbidden("Item not in your inventory"));
        if (slot != null && inv.getItem().getItemType() != slot) {
            throw ApiException.badRequest("This item cannot be equipped in slot " + slot);
        }
        // Unequip existing item in same slot
        if (slot != null) {
            childInventoryRepository.findEquippedByChildId(childId).stream()
                    .filter(ci -> slot == ci.getEquippedSlot())
                    .forEach(ci -> { ci.setEquippedSlot(null); childInventoryRepository.save(ci); });
        }
        inv.setEquippedSlot(slot);
        childInventoryRepository.save(inv);
        log.info("Item equipped: child={} item={} slot={}", childId, itemId, slot);
        return toDto(inv);
    }

    /**
     * Claim daily login bonus. Idempotent — returns same item if already claimed today.
     */
    @Transactional
    public DailyBonusResponse claimDailyBonus(Long childId) {
        Child child = childRepository.findById(childId)
                .orElseThrow(() -> ApiException.notFound("Child not found"));
        LocalDate today = LocalDate.now();

        // Find or create today's streak record
        Streak todayStreak = streakRepository.findByChildIdAndDate(childId, today)
                .orElseGet(() -> {
                    Streak s = Streak.builder()
                            .child(child).date(today).practiceCount(0).dailyBonusClaimed(false)
                            .build();
                    return streakRepository.save(s);
                });

        if (Boolean.TRUE.equals(todayStreak.getDailyBonusClaimed())) {
            // Already claimed — find the item that was granted today
            List<ChildInventory> todayItems = childInventoryRepository.findByChildIdOrderByEarnedAtDesc(childId)
                    .stream()
                    .filter(ci -> ci.getEarnedAt().toLocalDate().equals(today))
                    .toList();
            if (!todayItems.isEmpty()) {
                log.debug("Daily bonus already claimed today for child {}", childId);
                return DailyBonusResponse.builder()
                        .itemGranted(true).item(toDto(todayItems.get(0))).alreadyClaimed(true).build();
            }
            return DailyBonusResponse.builder().itemGranted(false).alreadyClaimed(true).build();
        }

        // Select a random COMMON item not yet owned
        List<AvatarItem> eligible = avatarItemRepository.findEligibleItemsForChild(childId)
                .stream().filter(i -> i.getTier() == ItemTier.COMMON).collect(Collectors.toList());

        if (eligible.isEmpty()) {
            log.warn("No common items available for daily bonus for child {}", childId);
            todayStreak.setDailyBonusClaimed(true);
            streakRepository.save(todayStreak);
            return DailyBonusResponse.builder().itemGranted(false).alreadyClaimed(false).build();
        }

        AvatarItem selected = eligible.get(new Random().nextInt(eligible.size()));
        ChildInventory granted = ChildInventory.builder()
                .child(child).item(selected).earnedAt(LocalDateTime.now()).build();
        try {
            childInventoryRepository.save(granted);
        } catch (DataIntegrityViolationException e) {
            // Race condition — already granted
            return DailyBonusResponse.builder().itemGranted(false).alreadyClaimed(true).build();
        }

        todayStreak.setDailyBonusClaimed(true);
        streakRepository.save(todayStreak);
        log.info("Daily bonus granted: child={} item={} ({})", childId, selected.getName(), selected.getEmoji());
        return DailyBonusResponse.builder()
                .itemGranted(true).item(toDto(granted)).alreadyClaimed(false).build();
    }

    /**
     * Consume a streak shield. Returns true if shield was consumed.
     * MANDATORY — must be within an existing transaction.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public boolean consumeShield(Long childId) {
        // A streak shield is an item with the name "Streak Shield"
        List<ChildInventory> allItems = childInventoryRepository.findAllByChildIdWithItem(childId);
        Optional<ChildInventory> shield = allItems.stream()
                .filter(ci -> "Streak Shield".equals(ci.getItem().getName()))
                .findFirst();
        if (shield.isEmpty()) {
            return false;
        }
        childInventoryRepository.delete(shield.get());
        log.info("Streak shield consumed for child {}", childId);
        return true;
    }

    /**
     * Dispatch an item drop. Returns Optional.empty() if no eligible items.
     * MANDATORY — must be within an existing transaction.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public Optional<InventoryItemDto> dispatchItemDrop(Long childId, ItemDropSource source) {
        Child child = childRepository.findById(childId)
                .orElseThrow(() -> ApiException.notFound("Child not found"));

        List<AvatarItem> eligible = avatarItemRepository.findEligibleItemsForChild(childId);
        if (eligible.isEmpty()) {
            log.debug("No eligible items for drop: child={} source={}", childId, source);
            return Optional.empty();
        }

        // Filter by tier based on source
        List<AvatarItem> pool;
        if (source == ItemDropSource.DAILY_BONUS) {
            pool = eligible.stream().filter(i -> i.getTier() == ItemTier.COMMON).collect(Collectors.toList());
        } else if (source == ItemDropSource.MILESTONE) {
            pool = eligible; // any tier on milestone
        } else {
            // Weighted random: COMMON=70%, RARE=25%, LEGENDARY=5%
            double roll = Math.random();
            ItemTier targetTier;
            if (roll < 0.70) targetTier = ItemTier.COMMON;
            else if (roll < 0.95) targetTier = ItemTier.RARE;
            else targetTier = ItemTier.LEGENDARY;
            pool = eligible.stream().filter(i -> i.getTier() == targetTier).collect(Collectors.toList());
            if (pool.isEmpty()) pool = eligible.stream()
                    .filter(i -> i.getTier() == ItemTier.COMMON).collect(Collectors.toList());
            if (pool.isEmpty()) pool = eligible;
        }

        if (pool.isEmpty()) {
            return Optional.empty();
        }

        AvatarItem selected = pool.get(new Random().nextInt(pool.size()));
        ChildInventory inv = ChildInventory.builder()
                .child(child).item(selected).earnedAt(LocalDateTime.now()).build();
        try {
            childInventoryRepository.save(inv);
        } catch (DataIntegrityViolationException e) {
            return Optional.empty(); // race condition, item already in inventory
        }
        log.info("Item drop: child={} item={} ({}) source={}", childId, selected.getName(), selected.getEmoji(), source);
        return Optional.of(toDto(inv));
    }

    private InventoryItemDto toDto(ChildInventory ci) {
        return InventoryItemDto.builder()
                .inventoryId(ci.getId())
                .itemId(ci.getItem().getId())
                .name(ci.getItem().getName())
                .emoji(ci.getItem().getEmoji())
                .tier(ci.getItem().getTier())
                .itemType(ci.getItem().getItemType())
                .equipped(ci.getEquippedSlot() != null)
                .equippedSlot(ci.getEquippedSlot())
                .earnedAt(ci.getEarnedAt())
                .build();
    }
}
