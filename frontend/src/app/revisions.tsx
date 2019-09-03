import React, { Component } from 'react';
import { Button } from '@patternfly/react-core';
import {
    Card,DataList, DataListItem, DataListItemRow, DataListItemCells, DataListCell,
    DataListToggle, DataListContent, DataListAction,Dropdown, DropdownToggle, 
    DropdownItem, DropdownSeparator, DropdownPosition, DropdownDirection,Grid, 
    GridItem, KebabToggle, Level, LevelItem
  } from '@patternfly/react-core';
import CheckImage from '@app/images/check_image.jpg';
import BlankImage from '@app/images/blank.jpg';

  export interface IProps {
    modulePath: string
  }

class Revisions extends Component<IProps> {

    public draft= [{ "icon": BlankImage, "revision": "", "publishedState": 'Draft', "updatedDate": 'dummy date', "firstButtonType": 'primary',"secondButtonType": 'secondary', "firstButtonText": 'Publish',"secondButtonText": 'Preview',"isDropdownOpen": false,"isArchiveDropDownOpen": false}]
    public release= [{ "icon": CheckImage, "revision": "Version 1", "publishedState": 'Released', "updatedDate": 'dummy date', "firstButtonType": 'secondary',"secondButtonType": 'primary', "firstButtonText": 'Unpublish',"secondButtonText": 'View',"isDropdownOpen": false,"isArchiveDropDownOpen": false}]

    public state = {
        initialLoad: true,
        isArchiveDropDownOpen: false,
        isArchiveSelect: false,
        isDropDownOpen: false,
        isHeadingToggle: false,
        isOpen: false,
        isRowToggle: false,
        login: false,
        results: [this.draft,this.release]
    };

