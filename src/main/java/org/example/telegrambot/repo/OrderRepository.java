package org.example.telegrambot.repo;

import org.example.telegrambot.entity.Order;
import org.example.telegrambot.entity.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    List<Order> findAllByStatus(OrderStatus orderStatus);
}