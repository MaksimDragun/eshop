package org.dragberry.eshop.controller.product;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.dragberry.eshop.common.ResultTO;
import org.dragberry.eshop.common.Results;
import org.dragberry.eshop.controller.exception.BadRequestException;
import org.dragberry.eshop.controller.exception.ResourceNotFoundException;
import org.dragberry.eshop.model.comment.ProductCommentRequest;
import org.dragberry.eshop.model.comment.ProductCommentResponse;
import org.dragberry.eshop.model.common.KeyValue;
import org.dragberry.eshop.model.product.ProductCategory;
import org.dragberry.eshop.model.product.ProductDetails;
import org.dragberry.eshop.model.product.ProductSearchQuery;
import org.dragberry.eshop.model.product.ProductsDetails;
import org.dragberry.eshop.navigation.Breadcrumb;
import org.dragberry.eshop.service.AppInfoService;
import org.dragberry.eshop.service.CommentService;
import org.dragberry.eshop.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.mobile.device.Device;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.ModelAndView;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Controller
@Scope(WebApplicationContext.SCOPE_SESSION)
public class ProductController {
    
	public static enum DisplayOption {
        LIST("products/list/product-list-item :: product-item(product = ${productItem})"),
        TILES("products/list/product-tile-item :: product-item(product = ${productItem})");
        
        public final String template;
        
        private DisplayOption(String template) {
            this.template = template;
        }
        
        public String getTemplate() {
            return template;
        }
    }
	
	private static final String MODEL_SEARCH_RESULTS = "searchResults";

    private static final String MODEL_QUERY = "query";

    private static final String MODEL_CATALOG_REFERENCE = "catalogReference";
    
    private static final String MODEL_IMAGES_REFERENCE = "imagesReference";
    
    private static final String MODEL_NO_IMAGE_REFERENCE = "noImageReference";
    
    private static final String MODEL_SHOP = "shop";

    private static final String MODEL_CATEGORY = "category";

	private static final String MODEL_PRODUCT_LIST = "productList";

	private static final String MODEL_CATEGORY_LIST = "categoryList";
	
	private static final String MODEL_SORTING_OPTION_LIST = "sortingOptionList";

	private static final String MODEL_SEARCH_PARAMS = "searchParams"; 
	
	private static final String MODEL_SEARCH_PARAMS_COUNT = "searchParamsCount";
	
	private static final String MODEL_DISPLAY_OPTION = "displayOption";
	
	private static final String MODEL_PRODUCT = "product";
	
	private static final String MODEL_PRODUCTS = "products";
	
	private static final String MSG_MENU_CATALOG = "msg.menu.catalog";
	
	private static final String MSG_SEARCH_RESULTS = "msg.common.searchResults";

	private static final Long DEFAULT_CATEGORY_KEY = 0L;
	
	private static final String SORT_PARAM = "sort";
	
	private static final String DISPLAY_PARAM = "display";
	
	@Value("${url.catalog}")
	private String catalogReference;
	
	@Value("${url.images}")
    private String imagesReference;
	
	@Value("${url.no-image}")
    private String noImageReference;
	
	@Autowired
	private HttpServletRequest request;
	
	@Autowired
	private MessageSource messageSource;
	
	@Autowired
	@Qualifier("templateEngine")
	private TemplateEngine templateEngine;
	
	@Autowired
    private AppInfoService appInfoService;
	
	@Autowired
	private CommentService commentService;
	
	@Autowired
	private ProductService productService;
	
	private LinkedHashMap<String, ProductCategory> categories;
	
	private Set<Long> categoriesInitialized = new HashSet<>();
	
	private Map<Long, Map<String, String[]>> categorySearchParams = new HashMap<>();

	private DisplayOption displayOption = DisplayOption.LIST;
	
	private static final List<KeyValue> SORTING_OPTION_LIST = new ArrayList<>();

