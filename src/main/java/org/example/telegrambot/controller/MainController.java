package org.example.telegrambot.controller;

import lombok.RequiredArgsConstructor;
import org.example.telegrambot.entity.Order;
import org.example.telegrambot.entity.enums.OrderStatus;
import org.example.telegrambot.repo.OrderRepository;
import org.example.telegrambot.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MainController {
    private final OrderService orderService;

    @GetMapping("/")
    public String getOrders(Model model) {
        List<Order> openOrders = orderService.getOrdersByStatus(OrderStatus.CREATED.toString());
        List<Order> inProgressOrders = orderService.getOrdersByStatus(OrderStatus.PROCESS.toString());
        List<Order> completedOrders = orderService.getOrdersByStatus(OrderStatus.COMPLETED.toString());


        model.addAttribute("openOrders", openOrders);
        model.addAttribute("inProgressOrders", inProgressOrders);
        model.addAttribute("completedOrders", completedOrders);

        return "order";
    }
}
