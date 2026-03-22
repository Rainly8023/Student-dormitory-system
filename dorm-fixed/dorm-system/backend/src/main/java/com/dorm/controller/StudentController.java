package com.dorm.controller;

import com.dorm.dto.Result;
import com.dorm.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/students")
@RequiredArgsConstructor
public class StudentController {
    private final StudentService studentService;

    @GetMapping
    public Result<?> list(@RequestParam(required = false) String keyword,
                          @RequestParam(required = false) String status,
                          @RequestParam(required = false) Long roomId,
                          @RequestParam(defaultValue = "1")  int page,
                          @RequestParam(defaultValue = "20") int size) {
        return Result.success(studentService.list(keyword, status, roomId, page, size));
    }

    @GetMapping("/{id}")
    public Result<?> getOne(@PathVariable Long id) {
        return Result.success(studentService.getOne(id));
    }

    @PostMapping
    public Result<?> create(@RequestBody StudentService.StudentDto dto) {
        return Result.success(studentService.create(dto));
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody StudentService.StudentDto dto) {
        return Result.success(studentService.update(id, dto));
    }

    @PostMapping("/{id}/checkout")
    public Result<?> checkout(@PathVariable Long id,
                               @RequestBody(required = false) Map<String, String> body) {
        String date = body != null ? body.get("checkOutDate") : null;
        studentService.checkout(id, date);
        return Result.success();
    }

    @PostMapping("/{id}/transfer")
    public Result<?> transfer(@PathVariable Long id,
                               @RequestBody StudentService.TransferDto dto) {
        studentService.transfer(id, dto.getNewRoomId(), dto.getBedNumber());
        return Result.success();
    }
}
