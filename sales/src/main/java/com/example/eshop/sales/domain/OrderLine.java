package com.example.eshop.sales.domain;

import com.example.eshop.sharedkernel.domain.Assertions;
import com.example.eshop.sharedkernel.domain.base.Entity;
import com.example.eshop.sharedkernel.domain.valueobject.Ean;
import com.example.eshop.sharedkernel.domain.valueobject.Money;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.hibernate.Hibernate;
import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.Objects;

/**
 * Represents single line in {@link Order}.
 * <p>
 * Each line is identified by EAN and has information about
 * product, price and quantity.
 */
@javax.persistence.Entity
@Table(
        name = "order_lines",
        indexes = @Index(name = "order_lines_order_id_idx", columnList = "order_id")
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderLine implements Entity<Long> {
    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false)
    private Long id;

    @Embedded
    @NotNull
    private Ean ean;

    @Column(name = "quantity", nullable = false)
    @Positive
    private int quantity;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "item_price", nullable = false))
    @AttributeOverride(name = "currency", column = @Column(name = "item_price_currency", nullable = false))
    @NotNull
    private Money itemPrice;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull
    private Order order;

    // TODO Attributes


    public OrderLine(Ean ean, int quantity, Money itemPrice, String productName) {
        Assertions.notNull(ean, "ean must be not null");
        Assertions.positive(quantity, "quantity must be positive");
        Assertions.notNull(itemPrice, "itemPrice must be not null");
        Assertions.notEmpty(productName, "productName must be not empty");

        this.ean = ean;
        this.quantity = quantity;
        this.itemPrice = itemPrice;
        this.productName = productName;
    }

    @Override
    public Long getId() {
        return id;
    }

    /**
     * @return total price of this line, i.e. {@code itemPrice * quantity}
     */
    public Money getPrice() {
        return itemPrice.multiply(quantity);
    }

    public Money getItemPrice() {
        return itemPrice;
    }

    public Ean getEan() {
        return ean;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getProductName() {
        return productName;
    }

    void setOrder(Order order) {
        this.order = order;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        OrderLine orderLine = (OrderLine) o;
        return id != null && Objects.equals(id, orderLine.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(order, ean);
    }
}
