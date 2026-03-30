package com.example.demo.Repositories;

import com.example.demo.Data.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    // Найти корзину пользователя
    Optional<Cart> findByUserId(Long userId);

    // Проверить, есть ли корзина у пользователя
    boolean existsByUserId(Long userId);
}