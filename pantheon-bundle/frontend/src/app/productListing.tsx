import React, { useContext, useEffect, Component, useState } from "react";
import {
  DataList, DataListItem, DataListCell, DataListItemRow, DataListItemCells, DataListAction, FormGroup,
  OptionsMenu, OptionsMenuItem, OptionsMenuToggle, TextInput
} from "@patternfly/react-core";
import "@app/app.css";
import { CaretDownIcon } from "@patternfly/react-icons";
import { ProductContext, IProduct } from "@app/contexts/ProductContext"

export default function ProductListing(props: any) {
  const allProducts = useContext(ProductContext);
  
  const [input, setInputField] = useState("")
  const [filteredProducts, setFilteredProducts] = useState([] as IProduct[])

  useEffect(() => {
    setFilteredProducts(allProducts)
  }, [allProducts])

  const onToggle = (id) => {
    updateProduct(id);
  };

  const onSelect = (id) => {
    // @ts-ignore
    window.location += `/${id}`;
  };

  const setInput = (text, event) => {
    setInputField(text)
    const filteredProducts = allProducts.filter(product => {
      return product.name.toLowerCase().includes(text.toLowerCase())
    });
    setFilteredProducts(filteredProducts)
  }

  function updateProduct(id: string) {
    const newProducts = filteredProducts.map(product => {
      if (product['jcr:uuid'] === id) {
        product.isOpen = !product.isOpen;
      }
      return product;
    });
    setFilteredProducts(newProducts);
  }

  return (
    <>
      <FormGroup
        label="Search Products"
        fieldId="search">
        <div className="row-view">
          <TextInput id="search" type="text" onChange={(text, event) => setInput(text, event)} placeholder="Type product name to search" value={input} />
        </div>
      </FormGroup>
      <DataList aria-label="single action data list example ">
        {(filteredProducts && filteredProducts.length > 0) && (
          <DataListItem aria-labelledby="single-action-item1">
            <DataListItemRow>
              <DataListItemCells
                dataListCells={[
                  <DataListCell key="primary content">
                    <span className="sp-prop-nosort" id="product-name">Product Name</span>
                  </DataListCell>,
                  <DataListCell key="secondary content" width={2}>
                    <span className="sp-prop-nosort" id="product-description">Product Description</span>
                  </DataListCell>
                ]}
              />
            </DataListItemRow>
          </DataListItem>
        )}
        {(filteredProducts && filteredProducts.length > 0) &&
          filteredProducts.map((product, key) => {
            return <DataListItem key={key} aria-labelledby="multi-actions-item1">
              <DataListItemRow>
                <DataListItemCells key={product["jcr:uuid"]}
                  dataListCells={[
                    <DataListCell key="primary content">
                      <span id={product.name}>{product.name}</span>
                    </DataListCell>,
                    <DataListCell key="secondary content" width={2}>{product.description}</DataListCell>,
                    <DataListCell key="Dropdown content">
                      <DataListAction
                        aria-labelledby={`multi-actions-item1 ${product['jcr:uuid']}`}
                        id={`${product['jcr:uuid']}`}
                        aria-label="Actions"
                      >
                        <OptionsMenu
                          isPlain={true}
                          id={`product-${product["jcr:uuid"]}`}
                          menuItems={[
                            <OptionsMenuItem onSelect={() => onSelect(product["jcr:uuid"])} key="dropdown">Product Details</OptionsMenuItem>]}
                          isOpen={product.isOpen}
                          toggle={<OptionsMenuToggle onToggle={() => onToggle(product["jcr:uuid"])} toggleTemplate={<CaretDownIcon aria-hidden="true" />} aria-label="Sort by" hideCaret={true} id={`product-${product["jcr:uuid"]}-button`} />} />
                      </DataListAction>
                    </DataListCell>
                  ]}
                />
              </DataListItemRow>
            </DataListItem>
          })}
        {(!(filteredProducts && filteredProducts.length > 0)) &&
          <DataListItem aria-labelledby="single-action-item0" data-testid="emptyResults">
            <DataListItemRow>
              <DataListItemCells
                dataListCells={[
                  <DataListCell key="primary content" width={2}>
                    <span className="sp-prop-nosort" id="product-name">No products found</span>
                  </DataListCell>
                ]}
              />
            </DataListItemRow>
          </DataListItem>
        }
      </DataList>
    </>
  );
}