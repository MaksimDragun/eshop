import { NavigationService } from './../../../core/service/navigation.service';
import { ActivatedRoute } from '@angular/router';
import { OrderService } from './../../service/order.service';
import { Component } from '@angular/core';
import { OrderDetailsEditableComponent } from './order-details-editable.component';
import { MessageService } from 'src/app/core/service/message.service';
import { MessageType } from 'src/app/shared/model';

@Component({
  selector: 'app-order-create',
  templateUrl: './order-create.component.html'
})
export class OrderCreateComponent extends OrderDetailsEditableComponent {

  constructor(
    protected route: ActivatedRoute,
    private navigationService: NavigationService,
    private messageService: MessageService,
    protected orderService: OrderService) {
      super(route);
  }

  updateOrder(): void { }

  saveOrder(): void {
    this.orderService.createOrder(this.order)
    .then(order => {
      this.navigationService.navigateBack('/orders')
      .then(() => this.messageService.showMessage(MessageType.SUCCESS, 'orders.messages.successCreated', {order: order.id}));
    })
    .catch(error => console.log('An error has occured', error));
  }
}
