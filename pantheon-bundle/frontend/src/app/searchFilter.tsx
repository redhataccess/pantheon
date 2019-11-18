import React, { Component } from 'react';
import {
  Button, ButtonVariant, TextInput, InputGroup, Chip, ChipGroup, ChipGroupToolbarItem, FormSelect, FormSelectOption
} from '@patternfly/react-core';
import '@app/app.css';
import { SearchIcon, SortAlphaDownIcon, SortAlphaUpIcon } from '@patternfly/react-icons';

class SearchFilter extends Component<any, any> {
  constructor(props) {
    super(props);
    this.state = {
      allProducts: [],
      chipGroups: [],
      isSortedUp: true,
      moduleTypeValue: '',
      productOptions: [
        { value: '', label: 'Select a Product', disabled: false },
      ],
      productValue: '',
      productsQueryParam: '',
      productsToQuery: [],
      productsUUID: [],
      productversionsQueryParam: '',
      searchText: '',
      sortByValue: '',
      versionOptions: [
        { value: '', label: 'Select a Version', disabled: false },
      ],
      versionSelected: '',
      versionUUID: '',
      versionValue: '',
      versionsToQuery: [],
    };
  }

  public componentDidMount() {
    this.fetchProductVersionDetails()
  }

  public render() {
    const { chipGroups } = this.state;

    let verOptions = this.state.versionOptions
    if (this.state.allProducts[this.state.productValue]) {
      verOptions = this.state.allProducts[this.state.productValue]
    }

    const moduleTypeItems = [
      { value: 'All', label: 'All', disabled: false },
      { value: 'Concept', label: 'Concept', disabled: false },
      { value: 'Procedure', label: 'Procedure', disabled: false },
      { value: 'Reference', label: 'Reference', disabled: false }
    ]

    const sortItems = [
      { value: 'Title', label: 'Title', disabled: false },
      { value: 'Product', label: 'Product', disabled: false },
      { value: 'Published date', label: 'Published date', disabled: false },
      { value: 'Updated date', label: 'Updated date', disabled: false },
      { value: 'Module type', label: 'Module type', disabled: false }
    ]


    return (
      <React.Fragment>
        <div className="row-filter" >
          <InputGroup className="small-margin">
            <TextInput id="searchFilterInput" type="text" onKeyDown={this.props.onKeyDown} value={this.state.searchText} onChange={this.setSearchText} />
            <Button onClick={this.props.onClick} variant={ButtonVariant.control} aria-label="search button for search input">
              <SearchIcon />
            </Button>
          </InputGroup>


          <FormSelect value={this.state.productValue} onChange={this.onChangeProduct} aria-label="FormSelect Product" id="productForm">
            {this.state.productOptions.map((option, index) => (
              <FormSelectOption isDisabled={option.disabled} key={index} value={option.value} label={option.label} />
            ))}
          </FormSelect>

          <FormSelect className="small-margin" value={this.state.versionUUID} onChange={this.onChangeVersion} aria-label="FormSelect Version" id="productVersionForm">
            {verOptions.map((option) => (
              <FormSelectOption isDisabled={false} key={option.value} value={option.value} label={option.label} required={false} />
            ))}
          </FormSelect>

          <FormSelect className="small-margin" value={this.state.moduleTypeValue} onChange={this.onChangeModuleType} aria-label="FormSelect ModuleType" id="moduleTypeForm">
            {moduleTypeItems.map((option) => (
              <FormSelectOption isDisabled={false} key={option.value} value={option.value} label={option.label} required={false} />
            ))}
          </FormSelect>

          <FormSelect className="small-margin" value={this.state.sortByValue} onChange={this.onChangeSort} aria-label="FormSelect Sort" id="sortForm">
            {sortItems.map((option) => (
              <FormSelectOption isDisabled={false} key={option.value} value={option.value} label={option.label} required={false} />
            ))}
          </FormSelect>

          <Button onClick={this.setSortedUp} variant={ButtonVariant.control} aria-label="search button for search input">
            {this.state.isSortedUp ? <SortAlphaDownIcon /> : <SortAlphaUpIcon />}
          </Button>


        </div>
        <ChipGroup withToolbar={true}>
          {chipGroups.map(currentGroup => (
            <ChipGroupToolbarItem key={currentGroup.category} categoryName={currentGroup.category}>
              {currentGroup.chips.map(chip => (
                <Chip key={chip} onClick={this.deleteItem(chip)}>
                  {chip}
                </Chip>
              ))}
            </ChipGroupToolbarItem>
          ))}
        </ChipGroup>
      </React.Fragment>

    );
  }

