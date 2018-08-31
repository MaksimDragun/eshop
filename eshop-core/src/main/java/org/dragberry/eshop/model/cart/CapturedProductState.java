package org.dragberry.eshop.model.cart;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CapturedProductState {

    private Long productId;
    
    private int quantity;
    
    private BigDecimal price;
    
    private BigDecimal totalPrice;
    
    public CapturedProductState(Long productId, BigDecimal price) {
        this.productId = productId;
        this.price = price;
        this.totalPrice = price;
    }

    public void increment() {
        update(quantity + 1);
    }
    public void decrement() {
        update(quantity <= 0 ? 0 : quantity - 1);
    }
    
    public void update(int quantity) {
        this.quantity = quantity;
        if (totalPrice != null && price != null) {
            totalPrice = price.multiply(new BigDecimal(quantity)).setScale(2);
        } else {
            totalPrice = BigDecimal.ZERO;
        }
        
    }
}