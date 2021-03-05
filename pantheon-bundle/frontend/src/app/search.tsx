import React, { Component, FormEvent } from "react";
import {
  Drawer,
  DrawerPanelContent,
  DrawerContent,
  DrawerContentBody,
  DrawerPanelBody,
  DrawerHead,
  DrawerActions,
  DrawerCloseButton,
  Button, ButtonVariant,
  InputGroup,
  Select,
  SelectOption,
  Dropdown,
  DropdownItem,
  DropdownSeparator,
  KebabToggle,
  Toolbar, ToolbarItem, ToolbarContent, ToolbarFilter, ToolbarToggleGroup, ToolbarGroup,
  TextInput,
  SelectVariant,
  ExpandableSection,
  Checkbox,
  Divider,
  SimpleListItem,
  SimpleList,
  SearchInput,
  Alert,
  ToolbarChipGroup,
  ToolbarChip,
  Modal,
  Form,
  FormGroup,
  AlertActionCloseButton,
  FormSelect,
  FormSelectOption,
  InputGroupText,
  Title,
  BaseSizes,
  ModalVariant,

} from "@patternfly/react-core";

import { SearchResults } from "@app/searchResults";
import { BuildInfo } from "./components/Chrome/Header/BuildInfo"

import "@app/app.css";
import SearchIcon from "@patternfly/react-icons/dist/js/icons/search-icon";
import FilterIcon from "@patternfly/react-icons/dist/js/icons/filter-icon";
import { IAppState } from "@app/app"
import { Metadata } from "@app/Constants"

export interface ISearchState {
  filterLabel: string
  isExpanded: boolean
  assembliesIsExpanded: boolean
  expandableSectionIsExpanded: boolean
  modulesIsExpanded: boolean
  productFilterIsExpanded: boolean
  repoFilterIsExpanded: boolean
  products: Array<{ name: string, id: string }>
  repositories: Array<{ name: string, id: string, checked: boolean }>
  filteredRepositories: Array<{ name: string, id: string, checked: boolean }>

  inputValue: string,
  statusIsExpanded: boolean,
  ctypeIsExpanded: boolean,
  filters: {
    ctype: any,
    status: any
  },

  productFilterValue: string
  repoFilterValue: string

  productsSelected: string[]
  repositoriesSelected: string[]
  documentsSelected: string[]
  isModalOpen: boolean
  alertTitle: string
  allProducts: any
  allProductVersions: any
  isMissingFields: boolean
  product: { label: string, value: string }
  productVersion: { label: string, uuid: string }
  urlFragment: string
  keywords: string
  usecaseOptions: any
  usecaseValue: string
}
class Search extends Component<IAppState, ISearchState> {
  private drawerRef: React.RefObject<HTMLInputElement>;

  constructor(props) {
    super(props);
    this.state = {
      // states for drawer
      filterLabel: "repo",
      isExpanded: true,
      assembliesIsExpanded: true,
      expandableSectionIsExpanded: true,
      modulesIsExpanded: true,
      productFilterIsExpanded: true,
      repoFilterIsExpanded: true,
      products: [{ name: "", id: "" }],
      repositories: [{ name: "", id: "", checked: false }],
      filteredRepositories: [{ name: "", id: "", checked: false }],
      // states for toolbar
      inputValue: "",
      statusIsExpanded: false,
      ctypeIsExpanded: false,
      filters: {
        ctype: [],
        status: []
      },

      // filters
      productFilterValue: "",
      repoFilterValue: "",

      // search
      productsSelected: [],
      repositoriesSelected: [],

      // bulk operation
      documentsSelected: [],
      isModalOpen: false,
      alertTitle: "",
      allProducts: [],
      allProductVersions: [],
      isMissingFields: false,
      product: { label: "", value: "" },
      productVersion: { label: "", uuid: "" },
      urlFragment: "",
      keywords: "",
      usecaseOptions: [
        { value: "", label: "Select Use Case", disabled: false }
      ],
      usecaseValue: "",
    };
    this.drawerRef = React.createRef();

  }

