import React, { Component } from 'react';
import {
  Button, ButtonVariant, TextInput, InputGroup, Chip, ChipGroup, ChipGroupToolbarItem, FormSelect, FormSelectOption
} from '@patternfly/react-core';
import '@app/app.css';
import { CaretDownIcon, SearchIcon, SortAlphaDownIcon, SortAlphaUpIcon } from '@patternfly/react-icons';

class SearchFilter extends Component<any, any> {
  constructor(props) {
    super(props);
    this.state = {
      allProducts: [],
      chipGroups: [],
      moduleTypeValue: '',
      productOptions: [
        { value: '', label: 'Select a Product', disabled: false },
      ],
      productValue: '',
      sortByValue: '',
      versionOptions: [
        { value: '', label: 'Select a Version', disabled: false },
      ],
      versionSelected: '',
      versionUUID: '',
      versionValue: '',
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
            <TextInput id="searchFilterInput" type="text" onKeyDown={this.props.onKeyDown} value={this.props.value} onChange={this.props.onChange} />
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

          <Button onClick={this.props.onSort} variant={ButtonVariant.control} aria-label="search button for search input">
            {this.props.isSortedUp ? <SortAlphaDownIcon /> : <SortAlphaUpIcon />}
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

  private fetchProductVersionDetails = () => {

    const path = '/content/products.3.json'
    let key
    const products = new Array()

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
          if ((key !== 'jcr:primaryType')) {
            if (responseJSON[key][nameKey] !== undefined) {
              const pName = responseJSON[key][nameKey]
              const versionObj = responseJSON[key][versionKey]

              if (versionObj) {
                let vKey;
                const versions = [{ value: '', label: 'Select a Version', disabled: false }, { value: 'All', label: 'All', disabled: false },]
                const uuidKey = "jcr:uuid";
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
              }
            }
          }
        }
        this.setState({
          allProducts: products
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
    this.setState({ sortByValue });
  }
  private onChangeModuleType = (moduleTypeValue) => {
    this.setState({ moduleTypeValue });
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
          this.setState({ chipGroups: copyOfChipGroups });
        } else {
          this.setState({ chipGroups: copyOfChipGroups });
        }
      }
    }
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
      if (!chipExists) {
        copyOfChipGroups[index].chips.push(this.state.versionSelected);
      }
    } else {
      copyOfChipGroups.push({
        category: this.state.productValue,
        chips: [this.state.versionSelected]
      })
    }
    this.setState({ chipGroups: copyOfChipGroups });
    this.props.filterQuery(this.state.productValue)
  };
}

export { SearchFilter }; 