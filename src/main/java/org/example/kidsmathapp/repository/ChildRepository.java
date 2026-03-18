package org.example.kidsmathapp.repository;

import org.example.kidsmathapp.entity.Child;
import org.example.kidsmathapp.entity.enums.GradeLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChildRepository extends JpaRepository<Child, Long> {

    List<Child> findByParentId(Long parentId);

    Optional<Child> findByIdAndParentId(Long id, Long parentId);

    boolean existsByIdAndParentId(Long id, Long parentId);

    List<Child> findByGradeLevel(GradeLevel gradeLevel);

    List<Child> findByParentIdOrderByNameAsc(Long parentId);
}
