package com.example.eshop.cart.application.usecases.createcart;

import com.example.eshop.cart.domain.cart.Cart;

public interface CreateCartService {
    /**
     * Creates new {@link Cart} for given customer
     *
     * @throws CartAlreadyExistException if cart already exist for this customer
     */
    void create(String customerId);
}
