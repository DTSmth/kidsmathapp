package org.example.kidsmathapp.mapper;

import org.example.kidsmathapp.dto.child.ChildDto;
import org.example.kidsmathapp.dto.child.ChildSummaryDto;
import org.example.kidsmathapp.entity.Child;
import org.springframework.stereotype.Component;

@Component
public class ChildMapper {

    public ChildDto toDto(Child child) {
        if (child == null) {
            return null;
        }

        return ChildDto.builder()
            .id(child.getId())
            .name(child.getName())
            .avatarId(child.getAvatarId())
            .birthDate(child.getBirthDate())
            .gradeLevel(child.getGradeLevel())
            .totalStars(child.getTotalStars())
            .currentStreak(child.getCurrentStreak())
            .createdAt(child.getCreatedAt())
            .build();
    }

    public ChildSummaryDto toSummaryDto(Child child) {
        if (child == null) {
            return null;
        }

        return ChildSummaryDto.builder()
            .id(child.getId())
            .name(child.getName())
            .avatarId(child.getAvatarId())
            .totalStars(child.getTotalStars())
            .build();
    }
}
