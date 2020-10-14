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


export interface ISearchState {
  filterLabel: string
  isExpanded: boolean
  isExpandedAssemblies: boolean
  isExpandedModules: boolean
  isExpandedProductFilter: boolean
  isExpandedRepoFilter: boolean
  repositories: Array<{ name: string, id: "" }>

  inputValue: string,
  statusIsExpanded: boolean,
  riskIsExpanded: boolean,
  filters: {
    risk: any,
    status: any
  },
  kebabIsOpen: boolean,

  filterValue: string


}
class SearchBeta extends Component<any, ISearchState> {
  private drawerRef: React.RefObject<HTMLInputElement>;

  constructor(props) {
    super(props);
    this.state = {
      // states for drawer
      filterLabel: "repo",
      isExpanded: true,
      isExpandedAssemblies: true,
      isExpandedModules: true,
      isExpandedProductFilter: true,
      isExpandedRepoFilter: true,
      repositories: [{ name: "", id: "" }],
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
      filterValue: "",

    };
    this.drawerRef = React.createRef();

  }

  public componentDidMount() {
    // tree inside the drawer
    this.getRepositories()
    // TODO: enable resize
    // toolbar
    // window.addEventListener("resize", this.closeExpandableContent);
  }

  public componentWillUnmount() {
    // TODO: enable resize
    // toolbar
    // window.removeEventListener("resize", this.closeExpandableContent);
  }
  public render() {
    const { filterLabel, isExpanded, isExpandedProductFilter, isExpandedRepoFilter, repositories, inputValue, filters, statusIsExpanded, riskIsExpanded, kebabIsOpen } = this.state;
    // TODO: load real data
    const repoList = [
      <SimpleListItem key="repo1">
        <Checkbox label="ceph storage commons" aria-label="uncontrolled checkbox" id="check-repo-1" />
      </SimpleListItem>,
      <SimpleListItem key="repo2">
        <Checkbox label="red-hat-cost-management" aria-label="uncontrolled checkbox" id="check-repo-2" />
      </SimpleListItem>,
      <SimpleListItem key="repo3">
        <Checkbox label="rhel-8-docs" aria-label="uncontrolled checkbox" id="check-repo-3" />
      </SimpleListItem>
    ];

    // TODO: load real data
    const productList = [
      <SimpleListItem key="product1">
        <Checkbox label="Ceph Storage Commmons" aria-label="uncontrolled checkbox" id="check-product-1" />
      </SimpleListItem>,
      <SimpleListItem key="product2">
        <Checkbox label="Cost Management" aria-label="uncontrolled checkbox" id="check-product-2" />
      </SimpleListItem>,
      <SimpleListItem key="product3">
        <Checkbox label="Red Hat Enterprise Linux" aria-label="uncontrolled checkbox" id="check-product-3" />
      </SimpleListItem>
    ];
    const panelContent = (
      <DrawerPanelContent widths={{ lg: "width_25" }}>
        <DrawerHead>
          <span className="pf-c-title pf-m-2xl" tabIndex={isExpanded ? 0 : -1} ref={this.drawerRef}>Filters</span>
          <DrawerActions>
            <DrawerCloseButton onClick={this.onCloseClick} />
          </DrawerActions>
          {/* By {filterLabel} */}
          <ExpandableSection toggleText="By repo" isActive={true}>
            <SearchInput
              placeholder="Filter"
              value={this.state.filterValue}
              onChange={this.onChangeFilter}
              onClear={(evt) => this.onChangeFilter("", evt)}
            />
            <SimpleList aria-label="Repository List">
              {repoList}
            </SimpleList>

          </ExpandableSection>
          <br />
          <ExpandableSection toggleText="By product" isActive={true}>
            <SearchInput
              placeholder="Filter"
              value={this.state.filterValue}
              onChange={this.onChangeFilter}
              onClear={(evt) => this.onChangeFilter("", evt)}
            />
            <SimpleList aria-label="Product List">
              {productList}
            </SimpleList>

          </ExpandableSection>
        </DrawerHead>
      </DrawerPanelContent>
    );
    const drawerContent = (
      <React.Fragment>
        <ExpandableSection toggleText="Modules" className="pf-c-title pf-m-2xl" isActive={true}>
          <SearchResults />

        </ExpandableSection>
        <br />
        <ExpandableSection toggleText="Assemblies" className="pf-c-title pf-m-2xl" isActive={true}>
          <SearchResults />

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
              aria-label="search input example"
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
        console.log("[getRepositories] repositories=>", this.state.repositories)
      })
      .catch((error) => {
        console.log(error)
      })

  }

  private onExpand = () => {
    this.drawerRef.current && this.drawerRef.current.focus()
  };

  private onClickRepoFilter = () => {
    const isExpandedRepoFilter = !this.state.isExpandedRepoFilter;
    this.setState({
      isExpandedRepoFilter
    });
  };

  private onClickProductFilter = () => {
    const isExpandedProductFilter = !this.state.isExpandedProductFilter;
    this.setState({
      isExpandedProductFilter
    });
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
  private onChangeFilter = (value, event) => {
    this.setState({
      filterValue: value
    });
  };

}


export { SearchBeta }; 