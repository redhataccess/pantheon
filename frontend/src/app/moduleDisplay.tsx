import React, { Component } from 'react';
import { Button } from '@patternfly/react-core';
import { Breadcrumb, BreadcrumbItem, BreadcrumbHeading } from '@patternfly/react-core';
import { Link } from "react-router-dom";
import { Level, LevelItem } from '@patternfly/react-core';
import { Tooltip } from '@patternfly/react-core';
import {
    DataList, DataListItem, DataListItemRow, DataListItemCells,
    DataListCell, Card, Text, TextContent, TextVariants, Grid, GridItem
  } from '@patternfly/react-core';
import { Revisions } from '@app/revisions';
import { HelpIcon } from '@patternfly/react-icons';
  
  class ModuleDisplay extends Component {

    public state = {
        draftPath: '',
        draftUpdateDate: '',
        initialLoad: true,
        modulePath: '',
        moduleTitle: "",
        releasePath: '',
        releaseUpdateDate: '',
        resourceType: '',
        results: {}
    };

    public render() {                
        // console.log('Props: ',this.props);
        return (  
            <React.Fragment>
                {this.state.initialLoad && this.fetchModuleDetails(this.props)}
                {!this.state.initialLoad && 
                <div>
                    <div>
                        <Level gutter="md">
                            <LevelItem>
                                    <TextContent>
                                        <Text component={TextVariants.h1}>{this.state.moduleTitle}</Text>  
                                    </TextContent>
                            </LevelItem>
                            <LevelItem />
                        </Level>                
                    </div>
                    <div>
                        <a href='http://access.redhat.com'>View on Customer Portal</a>
                    </div>
                    <div>
                        <DataList aria-label="single action data list example ">
                            <DataListItem aria-labelledby="simple-item1">
                                <DataListItemRow id="data-rows-header" >
                                    <DataListItemCells
                                        dataListCells={[
                                            <DataListCell width={2} key="products">
                                                <span className="sp-prop-nosort" id="span-source-type">Products</span>                                            
                                            </DataListCell>,
                                            <DataListCell key="published">
                                                <span className="sp-prop-nosort" id="span-source-type">Published</span>
                                            </DataListCell>,
                                            <DataListCell key="updated">
                                                <span className="sp-prop-nosort" id="span-source-type">Draft Uploaded</span>
                                            </DataListCell>,
                                            <DataListCell key="module_type">
                                                <span className="sp-prop-nosort" id="span-source-name">Module Type</span>
                                            </DataListCell>
                                        ]}
                                    />
                                </DataListItemRow>

                                <DataListItemRow>
                                    <DataListItemCells
                                        dataListCells={[
                                            <DataListCell width={2} key="products">
                                                <span>Dummy Product Name</span>
                                            </DataListCell>,
                                            <DataListCell key="published">
                                                <span>{this.state.releaseUpdateDate.substring(4,15)}</span>
                                            </DataListCell>,
                                            <DataListCell key="updated">
                                                <span>{this.state.draftUpdateDate.substring(4,15)}</span>
                                            </DataListCell>,
                                            <DataListCell key="module_type">
                                                <span>{this.state.resourceType}</span>
                                            </DataListCell>,
                                        ]}
                                    />
                                </DataListItemRow>
                                ))}
                        </DataListItem>
                    </DataList>
                    </div>
                    <div>
                        <Card>
                            <Revisions 
                                modulePath={this.state.modulePath}
                                revisionModulePath={this.state.moduleTitle}
                                draftUpdateDate={this.updateDate}
                                releaseUpdateDate={this.updateDate}
                            />
                        </Card>
                    </div> 
                </div>
                }
            </React.Fragment>

        );
    }

    private updateDate = (date,type,path) => {
        if(type==="draft"){
            this.setState({
                draftUpdateDate: date,
                draftPath: path
            },() => {
                // console.log('changed draft date: ', this.state.draftUpdateDate, "version path: ",this.state.draftPath)
            });    
        }
        else{
            this.setState({
                releaseUpdateDate: date,
                releasePath: path
            },() => {
                // console.log('changed release date: ', this.state.releaseUpdateDate, "version path: ",this.state.releasePath)
            });
        }
      };

      private fetchModuleDetails = (data) => {
        this.setState({ initialLoad: false,  modulePath: data["location"]["pathname"]}
        ,() => {
            // console.log("data: ",data);
            // console.log("module Path: ",this.state.modulePath);
        })  

        fetch(data["location"]["pathname"]+'.4.json')
        .then(response => response.json())
        .then(responseJSON => {
            // console.log('fetch results:',responseJSON["en_US"])
            this.setState({
                moduleTitle: responseJSON["en_US"]["1"]["metadata"]["jcr:title"],
                resourceType: responseJSON["sling:resourceType"],
            })

        })
        }
    
}

export { ModuleDisplay }