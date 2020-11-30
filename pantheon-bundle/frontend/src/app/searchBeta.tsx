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

} from "@patternfly/react-core";

import { SearchResults } from "@app/searchResults";

import "@app/app.css";
import SearchIcon from "@patternfly/react-icons/dist/js/icons/search-icon";
// import TextInput from "@patternfly/react-icons/dist/js/icons/text-input";
import FilterIcon from "@patternfly/react-icons/dist/js/icons/filter-icon";
import { Pagination } from "@app/Pagination"
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
  riskIsExpanded: boolean,
  filters: {
    risk: any,
    status: any
  },
  kebabIsOpen: boolean,

  productFilterValue: string
  repoFilterValue: string

  keyword: string
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
      riskIsExpanded: false,
      filters: {
        risk: ["Low"],
        status: ["New", "Pending"]
      },
      kebabIsOpen: false,

      // filters
      productFilterValue: "",
      repoFilterValue: "",

      // search
      keyword: "",
      productsSelected: [],
      repositoriesSelected: [],
    };
    this.drawerRef = React.createRef();

  }

  public componentDidMount() {
    // list repos inside the drawer
    this.getRepositories()
    this.getProducts()

    // TODO: enable resize
    // toolbar
    // window.addEventListener("resize", this.closeExpandableContent);
  }

  public componentWillMount() {
    // list repos inside the drawer
    this.getRepositories()
    this.getProducts()
  }

  public componentWillUnmount() {
    // TODO: enable resize
    // toolbar
    // window.removeEventListener("resize", this.closeExpandableContent);
  }
  public render() {
    const { filterLabel, isExpanded, assembliesIsExpanded, modulesIsExpanded, productFilterIsExpanded, repoFilterIsExpanded, expandableSectionIsExpanded, repositories, inputValue, filters, statusIsExpanded, riskIsExpanded, kebabIsOpen } = this.state;

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
          <ExpandableSection toggleText="By product">
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

          </ExpandableSection>
        </DrawerHead>
      </DrawerPanelContent>
    );
    const drawerContent = (
      <React.Fragment>
        {/* <ExpandableSection toggleText="Modules" className="pf-c-title pf-m-2xl" isActive={true}> */}
        <ExpandableSection toggleText="Modules" className="pf-c-title" isActive={true} isExpanded={modulesIsExpanded} onToggle={this.onModulesToggle}>
          <SearchResults
            contentType="module"
            keyWord={this.state.keyword}
            repositoriesSelected={this.state.repositoriesSelected}
            productsSelected={this.state.productsSelected}
            userAuthenticated={this.props.userAuthenticated}
          />

        </ExpandableSection>
        <br />
        <ExpandableSection toggleText="Assemblies" className="pf-c-title" isActive={true} isExpanded={assembliesIsExpanded} onToggle={this.onAssembliesToggle}>
          <SearchResults
            contentType="assembly"
            keyWord={this.state.keyword}
            repositoriesSelected={this.state.repositoriesSelected}
            productsSelected={this.state.productsSelected}
            userAuthenticated={this.props.userAuthenticated}
          />

        </ExpandableSection>
      </React.Fragment>
    );

    const statusMenuItems = [
      <SelectOption key="statusNew" value="New" />,
      <SelectOption key="statusPending" value="Pending" />,
      <SelectOption key="statusRunning" value="Running" />,
      <SelectOption key="statusCancelled" value="Cancelled" />
    ];

    const riskMenuItems = [
      <SelectOption key="riskLow" value="Low" />,
      <SelectOption key="riskMedium" value="Medium" />,
      <SelectOption key="riskHigh" value="High" />
    ];

    const toggleGroupItems = (
      <React.Fragment>
        <ToolbarItem>
          <Button variant="tertiary" aria-expanded={isExpanded} onClick={this.onClick} icon={<FilterIcon />} />
        </ToolbarItem>
        <ToolbarItem>
          <InputGroup>
            <TextInput
              name="textInput2"
              id="textInput2"
              type="search"
              aria-label="search input"
              onChange={this.onInputChange}
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
          {/* <ToolbarFilter chips={filters.risk} deleteChip={this.onDelete} categoryName="Risk"> */}
          <ToolbarFilter chips={filters.risk} categoryName="Risk" >
            <Select
              variant={SelectVariant.checkbox}
              aria-label="Risk"
              onToggle={this.onRiskToggle}
              onSelect={this.onRiskSelect}
              selections={filters.risk}
              isOpen={riskIsExpanded}
              placeholderText="Risk"
            >
              {riskMenuItems}
            </Select>
          </ToolbarFilter>
        </ToolbarGroup>
      </React.Fragment>
    );

    const dropdownItems = [
      <DropdownItem key="link">Link</DropdownItem>,
      <DropdownItem key="action" component="button">
        Action
          </DropdownItem>,
      <DropdownItem key="disabled link" isDisabled={true}>
        Disabled Link
          </DropdownItem>,
      <DropdownItem key="disabled action" isDisabled={true} component="button">
        Disabled Action
          </DropdownItem>,
      <DropdownSeparator key="separator" />,
      <DropdownItem key="separated link">Separated Link</DropdownItem>,
      <DropdownItem key="separated action" component="button">
        Separated Action
          </DropdownItem>
    ];

    const toolbarItems = (
      <React.Fragment>
        <ToolbarToggleGroup toggleIcon={<FilterIcon />} breakpoint="xl">
          {toggleGroupItems}
        </ToolbarToggleGroup>
        <ToolbarGroup variant="icon-button-group">
        </ToolbarGroup>
        <ToolbarItem>
          <Dropdown
            toggle={<KebabToggle onToggle={this.onKebabToggle} />}
            isOpen={kebabIsOpen}
            isPlain={true}
            dropdownItems={dropdownItems}
          />
        </ToolbarItem>
      </React.Fragment>
    );

    return (
      <React.Fragment>

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
          <DrawerContent panelContent={panelContent}>
            <DrawerContentBody>
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

  private onRiskSelect = (event, selection) => {
    this.onSelect("risk", event, selection);
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
          risk: [],
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

  private onRiskToggle = isExpanded => {
    this.setState({
      riskIsExpanded: isExpanded
    });
  };
  private onKebabToggle = isOpen => {
    this.setState({
      kebabIsOpen: isOpen
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
    console.log("[onSelectRepositories] event.target id =>", event.target.id)
    const checked = event.target.checked;
    console.log("[onSelectRepositories] checked =>", checked)
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
    })
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