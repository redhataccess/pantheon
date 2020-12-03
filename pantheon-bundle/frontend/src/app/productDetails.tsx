import React, { useState, useEffect, useContext } from "react"
import {
    ActionGroup, Alert, AlertActionCloseButton, Breadcrumb, BreadcrumbItem, Button, Form, FormGroup, Level, LevelItem, List, ListItem,
    Text, TextContent, TextVariants, TextInput, InputGroup
} from "@patternfly/react-core"
import { Fields, JcrTypes, SlingTypes } from "@app/Constants"
import { ProductContext, IProduct } from "@app/contexts/ProductContext"


export const ProductDetails = (props: any) => {
    const allProducts = useContext(ProductContext);

    const [failedPost, setFailedPost] = useState(false)
    const [isMissingFields, setIsMissingFields] = useState(false)
    const [urlFragment, setUrlFragment] = useState("")
    const [isUrlFragmentValid, setIsUrlFragmentValid] = useState(true)
    const [newVersion, setNewVersion] = useState("")
    const [allVersionNames, setAllVersionNames] = useState([] as string[])
    const [currentProduct, setCurrentProduct] = useState(undefined as IProduct | undefined)

    useEffect(() => {
        if (allProducts) {
            const currentProduct = allProducts.find(a => a["jcr:uuid"] === props.match.params.id)
            setCurrentProduct(currentProduct)
            fetchProductVersions(currentProduct)
        }
    }, [allProducts])

    const fetchProductVersions = (currentProduct) => {
        const urlFragment = currentProduct && currentProduct.name.toLowerCase().replace(/[^A-Z0-9]+/ig, "_")
        const path = "/content/products/" + urlFragment + "/versions.2.json"
        let versionNames: string[] = []
        fetch(path).then((response) => {
            if (response.ok) {
                return response.json()
            } else if (response.status === 404) {
                // create versions path
                createVersionsPath(urlFragment)
                return versionNames
            } else {
                throw new Error(response.statusText)
            }
        }).then(json => {
            for (let i = 0; i < Object.keys(json).length; i++) {
                const key = Object.keys(json)[i]
                if (key !== Fields.JCR_PRIMARYTYPE && json[key].name !== undefined) {
                    versionNames.push(json[key].name)
                }
            }
            setAllVersionNames(versionNames)
        })
            .catch((error) => {
                console.log(error)
            })
    }

    const createVersionsPath = (urlFragment) => {
        const formData = new FormData()
        fetch(encodeURI("/content/products/" + urlFragment + "/versions"), {
            body: formData,
            method: "post",
        }).then(response => {
            if (response.status === 200 || response.status === 201) {
                console.log(" Created versions path " + response.status)
            } else {
                console.log(" Created versions path failed!")
            }
        })
    }

    const dismissNotification = () => {
        setFailedPost(false)
        setIsMissingFields(false)
    }

    const handleTextInputChange = newVersion => {
        setNewVersion(newVersion)
    }

    const handleUrlInputChange = urlFragment => {
        if (/^[-.\w]+$/.test(urlFragment)) {
            setUrlFragment(urlFragment)
            setIsUrlFragmentValid(true)
        } else {
            setIsUrlFragmentValid(false)
        }
    }

    const saveVersion = () => {
        if (newVersion === "" || urlFragment === "") {
            setIsMissingFields(true)
        } else {
            // creating formData obj and adding form's data to it
            const formData = new FormData()
            formData.append(Fields.NAME, newVersion)
            formData.append(Fields.SLING_RESOURCETYPE, SlingTypes.PRODUCT_VERSION)
            formData.append(Fields.JCR_PRIMARYTYPE, JcrTypes.PRODUCT_VERSION)
            formData.append(Fields.URL_FRAGMENT, urlFragment)

            // creating url-friendly version of product name and product version
            const productUrlFragment = currentProduct && currentProduct.name.toLowerCase().replace(/[^A-Z0-9]+/ig, "_")
            const encodedVersion = newVersion.toString().toLowerCase().replace(/[^A-Z0-9]+/ig, "_")
            // sending formData to backend
            fetch(encodeURI("/content/products/" + productUrlFragment + "/versions/" + encodedVersion), {
                body: formData,
                method: "post",
            }).then(response => {
                if (response.status === 200 || response.status === 201) {
                    setNewVersion("")
                    setUrlFragment("")
                    currentProduct && fetchProductVersions(currentProduct)
                } else {
                    setFailedPost(true)
                    setIsMissingFields(false)
                    console.log("Version adding failure")
                }
            })
        }
    }

    return (
        <React.Fragment>
            <div className="app-container">
                <Breadcrumb>
                    <BreadcrumbItem ><a href="#/products">All Products</a></BreadcrumbItem>
                    <BreadcrumbItem to="#" isActive={true}>Product Details</BreadcrumbItem>
                </Breadcrumb>
            </div>
            {currentProduct && <div className="app-container">
                <Level>
                    <LevelItem>
                        <TextContent>
                            <Text component={TextVariants.h1}>{currentProduct.name}{"  "}</Text>
                        </TextContent>
                    </LevelItem>
                    <LevelItem />
                </Level>
            </div>}
            {allVersionNames.length !== 0 && (
                <div className="app-container">
                    Versions:
                    <List>
                        {allVersionNames.map((version) =>
                            <ListItem key={version}>{version}</ListItem>
                        )}
                    </List>
                </div>)}
            <br />
            <div>
                <Form>
                    <div className="pant-form-width-md">
                        {isMissingFields &&
                            <div className="notification-container">
                                <Alert variant="warning"
                                    title="Fields indicated by * are mandatory"
                                    actionClose={<AlertActionCloseButton onClose={dismissNotification} />}
                                />
                            </div>
                        }
                        {!isUrlFragmentValid &&
                            <div className="notification-container">
                                <Alert variant="warning"
                                    title="Allowed input for ulrFragment: alphanumeric, hyphen, period and underscore"
                                    actionClose={<AlertActionCloseButton onClose={dismissNotification} />}
                                />
                            </div>
                        }
                        <FormGroup
                            label="New Version:"
                            isRequired={true}
                            fieldId="new_version_name"
                        >
                            <TextInput id="new_version_name_text" type="text" placeholder="New version name" onChange={handleTextInputChange} value={newVersion} />
                        </FormGroup>
                        <br />
                        <FormGroup
                            label=" URL Fragment:"
                            isRequired={true}
                            fieldId="url_fragment"
                        >
                            <TextInput id="url_fragment_text" type="text" placeholder="Url fragment" onChange={handleUrlInputChange} value={urlFragment} />
                        </FormGroup>
                        {failedPost &&
                            <div className="notification-container">
                                <Alert
                                    variant="danger"
                                    title="Failed to create product version."
                                    actionClose={<AlertActionCloseButton onClose={dismissNotification} />}
                                >
                                    Please check if you are logged in as a publisher.
                                    </Alert>
                            </div>
                        }
                        {allProducts && allProducts.length > 0 &&
                            <ActionGroup>
                                <Button aria-label="Creates a new Version Name." onClick={saveVersion}>Save</Button>
                            </ActionGroup>
                        }
                    </div>
                </Form>
            </div>
        </React.Fragment>
    )
}