  public componentDidMount() {

    // list repos inside the drawer
    this.getRepositories()
    // product and id used for Filter
    // this.getProducts()

    // fetch products and label for metadata Modal
    this.fetchProducts()
    // TODO: enable resize
    // toolbar
    // window.addEventListener("resize", this.closeExpandableContent);
  }

  public componentWillMount() {
    // list repos inside the drawer
    this.getRepositories()
    // this.getProducts()
  }

  public componentWillUnmount() {
    // TODO: enable resize
    // toolbar
    // window.removeEventListener("resize", this.closeExpandableContent);
  }
  public render() {
    const { filterLabel, isExpanded, assembliesIsExpanded, modulesIsExpanded, productFilterIsExpanded, repoFilterIsExpanded, expandableSectionIsExpanded, repositories, inputValue, filters, statusIsExpanded, ctypeIsExpanded } = this.state;

    const panelContent = (
      <DrawerPanelContent widths={{ lg: "width_25" }}>
        <DrawerHead>
          <span className="pf-c-title pf-m-2xl" tabIndex={isExpanded ? 0 : -1} ref={this.drawerRef}>Filters</span>
          <DrawerActions>
            <DrawerCloseButton onClick={this.onCloseClick} />
          </DrawerActions>
          <ExpandableSection className="filters-drawer filters-drawer--by-repo" toggleText="By repository" isActive={true} isExpanded={repoFilterIsExpanded} onToggle={this.onRepositoriesToggle}>
            <SearchInput
              placeholder="Filter"
              value={this.state.repoFilterValue}
              onChange={this.onChangeRepoFilter}
              onClear={(evt) => this.onChangeRepoFilter("", evt)}
              className='filters-drawer__repo-search'
            />
            {this.state.filteredRepositories && this.state.filteredRepositories.length > 0 &&
              <SimpleList aria-label="Repository List">
                {this.state.filteredRepositories.map((data) => (
                  <SimpleListItem key={data.id} className='repo-list filters-drawer__repo-list'>
                    <Checkbox label={data.name} aria-label="uncontrolled checkbox" id={data.id} onChange={this.onSelectRepositories} isChecked={data.checked}/>
                  </SimpleListItem>
                ))}
              </SimpleList>
            }

          </ExpandableSection>
          <br />
          {/* <ExpandableSection toggleText="By product">
            <SearchInput
              placeholder="Filter"
              value={this.state.productFilterValue}
              onChange={this.onChangeProductFilter}
              onClear={(evt) => this.onChangeProductFilter("", evt)}
            />
            <SimpleList aria-label="Product List">
              {this.state.products.map((data) => (
                <SimpleListItem key={data.id}>
                  <Checkbox label={data.name} aria-label="uncontrolled checkbox" id={data.id} />
                </SimpleListItem>
              ))}
            </SimpleList>

          </ExpandableSection> */}
        </DrawerHead>
      </DrawerPanelContent>
    );
    const drawerContent = (
      <React.Fragment>
        <ExpandableSection toggleText="Modules" className="pf-c-title search-results__section search-results__section--module" isActive={true} isExpanded={modulesIsExpanded} onToggle={this.onModulesToggle}>
          <SearchResults
            contentType="module"
            keyWord={this.state.inputValue}
            repositoriesSelected={this.state.repositoriesSelected}
            productsSelected={this.state.productsSelected}
            userAuthenticated={this.props.userAuthenticated}
            filters={this.state.filters}
            onGetdocumentsSelected={this.getdocumentsSelected}
          />

        </ExpandableSection>
        <br />
        <ExpandableSection toggleText="Assemblies" className="pf-c-title search-results__section search-results__section--assembly" isActive={true} isExpanded={assembliesIsExpanded} onToggle={this.onAssembliesToggle}>
          <SearchResults
            contentType="assembly"
            keyWord={this.state.inputValue}
            repositoriesSelected={this.state.repositoriesSelected}
            productsSelected={this.state.productsSelected}
            userAuthenticated={this.props.userAuthenticated}
            filters={this.state.filters}
            onGetdocumentsSelected={this.getdocumentsSelected}
          />

        </ExpandableSection>
      </React.Fragment>
    );

    const statusMenuItems = [
      <SelectOption key="statusDraft" value="draft" label="Draft" className="dropdown-filter__option dropdown-filter__option--status dropdown-filter__option--draft" />,
      <SelectOption key="statusPublished" value="published" label="Published" className="dropdown-filter__option dropdown-filter__option--status dropdown-filter__option--published" />
    ];

    const contentTypeMenuItems = [
      <SelectOption key="ctypeConcept" value="CONCEPT" label="Concept" className="dropdown-filter__option dropdown-filter__option--content-type dropdown-filter__option--concept" />,
      <SelectOption key="ctypeProcedure" value="PROCEDURE" label="Procedure" className="dropdown-filter__option dropdown-filter__option--content-type dropdown-filter__option--procedure" />,
      <SelectOption key="ctypeReference" value="REFERENCE" label="Reference" className="dropdown-filter__option dropdown-filter__option--content-type dropdown-filter__option--reference" />
    ];

    const toggleGroupItems = (
      <React.Fragment>
        <ToolbarItem id="filters-bar__toolbar-toggle">
          <Button variant="tertiary" aria-expanded={isExpanded} onClick={this.onClick} icon={<FilterIcon />} />
        </ToolbarItem>
        <ToolbarItem>
          <InputGroup>
            <SearchInput
              className="filters-bar__name-search"
              name="textInput"
              id="textInput"
              placeholder="Find by name"
              type="search"
              aria-label="search input"
              onChange={this.onInputChange}
              onClear={this.onInputClear}
              value={inputValue}
            />
            <Button variant={ButtonVariant.control} aria-label="search button for search input">
              <SearchIcon />
            </Button>
          </InputGroup>
        </ToolbarItem>
        <ToolbarGroup variant="filter-group">
          <ToolbarFilter
            chips={filters.status}
            deleteChip={this.onDelete}
            deleteChipGroup={this.onDeleteGroup}
            categoryName="Status"
            className="dropdown-filter filters-bar__dropdown-filter filters-bar__dropdown-filter--status"
          >
            <Select
              variant={SelectVariant.checkbox}
              aria-label="Status"
              onToggle={this.onStatusToggle}
              onSelect={this.onStatusSelect}
              selections={filters.status}
              isOpen={statusIsExpanded}
              placeholderText="Status"
            >
              {statusMenuItems}
            </Select>
          </ToolbarFilter>
          <ToolbarFilter chips={filters.ctype} deleteChipGroup={this.onDeleteGroup} deleteChip={this.onDelete} categoryName="Content Type" className="dropdown-filter filters-bar__dropdown-filter filters-bar__dropdown-filter--content-type">
            <Select
              variant={SelectVariant.checkbox}
              aria-label="Content Type"
              onToggle={this.onCtypeToggle}
              onSelect={this.onCtypeSelect}
              selections={filters.ctype}
              isOpen={ctypeIsExpanded}
              placeholderText="Content Type"
            >
              {contentTypeMenuItems}
            </Select>
          </ToolbarFilter>
        </ToolbarGroup>
      </React.Fragment>
    );

    const toolbarItems = (
      <React.Fragment>
        <ToolbarToggleGroup toggleIcon={<FilterIcon />} breakpoint="xl">
          {toggleGroupItems}
        </ToolbarToggleGroup>
        <ToolbarGroup variant="icon-button-group">
        </ToolbarGroup>
        {this.props.userAuthenticated && (this.props.isAuthor || this.props.isPublisher || this.props.isAdmin) && <ToolbarItem>
          <Button variant="primary" onClick={this.handleModalToggle} id="edit_metadata">Edit metadata</Button>
        </ToolbarItem>}
        {this.props.userAuthenticated && (this.props.isPublisher || this.props.isAdmin) && <ToolbarItem>
          <Button variant="primary" isAriaDisabled={true}>Publish</Button>
        </ToolbarItem>}
        {this.props.userAuthenticated && (this.props.isPublisher || this.props.isAdmin) && <ToolbarItem>
          <Button variant="primary" isAriaDisabled={true}>Unpublish</Button>
        </ToolbarItem>}
        
      </React.Fragment>
    );

    const header = (
      <React.Fragment>
          <Title headingLevel="h1" size={BaseSizes["2xl"]}>
              Edit Metadata
        </Title>
      </React.Fragment>
  )
    const metadataModal = (
      <React.Fragment>
         <Modal
                    variant={ModalVariant.medium}
                    title="Edit metadata"
                    isOpen={this.state.isModalOpen}
                    header={header}
                    aria-label="Edit metadata"
                    onClose={this.handleModalClose}
                    actions={[
                        <Button form="edit_metadata" key="confirm" variant="primary" onClick={this.saveMetadata}>
                            Save
          </Button>,
                        <Button key="cancel" variant="secondary" onClick={this.handleModalToggle}>
                            Cancel
            </Button>
                    ]}
                >

                    {this.state.isMissingFields && (
                        <div className="notification-container">
                            <Alert
                                variant="warning"
                                title="Fields indicated by * are mandatory"
                                actionClose={<AlertActionCloseButton onClose={this.dismissNotification} />}
                            />
                            <br />
                        </div>
                    )}
                    <div id="edit_metadata_helper_text"><p>Editing multiple items. Changes made apply to all selected docs.</p></div>
                    <br />
                    <Form isWidthLimited={true} id="edit_metadata">
                        <FormGroup
                            label="Product Name"
                            isRequired={true}
                            fieldId="product-name"
                        >
                            <InputGroup>
                                <FormSelect value={this.state.product.value} onChange={this.onChangeProduct} aria-label="FormSelect Product">
                                    <FormSelectOption label="Select a Product" />
                                    {this.state.allProducts.map((option, key) => (
                                        <FormSelectOption key={key} value={option.value} label={option.label} />
                                    ))}
                                </FormSelect>
                                <FormSelect value={this.state.productVersion.uuid} onChange={this.onChangeVersion} aria-label="FormSelect Version" id="productVersion">
                                    <FormSelectOption label="Select a Version" />
                                    {this.state.allProductVersions.map((option, key) => (
                                        <FormSelectOption key={key} value={option["jcr:uuid"]} label={option.name} />
                                    ))}
                                </FormSelect>
                            </InputGroup>
                        </FormGroup>
                        <FormGroup
                            label="Document use case"
                            isRequired={true}
                            fieldId="document-usecase"
                            helperText="Explanations of document user cases included in documentation."
                        >
                            <FormSelect value={this.state.usecaseValue} onChange={this.onChangeUsecase} aria-label="FormSelect Usecase">
                                {Metadata.USE_CASES.map((option, key) => (
                                    <FormSelectOption key={"usecase_" + key} value={option} label={option} />
                                ))}
                            </FormSelect>
                        </FormGroup>
                        <FormGroup
                            label="Vanity URL fragment"
                            fieldId="url-fragment"
                            helperText="Edit individually to set or change vanity URL."
                        >
                            {/* <InputGroup>
                                <InputGroupText id="slash" aria-label="/">
                                    <span>/</span>
                                </InputGroupText>
                                <TextInput isRequired={false} id="url-fragment" type="text" placeholder="Edit individually to set or change vanity URL." value="" isDisabled={true}/>
                            </InputGroup> */}
                        </FormGroup>
                        <FormGroup
                            label="Search keywords"
                            isRequired={false}
                            fieldId="search-keywords"
                        >
                            <InputGroup>
                                <TextInput isRequired={false} id="search-keywords" type="text" placeholder="cat, dog, bird..." value={this.state.keywords} onChange={this.handleKeywordsInput} />
                            </InputGroup>
                        </FormGroup>
                        <div>
                            <input name="productVersion@TypeHint" type="hidden" value="Reference" />
                        </div>
                    </Form>
                </Modal>
      </React.Fragment>
    );

    return (
      <React.Fragment>
        <Toolbar
          id="toolbar-with-filter"
          className="pf-m-toggle-group-container filters-bar__filters-wrapper"
          collapseListedFiltersBreakpoint="xl"
          clearAllFilters={this.onDelete}
        >
          <ToolbarContent>{toolbarItems}</ToolbarContent>
        </Toolbar>
        <Divider />
        <Drawer isExpanded={isExpanded} isInline={true} position="left" onExpand={this.onExpand}>
          <DrawerContent panelContent={panelContent}>
            <DrawerContentBody className="search-results">
              {drawerContent}
              {metadataModal}
            </DrawerContentBody>
          </DrawerContent>
        </Drawer>
        <BuildInfo />
      </React.Fragment>
    );
  }

