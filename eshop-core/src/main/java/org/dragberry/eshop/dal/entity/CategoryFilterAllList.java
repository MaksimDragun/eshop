package org.dragberry.eshop.dal.entity;

import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.dragberry.eshop.dal.repo.ProductAttributeListRepository;
import org.dragberry.eshop.model.common.KeyValue;
import org.dragberry.eshop.model.product.Filter;
import org.dragberry.eshop.model.product.ListFilter;

@Entity
@DiscriminatorValue("lAll")
public class CategoryFilterAllList extends CategoryFilter<List<String>, ProductAttributeList, ProductAttributeListRepository> {

	private static final long serialVersionUID = 3389528340207234584L;

	public CategoryFilterAllList() {
		super(ProductAttributeListRepository.class);
	}

	@Override
	protected Filter buildFilter(ProductAttributeListRepository repo) {
		ListFilter filter = new ListFilter();
		filter.setId(MessageFormat.format("attribute[{0}][all]", name));
		filter.setName(name);
		filter.setAttributes(repo.findByNameAndCategory(name, category).stream().map(pa -> new KeyValue(pa, pa)).collect(Collectors.toList()));
		return filter;
	}

}
