import React, { Component } from "react";
import {
  Drawer, DrawerPanelContent, DrawerContent, DrawerContentBody, DrawerHead, DrawerActions, DrawerCloseButton,
  Button, ButtonVariant,
  InputGroup,
  Select, SelectOption,
  Toolbar, ToolbarItem, ToolbarContent, ToolbarFilter, ToolbarToggleGroup, ToolbarGroup,
  TextInput,
  SelectVariant,
  ExpandableSection,
  Checkbox,
  Divider,
  SimpleListItem, SimpleList,
  SearchInput,
  Alert,
  ToolbarChipGroup, ToolbarChip,
  Modal, ModalVariant,
  Form, FormGroup, FormSelect, FormSelectOption, FormAlert,
  Title,
  BaseSizes,
} from "@patternfly/react-core";

import { SearchResults } from "@app/searchResults";
import { BuildInfo } from "./components/Chrome/Header/BuildInfo"
import { BulkOperationConfirmation } from "./bulkOperationConfirmation";

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

  // metadatat
  productsSelected: string[]
  repositoriesSelected: string[]
  documentsSelected: Array<{ cells: [{}, { title: { props: { href: string } } }, {}, {}, {}], selected: boolean }>
  contentTypeSelected: string
  isModalOpen: boolean
  alertTitle: string
  allProducts: any
  allProductVersions: any
  isMissingFields: boolean
  productValidated: any
  productVersionValidated: any
  useCaseValidated: any
  product: { label: string, value: string }
  productVersion: { label: string, uuid: string }
  showBulkEditConfirmation: boolean
  keywords: string
  usecaseOptions: any
  usecaseValue: string
  metadataEditError: string

  progressFailureValue: number
  progressSuccessValue: number
  progressWarningValue: number
  bulkUpdateFailure: number
  bulkUpdateSuccess: number
  bulkUpdateWarning: number

  documentsSucceeded: string[]
  documentsFailed: string[]
  documentsIgnored: string[]
  confirmationBody: string
  confirmationSucceeded: string
  confirmationIgnored: string
  confirmationFailed: string
}
class Search extends Component<IAppState, ISearchState> {
  private drawerRef: React.RefObject<HTMLInputElement>;
  private SearchResults;

