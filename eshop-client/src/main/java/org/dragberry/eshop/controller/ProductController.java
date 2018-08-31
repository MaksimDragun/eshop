package org.dragberry.eshop.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.dragberry.eshop.controller.exception.ResourceNotFoundException;
import org.dragberry.eshop.model.product.ProductCategory;
import org.dragberry.eshop.model.product.ProductSearchQuery;
import org.dragberry.eshop.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ProductController {
	
	@Autowired
	private ProductService productService;
	
	/**
     * Serves images
     * @return
	 * @throws IOException 
     */
    @GetMapping({"${url.products-images}/{productKey}/{imageKey}/{imageName}"})
    public void productMainImage(HttpServletResponse response,
            @PathVariable Long productKey,
            @PathVariable Long imageKey,
            @PathVariable String imageName) throws IOException {
        var img = productService.getProductImage(productKey, imageKey);
        if (img != null) {
            response.setContentType(img.getType());
            IOUtils.copy(new ByteArrayInputStream(img.getContent()), response.getOutputStream());
        }
    }
	
	/**
	 * Return a list of products
	 * @return
	 */
	@GetMapping({"${url.catalog}", "${url.catalog}/{selectedCategory}"})
	public ModelAndView catalog(@PathVariable(required = false) String selectedCategory) {
		ModelAndView mv = new ModelAndView("pages/products/product-list");
		var categoryList = productService.getCategoryList();
		mv.addObject("categoryList", categoryList);
		var category = categoryList.stream().filter(c -> c.getReference().equals(selectedCategory)).findFirst().orElse(new ProductCategory(0L, "all", "Все товары"));
		mv.addObject("category", category);
		var query = new ProductSearchQuery();
		query.setCategoryReference(selectedCategory);
		mv.addObject("productList", productService.getProductList(query));
		return mv;
	}

	/**
     * Return a product page
     * @return
     */
    @GetMapping({"${url.product}/{productReference}"})
    public ModelAndView prodcut(@PathVariable String productReference) {
        if (productReference != null) {
            var product = productService.getProductArticleDetails(productReference);
            if (product != null) {
                var mv = new ModelAndView("pages/products/product");
                mv.addObject("product", product);
                return mv;
            }
        }
        throw new ResourceNotFoundException();
    }
}