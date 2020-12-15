import React, { Component } from "react"
import { Bullseye, Button, Alert, AlertActionCloseButton, Form, FormGroup, TextInput, ActionGroup } from "@patternfly/react-core"
import "@app/app.css"
import { Redirect } from "react-router-dom"
import { Fields, JcrTypes, SlingTypes } from "./Constants"

interface IState {
    failedPost: boolean
    isDup: boolean
    isMissingFields: boolean
    isUrlFragmentValid: boolean
    productDescription: string
    productName: string
    productUrlFragment: string
    versionName: string
    versionUrlFragment: string
    redirect: boolean
}

class Product extends Component<any, IState> {
    constructor(props) {
        super(props)
        this.state = {
            failedPost: false,
            isDup: false,
            isMissingFields: false,
            isUrlFragmentValid: true,
            productDescription: "",
            productName: "",
            productUrlFragment: "",
            versionName: "",
            versionUrlFragment: "",
            redirect: false
        }

    }
    
    // render method transforms the react components into DOM nodes for the browser.
    public render() {
        return (
            <React.Fragment>
                    <Form className='p2-product__form'>
                        <div className="app-container">
                            <div>
                                {this.state.isMissingFields &&
                                    <div className="notification-container">
                                        <Alert  variant="warning"
                                                title="Fields indicated by * are mandatory"
                                                actionClose={<AlertActionCloseButton onClose={this.dismissNotification} />}
                                        />
                                    </div>
                                }
                                {this.state.isDup &&
                                    <div className="notification-container">
                                        <Alert  variant="warning"
                                                title="Duplicated Product name."
                                                actionClose={<AlertActionCloseButton onClose={this.dismissNotification} />}
                                        />
                                    </div>
                                }
                                {!this.state.isUrlFragmentValid &&
                                <div className="notification-container">
                                    <Alert variant="warning"
                                        title="Allowed input for Product ulrFragment: alphanumeric, hyphen, period and underscore"
                                        actionClose={<AlertActionCloseButton onClose={this.dismissNotification} />}
                                    />
                                </div>
                                }
                                {this.state.failedPost &&
                                    <div className="notification-container">
                                        <Alert  variant="danger"
                                                title="Failed to create product."
                                                actionClose={<AlertActionCloseButton onClose={this.dismissNotification} />}
                                        >
                                            Please check if you are logged in as a publisher.
                                        </Alert>
                                    </div>
                                }
                                <FormGroup
                                        label="Product Name"
                                        isRequired={true}
                                        fieldId="product_name"
                                        >
                                    <TextInput isRequired={true} id="product_name_text" type="text" placeholder="Product Name" value={this.state.productName} onChange={this.handleNameInput} />
                                </FormGroup>
                                <br />
                                <FormGroup
                                        label="Product URL Fragment"
                                        isRequired={true}
                                        fieldId="product_url_fragment" >
                                    <TextInput isRequired={true} id="product_url_fragment_text" type="text" placeholder="URL Fragment" value={this.state.productUrlFragment} onChange={this.handleUrlInput} />
                                </FormGroup>
                                <br />
                                <FormGroup
                                        label="Product Description"
                                        fieldId="product_description" >
                                    <TextInput id="product_description_text" type="text" placeholder="Product Description" value={this.state.productDescription} onChange={this.handleProductInput} />
                                </FormGroup>
                                <br />
                                <FormGroup
                                        label="Product Version:"
                                        isRequired={true}
                                        fieldId="version_name"
                                >
                                    <TextInput id="new_version_name_text" type="text" placeholder="Product Version" onChange={this.handleTextInputChange} value={this.state.versionName} />
                                </FormGroup>
                                <br />
                                <FormGroup
                                        label="Version URL Fragment:"
                                        isRequired={true}
                                        fieldId="version_url_fragment"
                                >
                                    <TextInput id="new_version_url_fragment" type="text" placeholder="Version URL Fragment" onChange={this.handleUrlInputChange} value={this.state.versionUrlFragment} />
                                </FormGroup>
                                <br />
                                <ActionGroup>
                                    <Button type='submit' aria-label="Creates a new Product Name with Description specified." onClick={e => this.saveProduct(e)}>Save</Button>
                                    <div>
                                        {this.renderRedirect()}
                                    </div>
                                </ActionGroup>
                            </div>
                        </div>
                    </Form>
            </React.Fragment>
        )
    }
    // methods that handle the state changes.
    private handleNameInput = productName => {
        this.setState({ productName })
    }

