package org.dragberry.eshop.cms.service;

import java.io.InputStream;
import java.util.Map;
import java.util.Optional;

import org.dragberry.eshop.cms.model.OrderDetailsTO;
import org.dragberry.eshop.cms.model.OrderTO;
import org.dragberry.eshop.common.PageableList;
import org.dragberry.eshop.common.ResultTO;
import org.springframework.data.domain.PageRequest;

public interface OrderCmsService {

	PageableList<OrderTO> getOrders(PageRequest pageRequest, Map<String, String[]> params);
	
	Optional<OrderDetailsTO> getOrderDetails(Long id);

    Optional<ResultTO<OrderDetailsTO>> updateOrder(Long id, OrderDetailsTO order);

	Optional<InputStream> generateReport(Long orderId);

}
