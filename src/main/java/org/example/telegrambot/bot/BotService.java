package org.example.telegrambot.bot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.request.EditMessageCaption;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SendPhoto;
import lombok.RequiredArgsConstructor;
import org.example.telegrambot.dto.OrderDto;
import org.example.telegrambot.entity.*;
import org.example.telegrambot.entity.enums.OrderStatus;
import org.example.telegrambot.entity.enums.TelegramState;
import org.example.telegrambot.interfaces.BotConstant;
import org.example.telegrambot.repo.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import java.io.File;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class BotService {

    private final TelegramBot telegramBot;
    private final TelegramUserRepository telegramUserRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final BasketRepository basketRepository;
    private final OrderRepository orderRepository;
    private final OrderProductRepository orderProductRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @Async
    public void handleUpdate(Update update) {
        if (update.message() != null) {
            Message message = update.message();
            TelegramUser user = getUser(message.from());
            if (message.text() != null) {
                if (message.text().equals("/start")) {
                    acceptStartAndChooseCategory(user);
                }
            }else if (update.callbackQuery() != null) {
                update.message();
            }
        } else if (update.callbackQuery() != null) {
            Long chatId = update.callbackQuery().from().id();
            String data = update.callbackQuery().data();
            TelegramUser user = getUser(update.callbackQuery().from());
            if (data.startsWith("chooseCategory")) {
                UUID categoryId = UUID.fromString(getParam(data, "id"));
                user.setChosenCategoryId(categoryId);
                telegramUserRepository.save(user);
                showCategoryProducts(user);
            } else if (data.startsWith("chooseProduct?id")) {
                UUID productId = UUID.fromString(getParam(data, "id"));
                user.setChosenProductId(productId);
                telegramUserRepository.save(user);
                showProduct(user);
            }
            else if (data.equals(BotConstant.BACK)){
                    back(user);
            }
            else if (user.checkStatus(TelegramState.PRODUCT_ADDED_TO_BASKET)){
                    if (data.equals(BotConstant.PRODUCT_ADDED_BASKET)){
                        back(user);
                    }
            }
            else if(user.checkStatus(TelegramState.PRODUCT_OPTION)){
                    if(data.equals(BotConstant.PLUS)) {
                    increaseCounterOfProduct(user, chatId, update);
                    }else if (data.equals(BotConstant.MINUS)) {
                    decreaseCounterOfProduct(user, chatId,update);
                    }else if(data.equals(BotConstant.ADD_TO_BASKET)){
                        addToBasket(user);
                        back(user);
                    }
            }
            else if(data.equals(BotConstant.BASKET)){
                SendMessage sendMessage = new SendMessage(
                        user.getChatId(),
                        "Your Basket:");
                sendMessage.replyMarkup(getBasketKeyboard(user));
                telegramBot.execute(sendMessage);
            }
            else if(data.contains("delete_")){
                int basketId = Integer.parseInt(data.replace("delete_", ""));
                basketRepository.deleteById(basketId);
                SendMessage sendMessage = new SendMessage(
                        user.getChatId(),
                        "Your Basket:");
                sendMessage.replyMarkup(getBasketKeyboard(user));
                telegramBot.execute(sendMessage);
            } else if (data.equals(BotConstant.MakeOrder)) {
                List<Basket> all = basketRepository.findAllByTelegramUser(user);
                if (!all.isEmpty()) {
                    Order order = Order.builder()
                        .telegramUserId(user.getChatId())
                        .orderDateTime(LocalDateTime.now())
                        .status(OrderStatus.CREATED)
                        .build();
                     orderRepository.save(order);

                     for (Basket basket : all) {
                    OrderProduct orderProduct = OrderProduct.builder()
                            .orderId(order.getId())
                            .productId(basket.getProduct().getId())
                            .amount(basket.getAmount())
                            .productId(basket.getProduct().getId())
                            .build();
                        orderProductRepository.save(orderProduct);
                    }
                    basketRepository.deleteAll(all);
                     sendStartMessage(user);
                     back(user);
                    simpMessagingTemplate.convertAndSend("/topic/orders",new OrderDto(order.getId().toString()));
                }
            }
        }
    }

    private InlineKeyboardMarkup getBasketKeyboard(TelegramUser user) {
        List<Basket> all = basketRepository.findAllByTelegramUser(user);
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        for (Basket basket : all) {
            InlineKeyboardButton productButton = new InlineKeyboardButton(basket.getProduct().getName())
                    .callbackData("?");
            InlineKeyboardButton deleteButton = new InlineKeyboardButton("❌")
                    .callbackData("delete_" + basket.getId());
            keyboardMarkup.addRow(productButton, deleteButton);}
        InlineKeyboardButton saveButton = new InlineKeyboardButton("Make order")
                .callbackData(BotConstant.MakeOrder);
        InlineKeyboardButton backButton = new InlineKeyboardButton("Back 🔙")
                .callbackData(BotConstant.BACK);
        keyboardMarkup.addRow(saveButton, backButton);
        return keyboardMarkup;
    }


    private void sendStartMessage(TelegramUser user) {
        SendMessage sendMessage = new SendMessage(
                user.getChatId(),
                "Order saved:");
        user.setTelegramState (TelegramState.START);
        telegramBot.execute(sendMessage);
    }

    private void updateState(TelegramUser user, TelegramState newStatus) {
        user.setTelegramState(newStatus);
        telegramUserRepository.save(user);
    }

        private void addToBasket(TelegramUser user) {
            Product product = productRepository.findById(user.getChosenProductId()).get();
            Basket basket = Basket.builder()
                    .telegramUser(user)
                    .product(product)
                    .amount(user.getCounter())
                    .build();
            basketRepository.save(basket);
            user.setTelegramState(TelegramState.START);
            telegramUserRepository.save(user);
        }
     private void back(TelegramUser user) {
        SendMessage sendMessage = new SendMessage(user.getChatId(),
                "Category");
        sendMessage.replyMarkup(generateCategoryBtn(categoryRepository.findAll()));
        telegramUserRepository.save(user);
        telegramBot.execute(sendMessage);
    }

//    private void addToBasket(Basket basket, UUID chosenProductId) {
//        BasketProduct basketProduct = BasketProduct.builder()
//                .basketId(basket.getId())
//                .productId(chosenProductId)
//                .amount(1)
//                .build();
//        basketProductRepository.save(basketProduct);
//
//    }
//
//    private Basket findOrCreateBasket(TelegramUser user) {
//        Long userId = user.getChatId();
//        Optional<Basket> basketOptional = basketRepository.findByCahtId(user.getChatId());
//        return basketOptional.orElseGet(() -> {
//            Basket newBasket = Basket.builder()
//                    .userId(userId)
//                    .build();
//            return basketRepository.save(newBasket);
//        });
//    }

    private void increaseCounterOfProduct(TelegramUser user, Long chatId, Update update) {
        Integer productCounter = user.getCounter();
        if (productCounter == null) {
            user.setCounter(1);
        } else {
            user.setCounter(user.getCounter() + 1);
            editProductMsg(update, chatId, user);
            telegramUserRepository.save(user);
        }

    }

    private void editProductMsg(Update update, Long chatId, TelegramUser user) {
        Product product = productRepository.findById(user.getChosenProductId()).get();
        Integer messageId = update.callbackQuery().message().messageId();
        EditMessageCaption editMessageCaption = new EditMessageCaption(chatId, messageId);
        editMessageCaption.caption(
                "Name: " + product.getName() + "\n" +
                        "Price: " + product.getPrice() + "\n" +
                        "Total Price: " + product.getPrice() * user.getCounter()
        );
        editMessageCaption.replyMarkup(generateCounterBtn(user));
        telegramBot.execute(editMessageCaption);
    }
    private void decreaseCounterOfProduct(TelegramUser user, Long chatId, Update update) {
        if (user.getCounter()== null) {
            user.setCounter(1);
        } else if (user.getCounter() > 1) {
            user.setCounter(user.getCounter() - 1);
            editProductMsg(update, chatId, user);
            telegramUserRepository.save(user);
        }
    }
    private void showProduct(TelegramUser user) {
        Product product = productRepository.findById(user.getChosenProductId()).get();
        SendPhoto sendPhoto = new SendPhoto(user.getChatId(), new File("files/"+product.getImage()));
        sendPhoto.caption(product.getName() + " " + product.getPrice());
        user.setCounter(1);
        sendPhoto.replyMarkup(generateCounterBtn(user));
        user.setTelegramState(TelegramState.PRODUCT_OPTION);
        telegramBot.execute(sendPhoto);
        telegramUserRepository.save(user);
    }
    public static String getParam(String data, String parameter) {
        data = data.substring(data.indexOf("?") + 1);
        String param = "";
        if (data.contains(parameter)) {
            int i = data.indexOf(parameter);
            String dataTemp = data.substring(i + parameter.length() + 1);
            if (dataTemp.contains("&")) {
                int andCharIndex = data.indexOf("&", i);
                param = data.substring(i + parameter.length() + 1, andCharIndex);
            } else {
                param = dataTemp;
            }
        }
        return param;
    }
    @Transactional
    public TelegramUser getUser(User user) {
        TelegramUser tgUser = telegramUserRepository.findByChatId(user.id());
        if (tgUser != null) {
            return tgUser;
        } else {
            return telegramUserRepository.save(TelegramUser.builder()
                    .chatId(user.id())
                    .firstName(user.firstName())
                    .lastName(user.lastName())
                    .telegramState(TelegramState.START)
                    .build());
        }
    }
    public void acceptStartAndChooseCategory(TelegramUser user) {
        SendMessage sendMessage = new SendMessage(user.getChatId(),
                "Welcome %s to our bot !!! Choose category".formatted(user.getFullName()));
        sendMessage.replyMarkup(generateCategoryBtn(categoryRepository.findAll()));
        telegramUserRepository.save(user);
        telegramBot.execute(sendMessage);
    }
    private void showCategoryProducts(TelegramUser user) {
        List<Product> products = productRepository.findAllByCategoryId(user.getChosenCategoryId());
        SendMessage sendMessage = new SendMessage(user.getChatId(),
                "Choose product");
        sendMessage.replyMarkup(generateProductBtn(products));
        user.setTelegramState(TelegramState.SELECT_PRODUCT);
        telegramUserRepository.save(user);
        telegramBot.execute(sendMessage);
    }
    public static InlineKeyboardMarkup generateCategoryBtn(List<Category> categories) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        int columnsCount = 3;
        for (int i = 0; i < categories.size(); i += columnsCount) {
            List<InlineKeyboardButton> buttons = new ArrayList<>();
            for (int j = 0; j < columnsCount && (i + j) < categories.size(); j++) {
                Category category = categories.get(i + j);
                buttons.add(new InlineKeyboardButton(category.getName()).callbackData("chooseCategory?id=" + category.getId()));}
            inlineKeyboardMarkup.addRow(buttons.toArray(new InlineKeyboardButton[0]));
        }
        InlineKeyboardButton basketButton = new InlineKeyboardButton("Basket 🛒")
                .callbackData(BotConstant.BASKET);
        inlineKeyboardMarkup.addRow(basketButton);
        return inlineKeyboardMarkup;
    }
    public static InlineKeyboardMarkup generateProductBtn(List<Product> products) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        int columnsCount = 3;
        for (int i = 0; i < products.size(); i += columnsCount) {
            List<InlineKeyboardButton> buttons = new ArrayList<>();
            for (int j = 0; j < columnsCount && (i + j) < products.size(); j++) {
                Product product = products.get(i + j);
                buttons.add(new InlineKeyboardButton(product.getName()).callbackData("chooseProduct?id=" + product.getId()));}
            inlineKeyboardMarkup.addRow(buttons.toArray(new InlineKeyboardButton[0]));
        }
        inlineKeyboardMarkup.addRow(new InlineKeyboardButton("Back").callbackData(BotConstant.BACK));
        return inlineKeyboardMarkup;
    }

    public  static InlineKeyboardMarkup generateCounterBtn(TelegramUser user) {
        InlineKeyboardMarkup inlineKeyboardMarkup =new InlineKeyboardMarkup();
        inlineKeyboardMarkup.addRow(
            new InlineKeyboardButton("➖").callbackData(BotConstant.MINUS),
            new InlineKeyboardButton(""+user.getCounter()).callbackData("?"),
            new InlineKeyboardButton("➕").callbackData(BotConstant.PLUS));
        inlineKeyboardMarkup.addRow(new InlineKeyboardButton("BACK").callbackData(BotConstant.BACK));
        inlineKeyboardMarkup.addRow(new InlineKeyboardButton("Basket").callbackData(BotConstant.ADD_TO_BASKET));
        return inlineKeyboardMarkup;
    }

}
