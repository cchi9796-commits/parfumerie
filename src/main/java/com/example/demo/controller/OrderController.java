package com.example.demo.controller;

import com.example.demo.dto.Dtos.*;
import com.example.demo.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderDto create(Authentication auth, @Valid @RequestBody OrderRequest r) {
        return service.create(auth.getName(), r);
    }

    @GetMapping
    public List<OrderDto> mine(Authentication auth) {
        return service.mine(auth.getName());
    }

    @GetMapping("/{code}")
    public OrderDto detail(Authentication auth, @PathVariable String code) {
        return service.detail(auth.getName(), code);
    }

    @PatchMapping("/{code}/cancel")
    public OrderDto cancel(Authentication auth, @PathVariable String code) {
        return service.cancel(auth.getName(), code);
    }
}