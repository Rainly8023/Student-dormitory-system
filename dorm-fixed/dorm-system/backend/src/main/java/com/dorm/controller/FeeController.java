package com.dorm.controller;

import com.dorm.dto.Result;
import com.dorm.service.FeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/fees")
@RequiredArgsConstructor
public class FeeController {
    private final FeeService feeService;

    @GetMapping
    public Result<?> list(@RequestParam(required = false) String status,
                          @RequestParam(required = false) String month,
                          @RequestParam(required = false) Long roomId,
                          @RequestParam(defaultValue = "1")  int page,
                          @RequestParam(defaultValue = "20") int size) {
        return Result.success(feeService.list(status, month, roomId, page, size));
    }

    @PostMapping
    public Result<?> create(@RequestBody FeeService.FeeDto dto) {
        return Result.success(feeService.create(dto));
    }

    @PostMapping("/{id}/pay")
    public Result<?> pay(@PathVariable Long id) {
        return Result.success(feeService.pay(id));
    }
}
