package com.example.demo.Repositories;

import com.example.demo.Data.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    // Все фото конкретного товара
    List<ProductImage> findByProductId(Long productId);

    // Главное фото товара
    ProductImage findByProductIdAndIsMainTrue(Long productId);

    // Удалить все фото товара (например, при удалении товара)
    void deleteByProductId(Long productId);
}