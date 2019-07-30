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
import { Revisions } from '@app/revisions';
import { HelpIcon } from '@patternfly/react-icons';

class ModuleDisplay extends Component {
    public state = {
        login: false  
    };

    public render() {
        return (  
            <React.Fragment>
                <div>
                    {/* {this.checkAuth()}
                    {this.loginRedirect()} */}
                    <Breadcrumb>
                        <BreadcrumbItem to="/">Modules</BreadcrumbItem>
                        <BreadcrumbItem to="#" isActive={true}>
                            Clicked Module
                        </BreadcrumbItem>
                    </Breadcrumb>
                </div>
                <div>
                    <Level gutter="md">
                        <LevelItem>Module Name  
                        <Tooltip
                                position="right"
                                content={
                                    <div>Title updated in latest revision</div>
                                }
                        >
                                <span><HelpIcon/></span>
                            </Tooltip>
                        </LevelItem>
                        <LevelItem />
                        <LevelItem>
                            <Button variant="secondary">Edit metadata</Button>
                        </LevelItem>
                    </Level>                
                </div>
                <div>
                    <Link to='https://access.redhat.com'>Link to Customer Portal</Link>
                </div>
                <div>
                <DataList aria-label="Simple data list example">
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
                                        <span className="sp-prop-nosort" id="span-source-type">Updated</span>
                                    </DataListCell>,
                                    <DataListCell key="module_type">
                                        <span className="sp-prop-nosort" id="span-source-name">Module Type</span>
                                    </DataListCell>
                                ]}
                            />
                        </DataListItemRow>

                  <DataListItemRow id="data-rows">
                    <DataListItemCells 
                          dataListCells={[
                                <DataListCell width={2} key="products">
                                  <span>Dummy Product</span>
                                </DataListCell>,
                                <DataListCell key="published">
                                  <span>Dummy Publish</span>
                                </DataListCell>,
                                <DataListCell key="updated">
                                  <span>Dummy Updated</span>
                                </DataListCell>,
                                <DataListCell key="module_type">
                                  <span>Dummy Module Type</span>
                                </DataListCell>,
                          ]}
                    />
                  </DataListItemRow>
                ))}
                    </DataListItem>
                </DataList>
                </div>
                <div>
                    <Revisions/>
                </div>
            </React.Fragment>

        );
    }
    
    //   private loginRedirect = () => {
    //     if (this.state.login) {
    //       return <Redirect to='/login' />
    //     } else {
    //       return ""
    //     }
    //   }
    
    //   private checkAuth = () => {
    //     fetch("/system/sling/info.sessionInfo.json")
    //       .then(response => response.json())
    //       .then(responseJSON => {
    //         const key = "userID"
    //         if (responseJSON[key] !== 'admin') {
    //           this.setState({ login: true })
    //         }
    //       })
    //   }
}

export { ModuleDisplay }