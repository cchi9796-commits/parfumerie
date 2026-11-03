package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter @Setter
public class CustomerOrder {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String orderCode;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private User user;

    @Column(nullable = false)
    private long total;

    @Column(nullable = false)
    private String shipName;

    @Column(nullable = false)
    private String shipPhone;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String shipAddress;

    private String shipNote = "";

    @Column(nullable = false, length = 10)
    private String paymentMethod;     // bank | cod

    @Column(nullable = false, length = 15)
    private String status = "pending";

    @CreationTimestamp
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();
}