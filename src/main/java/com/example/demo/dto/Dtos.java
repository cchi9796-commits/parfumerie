package com.example.demo.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

import java.util.List;

public class Dtos {

    // ---- Sản phẩm: dữ liệu trả về ----
    public record VariantDto(Long id, int sizeMl, int price, int stock) {}

    public record ProductSummary(Long id, String name, String brand, String gender,
                                 String imageUrl, int price, int size, Long variantId,
                                 String description) {}

    public record ProductDetail(Long id, String name, String brand, String gender,
                                String description, String imageUrl, List<VariantDto> variants) {}

    // ---- Sản phẩm: dữ liệu gửi lên (admin) ----
    public record VariantRequest(@Min(1) int sizeMl, @Min(0) int price, @Min(0) int stock) {}

    public record ProductRequest(@NotBlank String name, @NotBlank String brandName,
                                 @NotBlank String gender, String description, String imageUrl,
                                 @NotEmpty List<@Valid VariantRequest> variants) {}
    // ---- Tài khoản ----
    public record RegisterRequest(@NotBlank String name,
                                  @NotBlank @Email String email,
                                  @Size(min = 6) String password) {}

    public record LoginRequest(@NotBlank String email, @NotBlank String password) {}

    public record UpdateProfileRequest(@NotBlank String name, String phone,
                                       String address, String password) {}

    public record UserDto(Long id, String name, String email,
                          String phone, String address, String role) {}

    public record AuthResponse(String token, UserDto user) {}

    // ---- Đơn hàng ----
    public record ItemRequest(@NotNull Long variantId, @Min(1) int qty) {}

    public record ShippingRequest(@NotBlank String name, @NotBlank String phone,
                                  @NotBlank String address, String note) {}

    public record OrderRequest(@NotEmpty List<@Valid ItemRequest> items,
                               @NotNull @Valid ShippingRequest shipping,
                               @NotBlank String paymentMethod,
                               String orderCode) {}

    // Tên trường khớp với những gì orders.html và order-detail.html đang dùng
    public record OrderItemDto(String name, String image, int size, int price, int qty) {}

    public record OrderDto(String id, long total, String status, String paymentMethod,
                           ShippingRequest shipping, LocalDateTime createdAt,
                           String userEmail, List<OrderItemDto> items) {}

    public record StatusRequest(@NotBlank String status) {}
}