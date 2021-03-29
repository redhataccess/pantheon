import React, { Component } from "react";
import {
  Drawer, DrawerPanelContent, DrawerContent, DrawerContentBody, DrawerHead, DrawerActions, DrawerCloseButton,
  Button, ButtonVariant,
  InputGroup,
  Select, SelectOption,
  Toolbar, ToolbarItem, ToolbarContent, ToolbarFilter, ToolbarToggleGroup, ToolbarGroup,
  SelectVariant,
  ExpandableSection,
  Checkbox,
  Divider,
  SimpleListItem, SimpleList,
  SearchInput,
  ToolbarChipGroup, ToolbarChip, Alert, AlertActionLink,
} from "@patternfly/react-core";

import { SearchResults } from "@app/searchResults";
import { BuildInfo } from "./components/Chrome/Header/BuildInfo"

import "@app/app.css";
import SearchIcon from "@patternfly/react-icons/dist/js/icons/search-icon";
import FilterIcon from "@patternfly/react-icons/dist/js/icons/filter-icon";
import { IAppState } from "@app/app"
import { BulkOperationMetadata } from "./bulkOperationMetadata";
import { BulkOperationPublish } from "./BulkOperationPublish"
import { PathPrefixes } from "./Constants";


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

  // metadata
  productsSelected: string[]
  repositoriesSelected: string[]
  documentsSelected: Array<{ cells: [string, { title: { props: {children: string[], href: string } } }, string, string, string], selected: boolean }>
  contentTypeSelected: string
  isModalOpen: boolean
  isEditMetadata: boolean
  editMetadataWarn: boolean
  isBulkOperationButtonDisabled: boolean

  documentTitles: string[]
  isPublishModalOpen: boolean
  canChangePublishState: boolean
  showPublishMessage: boolean
  portalUrl: string

  // bulk publish
  isBulkPublish: boolean
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
      documentTitles: [],
      contentTypeSelected: "",
      isModalOpen: false,
      isEditMetadata: false,
      editMetadataWarn: false,
      isBulkOperationButtonDisabled: true,

      isPublishModalOpen: false,
      canChangePublishState: true,
      showPublishMessage: false,
      portalUrl: "",

      //bulk operation - publish
      isBulkPublish: false
    };
    this.drawerRef = React.createRef();

  }

  public componentDidMount() {

    // list repos inside the drawer
    this.getRepositories()
    // product and id used for Filter
    // this.getProducts()

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

  public componentDidUpdate(prevProps) {

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
              <SimpleList aria-label="Repository List" className='repo-list-container'>
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
          {toggleGroupItems}.
        </ToolbarToggleGroup>
        <ToolbarGroup variant="icon-button-group">
        </ToolbarGroup>
        {this.props.userAuthenticated && (this.props.isAuthor || this.props.isPublisher || this.props.isAdmin) && <ToolbarItem>
          <Button variant="primary" isAriaDisabled={this.state.isBulkOperationButtonDisabled || this.state.repositoriesSelected.length === 0} onClick={this.handleEditMetadata} data-testid="edit_metadata">Edit metadata</Button>
        </ToolbarItem>}
        {this.props.userAuthenticated && (this.props.isPublisher || this.props.isAdmin) && <ToolbarItem>
          <Button variant="primary" isAriaDisabled={this.state.documentsSelected.length > 0 ? false : true} onClick={this.handleBulkPublish}>Publish</Button>
        </ToolbarItem>}
        {this.props.userAuthenticated && (this.props.isPublisher || this.props.isAdmin) && <ToolbarItem>
          <Button variant="primary" isAriaDisabled={true}>Unpublish</Button>
        </ToolbarItem>}

      </React.Fragment>
    );

// TODO - move this publish stuff out like bulk meta data



    // const publishConfirmationHeader = (
    //   <React.Fragment>
    //     <Title headingLevel="h1" size={BaseSizes["2xl"]}>
    //       Published Message
    //     </Title>
    //   </React.Fragment>
    // )



// const publishConfirmationModal = (
//   <React.Fragment>
//     <Modal
//       variant={ModalVariant.medium}
//       title="Documents Successfully Published"
//       isOpen={this.state.showPublishMessage}
//       header={publishConfirmationHeader}
//       aria-label="Documents Successfully Published"
//       onClose={this.handlePublishConfirmationModalClose}
//     >
//       <div>
//     {this.state.documentTitles.length > 0 ? this.state.documentTitles.map(title => <p>{title}</p>) : <p>Documents failed to publish. Please ensure no metadata is missing from selected docs.</p>}
//     </div>
//     </Modal>
//   </React.Fragment>
// );

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
              {this.state.editMetadataWarn && <Alert variant="danger" isInline title="Attempt to apply the same product/version to multiple repositories is not allowed." />}
              {this.state.isEditMetadata && <BulkOperationMetadata
                documentsSelected={this.state.documentsSelected}
                contentTypeSelected={this.state.contentTypeSelected}
                isEditMetadata={this.state.isEditMetadata}
                updateIsEditMetadata={this.updateIsEditMetadata}
              />}
              {drawerContent}
              {this.state.isBulkPublish && <BulkOperationPublish 
                documentsSelected={this.state.documentsSelected}
                contentTypeSelected={this.state.contentTypeSelected}
                isBulkPublish={this.state.isBulkPublish}
                updateIsBulkPublish={this.updateIsBulkPublish}
              />}

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
    this.setState({ isExpanded });
  };

  private onCloseClick = () => {
    this.setState({ isExpanded: false });
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
    this.setState({ statusIsExpanded: isExpanded });
  };

  private onCtypeToggle = isExpanded => {
    this.setState({ ctypeIsExpanded: isExpanded });
  };

  // methods for filter search
  private onChangeRepoFilter = (value, event) => {
    this.setState({ repoFilterValue: value });

    // check for input value
    if (value) {
      // filter and return repositories that include input value, and set state to the filtered list
      let filtered = this.state.repositories.filter(data => data.name.toLowerCase().includes(value.toLowerCase()))
      this.setState({ filteredRepositories: filtered })
    } else {
      this.setState({
        filteredRepositories: this.state.repositories
      })
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
    }, () => {
      if (this.state.repositoriesSelected.length === 0) {
        this.setState({ documentsSelected: [], isBulkOperationButtonDisabled: true })
      }

      if (this.state.repositoriesSelected.length === 1 && this.state.editMetadataWarn === true) {
        this.setState({ editMetadataWarn: false })
      }

    });

    this.getdocumentsSelected(this.state.documentsSelected)

  }

  // Method for ExpandableSection
  private onExpandableToggle = isExpanded => {
    this.setState({ expandableSectionIsExpanded: isExpanded });
  };

  private onModulesToggle = () => {
    const modulesIsExpanded = !this.state.modulesIsExpanded
    this.setState({ modulesIsExpanded });
  };

  private onAssembliesToggle = () => {
    const assembliesIsExpanded = !this.state.assembliesIsExpanded
    this.setState({ assembliesIsExpanded });
  };

  private onRepositoriesToggle = () => {
    const repoFilterIsExpanded = !this.state.repoFilterIsExpanded
    this.setState({ repoFilterIsExpanded });
  };

  // methods for bulk operation
  private getdocumentsSelected = (documentsSelected) => {
    if (this.state.repositoriesSelected.length === 0) {
      this.setState({
        contentTypeSelected: '',
        documentsSelected: [],
        isBulkOperationButtonDisabled: true
      })
    } else {
      this.setState({ documentsSelected }, () => {
        if (this.state.documentsSelected.length > 0) {
          this.setState({ isBulkOperationButtonDisabled: false })
        } else {
          this.setState({ isBulkOperationButtonDisabled: true })
        }
      })
    }

  }

  private bulkEditSectionCheck = (contentTypeSelected) => {
    this.setState({ contentTypeSelected })
  }

  private handleEditMetadata = (event) => {
    if (this.state.repositoriesSelected.length > 1) {
      this.setState({ editMetadataWarn: true }, () => {
        this.setState({ isBulkOperationButtonDisabled: true })
      })
    } else {
      this.setState({
        isEditMetadata: !this.state.isEditMetadata,
        editMetadataWarn: false
      }, () => {
        if (this.state.editMetadataWarn === false && this.state.repositoriesSelected.length === 1) {
          this.setState({ isBulkOperationButtonDisabled: false })
        } else {
          this.setState({ isBulkOperationButtonDisabled: true })
        }
      })
    }

  }

  private handleBulkPublish = (event) => {
    this.setState({ isBulkPublish: !this.state.isBulkPublish })
  }
