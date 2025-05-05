package com.portfolio.backend.service.cart;

import com.portfolio.backend.common.exception.ResourceNotFoundException;
import com.portfolio.backend.domain.cart.entity.ProductCart;
import com.portfolio.backend.domain.cart.repository.ProductCartRepository;
import com.portfolio.backend.domain.product.entity.Product;
import com.portfolio.backend.domain.product.entity.ProductStatus;
import com.portfolio.backend.domain.product.repository.ProductRepository;
import com.portfolio.backend.domain.user.entity.User;
import com.portfolio.backend.domain.user.repository.UserRepository;
import com.portfolio.backend.service.cart.dto.ProductCartServiceMapper;
import com.portfolio.backend.service.cart.dto.ProductCartServiceRequest.AddItem;
import com.portfolio.backend.service.cart.dto.ProductCartServiceRequest.RemoveItem;
import com.portfolio.backend.service.cart.dto.ProductCartServiceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductCartService {

    private final ProductCartRepository productCartRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public final ProductCartServiceMapper mapper;

    @Transactional(readOnly = true)
    public List<ProductCartServiceResponse.Get> getCartItems(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 사용자가 존재하지 않습니다."));

        ProductCart productCart = productCartRepository.findByUserId(userId)
                .orElseGet(() -> productCartRepository.save(new ProductCart(user)));

        return productCart.getItems().stream().map(mapper::toGet).toList();
    }

    @Transactional
    public void addCartItem(AddItem request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 사용자가 존재하지 않습니다."));
        Product product = productRepository.findByIdAndStatusNot(request.productId(), ProductStatus.DELETED)
                .orElseThrow(() -> new ResourceNotFoundException("상품을 찾을 수 없습니다. ID: " + request.productId()));

        ProductCart productCart = productCartRepository.findByUserId(userId)
                .orElseGet(() -> productCartRepository.save(new ProductCart(user)));

        productCart.addItem(product, request.quantity());
    }

    @Transactional
    public void removeCartItem(RemoveItem request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 사용자가 존재하지 않습니다."));
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ResourceNotFoundException("상품을 찾을 수 없습니다. ID: " + request.productId()));

        ProductCart productCart = productCartRepository.findByUserId(userId)
                .orElseGet(() -> productCartRepository.save(new ProductCart(user)));

        productCart.removeItem(product);
    }
}
