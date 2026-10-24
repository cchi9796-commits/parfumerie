package com.example.demo.repository;

import com.example.demo.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query("""
        SELECT p FROM Product p JOIN p.brand b
        WHERE p.active = true
          AND (:q = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :q, '%'))
                       OR LOWER(b.name) LIKE LOWER(CONCAT('%', :q, '%')))
          AND (:gender = 'all' OR p.gender = :gender)
          AND (:brand = 'all' OR b.name = :brand)
        ORDER BY p.createdAt DESC, p.id DESC
        """)
    List<Product> search(@Param("q") String q,
                         @Param("gender") String gender,
                         @Param("brand") String brand);
}