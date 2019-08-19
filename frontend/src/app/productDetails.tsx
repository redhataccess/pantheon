import React, { Component } from 'react';
import { 
    ActionGroup, Breadcrumb, BreadcrumbItem, Button, Card, CardBody,Form, FormGroup, Level, LevelItem, List, ListItem,
    Text, TextContent, TextVariants, TextInput } from '@patternfly/react-core';
import { ProductListing } from '@app/productListing';

export interface IProps {
    productName: string
  }
  
  class ProductDetails extends Component<IProps> {

    public state = {
        allVersionNames: [],
        fetchProductDetails: true,
        newVersion: ''
    };

    public versionNames : string[] = [];

    public render() {
        console.log('props state: ',this.props)
        return (  
            <React.Fragment>
                {this.state.fetchProductDetails && this.fetchProductDetails(this.state.allVersionNames)}
                {console.log('pd:',this.state.fetchProductDetails)}
                <div className="app-container">
                    <Breadcrumb>
                        <BreadcrumbItem>All Products</BreadcrumbItem>
                        <BreadcrumbItem>Product Details</BreadcrumbItem>
                    </Breadcrumb>
                </div>
                <div className="app-container">
                    <Level gutter="md">
                        <LevelItem>
                                <TextContent>
                                    <Text component={TextVariants.h1}>{this.props.productName}{'  '}</Text>  
                                </TextContent>
                        </LevelItem>
                        <LevelItem />
                    </Level>                
                </div>
                {this.state.allVersionNames.length !== 0 && (
                <div className="app-container">
                    Versions:
                    <List>
                        {this.state.allVersionNames.map((version) =>
                            <ListItem>{version}</ListItem>
                        )}
                    </List>
                </div>)}
                <div className="app-container"></div>
                <div className="app-container">
                        <Form>
                            <div className="app-container">
                                <FormGroup
                                    label="New Version:"
                                    fieldId="new_version_name"
                                >
                                    <TextInput id="new_version_name_text" type="text" placeholder="New version name" onChange={this.handleTextInputChange} />
                                </FormGroup>
                                <ActionGroup>
                                    <Button aria-label="Creates a new Version Name." onClick={this.saveVersion}>Save</Button>
                                </ActionGroup>
                            </div>
                        </Form>
                </div>
            </React.Fragment>
        );
    }
 
    private fetchProductDetails = (versionNames) => {
        const path = '/content/products/'+this.props.productName+'/versions.2.json'
        let key;
        console.log('path:',path)
        fetch(path)
        .then(response => response.json())
        .then(responseJSON => {
            console.log('responseJson:',responseJSON)
             for(let i=Object.keys(responseJSON).length-1; i>2;i--){
                key = Object.keys(responseJSON)[i];
                console.log('key:',key)
                versionNames.push(responseJSON[key]["name"]);
             }
             console.log('versionNames: ',versionNames)
             this.setState({
                allVersionNames: versionNames,
                fetchProductDetails: false 
            })
        })
    };

    private handleTextInputChange = newVersion => {
        this.setState({ newVersion });
      };

    private saveVersion = () => {
        const formData = new FormData();
        formData.append("name", this.state.newVersion)
        console.log('new version name: ',this.state.newVersion)
        fetch('/content/'+this.props.productName+'/versions/'+this.state.newVersion, {
            body: formData,
            method: 'post',
          }).then(response => {
            if (response.status === 200) {
              console.log(" Posted version " + response.status)
              this.setState({ fetchProductDetails: true })
            } else{
                console.log('Version adding failure')
            //   this.setState({ authMessage: "Unknown failure - HTTP " + response.status + ": " + response.statusText })
            }
          });

    }; 
}

export { ProductDetails }