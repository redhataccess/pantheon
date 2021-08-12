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
import { TreeView, TreeViewDataItem } from '@patternfly/react-core';

import CheckImage from "@app/images/check_image.jpg"
import BlankImage from "@app/images/blank.jpg"
import { Redirect } from "react-router-dom"
import { ExclamationTriangleIcon, TimesIcon, PlusCircleIcon, JsIcon } from "@patternfly/react-icons"
import { Metadata, PantheonContentTypes, PathPrefixes } from "./Constants"
import { array } from "prop-types";

export interface IProps {
    contentType: string
    modulePath: string
    productInfo: string
    versionModulePath: string
    variant: string
    variantUUID: string
    attributesFilePath: string
    assemblies?: any
    onGetUrl: (url) => any
    updateDate: (releaseVersion, variantUUID) => any
    onGetProduct: (productValue) => any
    onGetVersion: (versionValue) => any
    canRegeneratePortalUrl: (regeneratePortalUrl) => any
}

// Define properties in Metadata
export interface IMetadata {
    documentUsercase?: string
    productVersion?: string
    searchKeywords?: string
    urlFragment?: string
}

interface IState {
    activeItems: any
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
    variantPath: string
    urlFragment: string
    product: { label: string, value: string }
    productVersion: { label: string, uuid: string }
    publishAlertVisible: boolean
    unpublishAlertForModuleVisible: boolean
    results: any
    showMetadataAlertIcon: boolean
    successAlertVisible: boolean
    usecaseOptions: any
    usecaseValue: string
    assemblyData: []
    draftValidations: any
    releasedValidations: any
}


class Versions extends Component<IProps, IState> {

    public draft = [{ type: "draft", icon: BlankImage, path: "", version: "", publishedState: "Not published", updatedDate: "", firstButtonType: "primary", secondButtonType: "secondary", firstButtonText: "Publish", secondButtonText: "Preview", isDropdownOpen: false, isArchiveDropDownOpen: false, metadata: { productVersion: {} }, validations: [] }]
    public release = [{ type: "release", icon: CheckImage, "path": "", version: "", publishedState: "Released", updatedDate: "", firstButtonType: "secondary", secondButtonType: "primary", firstButtonText: "Unpublish", secondButtonText: "View", isDropdownOpen: false, isArchiveDropDownOpen: false, metadata: { productVersion: {} }, validations: [], draftUploadDate: "" }]

    constructor(props) {
        super(props)
        this.state = {
            activeItems: {},
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
            variantPath: "",
            urlFragment: "",
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
            draftValidations: [],
            releasedValidations: []
        }
    }

