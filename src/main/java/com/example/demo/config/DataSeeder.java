package com.example.demo.config;

import com.example.demo.dto.Dtos.ProductRequest;
import com.example.demo.dto.Dtos.VariantRequest;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {
    private final UserRepository users;
    private final ProductRepository products;
    private final ProductService productService;
    private final PasswordEncoder encoder;

    private static String img(String id) {
        return "https://images.unsplash.com/photo-" + id + "?auto=format&fit=crop&w=700&q=80";
    }

    private static VariantRequest v(int size, int price) {
        return new VariantRequest(size, price, 50);
    }

    private void add(String name, String brand, String gender, String imgId, VariantRequest... vs) {
        productService.create(new ProductRequest(name, brand, gender,
                "Nước hoa chính hãng " + name, img(imgId), List.of(vs)));
    }

    @Override
    public void run(String... args) {
        if (!users.existsByEmail("admin@parfumerie.vn")) {
            User admin = new User();
            admin.setName("Admin");
            admin.setEmail("admin@parfumerie.vn");
            admin.setPasswordHash(encoder.encode("admin123"));
            admin.setRole(Role.ADMIN);
            users.save(admin);
        }
        if (products.count() > 0) return;

        add("Dior Sauvage EDP",           "Dior",     "Nam",    "1541643600914-78b084683601", v(10, 375000));
        add("Creed Aventus For Men EDP",  "Creed",    "Nam",    "1594035910387-fea47794261f", v(10, 795000));
        add("Chanel Bleu De Chanel EDP",  "Chanel",   "Nam",    "1523293182086-7651a899d37f", v(10, 455000));
        add("YSL Libre EDP",              "YSL",      "Nữ",     "1592945403244-b3fbafd7f539", v(10, 395000));
        add("Tom Ford Ombré Leather EDP", "Tom Ford", "Unisex", "1588405748880-12d1d2a59f75", v(10, 575000));
        add("Le Labo Another 13 EDP",     "Le Labo",  "Unisex", "1595425970375-c71cb879413a", v(10, 875000));
        add("MFK Baccarat Rouge 540 EDP", "MFK",      "Unisex", "1615634260167-c8cdede054de", v(10, 795000));
        add("Kilian Black Phantom EDP",   "Kilian",   "Unisex", "1594035910387-fea47794261f", v(10, 995000));
        add("Versace Eros For Men EDT",   "Versace",  "Nam",    "1541643600914-78b084683601", v(10, 285000));
        // 3 sản phẩm cao cấp (id 10, 11, 12)
        add("Royal Noir Extrait", "L'Élixir", "Unisex", "1523293182086-7651a899d37f", v(50, 2850000), v(100, 4500000));
        add("Fleur Blanche",      "L'Élixir", "Nữ",     "1594035910387-fea47794261f", v(50, 2450000));
        add("Golden Amber",       "L'Élixir", "Unisex", "1588405748880-12d1d2a59f75", v(50, 3100000));
    }
}