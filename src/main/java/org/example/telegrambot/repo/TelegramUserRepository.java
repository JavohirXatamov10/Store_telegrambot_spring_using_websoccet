package org.example.telegrambot.repo;

import org.example.telegrambot.entity.TelegramUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TelegramUserRepository extends JpaRepository<TelegramUser, UUID> {

    TelegramUser findByChatId(Long chatId);

}