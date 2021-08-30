package com.example.eshop.cart.domain;

import com.example.eshop.sharedkernel.domain.Assertions;
import com.example.eshop.sharedkernel.domain.base.Entity;
import com.example.eshop.sharedkernel.domain.valueobject.Ean;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.hibernate.annotations.CreationTimestamp;
import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.time.LocalDateTime;
import java.util.Objects;

@javax.persistence.Entity
@Table(
        name = "cart_items",
        uniqueConstraints = @UniqueConstraint(columnNames = { "cart_id", "ean" })
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Slf4j
@ToString(onlyExplicitlyIncluded = true)
public class CartItem implements Entity<Long> {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue
    @Getter(AccessLevel.NONE)
    @ToString.Include
    private Long id;

    @Embedded
    @AttributeOverride(
            name = "ean",
            column = @Column(name = "ean", nullable = false)
    )
    @NotNull
    @ToString.Include
    private Ean ean;

    @Column(name = "quantity", nullable = false)
    @Positive
    @ToString.Include
    private int quantity;

    @ManyToOne
    @JoinColumn
    private Cart cart;

    @Column(name = "create_time", nullable = false)
    @CreationTimestamp
    private LocalDateTime creationTime;

    CartItem(Cart cart, Ean ean, int quantity) {
        Assertions.notNull(cart, "cart must be positive number");
        Assertions.notNull(ean, "EAN must be not null");
        Assertions.positive(quantity, "quantity must be positive number");

        this.cart = cart;
        this.ean = ean;
        this.quantity = quantity;
    }

    void setQuantity(int quantity) {
        Assertions.positive(quantity, "Quantity should be positive number");

        this.quantity = quantity;

        log.info("CartItem quantity changed" + this);
    }

    @Override
    public Long id() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        CartItem other = (CartItem) o;

        return Objects.equals(cart, other.cart) && Objects.equals(ean, other.ean);
    }

    @Override
    public int hashCode() {
        return ean.hashCode();
    }
}