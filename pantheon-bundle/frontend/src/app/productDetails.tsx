import React, { Component } from 'react'
import {
    ActionGroup, Alert, AlertActionCloseButton, Breadcrumb, BreadcrumbItem, Button, Form, FormGroup, Level, LevelItem, List, ListItem,
    Text, TextContent, TextVariants, TextInput, InputGroup
} from '@patternfly/react-core'
import { Fields, JcrTypes, SlingTypes } from '@app/Constants'
import { tsObjectKeyword } from '@babel/types';

export interface IProps {
    productName: string
}

interface IState {
    allVersionNames: any[]
    failedPost: boolean
    isMissingFields: boolean
    isUrlFragmentValid: boolean
    newVersion: string
    urlFragment: string
}

class ProductDetails extends Component<IProps, IState> {
    public versionNames: string[] = []

    constructor(props) {
        super(props)
        this.state = {
            allVersionNames: [],
            failedPost: false,
            isMissingFields: false,
            isUrlFragmentValid: true,
            newVersion: '',
            urlFragment: '',
        }
    }

    public componentWillReceiveProps(nextProps) {
        // allow page load from productDetails to products listing
        if (nextProps.productName !== undefined && nextProps.productName.trim() !== '') {
            return window.location.reload(false)
        }
    }

    public componentDidMount() {
        this.fetchProductDetails(this.state.allVersionNames)
    }

    public render() {
        return (
            <React.Fragment>
                <div className="app-container">
                    <Breadcrumb>
                        <BreadcrumbItem ><a href="#/products" onClick={() => window.location.reload(false)}>All Products</a></BreadcrumbItem>
                        <BreadcrumbItem to="#" isActive={true}>Product Details</BreadcrumbItem>
                    </Breadcrumb>
                </div>
                <div className="app-container">
                    <Level>
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
                <br />
                <div>
                    <Form>
                        <div className="pant-form-width-md">
                            {this.state.isMissingFields &&
                                <div className="notification-container">
                                    <Alert variant="warning"
                                        title="Fields indicated by * are mandatory"
                                        actionClose={<AlertActionCloseButton onClose={this.dismissNotification} />}
                                    />
                                </div>
                            }
                            {!this.state.isUrlFragmentValid &&
                                <div className="notification-container">
                                    <Alert variant="warning"
                                        title="Allowed input for ulrFragment: alphanumeric, hyphen, period and underscore"
                                        actionClose={<AlertActionCloseButton onClose={this.dismissNotification} />}
                                    />
                                </div>
                            }
                            <FormGroup
                                label="New Version:"
                                isRequired={true}
                                fieldId="new_version_name"
                            >
                                <TextInput id="new_version_name_text" type="text" placeholder="New version name" onChange={this.handleTextInputChange} value={this.state.newVersion} />
                            </FormGroup>
                            <br />
                            <FormGroup
                                label=" URL Fragment:"
                                isRequired={true}
                                fieldId="url_fragment"
                            >
                                <TextInput id="url_fragment_text" type="text" placeholder="Url fragment" onChange={this.handleUrlInputChange} value={this.state.urlFragment} />
                            </FormGroup>
                            {this.state.failedPost &&
                                <div className="notification-container">
                                    <Alert
                                        variant="danger"
                                        title="Failed to create product version."
                                        actionClose={<AlertActionCloseButton onClose={this.dismissNotification} />}
                                    >
                                        Please check if you are logged in as a publisher.
                                    </Alert>
                                </div>
                            }
                            <ActionGroup>
                                <Button aria-label="Creates a new Version Name." onClick={this.saveVersion}>Save</Button>
                            </ActionGroup>
                        </div>
                    </Form>
                </div>
            </React.Fragment>
        )
    }

    private dismissNotification = () => {
        this.setState({ failedPost: false, isMissingFields: false })
    }

    private fetchProductDetails = (versionNames) => {
        // setup url fragment
        const urlFragment = this.props.productName.toString().toLowerCase().replace(/[^A-Z0-9]+/ig, "_")
        const path = '/content/products/' + urlFragment + '/versions.2.json'
        let key
        versionNames = []

        fetch(path)
            .then((response) => {
                if (response.ok) {
                    return response.json()
                } else if (response.status === 404) {
                    // create versions path
                    this.createVersionsPath()
                    return versionNames
                } else {
                    throw new Error(response.statusText)
                }
            })
            .then(responseJSON => {
                // tslint:disable-next-line: prefer-for-of
                for (let i = 0; i < Object.keys(responseJSON).length; i++) {
                    key = Object.keys(responseJSON)[i]
                    if ((key !== Fields.JCR_PRIMARYTYPE)) {
                        if (responseJSON[key].name !== undefined) {
                            versionNames.push(responseJSON[key].name)
                        }
                    }
                }
                this.setState({
                    allVersionNames: versionNames
                })
            })
            .catch((error) => {
                console.log(error)
            })
        return versionNames
    }

    private handleTextInputChange = newVersion => {
        this.setState({ newVersion })
    }

    private handleUrlInputChange = urlFragment => {
        if (/^[-.\w]+$/.test(urlFragment)) {
            this.setState({ urlFragment, isUrlFragmentValid: true })
        } else {
            this.setState({ isUrlFragmentValid: false})
        }
    }

    private saveVersion = () => {
        if (this.state.newVersion === '' || this.state.urlFragment === '') {
            this.setState({ isMissingFields: true })
        } else {
            const formData = new FormData()
            formData.append(Fields.NAME, this.state.newVersion)
            formData.append(Fields.SLING_RESOURCETYPE, SlingTypes.PRODUCT_VERSION)
            formData.append(Fields.JCR_PRIMARYTYPE, JcrTypes.PRODUCT_VERSION)
            formData.append(Fields.URL_FRAGMENT, this.state.urlFragment)

            const productUrlFragment = this.props.productName.toString().toLowerCase().replace(/[^A-Z0-9]+/ig, "_")
            const encodedVersion = this.state.newVersion.toString().toLowerCase().replace(/[^A-Z0-9]+/ig, "_")
            fetch(encodeURI('/content/products/' + productUrlFragment + '/versions/' + encodedVersion), {
                body: formData,
                method: 'post',
            }).then(response => {
                if (response.status === 200 || response.status === 201) {
                    this.setState({ newVersion: '', urlFragment: '' })
                    this.fetchProductDetails(this.state.allVersionNames)
                } else {
                    this.setState({ failedPost: true, isMissingFields: false })
                    console.log('Version adding failure')
                }
            })
        }

    }

    private createVersionsPath = () => {
        const formData = new FormData()
        const urlFragment = this.props.productName.toString().toLowerCase().replace(/[^A-Z0-9]+/ig, "_")
        fetch(encodeURI('/content/products/' + urlFragment + '/versions'), {
            body: formData,
            method: 'post',
        }).then(response => {
            if (response.status === 200 || response.status === 201) {
                console.log(" Created versions path " + response.status)
            } else {
                console.log(' Created versions path  failed!')

            }
        })
    }

}

export { ProductDetails }
