package com.dorm.controller;

import com.dorm.dto.Result;
import com.dorm.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/stats")
@RequiredArgsConstructor
public class StatsController {
    private final StatsService statsService;

    @GetMapping("/overview")
    public Result<?> overview() {
        return Result.success(statsService.overview());
    }
}
