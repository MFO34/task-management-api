package com.taskmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {
    
    private List<T> content;           // Veri listesi
    private int pageNumber;            // Mevcut sayfa (0-indexed)
    private int pageSize;              // Sayfa başına item sayısı
    private long totalElements;        // Toplam item sayısı
    private int totalPages;            // Toplam sayfa sayısı
    private boolean first;             // İlk sayfa mı?
    private boolean last;              // Son sayfa mı?
    private boolean empty;             // Boş mu?
    
    // Helper method: Spring Page'den PageResponse'a dönüştürme
    public static <T> PageResponse<T> of(org.springframework.data.domain.Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .empty(page.isEmpty())
                .build();
    }
}