  private setSearchText = (event) => this.setState({ searchText: event }, () => {
    this.setQuery();
  });

  private setSortedUp = () => {
    this.setState({ isSortedUp: !this.state.isSortedUp }, () => {
      this.setQuery()
    })
  };

  private fetchProductVersionDetails = () => {

    const path = '/content/products.3.json'
    let key
    const products = new Array()
    const prodUUID = new Array()

    fetch(path)
      .then((response) => {
        if (response.ok) {
          return response.json();
        } else if (response.status === 404) {
          return products
        } else {
          throw new Error(response.statusText);
        }
      })
      .then(responseJSON => {
        for (const i of Object.keys(responseJSON)) {
          key = i
          const nameKey = "name"
          const versionKey = "versions"
          const uuidKey = "jcr:uuid";
          if ((key !== 'jcr:primaryType')) {
            if (responseJSON[key][nameKey] !== undefined) {
              const pName = responseJSON[key][nameKey]
              const versionObj = responseJSON[key][versionKey]
              const productUUID = responseJSON[key][uuidKey]
              if (versionObj) {
                let vKey;
                const versions = [{ value: '', label: 'Select a Version', disabled: false }, { value: 'All', label: 'All', disabled: false },]
                for (const item in Object.keys(versionObj)) {
                  if (Object.keys(versionObj)[item] !== undefined) {
                    vKey = Object.keys(versionObj)[item]
                    if (vKey !== 'jcr:primaryType') {
                      if (versionObj[vKey][nameKey]) {
                        versions.push({ value: versionObj[vKey][uuidKey], label: versionObj[vKey][nameKey], disabled: false })
                      }
                    }
                  }
                }
                products[pName] = versions
                prodUUID[pName] = productUUID
              }
            }
          }
        }
        this.setState({
          allProducts: products,
          productsUUID: prodUUID
        })

        if (products) {
          const productItems = [{ value: 'Select a Product', label: 'Select a Product', disabled: false },]
          // tslint:disable-next-line: forin
          for (const item in products) {
            productItems.push({ value: item, label: item, disabled: false })
          }
          if (productItems.length > 1) {
            this.setState({ productOptions: productItems })
          }
        }
      })
      .catch((error) => {
        console.log(error)
      });
    return products;
  };

  private onChangeProduct = (productValue) => {
    this.setState({ productValue });
  }
  private onChangeVersion = () => {
    if (event !== undefined) {
      if (event.target !== null) {
        const selectedStr = "selectedOptions"
        if (this.state.versionUUID !== event.target[selectedStr][0].value) {
          this.setState({
            versionSelected: event.target[selectedStr][0].label,
            versionUUID: event.target[selectedStr][0].value,
            versionValue: event.target[selectedStr][0].label,
          }, () => {
            this.addChipItem();
          });
        }
      }
    }
  }

  private onChangeSort = (sortByValue) => {
    this.setState({ sortByValue }, () => {
      this.setQuery();
    });
  }
  private onChangeModuleType = (moduleTypeValue) => {
    this.setState({ moduleTypeValue }, () => {
      this.setQuery();
    });
  }