    private handleProductInput = productDescription => {
        this.setState({ productDescription })
        // console.log("Desc " + productDescription)
    }

    private handleUrlInput = productUrlFragment => {
        if (/^[-.\w]+$/.test(productUrlFragment)) {
            this.setState({ productUrlFragment, isUrlFragmentValid: true })
        } else {
            this.setState({ isUrlFragmentValid: false})
        }
    }

    private handleTextInputChange = versionName => {
        this.setState({ versionName })
    }

    private handleUrlInputChange = versionUrlFragment => {
        if (/^[-.\w]+$/.test(versionUrlFragment)) {
            this.setState({ versionUrlFragment, isUrlFragmentValid: true })
        } else {
            this.setState({ isUrlFragmentValid: false})
        }
    }

    private saveVersion = () => {
        // confirming form state is valid
        if (this.state.versionName.trim() === "" || this.state.versionUrlFragment === "") {
            this.setState({ isMissingFields: true })
        } else {
            // creating formData obj and adding form's data to it
            const formData = new FormData()
            formData.append(Fields.NAME, this.state.versionName)
            formData.append(Fields.SLING_RESOURCETYPE, SlingTypes.PRODUCT_VERSION)
            formData.append(Fields.JCR_PRIMARYTYPE, JcrTypes.PRODUCT_VERSION)
            formData.append(Fields.URL_FRAGMENT, this.state.versionUrlFragment)
            
            // creating url-friendly version of product name and product version
            const productUrlFragment = this.state.productName.toString().toLowerCase().replace(/[^A-Z0-9]+/ig, "_")
            const encodedVersion = this.state.versionName.toString().toLowerCase().replace(/[^A-Z0-9]+/ig, "_")
            // sending formData to backend
            fetch(encodeURI("/content/products/" + productUrlFragment + "/versions/" + encodedVersion), {
                body: formData,
                method: "post",
            }).then(response => {
                console.log('response', response)
                if (response.status === 200 || response.status === 201) {
                    this.setState({ versionName: "", productUrlFragment: "" })
                } else {
                    this.setState({ failedPost: true, isMissingFields: false })
                }
            })
        }

    }

    private saveProduct = (e) => {
        e.preventDefault()
        if (this.state.productName === ""|| this.state.productUrlFragment === "" || this.state.versionName === "" || this.state.versionUrlFragment === "") {
            this.setState({ isMissingFields: true })
        } else {
            this.productExist().then(exist => {
                if (!exist) {
                    const hdrs = {
                        "Accept": "application/json",
                        "cache-control": "no-cache"
                    }
                    // setup url fragment
                    const urlFragment = this.state.productName.toString().toLowerCase().replace(/[^A-Z0-9]+/ig, "_")
                    const formData = new FormData()
                    formData.append("name", this.state.productName)
                    formData.append("description", this.state.productDescription)
                    formData.append("sling:resourceType", "pantheon/product")
                    formData.append("jcr:primaryType", "pant:product")
                    // currently we don"t translate products in Customer Portal.
                    formData.append("locale", "en-US")
                    formData.append(Fields.URL_FRAGMENT, this.state.productUrlFragment)
                    // fetch makes the request to create a new product.
                    // transfor productName to lower case and replace special chars with _.
                    fetch(encodeURI("/content/products/" + urlFragment), {
                        body: formData,
                        headers: hdrs,
                        method: "post"
                    }).then(response => {
                        if (response.status === 201 || response.status === 200) {
                            //saveVersion makes another fetch call to create the product version
                            this.saveVersion();
                            this.setState({ redirect: true })
                        } else {
                            this.setState({ failedPost: true })
                        }
                    })
                }
            }
            )
        }
    }

    private renderRedirect = () => {
        if (this.state.redirect) {
            return <Redirect to="/products" />
        } else {
            return ""
        }
    }

    private dismissNotification = () => {
        this.setState({
            failedPost: false,
            isDup: false,
            isMissingFields: false
        })
    }

    private productExist = () => {
        let exists = false
        const backend = "/content/products/" + this.state.productName.toString().toLowerCase().replace(/[^A-Z0-9]+/ig, "_") + ".json"
        return fetch(backend)
            .then(response => {
                if (response.status === 200) {
                    exists = true
                    this.setState({ isDup: true })
                }
            }).then(() => exists)
    }
}

export { Product }
