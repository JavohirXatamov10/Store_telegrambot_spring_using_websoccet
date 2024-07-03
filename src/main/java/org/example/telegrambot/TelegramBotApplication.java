package org.example.telegrambot;

import com.pengrad.telegrambot.TelegramBot;
import org.example.telegrambot.bot.MyBot;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class TelegramBotApplication {


    public static void main(String[] args) {
        SpringApplication.run(TelegramBotApplication.class, args);
    }
    @Bean
    public TelegramBot telegramBot(){
        return new TelegramBot("6750351492:AAFeUg--3UmgZi5dcPCpbhFdoFBvC7oTjxw");
    }



}
