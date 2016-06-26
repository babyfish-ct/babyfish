/*
 * BabyFish, Object Model Framework for Java and JPA.
 * https://github.com/babyfish-ct/babyfish
 *
 * Copyright (c) 2008-2016, Tao Chen
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * Please visit "http://opensource.org/licenses/LGPL-3.0" to know more.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 */
package org.babyfish.persistence.expression;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.babyfish.model.jpa.JPAModel;

/**
 * @author Tao Chen
 */
@JPAModel
@Entity
@Table(name = "SALE_TRANSACTION")
@SequenceGenerator(
        name = "saleTransactionSequence", 
        sequenceName = "SALE_TRANSACTION_ID_SEQ",
        initialValue = 1,
        allocationSize = 1)
public class SaleTransaction {

    @Id
    @Column(name = "SALE_TRANSACTION_ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "saleTransactionSequence")
    private Long id;
    
    @Column(name = "DATE")
    @Temporal(TemporalType.TIME)
    private Date date;

    @Column(name = "QUANTITY")
    private int quantity;

    @Column(name = "PRODUCT_TRANSACTION_PRICE")
    private BigDecimal productTransactionPrice;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BUYER_CUSTOMER_ID")
    private Customer buyer;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PRODUCT_ID")
    private Product product;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getProductTransactionPrice() {
        return productTransactionPrice;
    }

    public void setProductTransactionPrice(BigDecimal productTransactionPrice) {
        this.productTransactionPrice = productTransactionPrice;
    }

    public Customer getBuyer() {
        return buyer;
    }

    public void setBuyer(Customer buyer) {
        this.buyer = buyer;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }
}
