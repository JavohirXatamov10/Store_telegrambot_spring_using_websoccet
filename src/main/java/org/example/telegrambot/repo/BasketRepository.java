package org.example.telegrambot.repo;

import org.example.telegrambot.entity.Basket;
import org.example.telegrambot.entity.TelegramUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BasketRepository extends JpaRepository<Basket, Integer> {

    List<Basket> findAllByTelegramUser(TelegramUser user);
}