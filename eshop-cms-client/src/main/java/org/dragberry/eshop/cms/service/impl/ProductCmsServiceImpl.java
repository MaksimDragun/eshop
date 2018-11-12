package org.dragberry.eshop.cms.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.dragberry.eshop.cms.mapper.OrderProductMapper;
import org.dragberry.eshop.cms.model.OrderProductTO;
import org.dragberry.eshop.cms.model.ProductArticleListItemTO;
import org.dragberry.eshop.cms.service.ProductCmsService;
import org.dragberry.eshop.common.PageableList;
import org.dragberry.eshop.dal.dto.ProductArticleListItemDTO;
import org.dragberry.eshop.dal.entity.Product;
import org.dragberry.eshop.dal.repo.ProductArticleRepository;
import org.dragberry.eshop.dal.repo.ProductRepository;
import org.dragberry.eshop.dal.repo.ProductRepository.ProductOrderItemSpecification;
import org.dragberry.eshop.service.ImageService;
import org.dragberry.eshop.utils.ProductTitleBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class ProductCmsServiceImpl implements ProductCmsService {
    
    @Autowired
    private ProductArticleRepository productArticleRepo;
    
    @Autowired
    private ProductRepository productRepo;
    
    @Autowired
    private ImageService imageService;
    
    @Override
    public PageableList<OrderProductTO> searchProducts(PageRequest pageRequest, Map<String, String[]> searchParams) {
    	Page<Product> page = productRepo.findAll(new ProductOrderItemSpecification(searchParams), pageRequest);
        return PageableList.of(page.stream().map(dto -> {
            OrderProductTO to = new OrderProductTO();
            to.setProductId(dto.getEntityKey());
            to.setProductArticleId(dto.getProductArticle().getEntityKey());
            to.setArticle(dto.getProductArticle().getArticle());
            to.setTitle(dto.getProductArticle().getTitle());
            to.setOptionsTitle(ProductTitleBuilder.buildOptionsTitle(dto));
            to.setPrice(dto.getPrice());
            if (!Objects.equals(dto.getPrice(), dto.getActualPrice())) {
                to.setActualPrice(dto.getActualPrice());
            }
            to.setMainImage(imageService.findMainImage(dto.getProductArticle().getEntityKey(), dto.getProductArticle().getArticle()));
            return to;
        }).collect(Collectors.toList()), page.getNumber() + 1, page.getSize(), 10, page.getTotalElements());
    }

    @Override
    public Optional<List<OrderProductTO>> getProductOptions(Long productArticleId) {
        return productArticleRepo.findById(productArticleId).map(pa -> {
           return pa.getProducts().stream().map(product -> {
               return OrderProductMapper.map(product, imageService::findMainImage);
           }).collect(Collectors.toList());
        });
    }
    
    @Override
    public PageableList<ProductArticleListItemTO> getProducts(Integer pageNumber, Integer pageSize,
            Map<String, String[]> parameterMap) {
        Page<ProductArticleListItemDTO> page = productArticleRepo.search(PageRequest.of(pageNumber, pageSize), parameterMap);
        return PageableList.of(page.stream().map(entity -> {
            ProductArticleListItemTO item = new ProductArticleListItemTO();
            item.setId(entity.getId());
            item.setArticle(entity.getArticle());
            item.setTitle(entity.getTitle());
            item.setMainImage(imageService.findMainImage(entity.getId(), entity.getArticle()));
            return item;
        }).collect(Collectors.toList()), page.getNumber() + 1, page.getSize(), page.getTotalPages(), page.getTotalElements());
    }
}
