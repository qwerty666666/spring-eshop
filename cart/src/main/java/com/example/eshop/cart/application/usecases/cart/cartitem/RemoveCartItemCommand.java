package com.example.eshop.cart.application.usecases.cart.cartitem;

import com.example.eshop.sharedkernel.domain.valueobject.Ean;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

public record RemoveCartItemCommand(
        @NotEmpty String customerId,
        @NotNull Ean ean) {
}
