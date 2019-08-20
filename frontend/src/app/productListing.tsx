import React, { Component } from 'react';
import { Button, Dropdown, DropdownItem, DropdownPosition, KebabToggle, DataList, DataListItem, DataListCell, DataListItemRow, DataListItemCells, DataListAction,
  OptionsMenu, OptionsMenuItem, OptionsMenuToggle } from '@patternfly/react-core';
import '@app/app.css';
import { ProductDetails } from '@app/productDetails';
import { Link } from "react-router-dom";
import { RouteComponentProps } from 'react-router-dom';

class ProductListing extends Component {
 
  public state = {
    allProducts: [],
    isOpen: false,
    isDeleted: false,
    loggedinStatus: false,
    initialLoad: true,
    isEmptyResults: false,
    results: [],
    filteredResults: [],
    q: '',
    //@TODO. removed unused state variables
    login: false,
    productDescription: '',
    productName: '',
    productUrl: '',
    redirect: false,
    isProductDetails: false
  };

  onSelect2 = event => {
    const id = event.currentTarget.id;
    this.setState((prevState) => {
      return { [id]: !prevState[id] };
    });
  };
  
  // onChange(event) {
  //   const q = event.target.value.toLowerCase();
  //   this.setState({ q }, () => this.filterList());
  // }

  // filterList() {
  //   let results = this.state.results;
  //   let q = this.state.q;

  //   results = results.filter((result) => {
  //     return result.name.toLowerCase().indexOf(q) != -1; // returns true or false
  //   });
  //   this.setState({ filteredResults: results });
  // }

  // filterList2(event) {
  //   let value = event.target.value;
  //   let results = this.state.results, result=[];
  //   result = results.filter((r)=>{
  //       return r.name.toLowerCase().search(value) != -1;
  //   });
  //   this.setState({result});
  // }
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
        <DataList aria-label="single action data list example ">

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
        </DataList>)}
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
        productName: data["name"],
        productUrl: data["url"]
      });
    };

}

export { ProductListing }