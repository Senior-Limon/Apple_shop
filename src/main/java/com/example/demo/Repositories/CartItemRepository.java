package com.example.demo.Repositories;

import com.example.demo.Data.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByCartId(Long cartId);

    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);

    void deleteByCartId(Long cartId);

    long countByCartId(Long cartId);

    @Query("SELECT SUM(ci.product.price * ci.quantity) FROM CartItem ci WHERE ci.cart.id = :cartId")
    Double getCartTotal(@Param("cartId") Long cartId);

    @Query("SELECT ci FROM CartItem ci JOIN FETCH ci.product WHERE ci.cart.user.id = :userId")
    List<CartItem> findUserCartWithProducts(@Param("userId") Long userId);

    @Query("SELECT ci FROM CartItem ci JOIN FETCH ci.product p JOIN FETCH p.images WHERE ci.cart.user.id = :userId")
    List<CartItem> findUserCartWithProductsAndImages(@Param("userId") Long userId);
}