package com.example.demo.Repositories;

import com.example.demo.Data.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // Все заказы пользователя
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    // Заказы по статусу (для админки)
    List<Order> findByStatusOrderByCreatedAtDesc(String status);

    // Сортировка NEW → SHIPPED → DELIVERED → CANCELLED
    @Query("SELECT o FROM Order o ORDER BY " +
            "CASE o.status " +
            "WHEN 'NEW' THEN 1 " +
            "WHEN 'SHIPPED' THEN 2 " +
            "WHEN 'DELIVERED' THEN 3 " +
            "WHEN 'CANCELLED' THEN 4 END, " +
            "o.createdAt DESC")
    List<Order> findAllSortedByStatus();

    // Количество заказов по статусам (для админки)
    @Query("SELECT o.status, COUNT(o) FROM Order o GROUP BY o.status")
    List<Object[]> getOrdersCountByStatus();

    // ============ НОВЫЕ МЕТОДЫ ============

    // Получить заказ с товарами (один запрос)
    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.items WHERE o.id = :id")
    Optional<Order> findByIdWithItems(@Param("id") Long id);

    // Получить заказы пользователя с товарами
    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.items WHERE o.user.id = :userId ORDER BY o.createdAt DESC")
    List<Order> findByUserIdWithItems(@Param("userId") Long userId);

    // Метод с блокировкой для создания заказа (многопоточность)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Order o WHERE o.id = :id")
    Optional<Order> findByIdWithLock(@Param("id") Long id);
}