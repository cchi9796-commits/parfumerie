package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "order_items")
@Getter @Setter
public class OrderItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private CustomerOrder order;

    @Column(nullable = false)
    private Long variantId;

    @Column(nullable = false)
    private String productName;

    @Column(length = 500)
    private String imageUrl;

    private int sizeMl;
    private int unitPrice;
    private int qty;
}