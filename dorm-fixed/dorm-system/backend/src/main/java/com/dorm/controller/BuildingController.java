package com.dorm.controller;

import com.dorm.dto.Result;
import com.dorm.service.BuildingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/buildings")
@RequiredArgsConstructor
public class BuildingController {
    private final BuildingService buildingService;

    @GetMapping
    public Result<?> list() {
        return Result.success(buildingService.listAll());
    }

    @PostMapping
    public Result<?> create(@RequestBody BuildingService.BuildingDto dto) {
        return Result.success(buildingService.save(dto));
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody BuildingService.BuildingDto dto) {
        dto.setId(id);
        return Result.success(buildingService.save(dto));
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        buildingService.delete(id);
        return Result.success();
    }
}
