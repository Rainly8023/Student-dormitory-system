package com.dorm.controller;

import com.dorm.dto.Result;
import com.dorm.service.RepairService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/repairs")
@RequiredArgsConstructor
public class RepairController {
    private final RepairService repairService;

    @GetMapping
    public Result<?> list(@RequestParam(required = false) String status,
                          @RequestParam(required = false) String repairType,
                          @RequestParam(required = false) Long studentId,
                          @RequestParam(defaultValue = "1")  int page,
                          @RequestParam(defaultValue = "20") int size) {
        return Result.success(repairService.list(status, repairType, studentId, page, size));
    }

    @PostMapping
    public Result<?> create(@RequestBody RepairService.RepairDto dto) {
        return Result.success(repairService.create(dto));
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id,
                             @RequestBody RepairService.UpdateDto dto,
                             Authentication auth) {
        return Result.success(repairService.update(id, dto, (Long) auth.getPrincipal()));
    }
}
