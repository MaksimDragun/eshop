import { Page } from './../../shared/model/page';
import { Component, OnInit } from '@angular/core';
import { OrderService } from '../order.service';
import { Order } from '../model/order';

@Component({
  selector: 'app-order-list',
  templateUrl: './order-list.component.html',
  styleUrls: ['./order-list.component.css']
})
export class OrderListComponent implements OnInit {

  orders: Page<Order>;

  constructor(private orderService: OrderService) { }

  ngOnInit() {
    this.orderService.getOrders({pageNumber: 0, pageSize: 20}).subscribe(orders => {
      this.orders = orders;
    });
  }

}
