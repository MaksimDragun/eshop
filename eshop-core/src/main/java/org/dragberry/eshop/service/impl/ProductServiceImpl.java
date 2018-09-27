package org.dragberry.eshop.service.impl;

import static java.util.stream.Collectors.*;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.dragberry.eshop.dal.entity.Category;
import org.dragberry.eshop.dal.entity.Comment.Status;
import org.dragberry.eshop.dal.entity.Product;
import org.dragberry.eshop.dal.entity.ProductArticle;
import org.dragberry.eshop.dal.entity.ProductAttribute;
import org.dragberry.eshop.dal.entity.ProductLabelType;
import org.dragberry.eshop.dal.repo.CategoryRepository;
import org.dragberry.eshop.dal.repo.ProductArticleRepository;
import org.dragberry.eshop.dal.repo.ProductRepository;
import org.dragberry.eshop.model.cart.CapturedProduct;
import org.dragberry.eshop.model.comment.CommentDetails;
import org.dragberry.eshop.model.common.KeyValue;
import org.dragberry.eshop.model.common.Modifier;
import org.dragberry.eshop.model.product.ProductListItem;
import org.dragberry.eshop.model.product.ActualPriceHolder;
import org.dragberry.eshop.model.product.CategoryItem;
import org.dragberry.eshop.model.product.Filter;
import org.dragberry.eshop.model.product.ListFilter;
import org.dragberry.eshop.model.product.ProductCategory;
import org.dragberry.eshop.model.product.ProductDetails;
import org.dragberry.eshop.model.product.ProductSearchQuery;
import org.dragberry.eshop.model.product.RangeFilter;
import org.dragberry.eshop.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import lombok.extern.log4j.Log4j;

@Log4j
@Service
public class ProductServiceImpl implements ProductService {
	
	private static final String ORDER_BY_PRICE = "price";
	
	private static final String PRODUCTS_DIR = "products";
	
	private static final Pattern MAIN_IMG_PATTERN = Pattern.compile("(.*?)-(main)\\.(.*?)");
	
	@Value("${db.images}")
    private String dbImages;
	
	@Autowired
	private ApplicationContext applicationContext;
	
    @Autowired
    private CategoryRepository categoryRepo;
    
    @Autowired
    private ProductArticleRepository productArticleRepo;
    
    @Autowired
    private ProductRepository productRepo;
    
    @Override
	public List<ProductCategory> getCategoryList() {
		return StreamSupport.stream(categoryRepo.findAllByOrderByOrder().spliterator(), false).map(category -> {
			ProductCategory pc = new ProductCategory();
			pc.setId(category.getEntityKey());
			pc.setName(category.getName());
			pc.setReference(category.getReference());
			return pc;
		}).collect(toList());
	}
    
    @Override
    public ProductCategory findCategory(String categoryReference) {
        Optional<Category> category = categoryRepo.findByReference(categoryReference);
        if (category.isPresent()) {
            ProductCategory pc = new ProductCategory();
            pc.setId(category.get().getEntityKey());
            pc.setName(category.get().getName());
            pc.setReference(category.get().getReference());
            return pc;
        }
        return null;
    }