  constructor(props) {
    super(props);
    this.SearchResults = React.createRef();
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
      contentTypeSelected: "",
      isModalOpen: false,
      alertTitle: "",
      allProducts: [],
      allProductVersions: [],
      isMissingFields: false,
      productValidated: "error",
      productVersionValidated: "error",
      useCaseValidated: "error",
      product: { label: "", value: "" },
      productVersion: { label: "", uuid: "" },
      showBulkEditConfirmation: false,
      keywords: "",
      usecaseOptions: [
        { value: "", label: "Select Use Case", disabled: false }
      ],
      usecaseValue: "",
      metadataEditError: "",

      //progress bar
      progressFailureValue: 0,
      progressSuccessValue: 0,
      progressWarningValue: 0,
      bulkUpdateFailure: 0,
      bulkUpdateSuccess: 0,
      bulkUpdateWarning: 0,

      documentsSucceeded: [""],
      documentsFailed: [""],
      documentsIgnored: [""],
      confirmationBody: "",
      confirmationSucceeded: "",
      confirmationIgnored: "",
      confirmationFailed: "",
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
    // console.log('content type selected', this.state.contentTypeSelected)
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
                    <Checkbox label={data.name} aria-label="uncontrolled checkbox" id={data.id} onChange={this.onSelectRepositories} isChecked={data.checked} />
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
            ref={this.SearchResults}
            contentType="module"
            keyWord={this.state.inputValue}
            repositoriesSelected={this.state.repositoriesSelected}
            productsSelected={this.state.productsSelected}
            userAuthenticated={this.props.userAuthenticated}
            filters={this.state.filters}
            onGetdocumentsSelected={this.getdocumentsSelected}
            onSelectContentType={this.bulkEditSectionCheck}
            currentBulkOperation={this.state.contentTypeSelected}
            disabledClassname={this.state.contentTypeSelected == 'assembly' ? 'disabled-search-results' : ''}
          />

        </ExpandableSection>
        <br />
        <ExpandableSection toggleText="Assemblies" className="pf-c-title search-results__section search-results__section--assembly" isActive={true} isExpanded={assembliesIsExpanded} onToggle={this.onAssembliesToggle}>
          <SearchResults
            ref={this.SearchResults}
            contentType="assembly"
            keyWord={this.state.inputValue}
            repositoriesSelected={this.state.repositoriesSelected}
            productsSelected={this.state.productsSelected}
            userAuthenticated={this.props.userAuthenticated}
            filters={this.state.filters}
            onGetdocumentsSelected={this.getdocumentsSelected}
            onSelectContentType={this.bulkEditSectionCheck}
            currentBulkOperation={this.state.contentTypeSelected}
            disabledClassname={this.state.contentTypeSelected == 'module' ? 'disabled-search-results' : ''}
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

    // TODO: move Edit metadata modal to its own component
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
            <Button form="bulk_edit_metadata" key="confirm" variant="primary" onClick={this.saveMetadata}>
              Save
          </Button>,
            <Button key="cancel" variant="secondary" onClick={this.handleModalToggle}>
              Cancel
            </Button>
          ]}
        >
          <div id="edit_metadata_helper_text"><p>Editing {this.state.documentsSelected.length} items. Changes made apply to all selected docs.</p></div>
          <br />
          <Form isWidthLimited={true} id="bulk_edit_metadata">
            {this.state.isMissingFields
              && (
                <FormAlert>
                  <Alert
                    variant="danger"
                    title="You must fill out all required fields before you can proceed."
                    aria-live="polite"
                    isInline={true}
                  />
                  <br />
                </FormAlert>
              )}

            {this.state.metadataEditError
              && (
                <FormAlert>
                  <Alert
                    variant="danger"
                    title={this.state.metadataEditError}
                    aria-live="polite"
                    isInline={true}
                  />
                  <br />
                </FormAlert>
              )}
            <FormGroup
              label="Product Name"
              isRequired={true}
              fieldId="product-name"
              validated={this.state.productValidated}
            >
              <InputGroup>
                <FormSelect value={this.state.product.value} onChange={this.onChangeProduct} aria-label="FormSelect Product" name="product" isRequired={true} validated={this.state.productValidated}>
                  <FormSelectOption label="Select a Product" />
                  {this.state.allProducts.map((option, key) => (
                    <FormSelectOption key={key} value={option.value} label={option.label} />
                  ))}
                </FormSelect>
                <FormSelect value={this.state.productVersion.uuid} onChange={this.onChangeVersion} aria-label="FormSelect Version" id="productVersion" name="productVersion" isRequired={true} validated={this.state.productVersionValidated}>
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
              validated={this.state.useCaseValidated}
            >
              <FormSelect value={this.state.usecaseValue} onChange={this.onChangeUsecase} aria-label="FormSelect Usecase" name="useCase" isRequired={true} validated={this.state.useCaseValidated}>
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
            </FormGroup>
            <FormGroup
              label="Search keywords"
              isRequired={false}
              fieldId="search-keywords"
            >
              <InputGroup>
                <TextInput isRequired={false} id="search-keywords" type="text" placeholder="cat, dog, bird..." value={this.state.keywords} onChange={this.handleKeywordsInput} name="keywords" />
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
              {this.state.showBulkEditConfirmation &&
                <BulkOperationConfirmation
                  header="Bulk Edit"
                  subheading="Documents updated in the bulk operation"
                  updateSucceeded={this.state.confirmationSucceeded}
                  updateIgnored={this.state.confirmationIgnored}
                  updateFailed={this.state.confirmationFailed}
                  footer=""
                  progressSuccessValue={this.state.progressSuccessValue}
                  progressFailureValue={this.state.progressFailureValue}
                  progressWarningValue={this.state.progressWarningValue}
                  onShowBulkEditConfirmation={this.updateShowBulkEditConfirmation}
                  onMetadataEditError={this.updateMetadataEditError}
                />}
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
        return {
          filters: {
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

  private bulkEditSectionCheck = (contentTypeSelected) => {
    this.setState({ contentTypeSelected })
  }

  private handleModalToggle = (event) => {
    this.setState({
      isModalOpen: !this.state.isModalOpen
    })

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
    } else {
      this.setState({ productValidated: "error" })
    }
    this.setState({
      product: { label: productLabel, value: productValue },
      productVersion: { label: "", uuid: "" }
    })

    if (productValue.length > 0) {
      this.setState({ productValidated: "success" })
    } else {
      this.setState({ productValidated: "error" })
    }
    this.populateProductVersions(productValue)
  }

  private populateProductVersions = (productValue) => {
    if (productValue.length > 0) {
      fetch("/content/products/" + productValue + "/versions.harray.1.json")
        .then(response => response.json())
        .then(json => {
          this.setState({ allProductVersions: json.__children__ })
        })
    }
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
      if (selectedOption.value !== "") {
        this.setState({ productVersionValidated: "success" })
      } else {
        this.setState({ productVersionValidated: "error" })
      }
    }
  }

  private onChangeUsecase = (usecaseValue, event) => {
    if (event != undefined) {
      this.setState({ usecaseValue: event.target.value })
      console.log("[onChangeUsecase] event.target.value=>", event.target.value)
      if (event.target.value !== "" && event.target.value.trim() !== "Select Use Case") {
        this.setState({ useCaseValidated: "success" })
      } else {
        this.setState({ useCaseValidated: "error" })
      }
    } else {
      this.setState({ useCaseValidated: "error" })
    }
  }

  private handleKeywordsInput = (keywords, event) => {
    if (event != undefined) {
      this.setState({ keywords: event.target.value })
    }
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
          if (product.name !== undefined) {
            products.push({ label: product.name, value: product.__name__ })
          }
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

  private saveMetadata = (event) => {
    if (this.state.productValidated === "error"
      || this.state.productVersionValidated === "error"
      || this.state.useCaseValidated === "error") {
      this.setState({ isMissingFields: true })
    } else {
      const hdrs = {
        "Accept": "application/json",
        "cache-control": "no-cache"
      }

      const metadataForm = event.target.form
      const formData = new FormData(metadataForm)

      formData.append("documentUsecase", this.state.usecaseValue)
      formData.append("searchKeywords", this.state.keywords === undefined ? "" : this.state.keywords)

      this.state.documentsSelected.map((r) => {
        // console.log("[saveMetadata] documentsSelected href =>", r.cells[1].title.props.href)
        if (r.cells[1].title.props.href) {
          let href = r.cells[1].title.props.href

          let variant = href.split("?variant=")[1]
          let hrefPart = href.slice(0, href.indexOf("?"))
          let docPath = hrefPart.match("/repositories/.*") ? hrefPart.match("/repositories/.*") : ""

          // check draft version
          const backend = "/content" + docPath + "/en_US/variants/" + variant + "/draft/metadata"

          this.draftExist(backend).then((exist) => {
            if (exist) {
              // Process form for each docPath
              fetch(backend, {
                body: formData,
                headers: hdrs,
                method: "post"
              }).then(response => {
                if (response.status === 201 || response.status === 200) {
                  console.log("successful edit ", response.status + " for  path: " + backend)
                  this.handleModalClose()
                  let docs = new Array()
                  docs = this.state.documentsSucceeded
                  docs.push(docPath)
                  this.setState({
                    documentsSucceeded: docs,
                    usecaseValue: "",
                    product: { label: "", value: "" },
                    productVersion: { label: "", uuid: "" },
                    keywords: "",
                    productValidated: "error",
                    productVersionValidated: "error",
                    useCaseValidated: "error",
                    bulkUpdateSuccess: this.state.bulkUpdateSuccess + 1,

                  }, () => {
                    this.calculateSuccessProgress(this.state.bulkUpdateSuccess)
                  })
                } else {
                  console.log(" User authenticated? " + response.status + " for  path: " + backend)
                  let docs = new Array()
                  docs = this.state.documentsFailed
                  docs.push(docPath)
                  // update state for progressbar
                  this.setState({ bulkUpdateFailure: this.state.bulkUpdateFailure + 1, documentsFailed: docs }, () => {
                    this.calculateFailureProgress(this.state.bulkUpdateFailure)
                  })

                }
              })
            } else {
              // draft does not exist
              console.log("[saveMetadata] no draft version found:", backend)
              let docs = new Array()
              docs = this.state.documentsIgnored
              docs.push(docPath)
              this.setState({ bulkUpdateWarning: this.state.bulkUpdateWarning + 1, documentsIgnored: docs }, () => {
                this.calculateWarningProgress(this.state.bulkUpdateWarning)
                if (this.state.bulkUpdateWarning > 0 && this.state.bulkUpdateWarning === this.state.documentsSelected.length) {
                  this.setState({ metadataEditError: "No draft versions found on selected items. Unable to save metadata." })
                }
              })
            }
          })
        }
      })
    }
  }

  private draftExist(path) {
    let exists = false
    return fetch(path + ".json")
      .then(response => {
        if (response.ok) {
          exists = true
        }
        return exists
      })
      .catch((error) => {
        console.log("[draftExist] error detected=>", error + " for " + path)
        return false
      })
  }

  private updateShowBulkEditConfirmation = (showBulkEditConfirmation) => {
    this.setState({ showBulkEditConfirmation })
  }

  private updateMetadataEditError = (metadataEditError) => {
    this.setState({ metadataEditError })
  }

  private calculateFailureProgress = (num: number) => {
    if (num >= 0) {
      let stat = (num) / this.state.documentsSelected.length * 100
      this.setState({ progressFailureValue: stat, showBulkEditConfirmation: true }, () => {
        this.getDocumentFailed()
      })
    }
  }

  private calculateSuccessProgress = (num: number) => {
    if (num >= 0) {
      let stat = (num) / this.state.documentsSelected.length * 100
      this.setState({ progressSuccessValue: stat, showBulkEditConfirmation: true }, () => {
        this.getDocumentsSucceeded()
      })
    }
  }

  private calculateWarningProgress = (num: number) => {
    if (num >= 0) {
      let stat = (num) / this.state.documentsSelected.length * 100
      this.setState({ progressWarningValue: stat, showBulkEditConfirmation: true }, () => {
        this.getDocumentIgnored()
      })
    }
  }
  private getDocumentsSucceeded = () => {
    if (this.state.documentsSucceeded.length > 0) {
      let succeeded = this.state.documentsSucceeded.join(",")
      console.log("[getdocomentsSucceeded] content=>", succeeded)
      this.setState({ confirmationSucceeded: succeeded })
    }
  }

  private getDocumentIgnored = () => {
    if (this.state.documentsIgnored.length > 0) {
      let ignored = this.state.documentsIgnored.join(",")
      this.setState({ confirmationIgnored: ignored })
    }
  }

  private getDocumentFailed = () => {
    if (this.state.documentsFailed.length > 0) {
      let failed = this.state.documentsFailed.join(",")
      this.setState({ confirmationFailed: failed })
    }
  }
}


export { Search }; 