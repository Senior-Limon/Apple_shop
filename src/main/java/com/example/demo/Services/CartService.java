package com.example.demo.Services;

import com.example.demo.Data.Cart;
import com.example.demo.Data.CartItem;
import com.example.demo.Data.User;
import com.example.demo.Repositories.CartItemRepository;
import com.example.demo.Repositories.CartRepository;
import com.example.demo.Repositories.ProductRepository;
import com.example.demo.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private CartItemRepository cartItemRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private UserRepository userRepository;

    @Transactional
    public Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId).orElseGet(() -> {
            Cart cart = new Cart();
            cart.setUser(userRepository.getReferenceById(userId));
            return cartRepository.save(cart);
        });
    }

    @Transactional
    public void addToCart(Long userId, Long productId, int quantity) {
        Cart cart = getOrCreateCart(userId);
        CartItem existing = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId).orElse(null);
        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + quantity);
            cartItemRepository.save(existing);
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(productRepository.findById(productId).orElseThrow());
            newItem.setQuantity(quantity);
            cartItemRepository.save(newItem);
        }
    }

    @Transactional(readOnly = true)
    public List<CartItem> getCartItems(Long userId) {
        // Используем метод с JOIN FETCH для загрузки изображений
        return cartItemRepository.findUserCartWithProductsAndImages(userId);
    }

    @Transactional
    public void updateQuantity(Long userId, Long cartItemId, int quantity) {
        CartItem item = cartItemRepository.findById(cartItemId).orElseThrow();
        if (!item.getCart().getUser().getId().equals(userId)) throw new RuntimeException("Not your cart");
        if (quantity <= 0) cartItemRepository.delete(item);
        else { item.setQuantity(quantity); cartItemRepository.save(item); }
    }

    @Transactional
    public void removeItem(Long userId, Long cartItemId) {
        CartItem item = cartItemRepository.findById(cartItemId).orElseThrow();
        if (!item.getCart().getUser().getId().equals(userId)) throw new RuntimeException("Not your cart");
        cartItemRepository.delete(item);
    }
}