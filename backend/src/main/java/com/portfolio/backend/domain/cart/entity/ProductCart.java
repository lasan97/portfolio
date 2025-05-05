package com.portfolio.backend.domain.cart.entity;

import com.portfolio.backend.domain.cart.value.ProductCartItem;
import com.portfolio.backend.domain.product.entity.Product;
import com.portfolio.backend.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "product_carts")
public class ProductCart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "product_cart_items", joinColumns = @JoinColumn(name = "product_cart_id"))
    private List<ProductCartItem> items = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public ProductCart(User user) {
        this.user = user;
    }

    public List<ProductCartItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public void addItem(Product product, int quantity) {
        ProductCartItem item = new ProductCartItem(product, quantity);
        items.remove(item);
        items.add(item);
    }

    public void removeItem(Product product) {
        ProductCartItem item = new ProductCartItem(product);
        items.remove(item);
    }

    public void removeAllItem() {
        items.clear();
    }
}