	@SuppressWarnings("unchecked")
	@Override
	public List<ProductListItem> getProductList(ProductSearchQuery query) {
		return productArticleRepo.search(query.getCategoryReference(), query.getSearchParams()).stream()
	    .map(dto -> {
	    	ProductListItem product = new ProductListItem();
	    	product.setId(dto.getId());
			product.setTitle(dto.getTitle());
			product.setArticle(dto.getArticle());
			product.setReference(dto.getReference());
			product.setActualPrice(dto.getActualPrice());
			product.setPrice(dto.getPrice());
			product.setRating(dto.getAverageMark());
			product.setCommentsCount(dto.getCommentsCount());
			Category ctg = categoryRepo.findByProductId(dto.getId()).get(0);
			product.setCategory(new CategoryItem(ctg.getEntityKey(), ctg.getName(), ctg.getReference()));
			product.setLabels(productArticleRepo.findLabels(dto.getId()).stream()
					.map(entry -> (Entry<String, ProductLabelType>) entry).collect(labelCollector()));
			Path imgDir = Paths.get(dbImages, PRODUCTS_DIR, dto.getArticle());
			if (Files.exists(imgDir) && Files.isDirectory(imgDir)) {
				try (DirectoryStream<Path> imgStream = Files.newDirectoryStream(imgDir)) {
					imgStream.forEach(img -> {
						if (MAIN_IMG_PATTERN.matcher(img.getFileName().toString()).matches()) {
							product.setMainImage(img.getFileName().toString());
						}
					});
				} catch (IOException exc) {
					log.error("An error has occurred whle reading images for product article: " + dto.getId(), exc);
				}
			}
			return product;
	    }).collect(toList());
		
	}

    private void setLowestPrice(ProductArticle article, ActualPriceHolder product) {
        Product lowerPriceProduct = null;
        BigDecimal lowerPrice = null;
        BigDecimal lowerActualPrice = null;
        for (Product p : article.getProducts()) {
            if (lowerPriceProduct == null && p.getPrice() != null) {
                lowerPriceProduct = p;
                lowerPrice = p.getPrice();
                lowerActualPrice = p.getActualPrice();
            } else {
                if (lowerPrice.compareTo(p.getPrice()) == -1) {
                    lowerPrice = p.getPrice();
                    
                }
                if (lowerActualPrice != null) {
                    if (p.getActualPrice() != null && lowerActualPrice.compareTo(p.getActualPrice()) == -1) {
                        lowerPrice = p.getActualPrice();
                    }
                } else {
                    lowerActualPrice = p.getActualPrice();
                }
            }
        }
        product.setPrice(lowerPrice);
        product.setActualPrice(lowerActualPrice);
    }
	
    @Override
    public ProductDetails getProductArticleDetails(String categoryReference, String productReference) {
        ProductArticle article = productArticleRepo.findByReferenceAndCategoryReference(categoryReference, productReference);
        if (article == null) {
            return null;
        }
        ProductDetails product = new ProductDetails();
        product.setId(article.getEntityKey());
        product.setArticle(article.getArticle());
        product.setTitle(article.getTitle());
        product.setDescription(article.getDescription());
        product.setDescriptionFull(article.getDescriptionFull());
        Category ctg = article.getCategories().get(0);
        product.setCategory(new CategoryItem(ctg.getEntityKey(), ctg.getName(), ctg.getReference()));
//        product.setMainImage(article.getMainImage() != null ? article.getMainImage().getEntityKey() : null);
//       
//        product.setImages(article.getImages().stream().map(Image::getEntityKey).collect(toList()));
        
        Map<String, Set<KeyValue>> optionValues = new HashMap<>();
        Map<Long, Set<KeyValue>> productOptions = new HashMap<>();
        
        article.getProducts().forEach(p -> {
            p.getOptions().forEach(o -> {
                optionValues.computeIfAbsent(
                        o.getName(),
                        value -> new HashSet<KeyValue>()).add(new KeyValue(o.getEntityKey(), o.getValue()));
            });
            productOptions.put(
            		p.getEntityKey(),
                    p.getOptions().stream().map(o -> new KeyValue(o.getName(), o.getEntityKey())).collect(toSet()));
        });
        product.setOptionValues(optionValues);
        product.setProductOptions(productOptions);
        product.setTagKeywords(article.getTagKeywords());
        product.setTagDescription(article.getTagDescription());
        product.setTagTitle(article.getTagTitle());
        setLowestPrice(article, product);
        
        product.setAttributes(article.getAttributes().stream()
        		.collect(groupingBy(ProductAttribute::getGroup, LinkedHashMap::new,
        				mapping(attr -> new KeyValue(attr.getName(), attr.getStringValue()), toList()))));
        
        product.setComments(article.getComments().stream()
        		.filter(entity -> Status.ACTIVE.equals(entity.getComment().getStatus()))
        		.sorted((c1, c2) -> -c1.getComment().getCreatedDate().compareTo(c2.getComment().getCreatedDate()))
        		.map(entity -> {
		        	CommentDetails comment = new CommentDetails();
		        	comment.setId(entity.getEntityKey().getCommentId());
		        	comment.setName(entity.getComment().getUserName());
		        	comment.setText(entity.getComment().getText());
		        	comment.setDate(entity.getComment().getCreatedDate());
		        	comment.setMark(entity.getMark());
		        	return comment;
        }).collect(toList()));
        
		product.setLabels(article.getLabels().entrySet().stream().collect(labelCollector()));
        return product;
    }
    
