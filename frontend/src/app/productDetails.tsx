import React, { Component } from 'react';
import { 
    ActionGroup, Breadcrumb, BreadcrumbItem, Button, Card, CardBody,Form, FormGroup, Level, LevelItem, List, ListItem,
    Text, TextContent, TextVariants, TextInput } from '@patternfly/react-core';
import { ProductListing } from '@app/productListing';

export interface IProps {
    productName: string
    //productUrl: string
  }
  
  class ProductDetails extends Component<IProps> {

    public state = {
        allVersionNames: [],
        fetchProductDetails: true,
        newVersion: ''
    };

    public versionNames : string[] = [];

    public render() {
        //console.log('props state: ',this.props)
        return (  
            <React.Fragment>
                {this.state.fetchProductDetails && this.fetchProductDetails(this.state.allVersionNames)}
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
                                    <TextInput id="new_version_name_text" type="text" placeholder="New version name" onChange={this.handleTextInputChange} value={this.state.newVersion}/>
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
        // setup url fragment
        let url_fragment = this.props.productName.toString().toLowerCase().replace(/[^A-Z0-9]+/ig, "_");
        const path = '/content/products/'+ url_fragment +'/versions.2.json'
        let key;
        versionNames = []
        
        fetch(path)
        .then((response) => {
            if (response.ok) {
                return response.json();
            } else if(response.status == 404){
                // create versions path
                this.createVersionsPath;
                return versionNames;
            } else {
                throw new Error(response.statusText);
            }
        }) 
        .then(responseJSON => {
             for(let i=0; i < Object.keys(responseJSON).length;i++){    
                key = Object.keys(responseJSON)[i];
               
                if ((key !== 'jcr:primaryType')) {    
                  versionNames.push(responseJSON[key]["name"]);
                }
             }
             //console.log('versionNames: ',versionNames)
             this.setState({
                allVersionNames: versionNames,
                fetchProductDetails: false 
            })
        })
        .catch((error) => {
            console.log(error)
          });
          return versionNames;
    };

    private handleTextInputChange = newVersion => {
        this.setState({ newVersion });
    };

    private saveVersion = () => {
        const formData = new FormData();
        formData.append("name", this.state.newVersion)
        formData.append("sling:resourceType", "pantheon/productVersion")
        formData.append("jcr:primaryType", 'pant:productVersion')
        
        let url_fragment = this.props.productName.toString().toLowerCase().replace(/[^A-Z0-9]+/ig, "_");
        fetch('/content/products/'+ url_fragment +'/versions/'+this.state.newVersion, {
            body: formData,
            method: 'post',
          }).then(response => {
            if (response.status === 200 || response.status === 201) {
              this.setState({ fetchProductDetails: true, newVersion: '' })
              //console.log("endpoint=> /content/"+url_fragment+"/versions/"+this.state.newVersion)
            } else{
                console.log('Version adding failure')
            }
          });

    };

    private createVersionsPath = () => {
        const formData = new FormData();
        let url_fragment = this.props.productName.toString().toLowerCase().replace(/[^A-Z0-9]+/ig, "_");
        fetch('/content/'+ url_fragment +'/versions', {
            body: formData,
            method: 'post',
          }).then(response => {
              if (response.status === 200 || response.status === 201) {
                console.log(" Created versions path " + response.status)
          } else{
              console.log(' Created versions path  failed!')
    
          }
        });
    }

}

export { ProductDetails }