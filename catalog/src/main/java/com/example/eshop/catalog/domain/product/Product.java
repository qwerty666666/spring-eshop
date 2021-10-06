package com.example.eshop.catalog.domain.product;

import com.example.eshop.catalog.domain.file.File;
import com.example.eshop.catalog.domain.product.Product.ProductId;
import com.example.eshop.sharedkernel.domain.Assertions;
import com.example.eshop.sharedkernel.domain.base.AggregateRoot;
import com.example.eshop.sharedkernel.domain.base.DomainObjectId;
import com.example.eshop.sharedkernel.domain.valueobject.Ean;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.lang.Nullable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.NamedEntityGraphs;
import javax.persistence.NamedSubgraph;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Product is a group of {@link Sku}, where each SKU is a distinct
 * product variant with unique {@link Attribute} set (like size, color, etc.).
 * <p>
 * From catalog perspective, users will work with {@code Product}
 * instead of {@code SKU}.
 */
@Entity
@Table(name = "products")
@NamedEntityGraphs({
        // Product.sku + Product.sku.attributes + Product.images
        @NamedEntityGraph(
                name = "Product.skuAndImages",
                attributeNodes = {
                        @NamedAttributeNode(value = "sku", subgraph = "sku.attributes"),
                        @NamedAttributeNode(value = "images"),
                },
                subgraphs = {
                        @NamedSubgraph(
                                name = "sku.attributes",
                                attributeNodes = @NamedAttributeNode(
                                        value = "attributes",
                                        subgraph = "sku.attributes.attribute"
                                )
                        ),
                        @NamedSubgraph(
                                name = "sku.attributes.attribute",
                                attributeNodes = @NamedAttributeNode(value = "attribute")
                        ),
                }
        )
})
@Getter
@ToString(onlyExplicitlyIncluded = true)
@Slf4j
public class Product extends AggregateRoot<ProductId> {
    @EmbeddedId
    @ToString.Include
    private ProductId id;

    @Column(name = "name", nullable = false)
    @ToString.Include
    private String name;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Sku> sku = new HashSet<>();

    @OneToMany(mappedBy = "product")
    private Set<ProductCategory> categories = new HashSet<>();

    @OneToMany
    @JoinTable(
            name = "product_images",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "file_id")
    )
    @OrderColumn(name = "sort")
    private List<File> images = new ArrayList<>();

    protected Product() {
        this(DomainObjectId.randomId(ProductId.class));
    }

    protected Product(ProductId id) {
        this.id = id;
    }

    @Override
    public ProductId getId() {
        return id;
    }

    public void setName(String name) {
        Assertions.notEmpty(name, "Name must be non empty");
        this.name = name;
    }

    /**
     * Set available quantity for SKU with given {@code ean}
     *
     * @throws SkuNotFoundException if SKU with given {@code ean} not found in this Product
     */
    public void setSkuAvailableQuantity(Ean ean, int availableQuantity) {
        Assertions.notNull(ean, "EAN must be non empty");
        getSku(ean).changeAvailableQuantity(availableQuantity);
    }

    /**
     * @throws SkuNotFoundException if SKU with given {@code ean} not found in this Product
     */
    public Sku getSku(Ean ean) {
        return this.sku.stream()
                .filter(sku -> sku.getEan().equals(ean))
                .findFirst()
                .orElseThrow(() -> new SkuNotFoundException("SKU " + ean + " does not exist in Product " + this));
    }

    public List<Sku> getSku() {
        return sku.stream()
                // We can't use sort on field (using @SortComparator or @OrderBy) because
                // comparator depends on nested collection Sku::attributes, and this
                // collection is not initialized at the moment of initializing this::sku
                // collection. Otherwise, sku collection initialization will fail with
                // "collection was evicted" exception.
                // Btw, it means that Sku::attribute collection must be Eager-loaded,
                // otherwise this method will lead to N + 1 problem.
                // And while we can access Sku only from Product AggregateRoot (i.e. from
                // this method), we keep Sku::attributes Eager-loaded.
                .sorted(new ByFirstAttributeSkuComparator())
                .toList();
    }

    /**
     * Add new SKU to this Product
     */
    public void addSku(Sku sku) {
        this.checkSkuHasTheSameAttributes(sku);

        sku.setProduct(this);
        this.sku.add(sku);

        log.info("Add new SKU " + sku);
    }

    /**
     * Check if given SKU has the same Attribute List as the other SKUs
     * in this Product have.
     *
     * @throws IllegalArgumentException if Attribute List is different
     */
    private void checkSkuHasTheSameAttributes(Sku sku) {
        if (getSku().isEmpty()) {
            return;
        }

        var existed = getSku().get(0);

        if (!existed.getAttributeList().equals(sku.getAttributeList())) {
            throw new IllegalArgumentException("Given SKU has different Attribute List than the existed SKUs have " +
                    "Given Attributes = " + sku.getAttributeList() + ". Existed Attributes = " + existed.getAttributeList());
        }
    }

    public Set<ProductCategory> getCategories() {
        return Collections.unmodifiableSet(categories);
    }

    public List<File> getImages() {
        return Collections.unmodifiableList(images);
    }

    private void addImage(File image) {
        this.images.add(image);
    }

    public static ProductBuilder builder() {
        return new ProductBuilder();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Product product = (Product) o;

        return id != null && Objects.equals(id, product.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Embeddable
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class ProductId extends DomainObjectId {
        public ProductId(String uuid) {
            super(uuid);
        }
    }

    public static class ProductBuilder {
        @Nullable
        private ProductId id;
        private String name;
        private List<File> images = new ArrayList<>();

        public ProductBuilder id(ProductId id) {
            this.id = id;
            return this;
        }

        public ProductBuilder name(String name) {
            this.name = name;
            return this;
        }
        
        public ProductBuilder addImage(File image) {
            images.add(image);
            return this;
        }

        public Product build() {
            var product = (id == null ? new Product() : new Product(id));
            product.setName(name);
            images.forEach(product::addImage);

            return product;
        }
    }
}
