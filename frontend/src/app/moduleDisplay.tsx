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

export interface IProps {
    moduleName: string
    modulePath: string
    moduleType: string
    updated: string    
  }
  
  class ModuleDisplay extends Component<IProps> {

    public state = {
        draftUpdateDate: '',
        releaseUpdateDate: '',
    };

    public render() {        
        return (  
            <React.Fragment>
                <div>
                    <Breadcrumb>
                        <BreadcrumbItem to="#">Modules</BreadcrumbItem>
                        <BreadcrumbItem to="#" isActive={true}>
                            {this.props.moduleName}
                        </BreadcrumbItem>
                    </Breadcrumb>
                </div>
                <div>
                    <Level gutter="md">
                        <LevelItem>
                                <TextContent>
                                    <Text component={TextVariants.h1}>{this.props.moduleName}{'  '}
                                    <Tooltip
                                                position="right"
                                                content={
                                                    <div>Title updated in latest revision</div>
                                                }>
                                            <span><HelpIcon/></span>
                                        </Tooltip>
                                    </Text>  
                                </TextContent>
                        </LevelItem>
                        <LevelItem />
                        <LevelItem>
                            <Button variant="secondary">Edit metadata</Button>
                        </LevelItem>
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
                                            <span>{this.props.moduleType}</span>
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
                            modulePath={this.props.modulePath}
                            revisionModulePath={this.props.moduleName}
                            draftUpdateDate={this.updateDate}
                            releaseUpdateDate={this.updateDate}
                        />
                    </Card>
                </div> 
            </React.Fragment>

        );
    }

    private updateDate = (date,type) => {
        if(type==="draft"){
            this.setState({
                draftUpdateDate: date
            },() => {
                console.log('changed draft date: ', this.state.draftUpdateDate)
            });    
        }
        else{
            this.setState({
                releaseUpdateDate: date
            },() => {
                console.log('changed release date: ', this.state.releaseUpdateDate)
            });
        }
      };

    
}

export { ModuleDisplay }