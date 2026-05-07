package com.example.demo.Repositories;

import com.example.demo.Data.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // Без сортировки (но с изображениями) – оставляем для совместимости
    @Query("SELECT DISTINCT p FROM Product p " +
            "LEFT JOIN FETCH p.images " +
            "WHERE p.category.id = :categoryId")
    List<Product> findByCategoryIdWithImages(@Param("categoryId") Long categoryId);

    // Сортировка по цене (возрастание)
    @Query("SELECT DISTINCT p FROM Product p " +
            "LEFT JOIN FETCH p.images " +
            "WHERE p.category.id = :categoryId " +
            "ORDER BY p.price ASC")
    List<Product> findByCategoryIdOrderByPriceAsc(@Param("categoryId") Long categoryId);

    // Сортировка по цене (убывание)
    @Query("SELECT DISTINCT p FROM Product p " +
            "LEFT JOIN FETCH p.images " +
            "WHERE p.category.id = :categoryId " +
            "ORDER BY p.price DESC")
    List<Product> findByCategoryIdOrderByPriceDesc(@Param("categoryId") Long categoryId);

    // Сортировка по названию (А-Я)
    @Query("SELECT DISTINCT p FROM Product p " +
            "LEFT JOIN FETCH p.images " +
            "WHERE p.category.id = :categoryId " +
            "ORDER BY p.name ASC")
    List<Product> findByCategoryIdOrderByNameAsc(@Param("categoryId") Long categoryId);

    // Сортировка по названию (Я-А)
    @Query("SELECT DISTINCT p FROM Product p " +
            "LEFT JOIN FETCH p.images " +
            "WHERE p.category.id = :categoryId " +
            "ORDER BY p.name DESC")
    List<Product> findByCategoryIdOrderByNameDesc(@Param("categoryId") Long categoryId);

    // Для страницы товара (оставляем без изменений)
    @Query("SELECT p FROM Product p " +
            "LEFT JOIN FETCH p.images " +
            "WHERE p.id = :id")
    Optional<Product> findByIdWithImages(@Param("id") Long id);
}