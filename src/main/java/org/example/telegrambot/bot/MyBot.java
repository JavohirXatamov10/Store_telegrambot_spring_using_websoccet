package org.example.telegrambot.bot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
import lombok.RequiredArgsConstructor;
import org.example.telegrambot.entity.TelegramUser;
import org.example.telegrambot.entity.enums.TelegramState;
import org.example.telegrambot.repo.TelegramUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
@Service
@RequiredArgsConstructor
public class MyBot implements CommandLineRunner {
    private final TelegramBot telegramBot;
    private final TelegramUserRepository telegramUserRepository;
    private final BotService botService;
    @Async
    @Override
    public void run(String... args) throws Exception {
        telegramBot.setUpdatesListener(updates -> {
            for (Update update : updates) {
                botService.handleUpdate(update);
            }
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        }, Throwable::printStackTrace);
    }



}
