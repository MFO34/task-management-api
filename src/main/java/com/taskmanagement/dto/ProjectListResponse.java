package com.taskmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectListResponse {

    private Long id;
    private String name;
    private String description;
    private String ownerName;  // Sadece owner'ın adı (nested yok)
    private Integer memberCount;
    private LocalDateTime createdAt;
}