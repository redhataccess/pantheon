import React, { Component } from "react"
import {
    Alert, AlertActionCloseButton,
    BaseSizes,
    Button,
    Card, CardHeader, CardBody, CardHeaderMain,
    Form, FormGroup, FormSelect, FormSelectOption, CardActions,
    Grid, GridItem,
    InputGroup, InputGroupText,
    Modal,
    Text, TextContent, TextInput, TextVariants, TextList, TextListItem, TextListVariants, TextListItemVariants,
    Title,
    Tooltip,
} from "@patternfly/react-core"

import CheckImage from "@app/images/check_image.jpg"
import BlankImage from "@app/images/blank.jpg"
import { Redirect } from "react-router-dom"
import { ExclamationTriangleIcon, TimesIcon, PlusCircleIcon } from "@patternfly/react-icons"
import { PantheonContentTypes, PathPrefixes } from "./Constants"

export interface IProps {
    contentType: string
    modulePath: string
    productInfo: string
    versionModulePath: string
    variant: string
    variantUUID: string
    attributesFilePath: string
    assemblies?: any
    updateDate: (draftUpdateDate, releaseUpdateDate, releaseVersion, variantUUID) => any
    onGetProduct: (productValue) => any
    onGetVersion: (versionValue) => any
    onPublishEvent: () => void
}

interface IState {
    alertTitle: string
    allProducts: any
    allProductVersions: any
    canChangePublishState: boolean
    documentsIncluded: Array<{ canonical_uuid: string, path: string, title: string }>
    isArchiveDropDownOpen: boolean
    isDropDownOpen: boolean
    isHeadingToggle: boolean
    isMissingFields: boolean
    isModalOpen: boolean
    keywords: string
    login: boolean
    metadataPath: string
    moduleUrl: string
    product: { label: string, value: string }
    productVersion: { label: string, uuid: string }
    publishAlertVisible: boolean
    unpublishAlertForModuleVisible: boolean
    results: any
    showMetadataAlertIcon: boolean
    successAlertVisible: boolean
    usecaseOptions: any
    usecaseValue: string
    assemblyData: [],
}

class Versions extends Component<IProps, IState> {
    private static USE_CASES = ["Select Use Case", "Administer", "Deploy", "Develop", "Install", "Migrate", "Monitor", "Network", "Plan", "Provision", "Release", "Troubleshoot", "Optimize"]

    public draft = [{ type: "draft", icon: BlankImage, path: "", version: "", publishedState: "Not published", updatedDate: "", firstButtonType: "primary", secondButtonType: "secondary", firstButtonText: "Publish", secondButtonText: "Preview", isDropdownOpen: false, isArchiveDropDownOpen: false, metadata: "" }]
    public release = [{ type: "release", icon: CheckImage, "path": "", version: "", publishedState: "Released", updatedDate: "", firstButtonType: "secondary", secondButtonType: "primary", firstButtonText: "Unpublish", secondButtonText: "View", isDropdownOpen: false, isArchiveDropDownOpen: false, metadata: "", draftUploadDate: "" }]

    constructor(props) {
        super(props)
        this.state = {
            alertTitle: "",
            allProducts: [],
            // tslint:disable-next-line: object-literal-sort-keys
            allProductVersions: [],
            canChangePublishState: true,
            documentsIncluded: [],
            isArchiveDropDownOpen: false,
            isDropDownOpen: false,
            isHeadingToggle: true,
            isMissingFields: false,
            isModalOpen: false,
            keywords: "",
            login: false,
            metadataPath: "",
            moduleUrl: "",
            product: { label: "", value: "" },
            productVersion: { label: "", uuid: "" },
            publishAlertVisible: false,
            unpublishAlertForModuleVisible: false,
            results: [this.draft, this.release],
            showMetadataAlertIcon: true,
            successAlertVisible: false,
            usecaseOptions: [
                { value: "", label: "Select Use Case", disabled: false }
            ],
            usecaseValue: "",
            assemblyData: [],
        }
    }

    public componentDidMount() {
        this.fetchProducts()
        this.fetchVersions()
        this.handlePublishButton()
    }