//TODO - delete - using handleModalToggle in builk operation publish file
  // private handlePublishModalToggle = () => {
  //   this.setState({
  //     isPublishModalOpen: !this.state.isPublishModalOpen
  //   })
  // }

  // private handlePublishModalClose = () => {
  //   this.setState({
  //     isPublishModalOpen: false,
  //   })
  // }

  // private handlePublishConfirmationModalClose = () => {
  //   this.setState({
  //     showPublishMessage: false,
  //     documentsSelected: [],
  //     documentTitles: []
  //   })
  // }
  

  private updateIsEditMetadata = (updateIsEditMetadata) => {
    this.setState({ isEditMetadata: updateIsEditMetadata })
  }

  private updateIsBulkPublish = updateIsBulkPublish => {
    this.setState({isBulkPublish: updateIsBulkPublish})
  }



//   private onBulkPublish = (event) => {
//     console.log('published clicked', event)
//       // Validate productValue before Publish
//       // if (this.props.productInfo !== undefined && this.props.productInfo.trim() === "") {
//       //     this.setState({ canChangePublishState: false})
//           //TODO add publish alert
//       // } else {

//           // if (this.state.canChangePublishState === true) {
//               const formData = new FormData();

//                   formData.append(":operation", "pant:publish");
//                   // console.log("Published file path:", this.props.modulePath)
//                   // this.draft[0].version = "";
//                   // this.onPublishEvent()

