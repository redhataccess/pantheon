import React, { Component } from 'react';
import { 
    Breadcrumb, BreadcrumbItem, Level, LevelItem, List, ListItem,
    Text, TextContent, TextVariants } from '@patternfly/react-core';
import { Link } from "react-router-dom";

export interface IProps {
    productName: string
    // navigateBack: () => any
  }
  
  class ProductDetails extends Component<IProps> {

    public render() {
        return (  
            <React.Fragment>
                <div>
                    <Breadcrumb>
                        <BreadcrumbItem to='/'>All Products</BreadcrumbItem>
                        <BreadcrumbItem to="#" isActive={true}>
                            {this.props.productName}
                        </BreadcrumbItem>
                    </Breadcrumb>
                </div>
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
                    <List>
                        <ListItem>Version 1</ListItem>
                        <ListItem>Version 2</ListItem>
                        <ListItem>Version 3</ListItem>
                    </List>
                </div>
            </React.Fragment>
        );
    }
    
}

export { ProductDetails }