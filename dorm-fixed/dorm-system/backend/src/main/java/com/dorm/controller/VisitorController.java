package com.dorm.controller;

import com.dorm.dto.Result;
import com.dorm.service.VisitorService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/visitors")
@RequiredArgsConstructor
public class VisitorController {
    private final VisitorService visitorService;

    @GetMapping
    public Result<?> list(@RequestParam(required = false) String status,
                          @RequestParam(required = false) String date,
                          @RequestParam(required = false) Long studentId,
                          @RequestParam(defaultValue = "1")  int page,
                          @RequestParam(defaultValue = "20") int size) {
        return Result.success(visitorService.list(status, date, studentId, page, size));
    }

    @PostMapping
    public Result<?> create(@RequestBody VisitorService.VisitorDto dto, Authentication auth) {
        return Result.success(visitorService.create(dto, (Long) auth.getPrincipal()));
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return Result.success(visitorService.update(id, body.get("status"), body.get("leaveTime")));
    }
}
