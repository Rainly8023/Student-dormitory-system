package com.dorm.controller;

import com.dorm.dto.Result;
import com.dorm.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rooms")
@RequiredArgsConstructor
public class RoomController {
    private final RoomService roomService;

    @GetMapping
    public Result<?> list(@RequestParam(required = false) Long buildingId,
                          @RequestParam(required = false) String status,
                          @RequestParam(defaultValue = "1")  int page,
                          @RequestParam(defaultValue = "20") int size) {
        return Result.success(roomService.list(buildingId, status, page, size));
    }

    @GetMapping("/available")
    public Result<?> available() {
        return Result.success(roomService.listAvailable());
    }

    @GetMapping("/{id}")
    public Result<?> detail(@PathVariable Long id) {
        return Result.success(roomService.detail(id));
    }

    @PostMapping
    public Result<?> create(@RequestBody RoomService.RoomDto dto) {
        return Result.success(roomService.save(dto));
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody RoomService.RoomDto dto) {
        dto.setId(id);
        return Result.success(roomService.save(dto));
    }
}
