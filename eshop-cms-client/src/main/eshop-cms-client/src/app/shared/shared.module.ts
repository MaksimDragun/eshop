import { DragulaModule, DragulaDirective } from 'ng2-dragula';
import { NgxMaskModule } from 'ngx-mask';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NgModule } from '@angular/core';
import { PaginatorComponent } from './components/table/paginator/paginator.component';
import { BooleanBadgeDirective } from './directives/boolean-badge/boolean-badge.directive';
import { TableActionColumnComponent } from './components/table/table-action-column/table-action-column.component';
import { BsDropdownModule } from 'ngx-bootstrap/dropdown';
import { TranslateModule, TranslatePipe, TranslateDirective } from '@ngx-translate/core';
import { TableListFilterComponent } from './components/table/filter/table-list-filter/table-list-filter.component';
import { TableNumericFilterComponent } from './components/table/filter/table-numeric-filter/table-numeric-filter.component';
import { TableDateFilterComponent } from './components/table/filter/table-date-filter/table-date-filter.component';
import { BsDatepickerModule } from 'ngx-bootstrap/datepicker';
import { FormatDate } from './pipes/format-date.pipe';
import { ModalModule } from 'ngx-bootstrap/modal';
import { ConfirmationModalComponent } from './components/confirmation-modal/confirmation-modal.component';
import { TimepickerModule } from 'ngx-bootstrap/timepicker';
import { TypeaheadModule } from 'ngx-bootstrap/typeahead';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    BsDropdownModule,
    BsDatepickerModule,
    DragulaModule,
    ModalModule,
    NgxMaskModule.forChild(),
    TranslateModule,
    TimepickerModule
  ],
  declarations: [
    BooleanBadgeDirective,
    ConfirmationModalComponent,
    PaginatorComponent,
    TableActionColumnComponent,
    TableDateFilterComponent,
    TableListFilterComponent,
    TableNumericFilterComponent,
    FormatDate
  ],
  exports: [
    FormsModule,
    BsDropdownModule,
    BsDatepickerModule,
    DragulaDirective,
    ModalModule,
    NgxMaskModule,
    TranslatePipe,
    TranslateDirective,
    TimepickerModule,
    TypeaheadModule,
    BooleanBadgeDirective,
    ConfirmationModalComponent,
    PaginatorComponent,
    TableActionColumnComponent,
    TableDateFilterComponent,
    TableListFilterComponent,
    TableNumericFilterComponent,
    FormatDate
  ],
  entryComponents: [
    ConfirmationModalComponent
  ],
  providers: []
})
export class SharedModule {

  constructor() {}

}
