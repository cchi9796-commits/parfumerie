package com.example.demo.controller;

import com.example.demo.dto.Dtos.*;
import com.example.demo.service.OrderService;
import com.example.demo.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final ProductService products;
    private final OrderService orders;

    // ---------- Sản phẩm ----------
    @PostMapping("/products")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Long> createProduct(@Valid @RequestBody ProductRequest r) {
        return Map.of("id", products.create(r));
    }

    @PutMapping("/products/{id}")
    public Map<String, String> updateProduct(@PathVariable Long id,
                                             @Valid @RequestBody ProductRequest r) {
        products.update(id, r);
        return Map.of("message", "Đã cập nhật sản phẩm");
    }

    @DeleteMapping("/products/{id}")
    public Map<String, String> hideProduct(@PathVariable Long id) {
        products.hide(id);
        return Map.of("message", "Đã ẩn sản phẩm");
    }

    // ---------- Đơn hàng ----------
    @GetMapping("/orders")
    public List<OrderDto> allOrders() {
        return orders.all();
    }

    @PatchMapping("/orders/{code}/status")
    public OrderDto updateStatus(@PathVariable String code,
                                 @Valid @RequestBody StatusRequest r) {
        return orders.updateStatus(code, r.status());
    }
}