    public componentDidUpdate(prevProps) {
        if (this.props.modulePath !== prevProps.modulePath) {
            this.fetchVersions()
        }
    }

    public render() {

        const header = (
            <React.Fragment>
                <Title headingLevel="h1" size={BaseSizes["2xl"]}>
                    Edit Metadata
              </Title>
            </React.Fragment>
        )

        return (
            <React.Fragment>
                {this.state.successAlertVisible && <div className="notification-container pant-notification-container-md">
                    <Alert
                        variant="success"
                        title="Edit Metadata"
                        actionClose={<AlertActionCloseButton onClose={this.hideSuccessAlert} />}
                    >
                        Update Successful!
                    </Alert>
                </div>
                }

                {this.state.publishAlertVisible && <div className="notification-container pant-notification-container-md">
                    <Alert
                        variant="warning"
                        title={this.state.alertTitle}
                        actionClose={<AlertActionCloseButton onClose={this.hidePublishAlert} />}
                    >
                        {this.capitalize(this.props.contentType)} failed to publish. Check the following:
                        <ul>
                            <li>Are you logged in as a publisher?</li>
                            <li>Does the {this.props.contentType} have all required metadata?</li>
                        </ul>
                    </Alert>
                </div>
                }

                {this.props.contentType === PantheonContentTypes.ASSEMBLY && this.state.unpublishAlertForModuleVisible &&
                    <div className="notification-container pant-notification-container-md">
                        <Alert
                            variant="info"
                            title="Unpublishing assembly"
                            actionClose={<AlertActionCloseButton onClose={this.hideUppublishAlertForModule} />}
                        >
                            Included modules are not unpublished by this action.
                        </Alert>
                    </div>
                }

                <Grid hasGutter={true}>
                    {/* {console.log("[results]", this.state.results)} */}
                    {this.state.results.map((type, key1) => (
                        type.map((data, key2) => (
                            data.version !== "" && data.type === "draft" && (
                                <GridItem span={6}>
                                    <Card className="pf-m-light pf-site-background-medium pf-c-card-draft">
                                        <CardHeader>
                                            <CardHeaderMain><strong>Draft</strong></CardHeaderMain>
                                            <CardActions>{}</CardActions>
                                            {data.metadata !== undefined && !this.state.showMetadataAlertIcon &&
                                                <CardActions>
                                                    <Button variant="link" isInline={true} onClick={this.handleModalToggle}>Add metadata</Button>
                                                </CardActions>}
                                            {data.metadata !== undefined && this.state.showMetadataAlertIcon &&
                                                <CardActions><i className="pf-icon pf-icon-warning-triangle" />
                                                    <Button variant="link" isInline={true} onClick={this.handleModalToggle}>Add metadata</Button>
                                                </CardActions>}
                                            <CardActions><Button variant="link" isInline={true} onClick={() => this.previewDoc(data.secondButtonText)}>Preview</Button>
                                            </CardActions>
                                            {data.metadata !== undefined && !this.state.showMetadataAlertIcon &&
                                                <CardActions>
                                                    <Button variant="primary" isSmall={true} onClick={() => this.changePublishState(data.firstButtonText)}>{data.firstButtonText}</Button>
                                                </CardActions>
                                            }
                                            {data.metadata !== undefined && this.state.showMetadataAlertIcon &&
                                                <CardActions>
                                                    <Tooltip content="Add metadata to publish">
                                                        <Button isAriaDisabled={true} variant="primary" isSmall={true} onClick={() => this.changePublishState(data.firstButtonText)}>{data.firstButtonText}</Button>
                                                    </Tooltip>
                                                </CardActions>
                                            }
                                        </CardHeader>

                                        <CardBody>
                                            <TextContent>
                                                <Text><strong>Upload time</strong></Text>
                                                <Text component={TextVariants.p}>{data.updatedDate}</Text>
                                            </TextContent>
                                            <br />
                                            <TextContent>
                                                <Text><strong>Attribute file</strong></Text>
                                                <Text component={TextVariants.p}>{this.props.attributesFilePath}</Text>
                                            </TextContent>
                                            <br />
                                            <TextContent>
                                                {this.props.contentType === PantheonContentTypes.MODULE &&
                                                    <Text><strong>Assemblies</strong></Text>

                                                }

                                                {this.props.contentType === PantheonContentTypes.ASSEMBLY &&
                                                    <Text><strong>Modules</strong></Text>
                                                }
                                            </TextContent>
                                            <TextContent>
                                                {this.props.assemblies && this.props.assemblies.map(item => (
                                                    <TextList component={TextListVariants.ul}>
                                                        <TextListItem component={TextListItemVariants.li}>
                                                            <a href={"/pantheon/#/assembly" + item.path.substring("/content".length) + "?variant=" + this.props.variant}> {item.title}</a>
                                                        </TextListItem>
                                                    </TextList>))}
                                            </TextContent>
                                            <TextContent>
                                                {this.state.documentsIncluded.length > 0 && this.state.documentsIncluded.map((item) => (
                                                    <TextList component={TextListVariants.ul}>
                                                        <TextListItem component={TextListItemVariants.li}>
                                                            <a href={"/pantheon/#/module" + item.path.substring("/content".length) + "?variant=" + this.props.variant}> {item.title}</a>
                                                        </TextListItem>
                                                    </TextList>))}
                                            </TextContent>
                                        </CardBody>
                                    </Card>
                                </GridItem>)
                        ))
                    ))}
                    {this.state.results.map((type, key1) => (
                        type.map((data, key2) => (
                            data.version !== "" && data.type === "release" && (
                                <GridItem span={6}>
                                    <div className="pf-c-card pf-m-selectable pf-m-selected">
                                        <Card className="pf-m-selected">
                                            <CardHeader>
                                                <CardHeaderMain><strong><span id="span-source-type-version-published">Published</span></strong></CardHeaderMain>
                                                <CardActions>{}</CardActions>
                                                <CardActions>
                                                    <Button variant="link" isInline={true} onClick={this.handleModalToggle}>Add metadata</Button>
                                                </CardActions>
                                                <CardActions>
                                                    <Button variant="link" isInline={true} onClick={() => this.previewDoc(data.secondButtonText)}>Preview</Button>
                                                </CardActions>
                                                <CardActions>
                                                    <Button variant="secondary" isSmall={true} onClick={() => this.changePublishState(data.firstButtonText)}>{data.firstButtonText}</Button>
                                                </CardActions>
                                            </CardHeader>

                                            <CardBody>
                                                <TextContent>
                                                    <Text><strong>Upload time</strong></Text>
                                                    <Text component={TextVariants.p}>{data.draftUploadDate}</Text>
                                                </TextContent>
                                                <br />
                                                <TextContent>
                                                    <Text><strong>Attribute file</strong></Text>
                                                    <Text component={TextVariants.p}>{this.props.attributesFilePath}</Text>
                                                </TextContent>
                                                <br />
                                                <TextContent>
                                                    {this.props.contentType === PantheonContentTypes.MODULE &&
                                                        <Text><strong>Assemblies</strong></Text>
                                                    }
                                                    {this.props.contentType === PantheonContentTypes.ASSEMBLY &&
                                                        <Text><strong>Modules</strong></Text>
                                                    }

                                                </TextContent>
                                                <TextContent>
                                                    {this.props.assemblies && this.props.assemblies.map(item => (
                                                        <TextList component={TextListVariants.ul}>
                                                            <TextListItem component={TextListItemVariants.li}>
                                                                <a href={"/pantheon/#/assembly" + item.path.substring("/content".length) + "?variant=" + this.props.variant}> {item.title}</a>
                                                            </TextListItem>
                                                        </TextList>))
                                                    }
                                                </TextContent>
                                                <TextContent>
                                                    {this.state.documentsIncluded.length > 0 && this.state.documentsIncluded.map((item) => (
                                                        <TextList component={TextListVariants.ul}>
                                                            <TextListItem component={TextListItemVariants.li}>
                                                                <a href={"/pantheon/#/module" + item.path.substring("/content".length) + "?variant=" + this.props.variant}> {item.title}</a>
                                                            </TextListItem>
                                                        </TextList>
                                                    ))}
                                                </TextContent>
                                            </CardBody>

                                        </Card>
                                    </div>
                                </GridItem>)
                        ))
                    ))}
                </Grid>

                <Modal
                    width={"60%"}
                    title="Edit metadata"
                    isOpen={this.state.isModalOpen}
                    header={header}
                    aria-label="Edit metadata"
                    onClose={this.handleModalClose}
                    actions={[
                        <Button form="edit_metadata" key="confirm" variant="primary" onClick={this.saveMetadata}>
                            Save
          </Button>,
                        <Button key="cancel" variant="secondary" onClick={this.handleModalToggle}>
                            Cancel
            </Button>
                    ]}
                >

                    {this.state.isMissingFields && (
                        <div className="notification-container">
                            <Alert
                                variant="warning"
                                title="Fields indicated by * are mandatory"
                                actionClose={<AlertActionCloseButton onClose={this.dismissNotification} />}
                            />
                            <br />
                        </div>
                    )}
                    <Form isHorizontal={true} id="edit_metadata">
                        <FormGroup
                            label="Product Name"
                            isRequired={true}
                            fieldId="product-name"
                        >
                            <InputGroup>
                                <FormSelect value={this.state.product.value} onChange={this.onChangeProduct} aria-label="FormSelect Product">
                                    <FormSelectOption label="Select a Product" />
                                    {this.state.allProducts.map((option, key) => (
                                        <FormSelectOption key={key} value={option.value} label={option.label} />
                                    ))}
                                </FormSelect>
                                <FormSelect value={this.state.productVersion.uuid} onChange={this.onChangeVersion} aria-label="FormSelect Version" id="productVersion">
                                    <FormSelectOption label="Select a Version" />
                                    {this.state.allProductVersions.map((option, key) => (
                                        <FormSelectOption key={key} value={option["jcr:uuid"]} label={option.name} />
                                    ))}
                                </FormSelect>
                            </InputGroup>
                        </FormGroup>
                        <FormGroup
                            label="Document use case"
                            isRequired={true}
                            fieldId="document-usecase"
                            helperText="Explanations of document user cases included in documentation."
                        >
                            <FormSelect value={this.state.usecaseValue} onChange={this.onChangeUsecase} aria-label="FormSelect Usecase">
                                {Versions.USE_CASES.map((option, key) => (
                                    <FormSelectOption key={"usecase_" + key} value={option} label={option} />
                                ))}
                            </FormSelect>
                        </FormGroup>
                        <FormGroup
                            label="Vanity URL fragment"
                            fieldId="url-fragment"
                        >
                            <InputGroup>
                                <InputGroupText id="slash" aria-label="/">
                                    <span>/</span>
                                </InputGroupText>
                                <TextInput isRequired={false} id="url-fragment" type="text" placeholder="Enter URL" value={this.state.moduleUrl} onChange={this.handleURLInput} />
                            </InputGroup>
                        </FormGroup>
                        <FormGroup
                            label="Search keywords"
                            isRequired={false}
                            fieldId="search-keywords"
                        >
                            <InputGroup>
                                <TextInput isRequired={false} id="search-keywords" type="text" placeholder="cat, dog, bird..." value={this.state.keywords} onChange={this.handleKeywordsInput} />
                            </InputGroup>
                        </FormGroup>
                        <div>
                            <input name="productVersion@TypeHint" type="hidden" value="Reference" />
                        </div>
                    </Form>
                </Modal>
            </React.Fragment>

        )
    }

