package com.dorm.service;

import com.dorm.dto.PageResult;
import com.dorm.entity.*;
import com.dorm.repository.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final UserRepository userRepository;

    public PageResult<Map<String, Object>> list(int page, int size) {
        var paged = noticeRepository.findAllWithAuthor(PageRequest.of(page - 1, size));
        return PageResult.of(paged.map(this::toMap));
    }

    public Notice create(NoticeDto dto, Long authorId) {
        Notice n = new Notice();
        n.setTitle(dto.getTitle());
        n.setContent(dto.getContent());
        n.setCategory(dto.getCategory() != null ? dto.getCategory() : "NOTICE");
        n.setIsPinned(Boolean.TRUE.equals(dto.getIsPinned()));
        n.setAuthor(userRepository.findById(authorId).orElse(null));
        return noticeRepository.save(n);
    }

    public Notice update(Long id, NoticeDto dto) {
        Notice n = noticeRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("公告不存在"));
        n.setTitle(dto.getTitle());
        n.setContent(dto.getContent());
        n.setCategory(dto.getCategory());
        n.setIsPinned(Boolean.TRUE.equals(dto.getIsPinned()));
        n.setUpdatedAt(LocalDateTime.now());
        return noticeRepository.save(n);
    }

    public void delete(Long id) {
        noticeRepository.deleteById(id);
    }

    private Map<String, Object> toMap(Notice n) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", n.getId());
        m.put("title", n.getTitle());
        m.put("content", n.getContent());
        m.put("category", n.getCategory());
        m.put("isPinned", n.getIsPinned());
        m.put("authorName", n.getAuthor() != null ? n.getAuthor().getRealName() : null);
        m.put("createdAt", n.getCreatedAt());
        m.put("updatedAt", n.getUpdatedAt());
        return m;
    }

    @Data public static class NoticeDto { private String title; private String content; private String category; private Boolean isPinned; }
}