  // methods for drawer
  private getRepositories = () => {
    const path = "/content/repositories.harray.1.json"
    const repos = new Array()
    fetch(path)
      .then((response) => {
        if (response.ok) {
          return response.json()
        } else {
          throw new Error(response.statusText)
        }
      })
      .then(responseJSON => {
        for (const repository of responseJSON.__children__) {
          repos.push({ name: repository.__name__, id: repository["jcr:uuid"] })
        }
        this.setState({
          repositories: repos,
          filteredRepositories: repos
        })
      })
      .catch((error) => {
        console.log(error)
      })

  }

  private getProducts = () => {
    const path = "/content/products.harray.1.json"
    const products = new Array()
    fetch(path)
      .then((response) => {
        if (response.ok) {
          return response.json()
        } else {
          throw new Error(response.statusText)
        }
      })
      .then(responseJSON => {
        for (const product of responseJSON.__children__) {
          products.push({ name: product.__name__, id: product["jcr:uuid"] })
        }
        this.setState({ products })
      })
      .catch((error) => {
        console.log(error)
      })

  }
  private onExpand = () => {
    this.drawerRef.current && this.drawerRef.current.focus()
  };

  private onClick = () => {
    const isExpanded = !this.state.isExpanded;
    this.setState({
      isExpanded
    });
  };

