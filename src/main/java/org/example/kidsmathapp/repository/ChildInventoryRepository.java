package org.example.kidsmathapp.repository;

import org.example.kidsmathapp.entity.ChildInventory;
import org.example.kidsmathapp.entity.enums.ItemType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChildInventoryRepository extends JpaRepository<ChildInventory, Long> {
    List<ChildInventory> findByChildIdOrderByEarnedAtDesc(Long childId);

    long countByChildId(Long childId);

    @Query("SELECT COUNT(ci) FROM ChildInventory ci WHERE ci.child.id = :childId AND ci.item.tier = 'COMMON' AND ci.equippedSlot IS NOT NULL AND ci.equippedSlot = :slotType")
    long countEquippedBySlot(@Param("childId") Long childId, @Param("slotType") ItemType slotType);

    Optional<ChildInventory> findByChildIdAndItemId(Long childId, Long itemId);

    @Query("SELECT ci FROM ChildInventory ci JOIN FETCH ci.item WHERE ci.child.id = :childId AND ci.equippedSlot IS NOT NULL")
    List<ChildInventory> findEquippedByChildId(@Param("childId") Long childId);

    @Query("SELECT CASE WHEN COUNT(ci) > 0 THEN TRUE ELSE FALSE END FROM ChildInventory ci JOIN ci.item i WHERE ci.child.id = :childId AND i.tier = 'SHIELD'")
    boolean hasStreakShield(@Param("childId") Long childId);

    @Query("SELECT ci FROM ChildInventory ci JOIN FETCH ci.item WHERE ci.child.id = :childId ORDER BY ci.earnedAt DESC")
    List<ChildInventory> findAllByChildIdWithItem(@Param("childId") Long childId);
}
