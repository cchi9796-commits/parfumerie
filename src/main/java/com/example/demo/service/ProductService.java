package com.example.demo.service;

import com.example.demo.dto.Dtos.*;
import com.example.demo.entity.Brand;
import com.example.demo.entity.Product;
import com.example.demo.entity.ProductVariant;
import com.example.demo.exception.ApiException;
import com.example.demo.repository.BrandRepository;
import com.example.demo.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {
    private final ProductRepository products;
    private final BrandRepository brands;

    public List<ProductSummary> search(String q, String gender, String brand) {
        return products.search(q.trim(), gender, brand).stream()
                .filter(p -> !p.getVariants().isEmpty())
                .map(p -> {
                    ProductVariant v = p.getVariants().stream()
                            .min(Comparator.comparingInt(ProductVariant::getPrice)).get();
                    return new ProductSummary(p.getId(), p.getName(), p.getBrand().getName(),
                            p.getGender(), p.getImageUrl(), v.getPrice(), v.getSizeMl(), v.getId());
                }).toList();
    }

    public ProductDetail get(Long id) {
        Product p = products.findById(id).filter(Product::isActive)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm"));
        List<VariantDto> vs = p.getVariants().stream()
                .sorted(Comparator.comparingInt(ProductVariant::getSizeMl))
                .map(v -> new VariantDto(v.getId(), v.getSizeMl(), v.getPrice(), v.getStock()))
                .toList();
        return new ProductDetail(p.getId(), p.getName(), p.getBrand().getName(), p.getGender(),
                p.getDescription(), p.getImageUrl(), vs);
    }

    public List<String> brandNames() {
        return brands.findAll().stream().map(Brand::getName).sorted().toList();
    }

    @Transactional
    public Long create(ProductRequest r) {
        Product p = new Product();
        apply(p, r);
        return products.save(p).getId();
    }

    @Transactional
    public void update(Long id, ProductRequest r) {
        Product p = products.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm"));
        apply(p, r);
    }

    @Transactional
    public void hide(Long id) {   // xóa mềm
        Product p = products.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm"));
        p.setActive(false);
    }

    private void apply(Product p, ProductRequest r) {
        if (!List.of("Nam", "Nữ", "Unisex").contains(r.gender()))
            throw new ApiException(HttpStatus.BAD_REQUEST, "Giới tính phải là Nam, Nữ hoặc Unisex");

        Brand b = brands.findByName(r.brandName().trim()).orElseGet(() -> {
            Brand nb = new Brand();
            nb.setName(r.brandName().trim());
            return brands.save(nb);
        });
        p.setName(r.name().trim());
        p.setBrand(b);
        p.setGender(r.gender());
        p.setDescription(r.description());
        p.setImageUrl(r.imageUrl());

        Map<Integer, ProductVariant> current = p.getVariants().stream()
                .collect(Collectors.toMap(ProductVariant::getSizeMl, v -> v));
        for (VariantRequest vr : r.variants()) {
            ProductVariant pv = current.get(vr.sizeMl());
            if (pv == null) {
                pv = new ProductVariant();
                pv.setProduct(p);
                pv.setSizeMl(vr.sizeMl());
                p.getVariants().add(pv);
            }
            pv.setPrice(vr.price());
            pv.setStock(vr.stock());
        }
    }
}