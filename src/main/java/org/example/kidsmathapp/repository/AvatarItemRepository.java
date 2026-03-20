package org.example.kidsmathapp.repository;

import org.example.kidsmathapp.entity.AvatarItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AvatarItemRepository extends JpaRepository<AvatarItem, Long> {
    boolean existsByName(String name);

    @Query("SELECT ai FROM AvatarItem ai WHERE ai.id NOT IN " +
           "(SELECT ci.item.id FROM ChildInventory ci WHERE ci.child.id = :childId)")
    List<AvatarItem> findEligibleItemsForChild(@Param("childId") Long childId);
}