    private fetchVersions = () => {
        // TODO: need a better fix for the 404 error.
        if (this.props.modulePath !== "") {
            // fetchpath needs to start from modulePath instead of modulePath/en_US.
            // We need extact the module uuid for customer portal url to the module.
            const fetchpath = "/content" + this.props.modulePath + ".harray.5.json"
            fetch(fetchpath)
                .then(response => response.json())
                .then(responseJSON => {
                    const en_US = this.getHarrayChildNamed(responseJSON, "en_US")
                    const source = this.getHarrayChildNamed(en_US, "source")
                    const variants = this.getHarrayChildNamed(en_US, "variants")

                    const firstVariant = this.getHarrayChildNamed(variants, this.props.variant)
                    // process draftUpdateDate from source/draft
                    let draftDate = ""
                    if (source !== "undefined" && source.__name__ === "source") {
                        for (const childNode of source.__children__) {
                            if (childNode.__name__ === "draft") {
                                draftDate = childNode["jcr:created"]
                            } else if (childNode.__name__ === "released") {
                                draftDate = childNode["jcr:created"]
                            }
                        }
                    }
                    // process variantUUID
                    let variantUuid = ""
                    if (firstVariant["jcr:primaryType"] !== "undefined" && (firstVariant["jcr:primaryType"] === "pant:moduleVariant" || firstVariant["jcr:primaryType"] === "pant:assemblyVariant")) {
                        variantUuid = firstVariant["jcr:uuid"]
                    }
                    const versionCount = firstVariant.__children__.length
                    for (let i = 0; i < versionCount; i++) {
                        const moduleVersion = firstVariant.__children__[i]
                        let variantReleased = false
                        // console.log("[versions] moduleVersion => ", moduleVersion)
                        if (moduleVersion.__name__ === "draft") {
                            this.draft[0].version = "Version " + moduleVersion.__name__
                            this.draft[0].metadata = this.getHarrayChildNamed(moduleVersion, "metadata")
                            // get created date from source/draft
                            this.draft[0].updatedDate = draftDate !== undefined ? draftDate : ""
                            // this.props.modulePath starts with a slash
                            this.draft[0].path = "/content" + this.props.modulePath + "/en_US/variants/" + firstVariant.__name__ + "/" + moduleVersion.__name__
                        }
                        if (moduleVersion.__name__ === "released") {
                            this.release[0].version = "Version " + moduleVersion.__name__
                            this.release[0].metadata = this.getHarrayChildNamed(moduleVersion, "metadata")
                            this.release[0].updatedDate = this.release[0].metadata["pant:datePublished"] !== undefined ? this.release[0].metadata["pant:datePublished"] : ""
                            // get created date from source/draft
                            this.release[0].draftUploadDate = draftDate !== undefined ? draftDate : ""
                            // this.props.modulePath starts with a slash
                            this.release[0].path = "/content" + this.props.modulePath + "/en_US/variants/" + firstVariant.__name__ + "/" + moduleVersion.__name__
                            variantReleased = true
                        }
                        if (!variantReleased) {
                            this.release[0].updatedDate = "-"
                        }
                        this.props.updateDate((draftDate !== "" ? draftDate : ""), this.release[0].updatedDate, this.release[0].version, variantUuid)
                    }
                    this.setState({
                        results: [this.draft, this.release],
                    })

                    if (this.draft && this.draft[0].version.length > 0) {
                        this.setState({ metadataPath: this.draft[0].path })
                    } else if (this.release && this.release[0].version.length > 0) {
                        this.setState({ metadataPath: this.release[0].path })
                    }
                    this.getMetadata(this.state.metadataPath)
                    // Get documents included in assembly
                    if (this.props.contentType === "assembly") {
                        this.getDocumentsIncluded(variantUuid)
                    }
                })
        }
    }