    private Collector<Entry<String, ProductLabelType>, ?, Map<String, Modifier>> labelCollector() {
    	return toMap(
    			(Entry<String, ProductLabelType> entry) -> entry.getKey(),
				(Entry<String, ProductLabelType> entry) -> {
					switch (entry.getValue()) {
					case A:
						return Modifier.DANGER;
					case B:
						return Modifier.SUCCESS;
					case C:
					default:
						return Modifier.PRIMARY;
					}
				});
    }
    
    @Override
    public InputStream getProductImage(Long productKey, String article, String imageName) throws IOException {
    	Path img = Paths.get(dbImages, PRODUCTS_DIR, article, imageName);
		if (Files.exists(img)) {
			return Files.newInputStream(img);
		}
		return null;
    }
    
    @Override
    public CapturedProduct getProductCartDetails(Long productId) {
        Product product = productRepo.findById(productId).get();
        CapturedProduct capturedProduct = new CapturedProduct();
        capturedProduct.setProductId(productId);
        capturedProduct.setProductArticleId(product.getProductArticle().getEntityKey());
        capturedProduct.setArticle(product.getProductArticle().getArticle());
        capturedProduct.setTitle(product.getProductArticle().getTitle());
        capturedProduct.setReference(product.getProductArticle().getReference());
        capturedProduct.setPrice(product.getActualPrice() != null ? product.getActualPrice() : product.getPrice());
        capturedProduct.setOptions(product.getOptions().stream().map(o -> new KeyValue(o.getName(), o.getValue())).collect(toSet()));
        capturedProduct.setMainImage(productArticleRepo.findMainImageKey(capturedProduct.getProductArticleId()));
        Category ctg = product.getProductArticle().getCategories().get(0);
        capturedProduct.setCategory(new CategoryItem(ctg.getEntityKey(), ctg.getName(), ctg.getReference()));
        capturedProduct.updateFullTitle();
        return capturedProduct;
    }
    
    @Override
    public List<Filter> getCategoryFilters(Long categoryId) {
    	RangeFilter priceFilter = new RangeFilter();
		priceFilter.setId(ORDER_BY_PRICE);
		priceFilter.setFromId("price[from]");
		priceFilter.setToId("price[to]");
		priceFilter.setName("msg.common.price");
    	return Stream.concat(
    			Stream.of(priceFilter),
    			
    			Stream.concat(
	    			categoryRepo.getOptionFilters(categoryId).stream()
			        .sorted(Comparator.comparing(kv -> kv.getValue().toString()))
			        .collect(groupingBy(kv -> kv.getKey().toString(), mapping(kv -> new KeyValue(kv.getValue(), kv.getValue()), toList())))
			        .entrySet().stream().map(entry -> {
			        	ListFilter optFilter = new ListFilter();
			        	optFilter.setId(MessageFormat.format("option[{0}]", entry.getKey()));
			        	optFilter.setName(entry.getKey());
			        	optFilter.setAttributes(entry.getValue());
			        	return optFilter;
			        }),
			    
			        categoryRepo.getCategoryFilters(categoryId).stream().map(ctgFilter -> ctgFilter.buildFilter(applicationContext)))
			     ).collect(toList());
    }
}