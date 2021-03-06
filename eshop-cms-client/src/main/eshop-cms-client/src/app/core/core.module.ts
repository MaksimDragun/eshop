import { DashboardComponent } from './main/dashboard/dashboard.component';
import { MessagesAllComponent } from './main/messages/messages-all.component';
import { MessagesComponent } from './main/messages/messages.component';
import { DateService } from './service/date.service';
import { MessageService } from './service/message.service';
import { HttpDelegateService } from './http/http-delegate.service';
import { NgModule, Optional, SkipSelf } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClientModule, HttpClient, JsonpInterceptor } from '@angular/common/http';
import { LoginComponent } from './login/login.component';
import { MainComponent } from './main/main.component';
import { TranslateModule, TranslateLoader, TranslateService } from '@ngx-translate/core';
import { TranslateHttpLoader } from '@ngx-translate/http-loader';
import { ModuleWithProviders } from '@angular/compiler/src/core';
import { AuthGuard } from './auth/auth.guard';
import { CoreRoutingModule } from './core-routing.module';
import { AuthenticationService } from './auth/authentication.service';
import { httpInterceptorProviders } from './http';

import { BsDropdownModule } from 'ngx-bootstrap/dropdown';
import { BsDatepickerModule } from 'ngx-bootstrap/datepicker';
import { CollapseModule } from 'ngx-bootstrap/collapse';
import { NgxMaskModule } from 'ngx-mask';
import { TimepickerModule } from 'ngx-bootstrap/timepicker';
import { TypeaheadModule } from 'ngx-bootstrap/typeahead';
import { NavigationService } from './service/navigation.service';
import { BreadcrumbComponent } from './main/breadcrumb/breadcrumb.component';
import { SideMenuComponent } from './main/side-menu/side-menu.component';
import { MenuItemComponent } from './main/side-menu/menu-item/menu-item.component';
import { ModalModule } from 'ngx-bootstrap/modal';
import { AlertModule } from 'ngx-bootstrap/alert';
import { DragulaModule } from 'ng2-dragula';

export function HttpLoaderFactory(http: HttpClient) {
  return new TranslateHttpLoader(http, 'assets/i18n/', '.json');
}

@NgModule({
  imports: [
    CommonModule,
    CoreRoutingModule,
    FormsModule,
    HttpClientModule,
    TranslateModule.forRoot({
      loader: {
        provide: TranslateLoader,
        useFactory: HttpLoaderFactory,
        deps: [HttpClient]
      }
    }),
    AlertModule.forRoot(),
    BsDropdownModule.forRoot(),
    BsDatepickerModule.forRoot(),
    CollapseModule.forRoot(),
    DragulaModule.forRoot(),
    ModalModule.forRoot(),
    NgxMaskModule.forRoot(),
    TimepickerModule.forRoot(),
    TypeaheadModule.forRoot()
  ],
  declarations: [
    DashboardComponent,
    LoginComponent,
    MainComponent,
    MessagesComponent,
    MessagesAllComponent,
    BreadcrumbComponent,
    SideMenuComponent,
    MenuItemComponent
  ],
  exports: [
    CoreRoutingModule
  ],
  providers: []
})
export class CoreModule {

  constructor(@Optional() @SkipSelf() parentModule: CoreModule, private translate: TranslateService) {
    if (parentModule) {
      throw new Error('CoreModule is already loaded. Import it in the AppModule only');
    }
    translate.setDefaultLang('en');
    translate.use('en');
  }

  static forRoot(): ModuleWithProviders {
    return {
      ngModule: CoreModule,
      providers: [
        AuthenticationService,
        AuthGuard,
        DateService,
        HttpDelegateService,
        httpInterceptorProviders,
        MessageService,
        NavigationService
      ]
    };
  }
}
