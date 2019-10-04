import React, { Component } from 'react';
import {
    ActionGroup, Breadcrumb, BreadcrumbItem, Button, Form, FormGroup, Level, LevelItem, List, ListItem,
    Text, TextContent, TextVariants, TextInput
} from '@patternfly/react-core';

export interface IProps {
    productName: string
}

class ProductDetails extends Component<IProps, any> {
    public versionNames: string[] = [];

    constructor(props) {
        super(props);
        this.state = {
            allVersionNames: [],
            newVersion: ''
        };
    }

    public render() {
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
                                <ListItem key={version}>{version}</ListItem>
                            )}
                        </List>
                    </div>)}
                <div className="app-container" />
                <div className="app-container">
                    <Form>
                        <div className="app-container">
                            <FormGroup
                                label="New Version:"
                                fieldId="new_version_name"
                            >
                                <TextInput id="new_version_name_text" type="text" placeholder="New version name" onChange={this.handleTextInputChange} value={this.state.newVersion} />
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
        const urlFragment = this.props.productName.toString().toLowerCase().replace(/[^A-Z0-9]+/ig, "_");
        const path = '/content/products/' + urlFragment + '/versions.2.json'
        let key;
        versionNames = []

        fetch(path)
            .then((response) => {
                if (response.ok) {
                    return response.json();
                } else if (response.status === 404) {
                    // create versions path
                    this.createVersionsPath();
                    return versionNames;
                } else {
                    throw new Error(response.statusText);
                }
            })
            .then(responseJSON => {
                // tslint:disable-next-line: prefer-for-of
                for (let i = 0; i < Object.keys(responseJSON).length; i++) {
                    key = Object.keys(responseJSON)[i];
                    if ((key !== 'jcr:primaryType')) {
                        if (responseJSON[key].name !== undefined) {
                            versionNames.push(responseJSON[key].name);
                        }
                    }
                }
                this.setState({
                    allVersionNames: versionNames
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

        const urlFragment = this.props.productName.toString().toLowerCase().replace(/[^A-Z0-9]+/ig, "_");
        const encodedVersion = this.state.newVersion.toString().toLowerCase().replace(/[^A-Z0-9]+/ig, "_");
        fetch(encodeURI('/content/products/' + urlFragment + '/versions/' + encodedVersion), {
            body: formData,
            method: 'post',
        }).then(response => {
            if (response.status === 200 || response.status === 201) {
                this.setState({ fetchProductDetails: true, newVersion: '' })
            } else {
                console.log('Version adding failure')
            }
        });

    };

    private createVersionsPath = () => {
        const formData = new FormData();
        const urlFragment = this.props.productName.toString().toLowerCase().replace(/[^A-Z0-9]+/ig, "_");
        fetch(encodeURI('/content/products/' + urlFragment + '/versions'), {
            body: formData,
            method: 'post',
        }).then(response => {
            if (response.status === 200 || response.status === 201) {
                console.log(" Created versions path " + response.status)
            } else {
                console.log(' Created versions path  failed!')

            }
        });
    }

}

export { ProductDetails }