//               const hdrs = {
//                   "Accept": "application/json",
//                   "cache-control": "no-cache",
//                   "Access-Control-Allow-Origin": "*",
//               }
//               this.state.documentsSelected.map((r) => {
//                 console.log("[saveMetadata] documentsSelected href =>", r.cells[1].title.props.href)
//                 if (r.cells[1].title.props.href) {
//                   let href = r.cells[1].title.props.href
//                   let documentTitle = r.cells[1].title.props.children[1]

//                   // let link = href.split("pantheon/#", "?variant=")
                  
//                   let variant = href.split("?variant=")[1]
                  


//                   //href part is module path
//                   let hrefPart = href.slice(0, href.indexOf("?"))
//                   let docPath = hrefPart.match("/repositories/.*") ? hrefPart.match("/repositories/.*") : ""
//                   let path = hrefPart.slice(hrefPart.indexOf("/module"))
//                   let modulePath = hrefPart.slice(hrefPart.indexOf("/repositories"))

//                   console.log('path', path)
//                   console.log('variant', variant)
//                   console.log('hrefPart', hrefPart)
//                   console.log('docPath', docPath)
//                   console.log('modulePath', modulePath)
//                   // this.onPublishEvent(variant, path)
//               formData.append("locale", "en_US")
//               formData.append("variant", variant)
//               fetch("/content" + modulePath, {
//                 body: formData,
//                 method: "post",
//                 headers: hdrs
//             }).then(response => {
//                 if (response.status === 201 || response.status === 200) {
//                     console.log("publish works: " + response.status)
//                     this.setState({
//                         canChangePublishState: true,
//                         documentTitles: [...this.state.documentTitles, documentTitle],
//                         isPublishModalOpen: false,
//                         showPublishMessage: true,
//                         documentsSelected: []
//                     })
//                 } else {
//                     console.log("publish failed " + response.status)
//                     this.setState({
//                       isPublishModalOpen: false,
//                       showPublishMessage: true,
//                     })

//                     // this.setAlertTitle()
//                 // }    this.fetchVersions()
//                 // return response.json()
//             // }).then(response => this.props.onGetUrl(response.path));
//                   }
//           })
//         }
//               })
// }