    private getHarrayChildNamed = (object, name) => {
        for (const childName in object.__children__) {
            if (object.__children__.hasOwnProperty(childName)) { // Not sure what this does, but makes tslin happy
                const child = object.__children__[childName]
                if (child.__name__ === name) {
                    return child
                }
            }
        }
        return ""
    }

    private changePublishState = (buttonText) => {
        // Validate productValue before Publish
        if (this.props.productInfo !== undefined && this.props.productInfo.trim() === "" && buttonText === "Publish") {
            this.setState({ canChangePublishState: false, publishAlertVisible: true })
        } else {

            if (this.state.canChangePublishState === true) {
                const formData = new FormData();
                if (buttonText === "Publish") {
                    formData.append(":operation", "pant:publish");
                    // console.log("Published file path:", this.props.modulePath)
                    this.draft[0].version = "";
                    this.setState({ unpublishAlertForModuleVisible: false })
                    this.props.onPublishEvent()
                } else {
                    formData.append(":operation", "pant:unpublish");
                    // console.log("Unpublished file path:", this.props.modulePath);
                    this.release[0].version = "";
                    this.setState({ unpublishAlertForModuleVisible: true })
                    this.props.onPublishEvent()
                }
                formData.append("locale", "en_US")
                formData.append("variant", this.props.variant)
                fetch("/content" + this.props.modulePath, {
                    body: formData,
                    method: "post"
                }).then(response => {
                    if (response.status === 201 || response.status === 200) {
                        console.log(buttonText + " works: " + response.status)
                        this.setState({
                            canChangePublishState: true,
                            publishAlertVisible: false,
                            showMetadataAlertIcon: false
                        })
                    } else {
                        console.log(buttonText + " failed " + response.status)
                        this.setState({ publishAlertVisible: true })
                        this.setAlertTitle()
                    }
                    this.fetchVersions()
                });
            }
        }
    }

