package org.example.kidsmathapp.controller;

import lombok.RequiredArgsConstructor;
import org.example.kidsmathapp.entity.User;
import org.example.kidsmathapp.exception.ApiException;
import org.example.kidsmathapp.repository.ChildRepository;
import org.example.kidsmathapp.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ControllerHelper {

    private final UserRepository userRepository;
    private final ChildRepository childRepository;

    public Long getParentId(UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> ApiException.unauthorized("User not found"));
        return user.getId();
    }

    public void validateChildOwnership(UserDetails userDetails, Long childId) {
        Long parentId = getParentId(userDetails);
        if (!childRepository.existsByIdAndParentId(childId, parentId)) {
            throw ApiException.forbidden("You do not have access to this child profile");
        }
    }
}
