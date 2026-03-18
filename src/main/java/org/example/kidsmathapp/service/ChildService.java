package org.example.kidsmathapp.service;

import lombok.RequiredArgsConstructor;
import org.example.kidsmathapp.dto.child.ChildDto;
import org.example.kidsmathapp.dto.child.ChildSummaryDto;
import org.example.kidsmathapp.dto.child.CreateChildRequest;
import org.example.kidsmathapp.dto.child.UpdateChildRequest;
import org.example.kidsmathapp.entity.Child;
import org.example.kidsmathapp.entity.User;
import org.example.kidsmathapp.exception.ApiException;
import org.example.kidsmathapp.mapper.ChildMapper;
import org.example.kidsmathapp.repository.ChildRepository;
import org.example.kidsmathapp.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChildService {

    private final ChildRepository childRepository;
    private final UserRepository userRepository;
    private final ChildMapper childMapper;

    @Transactional
    public ChildDto createChild(Long parentId, CreateChildRequest request) {
        User parent = userRepository.findById(parentId)
            .orElseThrow(() -> ApiException.notFound("Parent not found"));

        Child child = Child.builder()
            .name(request.getName())
            .avatarId(request.getAvatarId())
            .birthDate(request.getBirthDate())
            .gradeLevel(request.getGradeLevel())
            .totalStars(0)
            .currentStreak(0)
            .parent(parent)
            .build();

        child = childRepository.save(child);
        return childMapper.toDto(child);
    }

    @Transactional(readOnly = true)
    public List<ChildSummaryDto> getChildrenForParent(Long parentId) {
        List<Child> children = childRepository.findByParentIdOrderByNameAsc(parentId);
        return children.stream()
            .map(childMapper::toSummaryDto)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ChildDto getChild(Long childId, Long parentId) {
        Child child = childRepository.findByIdAndParentId(childId, parentId)
            .orElseThrow(() -> ApiException.notFound("Child not found"));

        return childMapper.toDto(child);
    }

    @Transactional
    public ChildDto updateChild(Long childId, Long parentId, UpdateChildRequest request) {
        Child child = childRepository.findByIdAndParentId(childId, parentId)
            .orElseThrow(() -> ApiException.notFound("Child not found"));

        if (request.getName() != null) {
            child.setName(request.getName());
        }
        if (request.getAvatarId() != null) {
            child.setAvatarId(request.getAvatarId());
        }
        if (request.getBirthDate() != null) {
            child.setBirthDate(request.getBirthDate());
        }
        if (request.getGradeLevel() != null) {
            child.setGradeLevel(request.getGradeLevel());
        }

        child = childRepository.save(child);
        return childMapper.toDto(child);
    }

    @Transactional
    public void deleteChild(Long childId, Long parentId) {
        Child child = childRepository.findByIdAndParentId(childId, parentId)
            .orElseThrow(() -> ApiException.notFound("Child not found"));

        childRepository.delete(child);
    }
}
