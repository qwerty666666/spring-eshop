package com.example.eshop.cart.application.usecases.createcart;

import com.example.eshop.cart.domain.cart.Cart;
import com.example.eshop.cart.domain.cart.CartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class CreateCartServiceImpl implements CreateCartService {
    private final CartRepository cartRepository;

    @Override
    @Transactional
    public void create(String customerId) {
        var cart = new Cart(customerId);

        cartRepository.save(cart);

        log.info("Cart created for customer " + customerId);
    }
}
