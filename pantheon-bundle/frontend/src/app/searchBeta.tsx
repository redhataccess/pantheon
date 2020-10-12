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
    TreeView, TreeViewDataItem,
    Toolbar, ToolbarItem, ToolbarContent, ToolbarFilter, ToolbarToggleGroup, ToolbarGroup,
    TextInput,
    SelectVariant,
    ExpandableSection,
    Checkbox,
} from "@patternfly/react-core";

import "@app/app.css";
import SearchIcon from "@patternfly/react-icons/dist/js/icons/search-icon";
// import TextInput from "@patternfly/react-icons/dist/js/icons/text-input";
import FilterIcon from "@patternfly/react-icons/dist/js/icons/filter-icon";

export interface ISearchState {
  activeItems: any[]
  checkedItems: any[]
  filterLabel: string
  isExpanded: boolean
  repositories: Array<{ name: string, id: ""}>
  
      inputValue: string,
      statusIsExpanded: boolean,
      riskIsExpanded: boolean,
      filters: {
        risk: any,
        status: any
      },
      
}
class SearchBeta extends Component<any, ISearchState> {
    private drawerRef: React.RefObject<HTMLInputElement>;
    private options: Array<{ name: string, id: string, checkProps: any, children: [] }>
    constructor(props) {
        super(props);
        this.state = {
            // states for tree view
            activeItems: [],
            checkedItems: [],
            // states for drawer
            filterLabel: "repo",
            isExpanded: true,
            repositories: [{ name: "", id: "" }],
            // states for toolbar
            inputValue: '',
            statusIsExpanded: false,
            riskIsExpanded: false,
            filters: { risk: ['Low'],
            status: ['New', 'Pending'] },
        };
        this.drawerRef = React.createRef();
        // options for treeview
        this.options = [
            {
              name: 'ApplicationLauncher',
              id: 'AppLaunch',
              checkProps: { 'aria-label': 'app-launcher-check', checked: false },
              children: [],
              
            },
            {
              name: 'Cost Management',
              id: 'Cost',
              checkProps: { 'aria-label': 'cost-check', checked: false },
              children: [
              ]
            },
            {
              name: 'Sources',
              id: 'Sources',
              checkProps: { 'aria-label': 'sources-check', checked: false },
              children: [
                
              ]
            },
            {
              name: 'Really really really long folder name that overflows the container it is in',
              id: 'Long',
              checkProps: { 'aria-label': 'long-check', checked: false },
              children: []
            }
          ];
    }

    public componentDidMount() {
        // tree inside the drawer
        this.getRepositories()
        // toolbar
        // window.addEventListener('resize', this.closeExpandableContent);
    }