    private previewDoc = (buttonText) => {
        let docPath = ""
        docPath = "/pantheon/preview/" + (buttonText === "Preview" ? "latest" : "released") + "/" + this.props.variantUUID
        return window.open(docPath)
    }

    private handleModalToggle = (event) => {
        this.setState({
            isModalOpen: !this.state.isModalOpen
        })

        // process path
        const target = event.nativeEvent.target
        if (target.id !== undefined && target.id.trim().length > 0) {
            this.setState({ metadataPath: target.id })
        }
    }

    private handleModalClose = () => {
        this.setState({
            isModalOpen: false
        })
    }

    private saveMetadata = (event) => {
        // save form data
        if (this.state.product.value === undefined || this.state.product.value === "Select a Product" || this.state.product.value === ""
            || this.state.productVersion.uuid === undefined || this.state.productVersion.label === "Select a Version" || this.state.productVersion.uuid === ""
            || this.state.usecaseValue === undefined || this.state.usecaseValue === "Select Use Case" || this.state.usecaseValue === "") {

            this.setState({ isMissingFields: true })
        } else {
            const hdrs = {
                "Accept": "application/json",
                "cache-control": "no-cache"
            }

            const formData = new FormData(event.target.form)
            formData.append("productVersion", this.state.productVersion.uuid)
            formData.append("documentUsecase", this.state.usecaseValue)
            formData.append("urlFragment", this.state.moduleUrl.trim().length > 0 ? "/" + this.state.moduleUrl.trim() : "")
            formData.append("searchKeywords", this.state.keywords === undefined ? "" : this.state.keywords)

            fetch(this.state.metadataPath + "/metadata", {
                body: formData,
                headers: hdrs,
                method: "post"
            }).then(response => {
                if (response.status === 201 || response.status === 200) {
                    // console.log("successful edit ", response.status)
                    this.handleModalClose()
                    this.setState({
                        canChangePublishState: true,
                        publishAlertVisible: false,
                        showMetadataAlertIcon: false,
                        successAlertVisible: true,
                    })
                    this.props.onGetProduct(this.state.product.label)
                    this.props.onGetVersion(this.state.productVersion.label)
                } else if (response.status === 500) {
                    // console.log(" Needs login " + response.status)
                    this.setState({ login: true })
                }
            })
        }
    }
    private onChangeProduct = (productValue: string, event: React.FormEvent<HTMLSelectElement>) => {
        let productLabel = ""
        const target = event.nativeEvent.target
        if (target !== null) {
            // Necessary because target.selectedOptions produces a compiler error but is valid
            // tslint:disable-next-line: no-string-literal
            productLabel = target["selectedOptions"][0].label
        }
        this.setState({
            product: { label: productLabel, value: productValue },
            productVersion: { label: "", uuid: "" }
        })
        this.populateProductVersions(productValue)
    }

