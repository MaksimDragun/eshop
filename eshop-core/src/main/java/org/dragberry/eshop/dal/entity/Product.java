package org.dragberry.eshop.dal.entity;

import java.math.BigDecimal;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import org.dragberry.era.domain.BaseEnum;
import org.dragberry.eshop.dal.entity.converter.SaleStatusConverter;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "PRODUCT")
@TableGenerator(
		name = "PRODUCT_GEN", 
		table = "GENERATOR",
		pkColumnName = "GEN_NAME", 
		pkColumnValue = "PRODUCT_GEN",
		valueColumnName = "GEN_VALUE",
		initialValue = 1000,
		allocationSize = 1)
@Setter
@Getter
public class Product extends BaseEntity {

	private static final long serialVersionUID = 6817451222642163283L;

	@Id
	@Column(name = "PRODUCT_KEY")
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "PRODUCT_GEN")
	private Long entityKey;
	
	@ManyToOne
	@JoinColumn(name = "PRODUCT_ARTICLE_KEY", referencedColumnName = "PRODUCT_ARTICLE_KEY")
	private ProductArticle productArticle;
	
	@ManyToMany
	@JoinTable(name = "PRODUCT_PRODUCT_ARTICLE_OPTION", 
        joinColumns = @JoinColumn(name = "PRODUCT_KEY", referencedColumnName = "PRODUCT_KEY"), 
        inverseJoinColumns = @JoinColumn(name = "PRODUCT_ARTICLE_OPTION_KEY", referencedColumnName = "PRODUCT_ARTICLE_OPTION_KEY"))
	private Set<ProductArticleOption> options;
	
	@Column(name = "PRICE")
    private BigDecimal price;
    
    @Column(name = "ACTUAL_PRICE")
    private BigDecimal actualPrice;
	
	@Column(name = "QUANTITY")
	private Integer quantity;
	
	@Column(name = "SALE_STATUS")
    @Convert(converter = SaleStatusConverter.class)
	private SaleStatus saleStatus;

	public enum SaleStatus implements BaseEnum<Character> {

	    EXPOSED ('E'), IN_STOCK('S'), OUT_OF_STOCK('O');
	    
	    public final Character value;
	    
	    private SaleStatus(Character value) {
	        this.value = value;
	    }
	    
	    public static SaleStatus valueOf(Character value) {
	        if (value == null) {
	            throw BaseEnum.npeException(SaleStatus.class);
	        }
	        for (SaleStatus base : SaleStatus.values()) {
	            if (value.equals(base.value)) {
	                return base;
	            }
	        }
	        throw BaseEnum.unknownValueException(SaleStatus.class, value);
	    }
	    
	    @Override
	    public Character getValue() {
	        return value;
	    }
	}
}