package org.example.telegrambot.entity;

import com.pengrad.telegrambot.model.Update;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.telegrambot.entity.enums.TelegramState;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity

public class TelegramUser {
    @Id
    private Long chatId;
    private String firstName;
    private String lastName;
    @Enumerated(EnumType.STRING)
    private TelegramState telegramState;
    private UUID chosenCategoryId;
    private UUID chosenProductId;
    private Integer counter=1;

   // private Integer productMessageId;




    public String getFullName() {
        return firstName+" "+lastName;
    }
    public boolean checkStatus(TelegramState telegramState) {
        return this.telegramState.equals(telegramState);
    }

}