  private onCloseClick = () => {
    this.setState({
      isExpanded: false
    });
  };

  // methods for toolbar
  private onInputChange = newValue => {
    this.setState({ inputValue: newValue });
  };

  private onInputClear = (event) => {
    this.setState({ inputValue: "" })
  }

  private onSelect = (type, event, selection) => {
    const checked = event.target.checked;
    this.setState(prevState => {
      const prevSelections = prevState.filters[type];
      return {
        filters: {
          ...prevState.filters,
          [type]: checked ? [...prevSelections, selection] : prevSelections.filter(value => value !== selection)
        }
      };
    });
  };

  private onStatusSelect = (event, selection) => {
    this.onSelect("status", event, selection);
  };

  private onCtypeSelect = (event, selection) => {
    this.onSelect("ctype", event, selection);
  };

  private onDelete = (type: string | ToolbarChipGroup = "", id: string | ToolbarChip = "") => {
    if (type) {
      let filterType
      filterType = typeof type === "object" ? type.name : type
      filterType = type === 'Content Type' ? 'ctype' : type
      this.setState(prevState => {
        const newState = Object.assign(prevState);
        return{
          filters:{
            ...prevState.filters,
            [filterType.toLowerCase()]: newState.filters[filterType.toLowerCase()].filter(s => s !== id),

          }
        }
      });
    } else {
      this.setState({
        filters: {
          ctype: [],
          status: []
        }
      });
    }
  };

