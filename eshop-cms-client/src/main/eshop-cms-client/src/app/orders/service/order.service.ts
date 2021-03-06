import { HttpDelegateService } from '../../core/http/http-delegate.service';
import { OrderStatus } from './../model/order-status';
import { NameValue } from './../../shared/components/table/common/name-value';
import { Page } from './../../shared/model/page';
import { Observable } from 'rxjs';
import { HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Order } from '../model/order';
import { OrderDetails } from '../model/order-details';
import { TranslateService } from '@ngx-translate/core';
import { OrderProduct } from '../model/order-product';

const ORDERS_URL = 'orders';

@Injectable()
export class OrderService {

  constructor(
    private httpService: HttpDelegateService,
    private translateService: TranslateService) {}

  getOrders(params: HttpParams): Promise<Page<Order>> {
    return this.httpService.get<Page<Order>>(ORDERS_URL, { params: params });
  }

  getOrderDetails(id: string): Promise<OrderDetails> {
    return this.httpService.get<OrderDetails>(`${ORDERS_URL}/${id}`);
  }

  updateOrder(order: OrderDetails): Promise<OrderDetails> {
      return this.httpService.put<OrderDetails>(`${ORDERS_URL}/${order.id}`, order);
  }

  createOrder(order: OrderDetails): Promise<OrderDetails> {
    return this.httpService.put<OrderDetails>(`${ORDERS_URL}/new`, order);
  }

  searchProducts(query: string): Promise<Page<OrderProduct>> {
      return this.httpService.get<Page<OrderProduct>>(`products/search`, {
        params: new HttpParams()
          .append('query', query)
          .append('pageSize', '20')
          .append('pageNumber', '1')
      });
  }

  getProductsForArticle(articleId: number): Promise<OrderProduct[]> {
      return this.httpService.get<OrderProduct[]>(`products/${articleId}/options`);
  }

  fetchOrderStatuses(): Promise<NameValue<string>[]> {
    const orderStatuses = [];
    return new Promise(resolve => {
      Object.entries(OrderStatus).forEach(status => {
        this.translateService.get(`orders.status.${status[1]}`).subscribe(translated => {
          orderStatuses.push({
            value: status[1],
            name: translated
          });
        });
      });
      resolve(orderStatuses);
    });
  }

  fetchPaidStatuses(): Promise<NameValue<boolean>[]> {
    const paidStatuses = [];
    return new Promise(resolve => {
      [true, false].forEach(value => {
        this.translateService.get(`orders.paid.${value}`).subscribe(translated => {
          paidStatuses.push({
            value: String(value),
            name: translated
          });
        });
      });
      resolve(paidStatuses);
    });
  }

  downloadReport(orderId: number): void {
    this.httpService.downloadFile(`orders/${orderId}/download`);
  }

}
