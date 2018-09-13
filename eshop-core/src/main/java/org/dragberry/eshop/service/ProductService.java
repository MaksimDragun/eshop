package org.dragberry.eshop.service;

import java.util.List;

import org.dragberry.eshop.model.product.ProductListItem;
import org.dragberry.eshop.model.cart.CapturedProduct;
import org.dragberry.eshop.model.common.ImageModel;
import org.dragberry.eshop.model.product.ProductCategory;
import org.dragberry.eshop.model.product.ProductDetails;
import org.dragberry.eshop.model.product.ProductSearchQuery;

public interface ProductService {
	
	/**
	 * Gets the category list for side menu
	 * @return
	 */
	List<ProductCategory> getCategoryList();
	
	/**
	 * Gets the product list
	 * @return
	 */
	List<ProductListItem> getProductList(ProductSearchQuery query);

	/**
	 * Get the product details
	 * @param categoryReference
	 * @param productReference
	 * @return
	 */
    ProductDetails getProductArticleDetails(String categoryReference, String productReference);

    /**
     * Get the product
     * @param productKey
     * @param imageKey 
     * @return
     */
    ImageModel getProductImage(Long productKey, Long imageKey);

    /**
     * Get details for captured product
     * @param productId
     * @return
     */
    CapturedProduct getProductCartDetails(Long productId);

    /**
     * Find a category by reference
     * @param categoryReference
     * @return
     */
    ProductCategory findCategory(String categoryReference);

}