	static {
	    SORTING_OPTION_LIST.add(new KeyValue("msg.common.sort.popular.desc", "popular[desc]"));
	    SORTING_OPTION_LIST.add(new KeyValue("msg.common.sort.date.desc", "date[desc]"));
	    SORTING_OPTION_LIST.add(new KeyValue("msg.common.sort.price.asc", "price[desc]"));
	    SORTING_OPTION_LIST.add(new KeyValue("msg.common.sort.price.desc", "price[asc]"));
	    SORTING_OPTION_LIST.add(new KeyValue("msg.common.sort.rated.desc", "rated[desc]"));
	}
	
	/**
	 * Get the category list
	 * @return 
	 */
	public Collection<ProductCategory> getCategoryList() {
		if (categories == null) {
			fetchCategories();
		}
		return categories.values();
	}
	
	/**
	 * Set the display option. The default should be list
	 */
	private void setDisplayOption(ModelAndView mv) {
		String reqParam = request.getParameter(DISPLAY_PARAM);
        if (StringUtils.isBlank(reqParam)) {
            if (displayOption == null) {
                displayOption = DisplayOption.LIST;
            }
        } else {
            displayOption = DisplayOption.valueOf(reqParam.toUpperCase());
        }
        mv.addObject(MODEL_DISPLAY_OPTION, displayOption);
	}
	
	/**
	 * Performs a quick search in the header
	 * @param query
	 * @param locale
	 * @return
	 */
	@GetMapping("${url.catalog.quick-search}")
	public ModelAndView quickSearch(@RequestParam(required = true) String query, Locale locale) {
		ModelAndView mv = new ModelAndView("products/search/quick-search :: search-results");
		mv.addObject(MODEL_CATALOG_REFERENCE, catalogReference);
		mv.addObject(MODEL_IMAGES_REFERENCE, imagesReference);
		mv.addObject(MODEL_NO_IMAGE_REFERENCE, noImageReference);
		mv.addObject(MODEL_SHOP, appInfoService.getShopDetails());
		mv.addObject(MODEL_QUERY, query);
		mv.addObject(MODEL_SEARCH_RESULTS, productService.getProductList(query, new HashMap<>(request.getParameterMap())));
        return mv;
	}
	
	/**
     * Return a list of products page with a search results
     */
    @GetMapping({"${url.catalog.search}"})
    public ModelAndView search(@RequestParam(required = true) String query) {
        ModelAndView mv = new ModelAndView("products/search/search-results");
        mv.addObject(Breadcrumb.MODEL_BREADCRUMB, Breadcrumb.builder()
                .append(MSG_SEARCH_RESULTS, StringUtils.EMPTY, true));
        mv.addObject(MODEL_SORTING_OPTION_LIST, SORTING_OPTION_LIST);
        mv.addObject(MODEL_CATEGORY_LIST, getCategoryList());
        mv.addObject(MODEL_QUERY, query);
        mv.addObject(MODEL_PRODUCT_LIST, productService.getProductList(query, new HashMap<>(request.getParameterMap())));
        mv.addObject(MODEL_SEARCH_PARAMS, Collections.emptyMap());
        mv.addObject(MODEL_SEARCH_PARAMS_COUNT, 0);
        setDisplayOption(mv);
        return mv;
    }
    
    /** 
     * Filter search result products
     * @param request
     * @return
     */
    @GetMapping("${url.catalog.search.filter}")
    public ModelAndView filterSearchResults(@RequestParam(required = true) String query) {
        ModelAndView mv = new ModelAndView("products/search/search-results :: products");
        mv.addObject(MODEL_QUERY, query);
        mv.addObject(MODEL_PRODUCT_LIST, productService.getProductList(query, new HashMap<>(request.getParameterMap())));
        setDisplayOption(mv);
        return mv;
    }
	
	
    /**
     * Get category by reference
     * @param selectedCategory
     * @return
     */
    private ProductCategory getCategory(String selectedCategory) {
    	if (categories == null) {
			fetchCategories();
		}
    	ProductCategory category = categories.getOrDefault(selectedCategory, new ProductCategory(DEFAULT_CATEGORY_KEY, "all", "Все товары"));
    	if (category == null) {
			throw new ResourceNotFoundException();
		} else {
			if (!categoriesInitialized.contains(category.getId())) {
				fetchCategoryDetails(category);
				categoriesInitialized.add(category.getId());
			}
		}
    	return category;
    }
    
