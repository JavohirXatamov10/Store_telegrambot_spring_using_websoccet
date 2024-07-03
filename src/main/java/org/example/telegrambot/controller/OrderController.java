package org.example.telegrambot.controller;

import lombok.RequiredArgsConstructor;
import org.example.telegrambot.dto.OrderUpdateRequest;
import org.example.telegrambot.repo.OrderRepository;
import org.example.telegrambot.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping("/updateOrderStatus")
    public ResponseEntity<?> updateOrderStatus(@RequestBody OrderUpdateRequest request) {
        orderService.updateOrderStatus(request.getId(), request.getStatus());
        return ResponseEntity.ok().build();
    }
}
