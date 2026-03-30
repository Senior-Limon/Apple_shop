package com.example.demo.Repositories;

import com.example.demo.Data.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // Все заказы пользователя
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    // Заказы по статусу (для админки)
    List<Order> findByStatusOrderByCreatedAtDesc(String status);

    // Сортировка как ты хотел: NEW → SHIPPED → DELIVERED → CANCELLED
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
}