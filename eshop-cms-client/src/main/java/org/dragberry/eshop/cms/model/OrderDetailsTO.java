package org.dragberry.eshop.cms.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.dragberry.eshop.dal.entity.Order.OrderStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderDetailsTO implements Serializable {

	private static final long serialVersionUID = 2253922422700962998L;

	private Long id;
	
	private String phone;
	
	private BigDecimal totalProductAmount;
	
	private BigDecimal shippingCost;
	
    private BigDecimal totalAmount;
	
	private String fullName;
	
	private String address;
	
	private String comment;
	
	private String email;
	
	private Boolean paid;
	
	private Long shippingMethodId;
	
	private Long paymentMethodId;
	
	private LocalDateTime date;
	
	private OrderStatus status;
	
	private Long version;
	
	private List<OrderItemTO> items;
}
