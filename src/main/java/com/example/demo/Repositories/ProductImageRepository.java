package com.example.demo.Repositories;

import com.example.demo.Data.Product;
import com.example.demo.Data.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    // Все фото конкретного товара
    List<ProductImage> findByProductId(Long productId);


    // Удалить все фото товара (например, при удалении товара)
    void deleteByProductId(Long productId);

    Optional<ProductImage> findByProductIdAndIsMainTrue(Long productId);

    @Query("SELECT DISTINCT p FROM Product p " +
            "LEFT JOIN FETCH p.images " +
            "WHERE p.category.id = :categoryId")
    List<Product> findByCategoryIdWithImages(@Param("categoryId") Long categoryId);
}