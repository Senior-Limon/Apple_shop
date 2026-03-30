package com.example.demo.Repositories;

import com.example.demo.Data.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Все товары из определенной категории
    List<Product> findByCategoryId(Long categoryId);

    // Все активные товары
    List<Product> findByIsActiveTrue();

    // Товары дешевле указанной цены
    List<Product> findByPriceLessThan(BigDecimal price);

    // Товары с остатком меньше указанного (для уведомлений)
    List<Product> findByStockQuantityLessThan(int quantity);

    // Поиск по названию (частичное совпадение, без учета регистра)
    List<Product> findByNameContainingIgnoreCase(String keyword);

    // Сложный запрос: товары с фильтрацией (JPQL)
    @Query("SELECT p FROM Product p WHERE " +
            "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
            "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
            "(:maxPrice IS NULL OR p.price <= :maxPrice)")
    List<Product> findWithFilters(
            @Param("categoryId") Long categoryId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice
    );
}