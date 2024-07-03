package org.example.telegrambot.component;

import lombok.RequiredArgsConstructor;
import org.example.telegrambot.entity.Category;
import org.example.telegrambot.entity.Product;
import org.example.telegrambot.repo.CategoryRepository;
import org.example.telegrambot.repo.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@RequiredArgsConstructor
public class Runnable implements CommandLineRunner {
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;


    @Override
    public void run(String... args) throws Exception {
        getInfo();
    }

    private void getInfo() {
        Category category2= Category.builder().name("Ichimliklar").build();
        Category category3= Category.builder().name("Mevalar").build();
        categoryRepository.save(category2);
        categoryRepository.save(category3);

        Product product3= Product.builder().name("Aloe").price(10000).categoryId(category2.getId()).image("aloe.jpg").build();
        Product product4= Product.builder().name("Chortoq").price(12000).categoryId(category2.getId()).image("chortoq.jpg").build();
        Product product5= Product.builder().name("Olma").price(8000).categoryId(category3.getId()).image("apple.jpg").build();
        Product product6= Product.builder().name("Banan").price(22000).categoryId(category3.getId()).image("banan.jpg").build();
        productRepository.save(product3);
        productRepository.save(product4);
        productRepository.save(product5);
        productRepository.save(product6);
    }
}


