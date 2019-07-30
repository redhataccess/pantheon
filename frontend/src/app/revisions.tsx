import React, { Component } from 'react';
import { Button } from '@patternfly/react-core';
import {
    Card,DataList, DataListItem, DataListItemRow, DataListItemCells, DataListCell,
    DataListToggle, DataListContent, DataListAction,Dropdown, DropdownToggle, 
    DropdownItem, DropdownSeparator, DropdownPosition, DropdownDirection,Grid, 
    GridItem, KebabToggle, Level, LevelItem
  } from '@patternfly/react-core';

class Revisions extends Component {
    public state = {
        isArchiveDropDownOpen: false,
        isArchiveSelect: false,
        isDropDownOpen: false,
        isHeadingToggle: false,
        isOpen: false,
        isRowExpanded: false,
        isRowToggle: false,
        login: false
    };

    public render() {
        return (  
            <React.Fragment>
                <Card>
                    <div>
                    <DataList aria-label="Simple data list example">
                        <DataListItem aria-labelledby="simple-item1">
                            <DataListItemRow id="data-rows-header" >
                            <DataListToggle
                                        onClick={()=>this.onHeadingToggle()}
                                        isExpanded={this.state.isHeadingToggle}
                                        id="ex-toggle1"
                                        aria-controls="ex-expand1"
                                    />
                                <DataListItemCells
                                    dataListCells={[
                                        <DataListCell key="revision">
                                            <span className="sp-prop-nosort" id="span-source-type">Revision</span>
                                        </DataListCell>,
                                        <DataListCell key="published">
                                            <span className="sp-prop-nosort" id="span-source-type">Published</span>
                                        </DataListCell>,
                                        <DataListCell key="updated">
                                            <span className="sp-prop-nosort" id="span-source-type">Updated</span>
                                        </DataListCell>,
                                        <DataListCell width={2} key="module_type">
                                            <span className="sp-prop-nosort" id="span-source-name"/>
                                        </DataListCell>
                                    ]}
                                />
                            </DataListItemRow>
                            <DataListContent
                                    aria-label="Primary Content Details"
                                    id="ex-expand1"
                                    isHidden={!this.state.isHeadingToggle}
                                    noPadding={true}
                                >
                                <DataListItemRow>
                                        <DataListToggle
                                            onClick={()=>this.onExpandableToggle()}
                                            isExpanded={this.state.isRowToggle}
                                            id="ex-toggle1"
                                            aria-controls="ex-expand1"
                                        />
                                        <DataListItemCells
                                            dataListCells={[
                                                <DataListCell key="products">
                                                    <span>Dummy Revision</span>
                                                </DataListCell>,
                                                <DataListCell key="published">
                                                    <span>Not Publish</span>
                                                </DataListCell>,
                                                <DataListCell key="updated">
                                                    <span>Dummy Date</span>
                                                </DataListCell>,
                                                <DataListCell width={2} key="module_type">
                                                    <span><Button variant="primary">Publish</Button></span><span>{'  '}</span>
                                                    <span><Button variant="secondary">Preview</Button></span>{'  '}
                                                    <span>
                                                        <Dropdown
                                                            isPlain={true}
                                                            position={DropdownPosition.right}
                                                            isOpen={this.state.isArchiveDropDownOpen}
                                                            onSelect={this.onArchiveSelect}
                                                            toggle={<KebabToggle onToggle={this.onArchiveToggle} />}
                                                            dropdownItems={[
                                                                <DropdownItem key="archive">Archive</DropdownItem>,
                                                            ]}
                                                        />
                                                    </span>
                                                </DataListCell>
                                            ]}
                                        />
                                    </DataListItemRow>
                                    <DataListContent
                                        aria-label="Primary Content Details"
                                        id="ex-expand1"
                                        isHidden={!this.state.isRowToggle}
                                        noPadding={true}
                                    >
                                        <Grid>
                                            <GridItem span={1}/>
                                            <GridItem span={10}>
                                                <Level gutter="md">
                                                    <LevelItem>
                                                        <span className="sp-prop-nosort" id="span-source-type">File name:</span>{'  '}
                                                        <span>Dummy FileName</span>
                                                    </LevelItem>
                                                    <LevelItem>
                                                        <span className="sp-prop-nosort" id="span-source-type">Module title:</span>{'  '}
                                                        <span>Dummy FileName</span>
                                                    </LevelItem>
                                                    <LevelItem>
                                                        <span className="sp-prop-nosort" id="span-source-type">Context package:</span>{'  '}
                                                        <span>Dummy FileName</span>
                                                    </LevelItem>
                                                </Level>
                                            </GridItem>
                                            <GridItem span={1}/>
                                        </Grid>
                                    </DataListContent>                        
                            </DataListContent>
                        </DataListItem>
                    </DataList>
                    </div>
                </Card>    
            </React.Fragment>

        );
    }

    private onArchiveSelect = event => {
        this.setState({
            isArchiveDropDownOpen: !this.state.isArchiveDropDownOpen
        });
      };

      private onArchiveToggle = () => {
        this.setState({ 
            isArchiveDropDownOpen: !this.state.isArchiveDropDownOpen
        });
      };

      private onExpandableToggle = () => {
        this.setState({
            isRowToggle: !this.state.isRowToggle
        });
      }

      private onHeadingToggle = () => {
        this.setState({
            isHeadingToggle: !this.state.isHeadingToggle
        });
      }
}

export { Revisions }