    public componentWillUnmount() {
      // toolbar
      // window.removeEventListener('resize', this.closeExpandableContent);
    }
    public render() {
        const { filterLabel, isExpanded, repositories,  activeItems,  inputValue, filters, statusIsExpanded, riskIsExpanded, } = this.state;
        // const mapped = this.options.map(item => this.mapTree(item));
        const panelContent = (
            <DrawerPanelContent>
                <DrawerHead>
                    <h5 className= "pf-c-title pf-m-2xl" tabIndex={isExpanded ? 0 : -1} ref={this.drawerRef}>Filters</h5>
                    <DrawerActions>
                        <DrawerCloseButton onClick={this.onCloseClick} />
                    </DrawerActions>
                    {/* By {filterLabel} */}
                    {/* By {filterLabel} */}
                    <ExpandableSection toggleText="By repo" isExpanded={true}>
                              {/* <React.Fragment> */}
                                <Checkbox label="ceph storage commons" aria-label="uncontrolled checkbox" id="check-6" />
                                <Checkbox label="red-hat-cost-management" aria-label="uncontrolled checkbox" id="check-7" />
                                <Checkbox label="rhel-8-docs" aria-label="uncontrolled checkbox" id="check-5" />
                              {/* </React.Fragment> */}
                            </ExpandableSection>
                </DrawerHead>
                {/* <DrawerPanelBody>drawer-panel</DrawerPanelBody> */}
            </DrawerPanelContent>
        );
        const drawerContent = "";
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
          <DropdownItem key="disabled link" isDisabled>
            Disabled Link
          </DropdownItem>,
          <DropdownItem key="disabled action" isDisabled component="button">
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
            
          </React.Fragment>
        );
        return (
            <React.Fragment>
                <Button variant="tertiary" aria-expanded={isExpanded} onClick={this.onClick} icon={<FilterIcon />} />
                <Drawer isExpanded={isExpanded} isInline={true} position="left" onExpand={this.onExpand}>
                    <DrawerContent panelContent={panelContent}>
                        <DrawerContentBody>
                            
                            {/* <TreeView data={mapped} activeItems={activeItems} onSelect={this.onClickTree} onCheck={this.onCheckTree} hasChecks={true} /> */}
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

    // methods for tree view
    // private onClickTree = (evt, treeViewItem, parentItem) => {
    //     this.setState({
    //       activeItems: [treeViewItem, parentItem]
    //     });
    //   };
  
    // private onCheckTree = (evt, treeViewItem) => {
    //     const checked = evt.target.checked;
    //     console.log(checked);
  
    //     const checkedItemTree = this.options
    //       .map(opt => Object.assign({}, opt))
    //       .filter(item => this.filterItems(item, treeViewItem));
    //     const flatCheckedItems = this.flattenTree(checkedItemTree);
  
    //     // this.setState(
    //     //   prevState => ({
    //     //     checkedItems: checked
    //     //       ? prevState.checkedItems.concat(
    //     //           flatCheckedItems.filter(item => !prevState.checkedItems.some(i => i.id === item.id))
    //     //         )
    //     //       : prevState.checkedItems.filter(item => !flatCheckedItems.some(i => i.id === item.id))
    //     //   }),
    //     //   () => {
    //     //     console.log('Checked items: ', this.state.checkedItems);
    //     //   }
    //     // );
    //   };

    //   // Tree view Helper functions
    // private isChecked = dataItem => this.state.checkedItems.some(item => item.id === dataItem.id);
    // private areAllDescendantsChecked = dataItem =>
    //   dataItem.children ? dataItem.children.every(child => this.areAllDescendantsChecked(child)) : this.isChecked(dataItem);
    // private areSomeDescendantsChecked = dataItem =>
    //   dataItem.children ? dataItem.children.some(child => this.areSomeDescendantsChecked(child)) : this.isChecked(dataItem);

    // private flattenTree = tree => {
    //   let result = [];
    //   if (tree !== undefined && tree.size > 0) {
    //     // tree.forEach((item: {}) => {
    //     //     result.push(item);
          
          
    //     //   // if (item.children) {
    //     //   //   result = result.concat(this.flattenTree(item.children));
    //     //   // }
    //     // });
    //   }
    //   return result;
    // };

    // private mapTree = item => {
    //   const hasCheck = this.areAllDescendantsChecked(item);
    //   // Reset checked properties to be updated
    //   item.checkProps.checked = false;
    //   item.checkProps.ref = elem => elem && (elem.indeterminate = false);

    //   if (hasCheck) {
    //     item.checkProps.checked = true;
    //   } else {
    //     const hasPartialCheck = this.areSomeDescendantsChecked(item);
    //     if (hasPartialCheck) {
    //       item.checkProps.checked = false;
    //       item.checkProps.ref = elem => elem && (elem.indeterminate = true);
    //     }
    //   }

    //   if (item.children) {
    //     return {
    //       ...item,
    //       children: item.children.map(child => this.mapTree(child))
    //     };
    //   }
    //   return item;
    // };

    // private filterItems = (item, checkedItem) => {
    //   if (item.id === checkedItem.id) {
    //     return true;
    //   }

    //   if (item.children) {
    //     return (
    //       (item.children = item.children
    //         .map(opt => Object.assign({}, opt))
    //         .filter(child => this.filterItems(child, checkedItem))).length > 0
    //     );
    //   }
    // };

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
      this.onSelect('status', event, selection);
    };

    private onRiskSelect = (event, selection) => {
      this.onSelect('risk', event, selection);
    };

    private onDelete = (type = '', id = '') => {
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

    // methods for
  }


export { SearchBeta }; 