    public render() {
        return (
            <React.Fragment>
                {this.state.initialLoad && this.fetchRevisions()}
                <Card>
                    <div>
                        <DataList aria-label="Simple data list example">
                            <DataListItem aria-labelledby="simple-item1" isExpanded={this.state.isHeadingToggle}>
                                <DataListItemRow id="data-rows-header" >
                                    <DataListToggle
                                        onClick={() => this.onHeadingToggle()}
                                        isExpanded={true}
                                        id="width-ex3-toggle1"
                                        aria-controls="width-ex3-expand1"
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
                                            <DataListCell key="module_type">
                                                <span className="sp-prop-nosort" id="span-source-name" />
                                            </DataListCell>,
                                            <DataListCell key="module_type">
                                                <span className="sp-prop-nosort" id="span-source-name" />
                                            </DataListCell>
                                        ]}
                                    />
                                </DataListItemRow>
                            </DataListItem>
                            <DataListContent
                                aria-label="Secondary Content Details"
                                id={"Content"}
                                isHidden={!this.state.isHeadingToggle}
                                noPadding={true}
                            >
                                {/* this is the data list for the inner row */}
                                {this.state.results.map(type => (
                                    type.map(data => (
                                        data["revision"] !== "" && (
                                            <DataList aria-label="Simple data list example2">
                                                {console.log("isExpanded: ", data["isDropdownOpen"])}
                                                <DataListItem aria-labelledby="simple-item2" isExpanded={data["isDropdownOpen"]}>
                                                    <DataListItemRow>
                                                        <DataListToggle
                                                            onClick={() => this.onExpandableToggle(data)}
                                                            isExpanded={data["isDropdownOpen"]}
                                                            id={data["revision"]}
                                                            aria-controls={data["revision"]}
                                                        />
                                                        <DataListItemCells
                                                            dataListCells={[
                                                                <DataListCell key="revision">
                                                                    {data["revision"]}
                                                                </DataListCell>,
                                                                <DataListCell key="published">
                                                                    {data["publishedState"]}
                                                                </DataListCell>,
                                                                <DataListCell key="updated">
                                                                    {data["updatedDate"]}
                                                                </DataListCell>,
                                                                <DataListCell key="module_type">
                                                                    <Button variant="primary">{data["firstButtonText"]}</Button>{'  '}
                                                                    <Button variant="secondary" onClick={this.previewDoc}>{data["secondButtonText"]}</Button>{'  '}
                                                                </DataListCell>,
                                                                <DataListCell key="image" width={1}>
                                                                    <Dropdown
                                                                        isPlain={true}
                                                                        position={DropdownPosition.right}
                                                                        isOpen={data["isArchiveDropDownOpen"]}
                                                                        onSelect={this.onArchiveSelect}
                                                                        toggle={<KebabToggle onToggle={() => this.onArchiveToggle(data)} />}
                                                                        dropdownItems={[
                                                                            <DropdownItem key="archive">Archive</DropdownItem>,
                                                                        ]}
                                                                    />
                                                                </DataListCell>
                                                            ]}
                                                        />
                                                    </DataListItemRow>
                                                    <DataListContent
                                                        aria-label={data["revision"]}
                                                        id={data["revision"]}
                                                        isHidden={!data["isDropdownOpen"]}
                                                        noPadding={true}
                                                    >
                                                        {/* this is the content for the inner data list content */}
                                                        <Grid>
                                                            <GridItem span={1} />
                                                            <GridItem span={10}>
                                                                <Level gutter="md">
                                                                    <LevelItem>
                                                                        <span className="sp-prop-nosort" id="span-source-type">File name:</span>{'  '}
                                                                        <span>Dummy FileName</span>
                                                                    </LevelItem>
                                                                    <LevelItem>
                                                                        <span className="sp-prop-nosort" id="span-source-type">Module title:</span>{'  '}
                                                                        <span>Dummy Module Title</span>
                                                                    </LevelItem>
                                                                    <LevelItem>
                                                                        <span className="sp-prop-nosort" id="span-source-type">Context package:</span>{'  '}
                                                                        <span>Dummy Context Package</span>
                                                                    </LevelItem>
                                                                </Level>
                                                            </GridItem>
                                                            <GridItem span={1} />
                                                        </Grid>
                                                    </DataListContent>
                                                </DataListItem>
                                            </DataList>)
                                    ))
                                ))}
                            </DataListContent>
                        </DataList>
                    </div>
                </Card>
            </React.Fragment>

        );
    }

    private fetchRevisions = () => {
            fetch("/modules/nana.2.json?")
            .then(response => response.json())
            .then(responseJSON => {
                this.setState(updateState => {    
                let releasedTag = responseJSON["en_US"]["released"];
                let draftTag = responseJSON["en_US"]["draft"];
        
                console.log("uuid v0: ",responseJSON["en_US"]["v0"]["jcr:uuid"])
                if(responseJSON["en_US"]["v0"]["jcr:uuid"]===draftTag){
                    this.draft[0]["revision"] = "Version 0";
                }
                    
                if(responseJSON["en_US"]["v0"]["jcr:uuid"]===releasedTag){
                    this.release[0]["revision"] = "Version 0";
                }
                    
                let objectKeys = Object.keys(responseJSON["en_US"]);
    
                for(var key in objectKeys){
                    if(objectKeys[key]==="jcr:primaryType"){
                        break;
                    }
                    else{
                        if(responseJSON["en_US"][objectKeys[key]]["jcr:uuid"]===draftTag){
                            this.draft[0]["revision"] = "Version "+objectKeys[key];
                        }
                        if(responseJSON["en_US"][objectKeys[key]]["jcr:uuid"]===releasedTag){
                            this.release[0]["revision"] = "Version "+objectKeys[key];
                        }                            
                    }
                        
                }
                return {
                    initialLoad: false,
                    results: [this.draft,this.release] 
                }        
            })
        })    
    }

    private onArchiveSelect = event => {
        this.setState({
            isArchiveDropDownOpen: !this.state.isArchiveDropDownOpen
        });
      };

      private onArchiveToggle = (data) => {
        data["isArchiveDropDownOpen"] = !data["isArchiveDropDownOpen"];
        this.setState({ 
            isArchiveDropDownOpen: this.state.isArchiveDropDownOpen
        });
      };

      private onExpandableToggle = (data) => {
        // console.log("complete data: ",data)
        data["isDropdownOpen"] = !data["isDropdownOpen"];
        // console.log('data id:',data["revision"],' data open status: ', data["isDropdownOpen"])
        this.setState({
            isRowToggle: this.state.isRowToggle
        });
      }

      private onHeadingToggle = () => {
        this.setState({
            isHeadingToggle: !this.state.isHeadingToggle
        });
      }

      private previewDoc = () => {
          console.log("Preview path: ", "/", this.props.modulePath, ".preview")
          return window.open("/" + this.props.modulePath + ".preview");
      }
}

export { Revisions }