// private getPortalUrl = (path, variant) => {
//   const variantPath = "/content" + path + "/en_US/variants/" + variant + ".url.txt"
//   console.log('getPortalUrl variantPath', variantPath)
//   fetch(variantPath)
//       .then(resp => {
//           if (resp.ok) {
//               resp.text().then(text => {
//                   // get portal url from api and set it only if it is not empty
//                   if(text.trim() !== "") {
//                       this.setState({portalUrl: text})
//                   }else{
//                       // if portal url is empty, assemble the URL at client side
//                       console.log("GetPortalURI API returned empty URI. Falling back to url construction at UI")
//                       this.getVersionUUID(path)
//                   }
//               })
//           }else {
//               console.log("GetPortalURI API returned error. Falling back to url construction at UI")
//               this.getVersionUUID(path)
//           }
//       })
// }

// private getVersionUUID = (path) => {
//   // remove /module from path
//   path =  path.substring(this.state.contentTypeSelected == "assembly" ? PathPrefixes.ASSEBMLY_PATH_PREFIX.length : PathPrefixes.MODULE_PATH_PREFIX.length)
//   // path = "/content" + path + "/en_US/1/metadata.json"
//   path = "/content" + path + "/en_US.harray.4.json"
//   console.log('VERSION URL', path)
//   fetch(path)
//       .then(response => response.json())
//       .then((responseJSON) => {
//           for (const locale of responseJSON.__children__) {
//               if (!locale.__children__) {
//                   continue
//               }
//               for (const localeChild of locale.__children__) {

//                   if (!localeChild.__children__) {
//                       continue
//                   }
//                   for (const variant of localeChild.__children__) {
//                       if (!variant.__children__) {
//                           continue
//                       }

//                       // for (const offspring of variant.__children__) {
//                       //     if (offspring.__name__ === "metadata") {

//                       //         if (offspring[Fields.PANT_PRODUCT_VERSION_REF] !== undefined) {

//                       //             this.getProductInitialLoad(offspring[Fields.PANT_PRODUCT_VERSION_REF])
//                       //         }
//                       //     }
//                       // }

//                   }
//               }
//           }
//       })
// }

// // private getHarrayChildNamed = (object, name) => {
// //   for (const childName in object.__children__) {
// //       if (object.__children__.hasOwnProperty(childName)) { // Not sure what this does, but makes tslin happy
// //           const child = object.__children__[childName]
// //           if (child.__name__ === name) {
// //               return child
// //           }
// //       }
// //   }
// //   return ""
// // }

// // private fetchVersions = (modulePath, variant) => {
// //   // TODO: need a better fix for the 404 error.
// //   if (modulePath !== "") {
// //       // fetchpath needs to start from modulePath instead of modulePath/en_US.
// //       // We need extact the module uuid for customer portal url to the module.
// //       const fetchpath = "/content" + modulePath + ".harray.5.json"
// //       fetch(fetchpath)
// //           .then(response => response.json())
// //           .then(responseJSON => {
// //               const en_US = this.getHarrayChildNamed(responseJSON, "en_US")
// //               const source = this.getHarrayChildNamed(en_US, "source")
// //               const variants = this.getHarrayChildNamed(en_US, "variants")

// //               const firstVariant = this.getHarrayChildNamed(variants, variant)
// //               // process draftUpdateDate from source/draft
// //               let draftDate = ""
// //               if (source !== "undefined" && source.__name__ === "source") {
// //                   for (const childNode of source.__children__) {
// //                       if (childNode.__name__ === "draft") {
// //                           draftDate = childNode["jcr:created"]
// //                       } else if (childNode.__name__ === "released") {
// //                           draftDate = childNode["jcr:created"]
// //                       }
// //                   }
// //               }
// //               // process variantUUID
// //               let variantUuid = ""
// //               if (firstVariant["jcr:primaryType"] !== "undefined" && (firstVariant["jcr:primaryType"] === "pant:moduleVariant" || firstVariant["jcr:primaryType"] === "pant:assemblyVariant")) {
// //                   variantUuid = firstVariant["jcr:uuid"]
// //               }
// //               const versionCount = firstVariant.__children__.length
// //               for (let i = 0; i < versionCount; i++) {
// //                   const moduleVersion = firstVariant.__children__[i]
// //                   let variantReleased = false

