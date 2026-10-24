package com.example.demo.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class Dtos {

    // ---- Sản phẩm: dữ liệu trả về ----
    public record VariantDto(Long id, int sizeMl, int price, int stock) {}

    public record ProductSummary(Long id, String name, String brand, String gender,
                                 String imageUrl, int price, int size, Long variantId) {}

    public record ProductDetail(Long id, String name, String brand, String gender,
                                String description, String imageUrl, List<VariantDto> variants) {}

    // ---- Sản phẩm: dữ liệu gửi lên (admin) ----
    public record VariantRequest(@Min(1) int sizeMl, @Min(0) int price, @Min(0) int stock) {}

    public record ProductRequest(@NotBlank String name, @NotBlank String brandName,
                                 @NotBlank String gender, String description, String imageUrl,
                                 @NotEmpty List<@Valid VariantRequest> variants) {}
}