package com.example.demo.Repositories;

import com.example.demo.Data.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Получение всех товаров с изображениями
    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.images")
    List<Product> findAllWithImages();

    // Поиск по названию с изображениями
    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.images WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Product> searchByNameWithImages(@Param("query") String query);

    // Простой поиск по названию
    List<Product> findByNameContainingIgnoreCase(String name);

    // Товары по категории
    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.images WHERE p.category.id = :categoryId")
    List<Product> findByCategoryIdWithImages(@Param("categoryId") Long categoryId);

    // Товары по категории с сортировкой
    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.images WHERE p.category.id = :categoryId ORDER BY p.price ASC")
    List<Product> findByCategoryIdOrderByPriceAsc(@Param("categoryId") Long categoryId);

    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.images WHERE p.category.id = :categoryId ORDER BY p.price DESC")
    List<Product> findByCategoryIdOrderByPriceDesc(@Param("categoryId") Long categoryId);

    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.images WHERE p.category.id = :categoryId ORDER BY p.name ASC")
    List<Product> findByCategoryIdOrderByNameAsc(@Param("categoryId") Long categoryId);

    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.images WHERE p.category.id = :categoryId ORDER BY p.name DESC")
    List<Product> findByCategoryIdOrderByNameDesc(@Param("categoryId") Long categoryId);

    // Получение одного товара с изображениями
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.images WHERE p.id = :id")
    Optional<Product> findByIdWithImages(@Param("id") Long id);

    // ============ МЕТОД С БЛОКИРОВКОЙ ДЛЯ МНОГОПОТОЧНОСТИ ============
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdWithLock(@Param("id") Long id);
}