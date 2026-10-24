package com.example.demo.repository;

import com.example.demo.entity.CustomerOrder;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {
    List<CustomerOrder> findByUserOrderByCreatedAtDesc(User user);
    Optional<CustomerOrder> findByOrderCodeAndUser(String orderCode, User user);
    Optional<CustomerOrder> findByOrderCode(String orderCode);
    List<CustomerOrder> findAllByOrderByCreatedAtDesc();
}