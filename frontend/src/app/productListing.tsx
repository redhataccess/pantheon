import React, { Component } from 'react';
import { DataList, DataListItem, DataListCell, DataListItemRow, DataListItemCells, DataListAction, FormGroup,
  OptionsMenu, OptionsMenuItem, OptionsMenuToggle, TextInput } from '@patternfly/react-core';
import '@app/app.css';
import { ProductDetails } from '@app/productDetails';
import { Redirect } from 'react-router-dom'

class ProductListing extends Component {
 
  public state = {
    allProducts: [],
    initialLoad: true,
    input: '',
    isEmptyResults: false,
    isOpen: false,
    isProductDetails: false,
    loggedinStatus: false,
    login: false,
    productName: '',
    redirect: false,
    results: []
  };

  // render method transforms the react components into DOM nodes for the browser.
  public render() {
    const id = 'userID';
    const descriptionKey = "description";
    const nameKey = "name";
    const propsKey = "match"
    const propsKeyChild = "isExact"
    const isOpenKey = "isOpen"

    if (!this.state.loggedinStatus && this.state.initialLoad===true) {
      fetch("/system/sling/info.sessionInfo.json")
        .then(response => response.json())
        .then(responseJSON => {
          if (responseJSON[id] !== 'anonymous') {
            this.setState({ loggedinStatus: true })
          }
        })
    }
   
    if (this.props[propsKey] !== undefined) {
      
      // prop will be true if it comes through nav links
       if(this.props[propsKey][propsKeyChild] === true){
         this.state.results.map(data => {
             (data[isOpenKey] as any) = false
         });
         this.setState({isProductDetails: false})
       }
  
       // setting prop to false once it comes through nav links
       this.props[propsKey][propsKeyChild]=false;
  
    }

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
                    <span id="{data['name']}">{data[nameKey]}</span>
                  </DataListCell>,
                  <DataListCell key="secondary content"  width={2}>{data[descriptionKey]}</DataListCell>,
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
                        isOpen={data[isOpenKey]}
                        toggle={<OptionsMenuToggle onToggle={this.onToggle(data['jcr:uuid'])} />} />
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
        <div>
          {this.checkAuth()}
          {this.loginRedirect()}
        </div>
      </React.Fragment>
    );
  }

  private getProducts = (allProducts) => {
    this.setState({ initialLoad: false })
    fetch(this.getProductsUrl())
      .then(response => response.json())
      .then(responseJSON => {
        let key; let singleProduct;
        for( const i of Object.keys(responseJSON.results)){
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
      const backend ="/content/products.query.json?nodeType=pant:product&orderby=name"
      return backend
    }

    private onToggle = (id) => (event: any) => {
      const isOpenKey = "isOpen"
      this.state.results.map(data => {
        if(data['jcr:uuid']===id){
            (data[isOpenKey] as any) = !data[isOpenKey]
            this.setState({isProductDetails: false})
        }
      });
     };
  
    private onSelect = (event,data) => () => {
      const nameKey = "name"
      const urlKey = "url"
      this.setState({
        initialLoad: false,
        isProductDetails: !this.state.isProductDetails,
        productName: data[nameKey],
      });
    };

    private setInput = input => {
      const versions = [];
      const nameKey = "name"
      let searchString = '';
      this.setState({input})
      this.state.allProducts.map(data => {
            searchString = ''+data[nameKey]
            if(searchString.toLowerCase().includes(input.toLowerCase())){
              versions.push(data)
            }
      });
      this.setState({results: versions})
  };

  private loginRedirect = () => {
    if (this.state.login) {
      return <Redirect to='/login' />
    } else {
      return ""
    }
  }

  private checkAuth = () => {
    fetch("/system/sling/info.sessionInfo.json")
      .then(response => response.json())
      .then(responseJSON => {
        const key = "userID"
        if (responseJSON[key] === 'anonymous') {
          this.setState({ login: true })
        }
      })
  }
}

export { ProductListing }