  private deleteItem = (id) => (event: any) => {
    const copyOfChipGroups = this.state.chipGroups;
    for (let i = 0; copyOfChipGroups.length > i; i++) {
      const index = copyOfChipGroups[i].chips.indexOf(id);
      if (index !== -1) {
        copyOfChipGroups[i].chips.splice(index, 1);
        // check if this is the last item in the group category
        if (copyOfChipGroups[i].chips.length === 0) {
          copyOfChipGroups.splice(i, 1);
          this.setState({ chipGroups: copyOfChipGroups }, () => {
            this.setQuery();
          });
        } else {
          this.setState({ chipGroups: copyOfChipGroups }, () => {
            this.setQuery();
          });
        }
      }
    }

    // TODO DELETE from productversionsQueryParam and productsQueryParam
  };

  private addChipItem = () => {
    const copyOfChipGroups = this.state.chipGroups;
    let exist = false
    let index = 0
    for (let i = 0; copyOfChipGroups.length > i; i++) {
      const category = copyOfChipGroups[i].category
      if (category === this.state.productValue) {
        exist = true
        index = i
        break
      }
    }
    if (exist) {
      let chipExists = false
      for (const i of copyOfChipGroups[index].chips) {
        const chip = i
        if (chip === this.state.versionSelected) {
          chipExists = true
        }

      }
      if (!chipExists && this.state.versionSelected !== "Select a Version") {
        copyOfChipGroups[index].chips.push(this.state.versionSelected);
      }
    } else {
      copyOfChipGroups.push({
        category: this.state.productValue,
        chips: [this.state.versionSelected]
      })
    }

    const uuidKey = "value"
    // console.log("This is the Selected Product UUID: " + JSON.stringify(this.state.productsUUID[this.state.productValue]))
    // console.log("This is the Selected Version: " + JSON.stringify(this.state.allProducts[this.state.productValue].filter((e) => e.label === this.state.versionValue)[0][uuidKey]))
    const versionUUID = this.state.allProducts[this.state.productValue].filter((e) => e.label === this.state.versionValue)[0][uuidKey]
    // If version is All just add the product.
    let prodQuery = this.state.productsQueryParam
    let verQuery = this.state.productversionsQueryParam
    if (versionUUID === "All") {
      if (this.state.productsQueryParam !== '') {
        prodQuery += '&'
      }
      prodQuery += "product=" + this.state.productsUUID[this.state.productValue]
    } else if (versionUUID !== "") {
      if (this.state.productversionsQueryParam !== '') {
        verQuery += '&'
      }
      verQuery += "productversion=" + versionUUID
    }

    this.setState({ chipGroups: copyOfChipGroups,productsQueryParam: prodQuery, productversionsQueryParam: verQuery }, () => {
      this.setQuery();
    });
  };

  // Should be called after each change of state
  private setQuery = () => {
    let searchQuery = ""
    if (this.state.searchText !== "") {
      searchQuery += "search=" + this.state.searchText
    }

    if (this.state.productsQueryParam !== "") {
      if (searchQuery !== "") {
        searchQuery += "&"
      }
      searchQuery += this.state.productsQueryParam
    }

    if (this.state.productversionsQueryParam !== "") {
      if (searchQuery !== "") {
        searchQuery += "&"
      }
      searchQuery += this.state.productversionsQueryParam
    }

    // Default is All and should not add to the filter.
    if (this.state.moduleTypeValue !== "" && this.state.moduleTypeValue !== "All") {
      if (searchQuery !== "") {
        searchQuery += "&"
      }
      searchQuery += "type=" + this.state.moduleTypeValue
    }

    // Default key is Title
    if (searchQuery !== "") {
      searchQuery += "&"
    }
    if (this.state.sortByValue === "") {
      searchQuery += "key=Title"
    } else {
      searchQuery += "key=" + this.state.sortByValue
    }

    // isSortedUp is a boolean and will always have a set default
    if (searchQuery !== "") {
      searchQuery += "&"
    }
    searchQuery += "direction=" + (this.state.isSortedUp ? "desc" : "asc")

    this.props.filterQuery(searchQuery)
    console.log("This is the query: " + searchQuery)
  }
}

export { SearchFilter }; 