import React, { useContext, useEffect, Component, useState } from "react";
import {
  DataList, DataListItem, DataListCell, DataListItemRow, DataListItemCells, DataListAction, FormGroup,
  OptionsMenu, OptionsMenuItem, OptionsMenuToggle, TextInput
} from "@patternfly/react-core";
import "@app/app.css";
import { ProductDetails } from "@app/productDetails";
import { CaretDownIcon } from "@patternfly/react-icons";
import { ProductProvider, ProductContext } from "@app/contexts/ProductContext"

const onSelect = (id) => () => {
  //@ts-ignore
  window.location += `/${id}`;
};

// private onSelect = (event, data) => () => {
//   this.setState({
//     isProductDetails: !this.state.isProductDetails,
//     productName: data.name,
//   });
// };
// let data: any = [];

export default function ProductListing(props: any) {
  const [input, setInputField] = useState("")
  const [results, setResults] = useState([])
  const [data, setData]: any = useState([]);
  let temp: any = useContext(ProductContext);
 
  
  useEffect(() => {
    // create dynamic isOpen field for each result
    let data: any = [];
    setData(data);
  }, [temp]);
  


  

  const onToggle = (id) => (event: any) => {
    let newState = data;
    //@ts-ignore
  //  data.map(i => {
  //     if (i["jcr:uuid"] === id) {
  //       console.log("data before click:", i.isOpen);
  //       i.isOpen = !i.isOpen;
  //       console.log("data after click:", i.isOpen);
  //     }
  //   });
    newState.forEach(product => {
      if(product["jcr:uuid"] === id){
        product.isOpen = !product.isOpen;
        setData(newState);
      }
    })
    // console.log("data after click", data);
   
  };

  const setInput = input => {
    const versions: string[] = [];
    let searchString = "";
    setInputField(input)
    //@ts-ignore

    data.map(data => {
      searchString = "" + data.name
      if (searchString.toLowerCase().includes(input.toLowerCase())) {
        versions.push(data)
      }
    });
    //@ts-ignore
    setResults(versions)
  }

  // let input;

  return (
      <>

        <FormGroup
          label="Search Products"
          fieldId="search"
        >
          <div className="row-view">
            <TextInput id="search" type="text" onChange={setInput} placeholder="Type product name to search" value={input} />
          </div>
        </FormGroup>
        <DataList aria-label="single action data list example ">
          {false && (
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

        {data.length &&
          data.map((product, key) => {
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
                        aria-labelledby="multi-actions-item1 {data['jcr:uuid']}"
                        id="{data['jcr:uuid']}"
                        aria-label="Actions"
                      >
                        <OptionsMenu
                          isPlain={true}
                          id={data["jcr:uuid"]}
                          menuItems={[
                            <OptionsMenuItem key="dropdown">Product Details</OptionsMenuItem>]}
                          isOpen={key.isOpen}
                          // @ts-ignore
                          toggle={<OptionsMenuToggle onToggle={onToggle(product["jcr:uuid"])} toggleTemplate={<CaretDownIcon aria-hidden="true" />} aria-label="Sort by" hideCaret={true} />} />
                      </DataListAction>
                    </DataListCell>
                  ]}
                />
              </DataListItemRow>
            </DataListItem>
          })}
          {!data.length && 
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