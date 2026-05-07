package com.example.demo.Data;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(nullable = false)
    private String name;

    private String description;
    private BigDecimal price;
    private String color;
    private String memory;
    private String chip;

    @Column(name = "stock_quantity")
    private Integer stockQuantity = 0;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<ProductImage> images = new ArrayList<>();

    // Хелперный метод для получения главного фото
    public ProductImage getMainImage() {
        if (images != null) {
            return images.stream()
                    .filter(ProductImage::isMain)
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    public String getImageUrl() {
        return images != null && !images.isEmpty()
                ? images.stream().filter(ProductImage::isMain).findFirst().map(ProductImage::getImageUrl).orElse("/images/no-image.png")
                : "/images/no-image.png";
    }
}