  private onDeleteGroup = type => {
    let filterType
    filterType = type === 'Content Type' ? 'ctype' : type
    this.setState(prevState => {
      return {
        filters: {
          ...prevState.filters,
          [filterType.toLowerCase()]: []
        }
      };
    });
  };

  private onStatusToggle = isExpanded => {
    this.setState({
      statusIsExpanded: isExpanded
    });
  };

  private onCtypeToggle = isExpanded => {
    this.setState({
      ctypeIsExpanded: isExpanded
    });
  };

  // methods for filter search
  private onChangeRepoFilter = (value, event) => {
    this.setState({
      repoFilterValue: value
    });

    // check for input value
    if (value) {
      // filter and return repositories that include input value, and set state to the filtered list
      let filtered = this.state.repositories.filter(data => data.name.toLowerCase().includes(value.toLowerCase()))
      this.setState({
        filteredRepositories: filtered
      })
    } else {
      this.getRepositories()
    }
  };

  private onChangeProductFilter = (value, event) => {
    this.setState({
      productFilterValue: value
    });

    if (value) {
      let inputString = "";
      const matchFound = [{ name: "", id: "" }];

      this.state.products.map(data => {
        inputString = "" + data.name
        if (inputString.toLowerCase().includes(value.toLowerCase())) {
          matchFound.push(data)
        }
      });
      this.setState({ products: matchFound })
    } else {
      this.getProducts()
    }
  };

