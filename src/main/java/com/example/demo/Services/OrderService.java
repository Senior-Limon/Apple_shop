package com.example.demo.Services;

import com.example.demo.Data.Cart;
import com.example.demo.Data.CartItem;
import com.example.demo.Data.Order;
import com.example.demo.Data.OrderItem;
import com.example.demo.Data.Product;
import com.example.demo.Data.User;
import com.example.demo.Repositories.CartRepository;
import com.example.demo.Repositories.OrderItemRepository;
import com.example.demo.Repositories.OrderRepository;
import com.example.demo.Repositories.ProductRepository;
import com.example.demo.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    // Получить заказы пользователя
    public List<Order> getUserOrders(Long userId) {
        return orderRepository.findByUserIdWithItems(userId);
    }

    // Получить заказ с товарами
    public Order getOrderWithItems(Long orderId) {
        return orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new RuntimeException("Заказ не найден"));
    }

    // Создание заказа с проверкой остатков и блокировкой для многопоточности
    @Transactional
    public Order createOrderWithStockCheck(Long userId, String customerName, String customerPhone,
                                           String deliveryAddress, String paymentType, String comment) {

        // 1. Получаем корзину
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Корзина не найдена"));

        List<CartItem> cartItems = cart.getItems();

        if (cartItems.isEmpty()) {
            throw new RuntimeException("Корзина пуста");
        }

        // 2. Проверяем остатки товаров (с блокировкой для многопоточности)
        for (CartItem cartItem : cartItems) {
            Product product = productRepository.findByIdWithLock(cartItem.getProduct().getId())
                    .orElseThrow(() -> new RuntimeException("Товар не найден: " + cartItem.getProduct().getName()));

            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new RuntimeException("Товара \"" + product.getName() + "\" недостаточно на складе. " +
                        "Доступно: " + product.getStockQuantity() + " шт., запрошено: " + cartItem.getQuantity() + " шт.");
            }
        }

        // 3. Резервируем товары (уменьшаем остатки)
        for (CartItem cartItem : cartItems) {
            Product product = productRepository.findByIdWithLock(cartItem.getProduct().getId())
                    .orElseThrow(() -> new RuntimeException("Товар не найден"));

            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(product);
        }

        // 4. Создаём заказ
        User user = userRepository.findById(userId).orElseThrow();

        Order order = new Order();
        order.setUser(user);
        order.setCustomerName(customerName);
        order.setCustomerPhone(customerPhone);
        order.setDeliveryAddress(deliveryAddress);
        order.setPaymentType(paymentType);
        order.setComment(comment);
        order.setStatus("NEW");
        order.setCreatedAt(LocalDateTime.now());

        BigDecimal total = BigDecimal.ZERO;

        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setProductName(cartItem.getProduct().getName());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPriceAtPurchase(cartItem.getProduct().getPrice());
            orderItem.setColor(cartItem.getProduct().getColor());
            orderItem.setMemory(cartItem.getProduct().getMemory());
            orderItem.setChip(cartItem.getProduct().getChip());

            order.getItems().add(orderItem);
            total = total.add(orderItem.getPriceAtPurchase().multiply(BigDecimal.valueOf(orderItem.getQuantity())));
        }

        order.setTotalCost(total);

        // 5. Сохраняем заказ
        Order savedOrder = orderRepository.save(order);

        // 6. Очищаем корзину
        cart.getItems().clear();
        cartRepository.save(cart);

        return savedOrder;
    }

    // Для админа: обновить статус заказа
    @Transactional
    public Order updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Заказ не найден"));
        order.setStatus(status);
        return orderRepository.save(order);
    }

    // Для админа: получить все заказы
    public List<Order> getAllOrders() {
        return orderRepository.findAllSortedByStatus();
    }
}