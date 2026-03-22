package com.dorm.controller;

import com.dorm.dto.Result;
import com.dorm.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notices")
@RequiredArgsConstructor
public class NoticeController {
    private final NoticeService noticeService;

    @GetMapping
    public Result<?> list(@RequestParam(defaultValue = "1")  int page,
                          @RequestParam(defaultValue = "10") int size) {
        return Result.success(noticeService.list(page, size));
    }

    @PostMapping
    public Result<?> create(@RequestBody NoticeService.NoticeDto dto, Authentication auth) {
        return Result.success(noticeService.create(dto, (Long) auth.getPrincipal()));
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody NoticeService.NoticeDto dto) {
        return Result.success(noticeService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        noticeService.delete(id);
        return Result.success();
    }
}
