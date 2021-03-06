import {
  Attribute,
  AttributeType,
  BooleanAttribute,
  ListAttribute,
  StringAttribute,
  NumericAttribute
} from 'src/app/catalog/model/attributes';
import { Component, Input, OnDestroy, OnChanges, SimpleChanges, SimpleChange } from '@angular/core';
import { ProductArticleDetails } from 'src/app/catalog/model';
import { DragulaService } from 'ng2-dragula';
import { Subscription } from 'rxjs';
import { ProductService } from 'src/app/catalog/services/product.service';

@Component({
    selector: 'app-product-attributes',
    templateUrl: './product-attributes.component.html'
})
export class ProductAttributesComponent implements OnDestroy, OnChanges {

  readonly GROUPS: string = 'GROUPS';
  readonly ATTRIBUTES: string = 'ATTRIBUTES';

  dragulaSubscription: Subscription;

  @Input()
  productArticle: ProductArticleDetails;

  attributes: {group: string, attrs: Attribute<any>[]}[] = [];
  oldAttributes: {group: string, attrs: Attribute<any>[]}[] = [];

  isBeingEdited: boolean;
  editedAttribute: Attribute<any>;
  isEditedAttributeNew: boolean;

  idGenerator = function* idGenerator() {
    let i = -1;
    while (true) {
      yield i--;
    }
  }();

  constructor(private dragulaService: DragulaService, private productService: ProductService) {
    this.dragulaService.createGroup(this.GROUPS, {
      invalid: function (el, handle) {
        return el.tagName.toLocaleLowerCase() === 'app-product-attribute';
      }
    });
    this.dragulaSubscription = this.dragulaService.drop()
      .subscribe(() => {
        this.calculateOrders();
      });
  }

  ngOnDestroy(): void {
    this.dragulaService.destroy(this.ATTRIBUTES);
    this.dragulaService.destroy(this.GROUPS);
    this.dragulaSubscription.unsubscribe();
  }

  ngOnChanges(changes: SimpleChanges): void {
    const productChange: SimpleChange = changes['productArticle'];
    if (productChange && productChange.currentValue) {
      this.setProductArticle(productChange.currentValue);
    }
  }

  setProductArticle(productArticle: ProductArticleDetails): void {
    this.productArticle = productArticle;
    this.attributes = [];
    this.groupAttributes(
      this.productArticle.booleanAttributes,
      this.productArticle.listAttributes,
      this.productArticle.numericAttributes,
      this.productArticle.stringAttributes,
    ).forEach((attrs, group) => {
      this.attributes.push({group: group, attrs: attrs});
    });

    this.attributes.forEach(group => {
      group.attrs.sort((attr1, attr2) => attr1.order - attr2.order);
    });
    this.attributes.sort((group1, group2) => {
      return group1.attrs[0].order - group2.attrs[0].order;
    });

    this.oldAttributes = this.copyAttributes(this.attributes);
  }

  private groupAttributes<T>(...all: Attribute<any>[][]): Map<string, Attribute<any>[]> {
    const attributeGroups: Map<string, Attribute<any>[]> = new Map();
    all.forEach(attrList => {
      attrList.forEach(attr => {
        let group = attributeGroups.get(attr.group);
        if (!group) {
          group = [];
          attributeGroups.set(attr.group, group);
        }
        group.push(attr);
      });
    });
    return attributeGroups;
  }

  calculateOrders(): void {
    let groupIndex = 0;
    this.attributes.forEach(attrGroup => {
      let attrIndex = groupIndex;
      attrGroup.attrs.forEach(attr => {
        attr.group = attrGroup.group;
        attr.order = attrIndex++;
      });
      groupIndex += 1000;
    });
  }

  copyAttributes(src: {group: string, attrs: Attribute<any>[]}[]): {group: string, attrs: Attribute<any>[]}[] {
    const dst: {group: string, attrs: Attribute<any>[]}[] = [];
    src.forEach(attr => {
      dst.push({
        group: attr.group,
        attrs: attr.attrs.map(a => {
          return {...a};
        })
      });
    });
    return dst;
  }

  edit(): void {
    this.isBeingEdited = true;
  }

