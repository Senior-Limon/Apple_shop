package com.example.demo.Repositories;

import com.example.demo.Data.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Repository  // Отмечаем, что это репозиторий
public interface UserRepository extends JpaRepository<User, Long> {

    // Поиск пользователя по логину (для входа)
    Optional<User> findByLogin(String login);

    // Поиск пользователя по телефону
    Optional<User> findByPhone(String phone);

    // Проверка существования логина (для регистрации)
    boolean existsByLogin(String login);

    // Поиск всех пользователей с определенной ролью
    // Например, все админы
    List<User> findByRole(String role);

    // В UserRepository (добавь, если хочешь)
    boolean existsByPhone(String phone);

    // В ProductRepository (для админки)
    Page<User> findAll(Pageable pageable);  // Постраничный вывод

    // В OrderRepository (для статистики)
    @Query("SELECT SUM(o.totalCost) FROM Order o WHERE o.createdAt BETWEEN :start AND :end")
    BigDecimal getRevenueBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}