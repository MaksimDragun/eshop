package org.dragberry.eshop.dal.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "PRODUCT_ATTRIBUTE_STRING")
public class ProductAttributeString  extends ProductAttribute<String> {

    @Column(name = "VALUE")
    private String value;
    
    @Override
    public String getValue() {
        return value;
    }

    @Override
    public void setValue(String value) {
        this.value = value;
    }
    
    public static ProductAttributeString of(ProductArticle productArticle, String name, String value, Integer order) {
    	var entity = new ProductAttributeString();
    	entity.setProductArticle(productArticle);
    	entity.setName(name);
    	entity.setValue(value);
    	entity.setOrder(order);
    	return entity;
    }
    
    @Override
    public String getStingValue() {
    	return value;
    }
}