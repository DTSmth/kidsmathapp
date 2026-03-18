package org.example.kidsmathapp.service;

import lombok.RequiredArgsConstructor;
import org.example.kidsmathapp.dto.progress.PointsHistoryDto;
import org.example.kidsmathapp.entity.Child;
import org.example.kidsmathapp.entity.PointsLog;
import org.example.kidsmathapp.exception.ApiException;
import org.example.kidsmathapp.repository.ChildRepository;
import org.example.kidsmathapp.repository.PointsLogRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PointsService {

    private final PointsLogRepository pointsLogRepository;
    private final ChildRepository childRepository;

    @Transactional
    public int awardPoints(Long childId, int points, String reason) {
        Child child = childRepository.findById(childId)
                .orElseThrow(() -> ApiException.notFound("Child not found with id: " + childId));

        PointsLog pointsLog = PointsLog.builder()
                .child(child)
                .points(points)
                .reason(reason)
                .build();

        pointsLogRepository.save(pointsLog);

        int newTotal = child.getTotalStars() + points;
        child.setTotalStars(newTotal);
        childRepository.save(child);

        return newTotal;
    }

    @Transactional(readOnly = true)
    public List<PointsHistoryDto> getPointsHistory(Long childId, int limit) {
        if (!childRepository.existsById(childId)) {
            throw ApiException.notFound("Child not found with id: " + childId);
        }

        List<PointsLog> logs = pointsLogRepository.findByChildIdOrderByCreatedAtDesc(
                childId, PageRequest.of(0, limit));

        return logs.stream()
                .map(this::toPointsHistoryDto)
                .collect(Collectors.toList());
    }

    private PointsHistoryDto toPointsHistoryDto(PointsLog log) {
        return PointsHistoryDto.builder()
                .id(log.getId())
                .points(log.getPoints())
                .reason(log.getReason())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
