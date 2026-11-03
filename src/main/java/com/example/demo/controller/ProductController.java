package com.example.demo.controller;

import com.example.demo.dto.Dtos.*;
import com.example.demo.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService service;

    @GetMapping("/products")
    public List<ProductSummary> list(@RequestParam(defaultValue = "") String q,
                                     @RequestParam(defaultValue = "all") String gender,
                                     @RequestParam(defaultValue = "all") String brand) {
        return service.search(q, gender, brand);
    }

    @GetMapping("/products/{id}")
    public ProductDetail detail(@PathVariable Long id) {
        return service.get(id);
    }

    @GetMapping("/brands")
    public List<String> brands() {
        return service.brandNames();
    }
}