    /**
	 * Fetches all category details
	 * @param category
	 */
	private void fetchCategoryDetails(ProductCategory category) {
		category.setFilters(productService.getCategoryFilters(category.getId()));
	}

	/**
	 * Fetch all available categories
	 */
	private void fetchCategories() {
		categories = productService.getCategoryList().stream().collect(Collectors.toMap(
				ProductCategory::getReference,
				ctg -> ctg,
				(k, v) -> {
					throw new IllegalStateException(MessageFormat.format("Duplicate key: {0}", k));
				},
				LinkedHashMap::new));
	}
    
    /**
     * Filter products for a category
     * @param request
     * @param selectedCategory
     * @return
     */
    @GetMapping("${url.catalog.filter}/{selectedCategory}")
    public ModelAndView filter(HttpServletRequest request, @PathVariable(required = true) String selectedCategory) {
    	ModelAndView mv = new ModelAndView("products/list/product-list :: products");
    	ProductCategory category = getCategory(selectedCategory);
		mv.addObject(MODEL_CATEGORY, category);
    	ProductSearchQuery query = new ProductSearchQuery();
    	query.setCategoryReference(selectedCategory);
    	Map<String, String[]> searchParams = new HashMap<>(request.getParameterMap());
    	categorySearchParams.put(category.getId(), searchParams);
    	query.setSearchParams(searchParams);
    	mv.addObject(MODEL_PRODUCT_LIST, productService.getProductList(query));
    	setDisplayOption(mv);
    	return mv;
    }
    
    /** 
     * Filter all products
     * @return
     */
    @GetMapping("${url.catalog.filter}")
    public ModelAndView filterAll() {
        ModelAndView mv = new ModelAndView("products/list/product-list :: products");
    	ProductSearchQuery query = new ProductSearchQuery();
    	Map<String, String[]> searchParams = new HashMap<>(request.getParameterMap());
    	categorySearchParams.put(DEFAULT_CATEGORY_KEY, searchParams);
    	query.setSearchParams(searchParams);
    	mv.addObject(MODEL_PRODUCT_LIST, productService.getProductList(query));
    	setDisplayOption(mv);
    	return mv;
    }
    
	/**
	 * Return a list of products
	 * @return
	 */
	@GetMapping({"${url.catalog}/{selectedCategory}"})
	public ModelAndView catalog(@PathVariable String selectedCategory) {
		ModelAndView mv = new ModelAndView("products/list/product-list");
		ProductCategory category = getCategory(selectedCategory);
		mv.addObject(MODEL_CATEGORY, category);
		mv.addObject(Breadcrumb.MODEL_BREADCRUMB, Breadcrumb.builder()
	     		.append(MSG_MENU_CATALOG, catalogReference, true)
	     		.append(category.getName(), StringUtils.EMPTY));
		mv.addObject(MODEL_SORTING_OPTION_LIST, SORTING_OPTION_LIST);
		mv.addObject(MODEL_CATEGORY_LIST, getCategoryList());
		ProductSearchQuery query = new ProductSearchQuery();
		query.setCategoryReference(selectedCategory);
		Map<String, String[]> searchParams = getCategorySearchParams(category.getId());
		query.setSearchParams(searchParams);
		mv.addObject(MODEL_PRODUCT_LIST, productService.getProductList(query));
		mv.addObject(MODEL_SEARCH_PARAMS, searchParams);
		mv.addObject(MODEL_SEARCH_PARAMS_COUNT, searchParams.entrySet().stream()
				.filter(params -> !SORT_PARAM.equals(params.getKey()) && !DISPLAY_PARAM.equals(params.getKey()))
				.flatMap(params -> Arrays.stream(params.getValue()))
				.filter(StringUtils::isNotBlank).count());
		setDisplayOption(mv);
		return mv;
	}
	
