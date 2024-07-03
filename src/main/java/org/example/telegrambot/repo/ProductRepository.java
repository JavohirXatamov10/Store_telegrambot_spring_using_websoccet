package org.example.telegrambot.repo;

import org.example.telegrambot.entity.Product;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {


    List<Product> findAllByCategoryId(UUID categoryId);

    @NotNull
    Optional<Product> findById(@NotNull UUID productId);
}