  save(): void {
    const productAttributes: ProductArticleDetails = new ProductArticleDetails();
    productAttributes.id = this.productArticle.id;
    this.attributes.forEach(group => {
      group.attrs.forEach(attr => {
        switch (attr.type) {
          case AttributeType.BOOLEAN:
            productAttributes.booleanAttributes.push(<BooleanAttribute>attr);
            break;
          case AttributeType.LIST:
            productAttributes.listAttributes.push(<ListAttribute>attr);
            break;
          case AttributeType.NUMERIC:
            productAttributes.numericAttributes.push(<NumericAttribute>attr);
            break;
          case AttributeType.STRING:
            productAttributes.stringAttributes.push(<StringAttribute>attr);
            break;
        }
      });
    });
    this.productService.updateProductAttributes(productAttributes)
    .then(result => {
      this.setProductArticle(result);
      this.isBeingEdited = false;
    })
    .catch(() => {
      this.isBeingEdited = true;
    });
  }

  cancelEditing(): void {
    this.attributes = this.copyAttributes(this.oldAttributes);
    this.editedAttribute = null;
    this.isBeingEdited = false;
    this.isEditedAttributeNew = false;
  }

  removeGroup(group: any): void {
    this.attributes.forEach((grp, index) => {
      if (grp === group) {
        this.attributes.splice(index, 1);
      }
    });
    this.calculateOrders();
  }

  removeAttribute(attribute: Attribute<any>): void {
    this.attributes.forEach(group => {
      group.attrs.forEach((attr, index) => {
        if (attribute === attr) {
          group.attrs.splice(index, 1);
        }
      });
    });
    this.calculateOrders();
  }

  addAttribute(): void {
    this.isEditedAttributeNew = true;
    this.editedAttribute = new BooleanAttribute();
  }

  startAttributeEditing(attribute: Attribute<any>): void {
    this.isEditedAttributeNew = false;
    this.editedAttribute = attribute;
  }

  finishAttributeEditing(attribute: Attribute<any>): void {
    if (this.isEditedAttributeNew) {
      attribute.id = this.idGenerator.next().value;
    }
    const existingGroup = this.attributes.find(grp => grp.group === attribute.group);
    if (!existingGroup) {
      // add a new group if doesnt exist
      this.attributes.push({group: attribute.group, attrs: [attribute]});
    } else {
      const existingAttribute = existingGroup.attrs.find(attr => attr.id === attribute.id);
      if (!existingAttribute) {
        // push a new attribute to the existing group, if such such attribute doesn't exist here
        existingGroup.attrs.push(attribute);
      } else {
        // replace an existing attribute in the existing group
        existingGroup.attrs.forEach((attr, index) => {
          if (attr.id === attribute.id) {
            existingGroup.attrs.splice(index, 1, attribute);
          }
        });
      }
    }
    // deletes atributes whose group doesn't match to the group where they are situated
    this.attributes.forEach((group) => {
      group.attrs.forEach((attr, index) => {
        if (attr.id === attribute.id && attr.group !== attribute.group) {
          group.attrs.splice(index, 1);
        }
      });
    });
    this.calculateOrders();
    this.isEditedAttributeNew = false;
    this.editedAttribute = null;
  }

  cancelAttributeEditing(attribute: Attribute<any>): void {
    this.isEditedAttributeNew = false;
    this.editedAttribute = null;
  }

  moveAttributeUp(attribute: Attribute<any>): void {
    this.attributes.forEach(group => {
      if (group.attrs.length > 1) {
        for (let i = 0; i < group.attrs.length; i++) {
          if (attribute === group.attrs[i] && i > 0) {
            const temp = group.attrs[i - 1];
            group.attrs[i - 1] = attribute;
            group.attrs[i] = temp;
            break;
          }
        }
      }
    });
    this.calculateOrders();
  }

  moveAttributeDown(attribute: Attribute<any>): void {
    this.attributes.forEach(group => {
      if (group.attrs.length > 1) {
        for (let i = 0; i < group.attrs.length; i++) {
          if (attribute === group.attrs[i] && i < group.attrs.length - 1) {
            const temp = group.attrs[i + 1];
            group.attrs[i + 1] = attribute;
            group.attrs[i] = temp;
            break;
          }
        }
      }
    });
    this.calculateOrders();
  }
}
