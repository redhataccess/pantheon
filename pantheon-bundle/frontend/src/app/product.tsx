import React, { useState } from "react"
import { Button, Alert, AlertActionCloseButton, Form, FormGroup, TextInput, ActionGroup } from "@patternfly/react-core"
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

export default function Product(props: any) {

    const [failedPost, setFailedPost] = useState(false)
    const [isDup, setIsDup] = useState(false)
    const [isMissingFields, setIsMissingFields] = useState(false)
    const [isUrlFragmentValid, setIsUrlFragmentValid] = useState(true)
    const [productDescription, setProductDescription] = useState("")
    const [productName, setProductName] = useState("")
    const [productUrlFragment, setProductUrlFragment] = useState("")
    const [versionName, setVersionName] = useState("")
    const [versionUrlFragment, setVersionUrlFragment] = useState("")
    const [redirect, setRedirect] = useState(false)

    // methods that handle the state changes.
    const handleNameInput = productName => {
        setProductName(productName)
    }

    const handleProductInput = productDescription => {
        setProductDescription(productDescription)
    }

    const handleUrlInput = productUrlFragment => {
        if (/^[-.\w]+$/.test(productUrlFragment)) {
            setProductUrlFragment(productUrlFragment)
            setIsUrlFragmentValid(true)
        } else {
            setIsUrlFragmentValid(false)
        }
    }

    const handleTextInputChange = versionName => {
        setVersionName(versionName)
    }

    const handleUrlInputChange = versionUrlFragment => {
        if (/^[-.\w]+$/.test(versionUrlFragment)) {
            setVersionUrlFragment(versionUrlFragment)
            setIsUrlFragmentValid(true)
        } else {
            setIsUrlFragmentValid(false)
        }
    }

    const saveVersion = () => {
        // confirming form state is valid
        if (versionName.trim() === "" || versionUrlFragment === "") {
            setIsMissingFields(true)
        } else {
            // creating formData obj and adding form's data to it
            const formData = new FormData()
            formData.append(Fields.NAME, versionName)
            formData.append(Fields.SLING_RESOURCETYPE, SlingTypes.PRODUCT_VERSION)
            formData.append(Fields.JCR_PRIMARYTYPE, JcrTypes.PRODUCT_VERSION)
            formData.append(Fields.URL_FRAGMENT, versionUrlFragment)

            // creating url-friendly version of product name and product version
            const productUrlFragment = productName.toString().toLowerCase().replace(/[^A-Z0-9]+/ig, "_")
            const encodedVersion = versionName.toString().toLowerCase().replace(/[^A-Z0-9]+/ig, "_")
            // sending formData to backend
            fetch(encodeURI("/content/products/" + productUrlFragment + "/versions/" + encodedVersion), {
                body: formData,
                method: "post",
            }).then(response => {
                console.log('response', response)
                if (response.status === 200 || response.status === 201) {
                    setVersionName("")
                    setProductUrlFragment("")
                } else {
                    setFailedPost(true)
                    setIsMissingFields(false)
                }
            })
        }

    }

    const saveProduct = (e) => {
        e.preventDefault()
        if (productName === "" || productUrlFragment === "" || versionName === "" || versionUrlFragment === "") {
            setIsMissingFields(true)
        } else {
            productExist().then(exist => {
                if (!exist) {
                    const hdrs = {
                        "Accept": "application/json",
                        "cache-control": "no-cache"
                    }
                    // setup url fragment
                    const urlFragment = productName.toString().toLowerCase().replace(/[^A-Z0-9]+/ig, "_")
                    const formData = new FormData()
                    formData.append("name", productName)
                    formData.append("description", productDescription)
                    formData.append("sling:resourceType", "pantheon/product")
                    formData.append("jcr:primaryType", "pant:product")
                    // currently we don"t translate products in Customer Portal.
                    formData.append("locale", "en-US")
                    formData.append(Fields.URL_FRAGMENT, productUrlFragment)
                    // fetch makes the request to create a new product.
                    // transfor productName to lower case and replace special chars with _.
                    fetch(encodeURI("/content/products/" + urlFragment), {
                        body: formData,
                        headers: hdrs,
                        method: "post"
                    }).then(response => {
                        if (response.status === 201 || response.status === 200) {
                            //saveVersion makes another fetch call to create the product version
                            saveVersion();
                            setRedirect(true)
                        } else {
                            setFailedPost(true)
                        }
                    })
                }
            }
            )
        }
    }

    const renderRedirect = () => {
        if (redirect) {
            return <Redirect to="/products" />
        } else {
            return ""
        }
    }

    const dismissNotification = () => {
        setFailedPost(false)
        setIsDup(false)
        setIsMissingFields(false)
    }

    const productExist = () => {
        let exists = false
        const backend = "/content/products/" + productName.toString().toLowerCase().replace(/[^A-Z0-9]+/ig, "_") + ".json"
        return fetch(backend)
            .then(response => {
                if (response.status === 200) {
                    exists = true
                    setIsDup(true)
                }
            }).then(() => exists)
    }

    return (
        <React.Fragment>
            <Form className='p2-product__form'>
                <div className="app-container">
                    <div>
                        {isMissingFields &&
                            <div className="notification-container">
                                <Alert variant="warning"
                                    title="Fields indicated by * are mandatory"
                                    actionClose={<AlertActionCloseButton onClose={dismissNotification} />}
                                />
                            </div>
                        }
                        {isDup &&
                            <div className="notification-container">
                                <Alert variant="warning"
                                    title="Duplicated Product name."
                                    actionClose={<AlertActionCloseButton onClose={dismissNotification} />}
                                />
                            </div>
                        }
                        {!isUrlFragmentValid &&
                            <div className="notification-container">
                                <Alert variant="warning"
                                    title="Allowed input for Product ulrFragment: alphanumeric, hyphen, period and underscore"
                                    actionClose={<AlertActionCloseButton onClose={dismissNotification} />}
                                />
                            </div>
                        }
                        {failedPost &&
                            <div className="notification-container">
                                <Alert variant="danger"
                                    title="Failed to create product."
                                    actionClose={<AlertActionCloseButton onClose={dismissNotification} />}
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
                            <TextInput isRequired={true} id="product_name_text" type="text" placeholder="Product Name" value={productName} onChange={handleNameInput} />
                        </FormGroup>
                        <br />
                        <FormGroup
                            label="Product URL Fragment"
                            isRequired={true}
                            fieldId="product_url_fragment" >
                            <TextInput isRequired={true} id="product_url_fragment_text" type="text" placeholder="URL Fragment" value={productUrlFragment} onChange={handleUrlInput} />
                        </FormGroup>
                        <br />
                        <FormGroup
                            label="Product Description"
                            fieldId="product_description" >
                            <TextInput id="product_description_text" type="text" placeholder="Product Description" value={productDescription} onChange={handleProductInput} />
                        </FormGroup>
                        <br />
                        <FormGroup
                            label="Product Version:"
                            isRequired={true}
                            fieldId="version_name"
                        >
                            <TextInput id="new_version_name_text" type="text" placeholder="Product Version" onChange={handleTextInputChange} value={versionName} />
                        </FormGroup>
                        <br />
                        <FormGroup
                            label="Version URL Fragment:"
                            isRequired={true}
                            fieldId="version_url_fragment"
                        >
                            <TextInput id="new_version_url_fragment" type="text" placeholder="Version URL Fragment" onChange={handleUrlInputChange} value={versionUrlFragment} />
                        </FormGroup>
                        <br />
                        <ActionGroup>
                            <Button type='submit' id='form-submit-button' aria-label="Creates a new Product Name with Description specified." onClick={e => saveProduct(e)}>Save</Button>
                            <div>
                                {renderRedirect()}
                            </div>
                        </ActionGroup>
                    </div>
                </div>
            </Form>
        </React.Fragment>
    )
}

export { Product }