    private populateProductVersions(productValue) {
        fetch("/content/products/" + productValue + "/versions.harray.1.json")
            .then(response => response.json())
            .then(json => {
                this.setState({ allProductVersions: json.__children__ })
            })
    }

    private onChangeVersion = (value: string, event: React.FormEvent<HTMLSelectElement>) => {
        if (event.target !== null) {
            // Necessary because target.selectedOptions produces a compiler error but is valid
            // tslint:disable-next-line: no-string-literal
            const selectedOption = event.target["selectedOptions"][0]
            if (this.state.productVersion.uuid !== selectedOption.value) {
                this.setState({
                    productVersion: { label: selectedOption.label, uuid: selectedOption.value }
                })
            }
        }
    }

    private onChangeUsecase = (usecaseValue, event) => {
        this.setState({ usecaseValue })
    }

    private handleURLInput = moduleUrl => {
        this.setState({ moduleUrl })
    }

    private handleKeywordsInput = keywords => {
        this.setState({ keywords })
    }

    private fetchProducts = () => {

        const path = "/content/products.harray.1.json"
        const products = new Array()

        fetch(path)
            .then((response) => {
                if (response.ok) {
                    return response.json()
                } else {
                    throw new Error(response.statusText)
                }
            })
            .then(responseJSON => {
                for (const product of responseJSON.__children__) {
                    products.push({ label: product.name, value: product.__name__ })
                }
                this.setState({
                    allProducts: products
                })
            })
            .catch((error) => {
                console.log(error)
            })
        return products
    }

