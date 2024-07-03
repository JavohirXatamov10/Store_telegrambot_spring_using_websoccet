package org.example.telegrambot.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.RequiredArgsConstructor;
import org.example.telegrambot.entity.Order;
import org.example.telegrambot.entity.enums.OrderStatus;
import org.example.telegrambot.repo.OrderRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final TelegramBot telegramBot;

    public List<Order> getOrdersByStatus(String status) {
        return orderRepository.findAllByStatus(OrderStatus.valueOf(status.toUpperCase()));
    }

    public void updateOrderStatus(String id, String status) {
        for (OrderStatus value : OrderStatus.values()) {
            if (status.equals(value.toString().toUpperCase())) {
                Order order = orderRepository.findById(UUID.fromString(id)).orElseThrow(() -> new RuntimeException("Order With Id: "+id+" Not Found"));
                order.setStatus(value);
                orderRepository.save(order);
                break;
            }
        }
        Order order = orderRepository.findById(UUID.fromString(id)).get();
        telegramBot.execute(new SendMessage(order.getTelegramUserId(),"Your Order Moved To "+status));
    }
}
