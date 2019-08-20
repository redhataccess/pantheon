import React, { Component } from 'react';
import { Button, Dropdown, DropdownItem, DropdownPosition, KebabToggle, DataList, DataListItem, DataListCell, DataListItemRow, DataListItemCells, DataListAction,Form, FormGroup,
  OptionsMenu, OptionsMenuItem, OptionsMenuToggle, Text, TextContent, TextVariants, TextInput } from '@patternfly/react-core';
import '@app/app.css';
import { ProductDetails } from '@app/productDetails';
import { Link } from "react-router-dom";
import { RouteComponentProps } from 'react-router-dom';
import { version } from 'react-dom';

class ProductListing extends Component {
 
  public state = {
    allProducts: [],
    isOpen: false, 
    isDeleted: false,
    loggedinStatus: false,
    initialLoad: true,
    input: '',
    isEmptyResults: false,
    results: [],
    //@TODO. removed unused state variables
    login: false,
    productDescription: '',
    productName: '',
    redirect: false,
    isProductDetails: false
  };

  //private onSelect = event => {
  //  this.setState(prevState => ({
  //    isOpen: !prevState.isOpen
  //  }));
  //};
  // render method transforms the react components into DOM nodes for the browser.
  public render() {
    const id = 'userID';
    if (!this.state.loggedinStatus && this.state.initialLoad===true) {
      fetch("/system/sling/info.sessionInfo.json")
        .then(response => response.json())
        .then(responseJSON => {
          if (responseJSON[id] !== 'anonymous') {
            this.setState({ loggedinStatus: true })
          }
        })
    }

    //prop will be true if it comes through nav links
    if(this.props['match']['isExact'] === true){
      this.state.results.map(data => {
          (data['isOpen'] as any) = false
      });
      this.setState({isProductDetails: false})
    }

    //setting prop to false once it comes through nav links
    this.props['match']['isExact']=false;

    return (
      <React.Fragment>
        {this.state.isProductDetails && (<ProductDetails productName={this.state.productName}/>)}
        {this.state.initialLoad && this.getProducts(this.state.allProducts)} 
        {!this.state.isProductDetails && (
        <div>  
        <FormGroup
          label="Search Products"
          fieldId="search"
        >
          <div className="row-view">
            <TextInput id="search" type="text" onChange={this.setInput} />
          </div>
        </FormGroup>
        <DataList aria-label="single action data list example ">
          {!this.state.isDeleted && (
            <DataListItem aria-labelledby="single-action-item1">
              <DataListItemRow>
                <DataListItemCells
                  dataListCells={[
                    <DataListCell key="primary content">
                      <span className="sp-prop-nosort" id="product-name">Product Name</span>
                    </DataListCell>,
                    <DataListCell key="secondary content"  width={2}>
                      <span className="sp-prop-nosort" id="product-description">Product Description</span>
                      </DataListCell>
                  ]}
                />
              </DataListItemRow>
            </DataListItem>
          )}

          { !this.state.isEmptyResults && this.state.results.map(data => ( 
          <DataListItem aria-labelledby="multi-actions-item1">
            <DataListItemRow>
            <DataListItemCells key={data["jcr:uuid"]} 
                dataListCells={[
                  <DataListCell key="primary content">
                    <span id="{data['name']}">{data["name"]}</span>
                  </DataListCell>,
                  <DataListCell key="secondary content"  width={2}>{data["description"]}</DataListCell>,
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
                        isOpen={data['isOpen']}
                        toggle={<OptionsMenuToggle onToggle={this.onToggle(data['jcr:uuid'])} />} />
                    </DataListAction>
                  </DataListCell>
                ]}
              />
            </DataListItemRow>
          </DataListItem>))}
        </DataList>
        </div>)}
      </React.Fragment>
    );
  }

  private getProducts = (allProducts) => {
    this.setState({ initialLoad: false })
    fetch(this.getProductsUrl())
      .then(response => response.json())
      .then(responseJSON => {
        let key; let singleProduct;
        for(let i=0; i<Object.keys(responseJSON.results).length; i++){
          key = Object.keys(responseJSON.results)[i];
          singleProduct=responseJSON.results[key];
          singleProduct= Object.assign({"isOpen":false},singleProduct)
          allProducts.push(singleProduct)
       }
        this.setState({ results: allProducts })
      })
      .then(() => {
        console.log(this.state.loggedinStatus)
        if (Object.keys(this.state.results).length === 0) {
          this.setState({
            isEmptyResults : true
          },()=>{console.log(this.state.isEmptyResults,this.state.initialLoad)});
        } else {
          this.setState({
            isEmptyResults : false
          },()=>{console.log(this.state.isEmptyResults,this.state.initialLoad)});
        }
      })
    }

    private getProductsUrl() {
      //let backend = "/content/products.1.json"
      let backend ="/content/products.query.json?nodeType=pant:product&orderby=name"
      console.log(backend)
      return backend
    }

    private onToggle = (id) => (event: any) => {
      this.state.results.map(data => {
        if(data['jcr:uuid']===id){
            console.log('data uuid: ',data['jcr:uuid']);
            (data['isOpen'] as any) = !data['isOpen']
            this.setState({isProductDetails: false})
        }
      });
     };
  
    private onSelect = (event,data) => () => {
      this.setState({
        initialLoad: false,
        isProductDetails: !this.state.isProductDetails,
        productName: data["name"]
      });
    };

    private setInput = (event) => {
      let versions = [];
      let searchString = '';
      this.state.allProducts.map(data => {
            searchString = ''+data["name"]
            if(searchString.includes(event)){
              versions.push(data)
            }
      });
      this.setState({results: versions})
  };


}

export { ProductListing }