// //                   if (moduleVersion.__name__ === "draft") {
// //                       this.draft[0].version = "Version " + moduleVersion.__name__
// //                       this.draft[0].metadata = this.getHarrayChildNamed(moduleVersion, "metadata")
// //                       // get created date from source/draft
// //                       this.draft[0].updatedDate = draftDate !== undefined ? draftDate : ""
// //                       // this.props.modulePath starts with a slash
// //                       this.draft[0].path = "/content" + modulePath + "/en_US/variants/" + firstVariant.__name__ + "/" + moduleVersion.__name__
// //                   }
// //                   if (moduleVersion.__name__ === "released") {
// //                       this.release[0].version = "Version " + moduleVersion.__name__
// //                       this.release[0].metadata = this.getHarrayChildNamed(moduleVersion, "metadata")
// //                       this.release[0].updatedDate = this.release[0].metadata["pant:datePublished"] !== undefined ? this.release[0].metadata["pant:datePublished"] : ""
// //                       // get created date from source/draft
// //                       this.release[0].draftUploadDate = draftDate !== undefined ? draftDate : ""
// //                       // modulePath starts with a slash
// //                       this.release[0].path = "/content" + modulePath + "/en_US/variants/" + firstVariant.__name__ + "/" + moduleVersion.__name__
// //                       variantReleased = true
// //                   }
// //                   if (!variantReleased) {
// //                       this.release[0].updatedDate = "-"
// //                   }
// //                   this.props.updateDate((draftDate !== "" ? draftDate : ""), this.release[0].updatedDate, this.release[0].version, variantUuid)
// //               }
// //               this.setState({
// //                   results: [this.draft, this.release],
// //                   variantPath: "/content" + modulePath + "/en_US/variants/" + variant
// //               })

// //               // Check metadata for draft. Show warning icon if metadata missing for draft
// //               if (this.draft && this.draft[0].path.length > 0) {
// //                   if (this.draft[0].metadata !== undefined &&
// //                       this.draft[0].metadata.productVersion === undefined) {
// //                       this.setState({ showMetadataAlertIcon: true })
// //                   } else {
// //                       this.setState({ showMetadataAlertIcon: false })
// //                   }
// //               }

// //               // Get documents included in assembly
// //               if (this.state.contentTypeSelected === "assembly") {
// //                   this.getDocumentsIncluded(variantUuid)
// //               }
// //           })
// //   }
// // }

// // private getProductInitialLoad = (uuid) => {
// //   const path = "/content/products.harray.3.json"
// //   fetch(path)
// //       .then(response => response.json())
// //       .then(responseJSON => {
// //           for (const product of responseJSON.__children__) {
// //               if (!product.__children__) {
// //                   continue
// //               }
// //               for (const productChild of product.__children__) {
// //                   if (productChild.__name__ !== "versions") {
// //                       continue
// //                   }
// //                   if (productChild.__children__) {
// //                       for (const productVersion of productChild.__children__) {
// //                           if (productVersion[Fields.JCR_UUID] === uuid) {
// //                               this.setState({ productValue: product.name, versionValue: productVersion.name, productUrlFragment: product.urlFragment, versionUrlFragment: productVersion.urlFragment })
// //                               const isGuideOrTopic = this.isAssembly ? '/guide/' : '/topic/'
// //                               const url = this.state.portalHostUrl + '/documentation/'+this.state.locale.toLocaleLowerCase()+'/' + this.state.productUrlFragment + '/' + this.state.versionUrlFragment + isGuideOrTopic + this.state.variantUUID
// //                               console.log("Constructed url="+url)
// //                               if(this.state.productUrlFragment!==""){
// //                                   this.setState({ portalUrl: url})
// //                               }
// //                               break
// //                           }
// //                       }
// //                   }
// //               }
// //           }
// //       })

// // }

}

export { Search }; 