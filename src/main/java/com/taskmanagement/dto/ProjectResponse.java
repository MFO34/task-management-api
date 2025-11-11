package com.taskmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectResponse {

    private Long id;
    private String name;
    private String description;
    
    // Owner bilgisi (nested object)
    private UserSummary owner;
    
    // Member'lar (nested list)
    private List<ProjectMemberResponse> members;
    
    private Integer memberCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Nested DTO (Owner için basit bilgi)
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserSummary {
        private Long id;
        private String email;
        private String fullName;
    }
}