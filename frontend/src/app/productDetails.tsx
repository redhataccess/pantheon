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
import { HelpIcon } from '@patternfly/react-icons';

export interface IProps {
    productName: string
    // modulePath: string
    // moduleType: string
    // updated: string    
  }
  
  class ProductDetails extends Component<IProps> {

    public render() {
        return (  
            <React.Fragment>
                <div>
                    <Breadcrumb>
                        <BreadcrumbItem to="/">All Products</BreadcrumbItem>
                        <BreadcrumbItem to="#" isActive={true}>
                            {this.props.productName}
                        </BreadcrumbItem>
                    </Breadcrumb>
                </div>
                <div>{"\n"}</div>
                <div>{"\n"}</div>
                <div>
                    <Level gutter="md">
                        <LevelItem>
                                <TextContent>
                                    <Text component={TextVariants.h1}>{this.props.productName}{'  '}</Text>  
                                </TextContent>
                        </LevelItem>
                        <LevelItem />
                    </Level>                
                </div>
                <div>
                    
                </div>
            </React.Fragment>

        );
    }
    
}

export { ProductDetails }