  private onSelectRepositories = (checked, event) => {
    let repositoriesSelected = new Array()
    let repositories

    repositories = this.state.repositories.map(item => {
      if (item.id === event.target.id) {
        item.checked = checked; 
      }
      return item;
    });

    repositoriesSelected = repositories.map(item => {
      if (item.checked !== undefined && item.checked === true) {
        if (item.name !== undefined) {
          return item.name
        }
      }
    });

    // filter undefined values
    repositoriesSelected = repositoriesSelected.filter(r => r !== undefined)

    this.setState({
      repositories,
      repositoriesSelected
    });

  }

  // Method for ExpandableSection
  private onExpandableToggle = isExpanded => {
    this.setState({
      expandableSectionIsExpanded: isExpanded
    });
  };

  private onModulesToggle = () => {
    const modulesIsExpanded = !this.state.modulesIsExpanded
    this.setState({
      modulesIsExpanded
    });
  };

  private onAssembliesToggle = () => {
    const assembliesIsExpanded = !this.state.assembliesIsExpanded
    this.setState({
      assembliesIsExpanded
    });
  };

  private onRepositoriesToggle = () => {
    const repoFilterIsExpanded = !this.state.repoFilterIsExpanded
    this.setState({
      repoFilterIsExpanded
    });
  };

  // methods for bulk operation
  private getdocumentsSelected = (documentsSelected) => {
    this.setState({ documentsSelected })
  }

  private handleModalToggle = (event) => {
    this.setState({
        isModalOpen: !this.state.isModalOpen
    })

    // process path
    // const target = event.nativeEvent.target
    // if (target.id !== undefined && target.id.trim().length > 0) {
    //     this.getMetadata(event)
    // }
}

  private handleModalClose = () => {
    this.setState({
        isModalOpen: false
    })
  }

  private onChangeProduct = (productValue: string, event: React.FormEvent<HTMLSelectElement>) => {
    let productLabel = ""
    const target = event.nativeEvent.target
    if (target !== null) {
        // Necessary because target.selectedOptions produces a compiler error but is valid
        // tslint:disable-next-line: no-string-literal
        productLabel = target["selectedOptions"][0].label
    }
    this.setState({
        product: { label: productLabel, value: productValue },
        productVersion: { label: "", uuid: "" }
    })
    this.populateProductVersions(productValue)
}

private populateProductVersions(productValue) {
    fetch("/content/products/" + productValue + "/versions.harray.1.json")
        .then(response => response.json())
        .then(json => {
            this.setState({ allProductVersions: json.__children__ })
        })
}

private onChangeVersion = (value: string, event: React.FormEvent<HTMLSelectElement>) => {
    if (event.target !== null) {
        // Necessary because target.selectedOptions produces a compiler error but is valid
        // tslint:disable-next-line: no-string-literal
        const selectedOption = event.target["selectedOptions"][0]
        if (this.state.productVersion.uuid !== selectedOption.value) {
            this.setState({
                productVersion: { label: selectedOption.label, uuid: selectedOption.value }
            })
        }
    }
}

private onChangeUsecase = (usecaseValue, event) => {
    this.setState({ usecaseValue })
}

private handleURLInput = urlFragment => {
    this.setState({ urlFragment })
}

private handleKeywordsInput = keywords => {
    this.setState({ keywords })
}

// used for metadata Modal
private fetchProducts = () => {

    const path = "/content/products.harray.1.json"
    const products = new Array()

    fetch(path)
        .then((response) => {
            if (response.ok) {
                return response.json()
            } else {
                throw new Error(response.statusText)
            }
        })
        .then(responseJSON => {
            for (const product of responseJSON.__children__) {
                products.push({ label: product.name, value: product.__name__ })
            }
            this.setState({
                allProducts: products
            })
        })
        .catch((error) => {
            console.log(error)
        })
    return products
}

  private dismissNotification = () => {
    this.setState({ isMissingFields: false })
  }
  
  private saveMetadata() {

  }
}


export { Search }; 