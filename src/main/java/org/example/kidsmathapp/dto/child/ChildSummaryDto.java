package org.example.kidsmathapp.dto.child;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChildSummaryDto {

    private Long id;
    private String name;
    private String avatarId;
    private Integer totalStars;
}