    private dismissNotification = () => {
        this.setState({ isMissingFields: false })
    }

    private hideSuccessAlert = () => {
        this.setState({ successAlertVisible: false })
    }

    private hidePublishAlert = () => {
        this.setState({ publishAlertVisible: false })
    }

    private hideUppublishAlertForModule = () => {
        this.setState({ unpublishAlertForModuleVisible: false })
    }

    private getMetadata = (versionPath) => {
        if (versionPath.trim() !== "") {
            fetch(versionPath + "/metadata.json")
                .then(response => response.json())
                .then(metadataResults => {
                    if (JSON.stringify(metadataResults) !== "[]") {
                        // Process results
                        // Remove leading slash.
                        if (metadataResults.urlFragment) {
                            let url = metadataResults.urlFragment
                            if (url.indexOf("/") === 0) {
                                url = url.replace("/", "")

                            }
                            this.setState({ moduleUrl: url })
                        }
                        this.setState({
                            keywords: metadataResults.searchKeywords,
                            productVersion: { label: "", uuid: metadataResults.productVersion },
                            usecaseValue: metadataResults.documentUsecase
                        })
                        if (metadataResults.productVersion !== undefined) {
                            this.getProductFromVersionUuid(metadataResults.productVersion)
                            this.setState({ showMetadataAlertIcon: false })
                        }
                    }
                })
        }
    }

    private getProductFromVersionUuid(versionUuid) {
        fetch("/pantheon/internal/node.json?ancestors=2&uuid=" + versionUuid)
            .then(response => response.json())
            .then(responseJSON => {
                this.setState({
                    product: { label: responseJSON.ancestors[1].name, value: responseJSON.ancestors[1].__name__ },
                    productVersion: { label: responseJSON.name, uuid: responseJSON["jcr:uuid"] }
                })
                this.populateProductVersions(this.state.product.value)
                this.props.onGetProduct(this.state.product.label)
                this.props.onGetVersion(this.state.productVersion.label)
            })
    }

    private handlePublishButton = () => {
        // console.log("[handlePublishButton] productInfo =>", this.props.productInfo)
        if (this.props.productInfo !== undefined && this.props.productInfo.trim().length > 0) {
            this.setState({ showMetadataAlertIcon: false })
        }
    }

    private getDocumentsIncluded = (variantUuid) => {
        if (variantUuid) {
            fetch('/pantheon/internal/assembly/includes.json/' + variantUuid)
                .then(response => response.json())
                .then(responseJSON => {
                    if (responseJSON.includes.documents !== undefined) {
                        this.setState({ documentsIncluded: responseJSON.includes.documents })
                    }
                })
        }
    }

    private capitalize = (str) => {
        if (str === undefined || str.trim().length === 0) {
            return
        }
        return str.charAt(0).toUpperCase() + str.slice(1)
    }

    private setAlertTitle = () => {
        const alertTitle = "Publishing " + this.props.contentType
        this.setState({ alertTitle })
    }
}

export { Versions }