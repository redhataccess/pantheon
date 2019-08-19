import React, { Component } from 'react';
import { Button, Dropdown, DropdownItem, DropdownPosition, KebabToggle, DataList, DataListItem, DataListCell, DataListItemRow, DataListItemCells, DataListAction } from '@patternfly/react-core';
import '@app/app.css';
import { Redirect } from 'react-router-dom'

interface PantheonProduct {
  name: string,
  description: string,
  "jcr:uuid": string
}
class ProductListing extends Component {
 
  public state = {
    isOpen: false, 
    //isDeleted: false,
    loggedinStatus: false,
    initialLoad: true,
    isEmptyResults: false,
    results: Array<PantheonProduct>(),
    filteredResults: Array<PantheonProduct>(),
    q: '',
    //@TODO. removed unused state variables
    login: false,
    productDescription: '',
    productName: '',
    redirect: false
  };

  private onToggle = isOpen => {
    this.setState({ isOpen });
  };

  private onSelect = event => {
    this.setState({
      isOpen: !this.state.isOpen
    });
  };

  onSelect2 = event => {
    const id = event.currentTarget.id;
    this.setState((prevState) => {
      return { [id]: !prevState[id] };
    });
  };
  
  onChange(event) {
    const q = event.target.value.toLowerCase();
    this.setState({ q }, () => this.filterList());
  }

  filterList() {
    let results = this.state.results;
    let q = this.state.q;

    results = results.filter((result) => {
      return result.name.toLowerCase().indexOf(q) != -1; // returns true or false
    });
    this.setState({ filteredResults: results });
  }

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
    return (
      <React.Fragment>
        {this.state.initialLoad && this.getProducts()}
        <input
          type="text"
          placeholder="Search"
          value={this.state.q}
          onChange={this.onChange}
        />
        <DataList aria-label="single action data list">
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
                <Dropdown
                  isPlain
                  position={DropdownPosition.right}
                  isOpen={this.state.isOpen}
                  onSelect={this.onSelect}
                  toggle={<KebabToggle onToggle={this.onToggle} />}
                  dropdownItems={[
                    <DropdownItem key={data["jcr:uuid"]}>Product Detail</DropdownItem>,
                                      
                  ]}
                />
              </DataListAction>
            </DataListItemRow>
          </DataListItem>))}
        </DataList>

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
          });
        } else {
          this.setState({
            isEmptyResults : false
          });
        }
        console.log(this.state.isEmptyResults)
      })
    }

    private getProductsUrl() {
      //let backend = "/content/products.1.json"
      let backend ="/content/products.query.json?nodeType=pant:product&orderby=name"
      console.log(backend)
      return backend
    }
}

export { ProductListing }