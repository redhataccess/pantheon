import React, { Component } from 'react';
import {
  DataList, DataListItem, DataListCell, DataListItemRow, DataListItemCells, DataListAction, FormGroup,
  OptionsMenu, OptionsMenuItem, OptionsMenuToggle, TextInput
} from '@patternfly/react-core';
import '@app/app.css';
import { ProductDetails } from '@app/productDetails';
import { CaretDownIcon } from '@patternfly/react-icons';

class ProductListing extends Component<any, any, any> {

  constructor(props) {
    super(props);
    this.state = {
      allProducts: [],
      input: '',
      isEmptyResults: false,
      isOpen: false,
      isProductDetails: false,
      productName: '',
      redirect: false,
      results: []
    };
  }

  public componentDidMount() {
    if (this.props.match !== undefined) {

      // prop will be true if it comes through nav links
      if (this.props.match.isExact === true) {
        this.state.results.map(data => {
          (data.isOpen as any) = false
        });
        this.setState({ isProductDetails: false })
      }

      // setting prop to false once it comes through nav links
      this.props.match.isExact = false;

    }

    this.getProducts(this.state.allProducts)
  }

  // render method transforms the react components into DOM nodes for the browser.
  public render() {


    return (
      <React.Fragment>
        {this.state.isProductDetails && (<ProductDetails productName={this.state.productName} />)}
        {!this.state.isProductDetails && (
          <div>
            <FormGroup
              label="Search Products"
              fieldId="search"
            >
              <div className="row-view">
                <TextInput id="search" type="text" onChange={this.setInput} placeholder="Type product name to search" value={this.state.input} />
              </div>
            </FormGroup>
            <DataList aria-label="single action data list example ">
              {!this.state.isEmptyResults && (
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

              {!this.state.isEmptyResults && this.state.results.map(data => (
                <DataListItem aria-labelledby="multi-actions-item1">
                  <DataListItemRow>
                    <DataListItemCells key={data["jcr:uuid"]}
                      dataListCells={[
                        <DataListCell key="primary content">
                          <span id="{data.name}">{data.name}</span>
                        </DataListCell>,
                        <DataListCell key="secondary content" width={2}>{data.description}</DataListCell>,
                        <DataListCell key="Dropdown content">
                          <DataListAction
                            aria-labelledby="multi-actions-item1 {data['jcr:uuid']}"
                            id="{data['jcr:uuid']}"
                            aria-label="Actions"
                          >
                            <OptionsMenu
                              isPlain={true}
                              id={data['jcr:uuid']}
                              menuItems={[
                                <OptionsMenuItem onSelect={this.onSelect(event, data)} key="dropdown">Product Details</OptionsMenuItem>]}
                              isOpen={data.isOpen}
                              toggle={<OptionsMenuToggle onToggle={this.onToggle(data['jcr:uuid'])} toggleTemplate={<CaretDownIcon aria-hidden="true" />} aria-label="Sort by" hideCaret={true} />} />
                          </DataListAction>
                        </DataListCell>
                      ]}
                    />
                  </DataListItemRow>
                </DataListItem>))}
              {this.state.isEmptyResults && (
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
              )}
            </DataList>
          </div>)}
      </React.Fragment>
    );
  }

  private getProducts = (allProducts) => {
    fetch(this.getProductsUrl())
      .then(response => response.json())
      .then(responseJSON => {
        let key; let singleProduct;
        for (const i of Object.keys(responseJSON.results)) {
          key = Object.keys(responseJSON.results)[i];
          singleProduct = responseJSON.results[key];
          singleProduct = Object.assign({ "isOpen": false }, singleProduct)
          allProducts.push(singleProduct)
        }
        this.setState({ results: allProducts })
      })
      .then(() => {
        if (Object.keys(this.state.results).length === 0) {
          this.setState({
            isEmptyResults: true
          });
        } else {
          this.setState({
            isEmptyResults: false
          });
        }
      })
  }

  private getProductsUrl() {
    const backend = "/content/products.query.json?nodeType=pant:product&orderby=name"
    return backend
  }

  private onToggle = (id) => (event: any) => {
    this.state.results.map(data => {
      if (data['jcr:uuid'] === id) {
        (data.isOpen as any) = !data.isOpen
        this.setState({ isProductDetails: false })
      }
    });
  };

  private onSelect = (event, data) => () => {
    this.setState({
      isProductDetails: !this.state.isProductDetails,
      productName: data.name,
    });
  };

  private setInput = input => {
    const versions: string[] = [];
    let searchString = '';
    this.setState({ input })
    this.state.allProducts.map(data => {
      searchString = '' + data.name
      if (searchString.toLowerCase().includes(input.toLowerCase())) {
        versions.push(data)
      }
    });
    this.setState({ results: versions })
  };
}

export { ProductListing }