package com.example.demo.Data;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Order order;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "price_at_purchase", nullable = false)
    private BigDecimal priceAtPurchase;

    private Integer quantity;
    private String color;
    private String memory;
    private String chip;

    // Специальный конструктор для создания из корзины
    public OrderItem(Order order, Product product, Integer quantity) {
        this.order = order;
        this.product = product;
        this.productName = product.getName();
        this.priceAtPurchase = product.getPrice();
        this.quantity = quantity;
        this.color = product.getColor();
        this.memory = product.getMemory();
        this.chip = product.getChip();
    }

    // Метод для подсчета суммы по позиции
    public BigDecimal getSubtotal() {
        return priceAtPurchase.multiply(BigDecimal.valueOf(quantity));
    }
}