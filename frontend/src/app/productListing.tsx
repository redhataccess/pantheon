import React, { Component } from 'react';
import { Button, Dropdown, DropdownItem, DropdownPosition, KebabToggle, DataList, DataListItem, DataListCell, DataListItemRow, DataListItemCells, DataListAction,
  OptionsMenu, OptionsMenuItem, OptionsMenuToggle } from '@patternfly/react-core';
import '@app/app.css';
import { ProductDetails } from '@app/productDetails';
import { Link } from "react-router-dom";

class ProductListing extends Component {
 
  public state = {
    isOpen: false, 
    isDeleted: false,
    loggedinStatus: false,
    initialLoad: true,
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

    return (
      <React.Fragment>
        {this.state.isProductDetails && (<ProductDetails productName={this.state.productName}/>)}
        {console.log('initial Load: ',this.state.initialLoad)}
        {console.log('isProducDetails: ', this.state.isProductDetails)}
        {this.state.initialLoad && this.getProducts()} 
        {!this.state.isProductDetails && (
        <DataList aria-label="single action data list example ">
          {!this.state.isDeleted && (
            <DataListItem aria-labelledby="single-action-item1">
              <DataListItemRow>
                <DataListItemCells
                  dataListCells={[
                    <DataListCell key="primary content">
                      <span id="single-action-item1">Single actionable Primary content</span>
                    </DataListCell>,
                    <DataListCell key="secondary content">Single actionable Secondary content</DataListCell>
                  ]}
                />
                <DataListAction
                  aria-labelledby="single-action-item1 single-action-action1"
                  id="single-action-action1"
                  aria-label="Actions"
                >
                  <Button
                    onClick={() => {
                      if (confirm('Are you sure?')) {
                        this.setState({ isDeleted: true });
                      }
                    }}
                    variant="primary"
                    key="delete-action"
                  >
                    Delete
                  </Button>
                </DataListAction>
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
                  <DataListCell key="secondary content">{data["description"]}</DataListCell>
                ]}
              />
              <DataListAction
                aria-labelledby="multi-actions-item1 {data['jcr:uuid']}"
                id="{data['jcr:uuid']}"
                aria-label="Actions"
              >
                {/* <Dropdown
                  isPlain={true}
                  position={DropdownPosition.right}
                  isOpen={this.state.isOpen}
                  onSelect={() => this.onSelect(event,data)}
                  toggle={<KebabToggle onToggle={() => this.onToggle(this.state.isOpen,data)} />}
                  dropdownItems={[
                    <DropdownItem key="action" href="@app/search">Product Details</DropdownItem>,                                  
                  ]
                }
                /> */}
                  <OptionsMenu
                    isPlain={true}
                    id="options-menu-single-option-example"
                    menuItems={[
                      <OptionsMenuItem onSelect={()=>this.onSelect(event,data)}>Product Details</OptionsMenuItem>]
                    }
                    isOpen={this.state.isOpen}
                    toggle={<OptionsMenuToggle onToggle={this.onToggle}/>} />

              </DataListAction>
            </DataListItemRow>
          </DataListItem>))}
        </DataList>)}
      </React.Fragment>
    );
  }

  private getProducts = () => {
    this.setState({ initialLoad: false })
    fetch(this.getProductsUrl())
      .then(response => response.json())
      .then(responseJSON => this.setState({ results: responseJSON.results }))
      .then(() => {
        console.log("results => " + this.state.results)      
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

    // private onToggle = (isOpen,data) => {
    //   this.setState({ isOpen,
    //     isProductDetails: !this.state.isProductDetails,
    //     productName: data["name"]
    //     },() => {console.log('isopen: ',this.state.isOpen)
    //   console.log('toggled')});
    // };
  
    // private onSelect = (event,data) => {
    //   this.setState({
    //     isOpen: !this.state.isOpen
    //   },() => {console.log('isopen on select: ',this.state.isOpen)});
    // };

    private onToggle = () => {
      this.setState({
          isOpen: !this.state.isOpen
      });
     };
  
    private onSelect = (event,data) => {
      this.setState({
        initialLoad: false,
        isProductDetails: !this.state.isProductDetails,
        productName: data["name"]
      });
    };

}

export { ProductListing }