	/**
	 * Return a list of products
	 * @return
	 */
	@GetMapping({"${url.catalog}"})
	public ModelAndView catalogAll() {
		ModelAndView mv = new ModelAndView("products/list/product-list");
		mv.addObject(Breadcrumb.MODEL_BREADCRUMB, Breadcrumb.builder()
				.append(MSG_MENU_CATALOG, StringUtils.EMPTY, true));
		mv.addObject(MODEL_SORTING_OPTION_LIST, SORTING_OPTION_LIST);
		mv.addObject(MODEL_CATEGORY_LIST, getCategoryList());
		ProductSearchQuery query = new ProductSearchQuery();
		Map<String, String[]> searchParams = getCategorySearchParams(DEFAULT_CATEGORY_KEY);
		query.setSearchParams(searchParams);
		mv.addObject(MODEL_PRODUCT_LIST, productService.getProductList(query));
		mv.addObject(MODEL_SEARCH_PARAMS, searchParams);
		mv.addObject(MODEL_SEARCH_PARAMS_COUNT, 0);
		setDisplayOption(mv);
		return mv;
	}
	
	/**
	 * Get category search params
	 * @param categoryId
	 * @return
	 */
	private Map<String, String[]> getCategorySearchParams(Long categoryId) {
		return categorySearchParams.getOrDefault(categoryId,  Collections.emptyMap());
	}

	/**
     * Return a product page
     * @return
     */
    @GetMapping({"${url.catalog}/{categoryReference}/{productReference}"})
    public ModelAndView product(@PathVariable String categoryReference, @PathVariable String productReference, Device device) {
        if (productReference != null) {
            ProductDetails product = productService.getProductArticleDetails(categoryReference, productReference);
            if (product != null) {
            	ModelAndView mv = new ModelAndView("products/details/product-details");
                mv.addObject(MODEL_PRODUCT, product);
                ProductsDetails products = new ProductsDetails();
                products.setOptionValues(product.getOptionValues());
                products.setProductOptions(product.getProductOptions());
                products.setProductActualPrices(product.getProductActualPrices());
                products.setProductPrices(product.getProductPrices());
                products.setOptionValues(product.getOptionValues());
                mv.addObject(MODEL_PRODUCTS, products);
                mv.addObject(Breadcrumb.MODEL_BREADCRUMB, Breadcrumb.builder()
                		.append(MSG_MENU_CATALOG, catalogReference, true)
                		.append(product.getCategory().getName(), categoryReference)
                		.append(product.getTitle(), StringUtils.EMPTY));
                return mv;
            }
        }
        throw new ResourceNotFoundException();
    }
    
    @PostMapping("${url.product.add-comment}")
    @ResponseBody
    public ResultTO<?> addComment(@RequestBody ProductCommentRequest cmtReq, HttpServletRequest request, Locale locale) {
    	if (cmtReq == null) {
    		throw new BadRequestException();
    	}
    	cmtReq.setDate(LocalDateTime.now());
    	cmtReq.setIp(request.getRemoteAddr());
    	ResultTO<ProductCommentResponse> resp = commentService.createComment(cmtReq);
    	if (resp.hasIssues()) {
    	    resp.getIssues().forEach(issue -> issue.setMessage(messageSource.getMessage(issue.getErrorCode(), issue.getParams().toArray(), locale)));
    		return resp;
    	} else {
    		Context context = new Context(locale);
    		context.setVariable("comment", resp.getValue());
            return Results.create(templateEngine.process("common/products/details/product-details-tabs",
    				new HashSet<>(Arrays.asList("product-comment")), context));
    	}
    }
    
}