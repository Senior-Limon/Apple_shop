package com.example.demo.Repositories;

import com.example.demo.Data.Order;
import com.example.demo.Data.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    // Все товары в заказе
    List<OrderItem> findByOrderId(Long orderId);

    // История покупок конкретного товара
    List<OrderItem> findByProductId(Long productId);

    // Товары из заказов пользователя
    @Query("SELECT oi FROM OrderItem oi " +
            "WHERE oi.order.user.id = :userId " +
            "ORDER BY oi.order.createdAt DESC")
    List<OrderItem> findUserOrderItems(@Param("userId") Long userId);



    // Загрузка заказа со всеми товарами (одним запросом)
    @Query("SELECT DISTINCT o FROM Order o " +
            "LEFT JOIN FETCH o.items " +
            "WHERE o.id = :id")
    Optional<Order> findByIdWithItems(@Param("id") Long id);

    // Заказы пользователя с товарами (одним запросом)
    @Query("SELECT DISTINCT o FROM Order o " +
            "LEFT JOIN FETCH o.items " +
            "WHERE o.user.id = :userId " +
            "ORDER BY o.createdAt DESC")
    List<Order> findByUserIdWithItems(@Param("userId") Long userId);
}