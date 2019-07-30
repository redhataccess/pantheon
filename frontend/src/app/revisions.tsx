import React, { Component } from 'react';
import { Redirect } from 'react-router-dom';
import { Button } from '@patternfly/react-core';
import { Breadcrumb, BreadcrumbItem, BreadcrumbHeading } from '@patternfly/react-core';
import { Link } from "react-router-dom";
import { Level, LevelItem } from '@patternfly/react-core';
import { Tooltip, TooltipPosition } from '@patternfly/react-core';
import {
    DataList, DataListItem, DataListItemRow, DataListItemCells,
    DataListCell, FormGroup, DataListCheck
  } from '@patternfly/react-core';
import { Dropdown, DropdownToggle, DropdownItem, DropdownSeparator, DropdownPosition, DropdownDirection, KebabToggle } from '@patternfly/react-core';
import { ThIcon } from '@patternfly/react-icons';

class Revisions extends Component {
    public state = {
        isDropDownOpen: false,
        login: false
    };

    public render() {

        const dropdownItems = [
            <DropdownItem key="link">Archive</DropdownItem>
          ];

        return (  
            <React.Fragment>
                <div>
                <DataList aria-label="Simple data list example">
                    <DataListItem aria-labelledby="simple-item1">
                        <DataListItemRow id="data-rows-header" >
                            <DataListItemCells
                                dataListCells={[
                                    <DataListCell key="products">
                                        <span className="sp-prop-nosort" id="span-source-type">Revision</span>
                                    </DataListCell>,
                                    <DataListCell key="published">
                                        <span className="sp-prop-nosort" id="span-source-type">Published</span>
                                    </DataListCell>,
                                    <DataListCell key="updated">
                                        <span className="sp-prop-nosort" id="span-source-type">Updated</span>
                                    </DataListCell>,
                                    <DataListCell key="module_type">
                                        <span className="sp-prop-nosort" id="span-source-name"></span>
                                    </DataListCell>
                                ]}
                            />
                        </DataListItemRow>

                  <DataListItemRow id="data-rows">
                    <DataListItemCells 
                          dataListCells={[
                                <DataListCell width={2} key="products">
                                  <span>Dummy Revision</span>
                                </DataListCell>,
                                <DataListCell key="published">
                                  <span>Not Publish</span>
                                </DataListCell>,
                                <DataListCell key="updated">
                                  <span>Dummy Date</span>
                                </DataListCell>,
                                <DataListCell key="module_type">
                                  <span><Button variant="primary">Publish</Button></span><span><Button variant="secondary">Preview</Button></span>
                                  <span>
                                        <Dropdown
                                            onSelect={this.onSelect}
                                            toggle={<KebabToggle onToggle={this.onToggle} />}
                                            isOpen={this.state.isDropDownOpen}
                                            isPlain={true}
                                            dropdownItems={dropdownItems}
                                        />
                                  </span>
                                </DataListCell>
                          ]}
                    />
                  </DataListItemRow>
                ))}
                    </DataListItem>
                </DataList>
                </div>
            </React.Fragment>

        );
    }

    private onToggle = isOpen => {
        this.setState({
          isOpen
        });
      };

    private onSelect = event => {
        this.setState({
          isOpen: !this.state.isDropDownOpen
        });
      };    
}

export { Revisions }