import React, { Component } from "react";
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

} from "@patternfly/react-core";

import { SearchResults } from "@app/searchResults";

import "@app/app.css";
import SearchIcon from "@patternfly/react-icons/dist/js/icons/search-icon";
import FilterIcon from "@patternfly/react-icons/dist/js/icons/filter-icon";
import { IAppState } from "@app/app"

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
}
class SearchBeta extends Component<IAppState, ISearchState> {
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
    };
    this.drawerRef = React.createRef();

  }

  public componentDidMount() {
    // list repos inside the drawer
    this.getRepositories()
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
  public render() {
    const { filterLabel, isExpanded, assembliesIsExpanded, modulesIsExpanded, productFilterIsExpanded, repoFilterIsExpanded, expandableSectionIsExpanded, repositories, inputValue, filters, statusIsExpanded, ctypeIsExpanded } = this.state;

    const panelContent = (
      <DrawerPanelContent widths={{ lg: "width_25" }}>
        <DrawerHead>
          <span className="pf-c-title pf-m-2xl" tabIndex={isExpanded ? 0 : -1} ref={this.drawerRef}>Filters</span>
          <DrawerActions>
            <DrawerCloseButton onClick={this.onCloseClick} />
          </DrawerActions>
          <ExpandableSection toggleText="By repo" isActive={true} isExpanded={repoFilterIsExpanded} onToggle={this.onRepositoriesToggle}>
            <SearchInput
              placeholder="Filter"
              value={this.state.repoFilterValue}
              onChange={this.onChangeRepoFilter}
              onClear={(evt) => this.onChangeRepoFilter("", evt)}
            />
            <SimpleList aria-label="Repository List">
              {this.state.repositories.map((data) => (
                <SimpleListItem key={data.id}>
                  <Checkbox label={data.name} aria-label="uncontrolled checkbox" id={data.id} onClick={this.onSelectRepositories} />
                </SimpleListItem>
              ))}
            </SimpleList>

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
        <ExpandableSection toggleText="Modules" className="pf-c-title" isActive={true} isExpanded={modulesIsExpanded} onToggle={this.onModulesToggle}>
          <SearchResults
            contentType="module"
            keyWord={this.state.inputValue}
            repositoriesSelected={this.state.repositoriesSelected}
            productsSelected={this.state.productsSelected}
            userAuthenticated={this.props.userAuthenticated}
            filters={this.state.filters}
          />

        </ExpandableSection>
        <br />
        <ExpandableSection toggleText="Assemblies" className="pf-c-title" isActive={true} isExpanded={assembliesIsExpanded} onToggle={this.onAssembliesToggle}>
          <SearchResults
            contentType="assembly"
            keyWord={this.state.inputValue}
            repositoriesSelected={this.state.repositoriesSelected}
            productsSelected={this.state.productsSelected}
            userAuthenticated={this.props.userAuthenticated}
            filters={this.state.filters}
          />

        </ExpandableSection>
      </React.Fragment>
    );

    const statusMenuItems = [
      <SelectOption key="statusDraft" value="draft" label= "Draft" />,
      <SelectOption key="statusPublished" value="released" label="Published" />
    ];

    const contentTypeMenuItems = [
      <SelectOption key="ctypeConcept" value="CONCEPT" label="Concept" />,
      <SelectOption key="ctypeProcedure" value="PROCEDURE" label="Procedure" />,
      <SelectOption key="ctypeReference" value="REFERENCE" label="Reference" />
    ];

    const toggleGroupItems = (
      <React.Fragment>
        <ToolbarItem>
          <Button variant="tertiary" aria-expanded={isExpanded} onClick={this.onClick} icon={<FilterIcon />} />
        </ToolbarItem>
        <ToolbarItem>
          <InputGroup>
            <SearchInput
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
            // deleteChip={this.onDelete}
            deleteChipGroup={this.onDeleteGroup}
            categoryName="Status"
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
          {/* <ToolbarFilter chips={filters.ctype} deleteChip={this.onDelete} categoryName="Content Type"> */}
          <ToolbarFilter chips={filters.ctype} categoryName="Content Type" >
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

    // const dropdownItems = [
    //   <DropdownItem key="link">Link</DropdownItem>,
    //   <DropdownItem key="action" component="button">
    //     Action
    //       </DropdownItem>,
    //   <DropdownItem key="disabled link" isDisabled={true}>
    //     Disabled Link
    //       </DropdownItem>,
    //   <DropdownItem key="disabled action" isDisabled={true} component="button">
    //     Disabled Action
    //       </DropdownItem>,
    //   <DropdownSeparator key="separator" />,
    //   <DropdownItem key="separated link">Separated Link</DropdownItem>,
    //   <DropdownItem key="separated action" component="button">
    //     Separated Action
    //       </DropdownItem>
    // ];

    const toolbarItems = (
      <React.Fragment>
        <ToolbarToggleGroup toggleIcon={<FilterIcon />} breakpoint="xl">
          {toggleGroupItems}
        </ToolbarToggleGroup>
        <ToolbarGroup variant="icon-button-group">
        </ToolbarGroup>
        {/* <ToolbarItem>
          <Dropdown
            toggle={<KebabToggle onToggle={this.onKebabToggle} />}
            isOpen={kebabIsOpen}
            isPlain={true}
            dropdownItems={dropdownItems}
          />
        </ToolbarItem> */}
      </React.Fragment>
    );

    return (
      <React.Fragment>
        <Alert variant="info" title="Beta feature" >
        <p>
        Please give us your feedback {"  "}
            <a href="https://projects.engineering.redhat.com/browse/CCS-3969" target="_blank">here.</a>
          </p>
        </Alert>
        <br />
        <Toolbar
          id="toolbar-with-filter"
          className="pf-m-toggle-group-container"
          collapseListedFiltersBreakpoint="xl"
          clearAllFilters={this.onDelete}
        >
          <ToolbarContent>{toolbarItems}</ToolbarContent>
        </Toolbar>
        <Divider />
        <Drawer isExpanded={isExpanded} isInline={true} position="left" onExpand={this.onExpand}>
          <DrawerContent panelContent={panelContent} width="width_50">
            <DrawerContentBody width="width_50">
              {drawerContent}
            </DrawerContentBody>
          </DrawerContent>
        </Drawer>

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
          repositories: repos
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

  private onDelete = (type = "", id = "") => {
    if (type) {
      this.setState(prevState => {
        const newState = Object.assign(prevState);
        newState.filters[type.toLowerCase()] = newState.filters[type.toLowerCase()].filter(s => s !== id);
        return {
          filters: newState.filters
        };
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
    this.setState(prevState => {
      prevState.filters[type.toLowerCase()] = [];
      return {
        filters: prevState.filters
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

    if (value) {
      let inputString = "";
      const matchFound = [{ name: "", id: "", checked: false }];

      this.state.repositories.map(data => {
        inputString = "" + data.name
        if (inputString.toLowerCase().includes(value.toLowerCase())) {
          matchFound.push(data)
        }
      });
      this.setState({ repositories: matchFound })
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

  private onSelectRepositories = (event) => {
    const checked = event.target.checked;
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
}


export { SearchBeta }; 