package org.dragberry.eshop.cms.service.impl;

import java.util.Objects;
import java.util.stream.Collectors;

import org.dragberry.eshop.cms.model.OrderProductTO;
import org.dragberry.eshop.cms.service.ProductCmsService;
import org.dragberry.eshop.common.PageableList;
import org.dragberry.eshop.dal.entity.Product;
import org.dragberry.eshop.dal.repo.ProductRepository;
import org.dragberry.eshop.service.ImageService;
import org.dragberry.eshop.utils.ProductTitleBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class ProductCmsServiceImpl implements ProductCmsService {

    @Autowired
    private ProductRepository productRepo;
    
    @Autowired
    private ImageService imageService;
    
    @Override
    public PageableList<OrderProductTO> searchProducts(PageRequest pageRequest, String query) {
        Page<Product> page = productRepo.findAll(pageRequest);
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
            to.setReference(dto.getProductArticle().getReference());
            to.setMainImage(imageService.findMainImage(dto.getProductArticle().getEntityKey(), dto.getProductArticle().getArticle()));
            return to;
        }).collect(Collectors.toList()), page.getNumber() + 1, page.getSize(), 10, page.getTotalElements());
    }

}
