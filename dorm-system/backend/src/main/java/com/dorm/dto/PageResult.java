package com.dorm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResult<T> {
    private List<T> records;
    private long total;
    private int page;
    private int pageSize;
    private int totalPages;

    public static <T> PageResult<T> of(Page<T> page) {
        return new PageResult<>(
            page.getContent(),
            page.getTotalElements(),
            page.getNumber() + 1,
            page.getSize(),
            page.getTotalPages()
        );
    }
}