    public componentDidMount() {
        this.fetchProducts()
        this.fetchVersions()
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
                    {this.state.results.map((type, key1) => (
                        type.map((data, key2) => (
                            data.version !== "" && data.type === "draft" && (
                                <GridItem span={6}>
                                    <Card className="pf-m-light pf-site-background-medium pf-c-card-draft">
                                        <CardHeader>
                                            <CardHeaderMain><strong>Draft</strong></CardHeaderMain>
                                            <CardActions>{ }</CardActions>
                                            {data.metadata !== undefined && !this.state.showMetadataAlertIcon &&
                                                <CardActions>
                                                    <Button variant="link" isInline={true} onClick={this.handleModalToggle} id="draft">Add metadata</Button>
                                                </CardActions>}
                                            {data.metadata !== undefined && this.state.showMetadataAlertIcon &&
                                                <CardActions><ExclamationTriangleIcon color="#f0ab00" />
                                                    <Button variant="link" isInline={true} onClick={this.handleModalToggle} id="draft">Add metadata</Button>
                                                </CardActions>}
                                            <CardActions><Button variant="link" isInline={true} onClick={() => this.previewDoc(data.secondButtonText)} id="draftPreview">Preview</Button>
                                            </CardActions>
                                            {data.metadata !== undefined && !this.state.showMetadataAlertIcon &&
                                                <CardActions>
                                                    <Button variant="primary" isSmall={true} onClick={() => this.changePublishState(data.firstButtonText)} id="publishButton">{data.firstButtonText}</Button>
                                                </CardActions>
                                            }
                                            {data.metadata !== undefined && this.state.showMetadataAlertIcon &&
                                                <CardActions>
                                                    <Tooltip content="Add metadata to publish">
                                                        <Button isAriaDisabled={true} variant="primary" isSmall={true} onClick={() => this.changePublishState(data.firstButtonText)} id="publishButton">{data.firstButtonText}</Button>
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
                                            <br />
                                            {data.validations !== undefined && data.validations.length > 0 && <TextContent>
                                                <Text><strong>Validations</strong></Text>
                                            </TextContent>}

                                            {data.validations !== undefined && data.validations.length > 0 &&
                                                <TreeView data={data.validations} activeItems={this.state.activeItems} onSelect={this.onClickTree} hasBadges />
                                            }
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
                                                <CardActions>{ }</CardActions>
                                                <CardActions>
                                                    <Button variant="link" isInline={true} onClick={this.handleModalToggle} id="released">Add metadata</Button>
                                                </CardActions>
                                                <CardActions>
                                                    <Button variant="link" isInline={true} onClick={() => this.previewDoc(data.secondButtonText)} id="releasedPreview">Preview</Button>
                                                </CardActions>
                                                <CardActions>
                                                    <Button variant="secondary" isSmall={true} onClick={() => this.changePublishState(data.firstButtonText)} id="unpublishButton">{data.firstButtonText}</Button>
                                                </CardActions>
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
                                                <br />
                                                {data.validations !== undefined && data.validations.length > 0 && <TextContent>
                                                    <Text><strong>Validations</strong></Text>
                                                </TextContent>}

                                                {data.validations !== undefined && data.validations.length > 0 &&
                                                    <TreeView data={data.validations} activeItems={this.state.activeItems} onSelect={this.onClickTree} hasBadges />
                                                }
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
                                {Metadata.USE_CASES.map((option, key) => (
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
                                <TextInput isRequired={false} id="url-fragment" type="text" placeholder="Enter URL" value={this.state.urlFragment} onChange={this.handleURLInput} />
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
            this.getValidations("draft")
            this.getValidations("released")
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
                    let draftUpload = ""
                    let releaseUpload = ""
                    if (source !== "undefined" && source.__name__ === "source") {
                        for (const childNode of source.__children__) {
                            if (childNode.__name__ === "draft") {
                                draftUpload = childNode["jcr:created"]
                            } else if (childNode.__name__ === "released") {
                                releaseUpload = childNode["jcr:created"]
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

                        if (moduleVersion.__name__ === "draft") {
                            this.draft[0].version = "Version " + moduleVersion.__name__
                            this.draft[0].metadata = this.getHarrayChildNamed(moduleVersion, "metadata")
                            // get created date from source/draft
                            this.draft[0].updatedDate = draftUpload
                            // this.props.modulePath starts with a slash
                            this.draft[0].path = "/content" + this.props.modulePath + "/en_US/variants/" + firstVariant.__name__ + "/" + moduleVersion.__name__
                            this.draft[0].validations = this.state.draftValidations
                        }
                        if (moduleVersion.__name__ === "released") {
                            this.release[0].version = "Version " + moduleVersion.__name__
                            this.release[0].metadata = this.getHarrayChildNamed(moduleVersion, "metadata")
                            this.release[0].updatedDate = releaseUpload
                            // get created date from source/draft
                            this.release[0].draftUploadDate = draftUpload
                            // this.props.modulePath starts with a slash
                            this.release[0].path = "/content" + this.props.modulePath + "/en_US/variants/" + firstVariant.__name__ + "/" + moduleVersion.__name__
                            this.release[0].validations = this.state.releasedValidations
                            variantReleased = true
                        }
                        if (!variantReleased) {
                            this.release[0].updatedDate = "-"
                        }
                        this.props.updateDate(this.release[0].version, variantUuid)
                    }
                    this.setState({
                        results: [this.draft, this.release],
                        variantPath: "/content" + this.props.modulePath + "/en_US/variants/" + this.props.variant
                    })

                    // Check metadata for draft. Show warning icon if metadata missing for draft
                    if (this.draft && this.draft[0].path.length > 0) {
                        if (this.draft[0].metadata !== undefined &&
                            this.draft[0].metadata.productVersion === undefined) {
                            this.setState({ showMetadataAlertIcon: true })
                        } else {
                            this.setState({ showMetadataAlertIcon: false })
                        }
                    }

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
                } else {
                    formData.append(":operation", "pant:unpublish");
                    // console.log("Unpublished file path:", this.props.modulePath);
                    this.release[0].version = "";
                    this.setState({ unpublishAlertForModuleVisible: true })
                }
                const hdrs = {
                    "Accept": "application/json",
                    "cache-control": "no-cache",
                    "Access-Control-Allow-Origin": "*"
                }
                formData.append("locale", "en_US")
                formData.append("variant", this.props.variant)
                fetch("/content" + this.props.modulePath, {
                    body: formData,
                    method: "post",
                    headers: hdrs
                }).then(response => {
                    if (response.status === 201 || response.status === 200) {
                        console.log(buttonText + " works: " + response.status)
                        this.setState({
                            canChangePublishState: true,
                            publishAlertVisible: false,
                            showMetadataAlertIcon: false
                        }, () => this.props.canRegeneratePortalUrl(true))
                    } else {
                        console.log(buttonText + " failed " + response.status)
                        this.setState({ publishAlertVisible: true })
                        this.setAlertTitle()
                    }
                    this.fetchVersions()
                    return response.json()
                }).then(response => this.props.onGetUrl(response.path));
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
            this.getMetadata(event)
        }
    }

    private handleModalClose = () => {
        this.setState({
            isModalOpen: false
        })
    }

    //TODO: refactor this method and move necessary code to Utils
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
            let eventID = ""
            formData.append("productVersion", this.state.productVersion.uuid)
            formData.append("documentUsecase", this.state.usecaseValue)
            formData.append("urlFragment", this.state.urlFragment.trim().length > 0 ? "/" + this.state.urlFragment.trim() : "")
            formData.append("searchKeywords", this.state.keywords === undefined ? "" : this.state.keywords)

            const target = event.nativeEvent.target
            if (target !== null
                && target.id !== undefined
                && target.id === "draft") {
                eventID = event.target.id
            }
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
                        successAlertVisible: true,
                    }, () => this.props.canRegeneratePortalUrl(true))
                    if (this.state.metadataPath.endsWith("/draft")) {
                        this.setState({ showMetadataAlertIcon: false })
                        this.fetchVersions()
                    }
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

    private handleURLInput = urlFragment => {
        this.setState({ urlFragment })
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
                    if (product.name !== undefined) {
                        products.push({ label: product.name, value: product.__name__ })
                    }
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

    private getMetadata = (event) => {
        let versionValue = ""
        let metadataPath = ""
        if (event !== undefined && event.target.id !== undefined) {
            versionValue = event.target.id
        } else {
            versionValue = "draft"
        }

        metadataPath = this.state.variantPath + "/" + versionValue

        fetch(`${this.state.variantPath}/${versionValue}/metadata.json`)
            .then(response => response.json())
            .then(metadataResults => {
                if (JSON.stringify(metadataResults) !== "[]") {
                    this.setState({ metadataPath })
                    // Process results
                    // Remove leading slash.
                    if (metadataResults.urlFragment) {
                        let urlFragment = metadataResults.urlFragment
                        if (urlFragment.indexOf("/") === 0) {
                            urlFragment = urlFragment.replace("/", "")
                        }
                        this.setState({ urlFragment })
                    }

                    if (metadataResults.searchKeywords) {
                        this.setState({ keywords: metadataResults.searchKeywords })
                    }

                    if (metadataResults.productVersion) {
                        this.setState({ productVersion: { label: "", uuid: metadataResults.productVersion } })
                    }

                    if (metadataResults.documentUsecase) {
                        this.setState({ usecaseValue: metadataResults.documentUsecase })
                    }

                    if (metadataResults.productVersion !== undefined) {
                        this.getProductFromVersionUuid(metadataResults.productVersion)

                        if (versionValue === "draft") {
                            this.setState({ showMetadataAlertIcon: false })
                        }

                    }
                }
            })
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

    private getValidations = (versionType) => {
        let versionValue = ""
        let validationPath = ""

        if (versionType !== undefined && versionType.length > 0) {
            versionValue = versionType
        }

        validationPath = "/content" + this.props.modulePath + "/en_US/variants/" + this.props.variant + "/" + versionValue + "/validations.harray.2.json"

        fetch(validationPath)
            .then(response => response.json())
            .then(json => {
                const xrefValidation = this.getHarrayChildNamed(json, "xref")

                let options = new Array()
                if (xrefValidation.__children__ != undefined && xrefValidation.__children__.length > 0) {

                    let rootChildren = new Array()

                    for (const childNode of xrefValidation.__children__) {
                        const children = {
                            "name": childNode["pant:xrefTarget"], "id": childNode["pant:xrefTarget"].split(" ").join(""),
                            "children": [{ name: childNode["pant:message"], id: childNode["pant:message"].split(" ").join("") }]
                        }
                        rootChildren.push(children)
                    }
                    const root = { name: "xref Targets", id: "xrefs", children: rootChildren, defaultExpanded: true }
                    options.push(root)

                }
                if (versionValue === "draft") {
                    this.setState({ draftValidations: options })
                } else {
                    this.setState({ releasedValidations: options })
                }

            })
            .catch((error) => {
                // console.log("No validations node for " + validationPath + " => ", error)
            })
    }

    private onClickTree = (evt, treeViewItem, parentItem) => {
        this.setState({
            activeItems: [treeViewItem, parentItem]
        });
    }
}

export { Versions }