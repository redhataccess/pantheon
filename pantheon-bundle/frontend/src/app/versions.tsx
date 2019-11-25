import React, { Component } from 'react'
import { Button } from '@patternfly/react-core'
import {
    Alert, AlertActionCloseButton, BaseSizes, Card, DataList, DataListItem, DataListItemRow,
    DataListItemCells, DataListCell, DataListToggle, DataListContent, Dropdown, DropdownItem,
    DropdownPosition, Form, FormGroup, FormSelect, FormSelectOption, InputGroup, KebabToggle,
    Modal, InputGroupText, Title, TitleLevel, TextInput
} from '@patternfly/react-core'
import CheckImage from '@app/images/check_image.jpg'
import BlankImage from '@app/images/blank.jpg'
import { Redirect } from 'react-router-dom'

export interface IProps {
    modulePath: string
    productInfo: string
    versionModulePath: string
    updateDate: (draftUpdateDate, releaseUpdateDate, releaseVersion, moduleUUID) => any
    onGetProduct: (productValue) => any
    onGetVersion: (versionValue) => any
}

class Versions extends Component<IProps, any> {
    private static USE_CASES = ['Select Use Case', 'Administer', 'Deploy', 'Develop', 'Install', 'Migrate', 'Monitor', 'Network', 'Plan', 'Provision', 'Release', 'Troubleshoot', 'Optimize']

    public draft = [{ "type": "draft", "icon": BlankImage, "path": "", "version": "", "publishedState": 'Not published', "updatedDate": "", "firstButtonType": 'primary', "secondButtonType": 'secondary', "firstButtonText": 'Publish', "secondButtonText": 'Preview', "isDropdownOpen": false, "isArchiveDropDownOpen": false, "metadata": '' }]
    public release = [{ "type": "release", "icon": CheckImage, "path": "", "version": "", "publishedState": 'Released', "updatedDate": "", "firstButtonType": 'secondary', "secondButtonType": 'primary', "firstButtonText": 'Unpublish', "secondButtonText": 'View', "isDropdownOpen": false, "isArchiveDropDownOpen": false, "metadata": '', "draftUploadDate": "" }]

    constructor(props) {
        super(props)
        this.state = {
            canChangePublishState: true,
            isArchiveDropDownOpen: false,
            isDropDownOpen: false,
            isHeadingToggle: true,
            login: false,
            results: [this.draft, this.release],

            allProducts: [],

            isMissingFields: false,
            isModalOpen: false,
            metadataInitialLoad: true,
            metadataPath: '',
            metadataResults: [],
            moduleUrl: '',
            productOptions: [
                { value: '', label: 'Select a Product', disabled: false },
            ],
            productValue: '',
            productVersion: '',
            publishAlertVisible: false,

            successAlertVisible: false,
            usecaseOptions: [
                { value: '', label: 'Select Use Case', disabled: false }
            ],
            usecaseValue: '',

            versionOptions: [
                { value: '', label: 'Select a Version', disabled: false },
            ],
            versionSelected: '',
            versionUUID: "",
            versionValue: '',
        }
    }

    public componentDidMount() {
        this.fetchProductVersionDetails()
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
                <Title headingLevel={TitleLevel.h1} size={BaseSizes["2xl"]}>
                    Edit Metadata
              </Title>
                <br />
                <p className="pf-u-pl-sm">
                    All fields are required.
              </p>
            </React.Fragment>
        )
        let verOptions = this.state.versionOptions
        if (this.state.allProducts[this.state.productValue]) {
            verOptions = this.state.allProducts[this.state.productValue]
        }

        return (
            <React.Fragment>
                {this.state.successAlertVisible && <Alert
                    variant="success"
                    title="Edit Metadata"
                    action={<AlertActionCloseButton onClose={this.hideSuccessAlert} />}
                >
                    Update Successful!
          </Alert>
                }

                {this.state.publishAlertVisible && <Alert
                    variant="warning"
                    title="Module Versions"
                    action={<AlertActionCloseButton onClose={this.hidePublishAlert} />}
                >
                    Empty Product info. Please edit metadata before publishing
          </Alert>
                }
                {this.state.metadataInitialLoad && this.getMetadata(this.state.metadataPath)}
                <Card>
                    <div>
                        <DataList aria-label="Simple data list">
                            <DataListItem aria-labelledby="simple-item1" isExpanded={this.state.isHeadingToggle}>
                                <DataListItemRow id="data-rows-header" >
                                    <DataListToggle
                                        // tslint:disable-next-line: jsx-no-lambda
                                        onClick={() => this.onHeadingToggle()}
                                        isExpanded={true}
                                        id="width-ex3-toggle1"
                                        aria-controls="width-ex3-expand1"
                                    />
                                    <DataListItemCells
                                        dataListCells={[
                                            <DataListCell key="version_header_version">
                                                <span className="sp-prop-nosort" id="span-source-type-version">Version</span>
                                            </DataListCell>,
                                            <DataListCell key="version_header_published">
                                                <span className="sp-prop-nosort" id="span-source-type-version-published">Published</span>
                                            </DataListCell>,
                                            <DataListCell key="version_header_updated">
                                                <span className="sp-prop-nosort" id="span-source-type-version-draft-uploaded">Draft Uploaded</span>
                                            </DataListCell>,
                                            <DataListCell key="version_header_publish_buttons">
                                                <span className="sp-prop-nosort" id="span-source-name-version-publish-buttons" />
                                            </DataListCell>,
                                            <DataListCell key="version_header_module_view_button">
                                                <span className="sp-prop-nosort" id="span-source-name" />
                                            </DataListCell>
                                        ]}
                                    />
                                </DataListItemRow>
                            </DataListItem>
                            <DataListContent
                                aria-label="Secondary Content Details"
                                id={"Content"}
                                isHidden={!this.state.isHeadingToggle}
                                noPadding={true}
                                key='details_dlc'
                            >
                                {/* this is the data list for the inner row */}
                                {/* {console.log("[results]", this.state.results)} */}
                                {this.state.results.map((type, key1) => (
                                    type.map((data, key2) => (
                                        data.version !== "" && (
                                            <DataList aria-label="Simple data list2" key={'datalist_' + key1 + '_' + key2}>
                                                <DataListItem aria-labelledby="simple-item2" isExpanded={data.isDropdownOpen} key={'datalistitem1_' + key1 + '_' + key2}>
                                                    <DataListItemRow key={'datalistitemrow1_' + key1 + '_' + key2}>
                                                        <DataListToggle
                                                            // tslint:disable-next-line: jsx-no-lambda
                                                            onClick={() => this.onExpandableToggle(data)}
                                                            isExpanded={data.isDropdownOpen}
                                                            id={data.version}
                                                            aria-controls={data.version}
                                                            key={'datalisttoggle1_' + key1 + '_' + key2}
                                                        />
                                                        <DataListItemCells
                                                            key={'datalistitemcells1_' + key1 + '_' + key2}
                                                            dataListCells={[
                                                                <DataListCell key={'version_value_' + key1 + '_' + key2}>
                                                                    {/* <img src={CheckImage} width="20px" height="20px"/> */}
                                                                    {data.version}
                                                                </DataListCell>,
                                                                <DataListCell key={'published_value_' + key1 + '_' + key2}>
                                                                    {data.publishedState==="Not published" && data.publishedState}
                                                                    {data.publishedState==="Released" && data.updatedDate}
                                                                </DataListCell>,
                                                                <DataListCell key={'version_updated_' + key1 + '_' + key2}>
                                                                    {data["type"]==="draft" && (data["updatedDate"].trim() !== "" ? data.updatedDate : "-")}
                                                                    {data["type"]==="release" && (data["draftUploadDate"].trim() !== "" ? data.draftUploadDate : "-")}
                                                                </DataListCell>,
                                                                <DataListCell key={'publish_buttons_' + key1 + '_' + key2}>
                                                                    <Button variant="primary" onClick={() => this.changePublishState(data.firstButtonText)}>{data.firstButtonText}</Button>{'  '}
                                                                    {/* tslint:disable-next-line: jsx-no-lambda*/}
                                                                    <Button variant="secondary" onClick={() => this.previewDoc(data.secondButtonText)}>{data.secondButtonText}</Button>{'  '}
                                                                </DataListCell>,
                                                                <DataListCell key={'image_' + key1 + '_' + key2} width={1}>
                                                                    <Dropdown
                                                                        isPlain={true}
                                                                        position={DropdownPosition.right}
                                                                        isOpen={data.isArchiveDropDownOpen}
                                                                        onSelect={this.onArchiveSelect}
                                                                        // tslint:disable-next-line: jsx-no-lambda
                                                                        toggle={<KebabToggle onToggle={() => this.onArchiveToggle(data)} />}
                                                                        key={'kebab_' + key1 + '_' + key2}
                                                                        dropdownItems={[
                                                                            <DropdownItem key={'archive_' + key1 + '_' + key2} isDisabled={true}>Archive</DropdownItem>,
                                                                            <DropdownItem id={data.path} key={'edit_metadata_' + key1 + '_' + key2} component="button" onClick={this.handleModalToggle}>Edit metadata</DropdownItem>,
                                                                        ]}
                                                                    />
                                                                </DataListCell>
                                                            ]}
                                                        />
                                                    </DataListItemRow>
                                                    <DataListContent
                                                        aria-label={data.version}
                                                        id={data.version}
                                                        isHidden={!data.isDropdownOpen}
                                                        noPadding={true}
                                                        key={'details_' + key1 + '_' + key2}
                                                    >
                                                        {/* this is the content for the inner data list content */}
                                                        <DataListItemCells
                                                            key={'details_cells_' + key1 + '_' + key2}
                                                            dataListCells={[
                                                                <DataListCell key={"details_whitespace_" + key1 + '_' + key2} width={2}>
                                                                    <span>{' '}</span>
                                                                </DataListCell>,
                                                                <DataListCell key={"details_file_name_" + key1 + '_' + key2} width={2}>
                                                                    <span className="sp-prop-nosort" id="span-source-type-filename">File Name</span>
                                                                </DataListCell>,
                                                                <DataListCell key={"details_modulePath_" + key1 + '_' + key2} width={4}>
                                                                    {this.props.modulePath}
                                                                </DataListCell>,
                                                                <DataListCell key={"details_upload_time_" + key1 + '_' + key2} width={2}>
                                                                    <span className="sp-prop-nosort" id="span-source-type-upload-time">Upload Time</span>
                                                                </DataListCell>,
                                                                <DataListCell key={"details_updated_" + key1 + '_' + key2} width={4}>
                                                                    {data["type"]==="draft" && (data["updatedDate"].trim() !== "" ? data.updatedDate : "-")}
                                                                    {data["type"]==="release" && (data["draftUploadDate"].trim() !== "" ? data.draftUploadDate : "-")}
                                                                </DataListCell>,
                                                            ]}
                                                        />

                                                        <DataListItemCells
                                                            key={'details_cells2_' + key1 + '_' + key2}
                                                            dataListCells={[
                                                                <DataListCell key={"details_whitespace2_" + key1 + '_' + key2} width={2}>
                                                                    <span>{' '}</span>
                                                                </DataListCell>,
                                                                <DataListCell key={"details_module_title_" + key1 + '_' + key2} width={2}>
                                                                    <span>{'  '}</span>
                                                                    <span className="sp-prop-nosort" id="span-source-type-module-title">Module Title</span>
                                                                </DataListCell>,
                                                                <DataListCell key={"details_jcr_title_" + key1 + '_' + key2} width={4}>
                                                                    {(data["metadata"]["jcr:title"] !== undefined) ? data["metadata"]["jcr:title"] : '-'}
                                                                </DataListCell>,
                                                                <DataListCell key={"details_context_package_" + key1 + '_' + key2} width={2}>
                                                                    <span className="sp-prop-nosort" id="span-source-type-context-package">Context Package</span>
                                                                </DataListCell>,
                                                                <DataListCell key={"details_context_value_" + key1 + '_' + key2} width={4}>
                                                                    N/A
                                                                </DataListCell>,
                                                            ]}
                                                        />

                                                    </DataListContent>
                                                </DataListItem>
                                            </DataList>)
                                    ))
                                ))}
                            </DataListContent>
                        </DataList>
                    </div>
                </Card>
                <Modal
                    width={'60%'}
                    title="Edit metadata"
                    isOpen={this.state.isModalOpen}
                    header={header}
                    ariaDescribedById="edit-metadata"
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
                    <div>
                        {this.loginRedirect()}
                    </div>
                    <div className="app-container">

                        {this.state.isMissingFields && (
                            <div className="notification-container">
                                <Alert
                                    variant="warning"
                                    title="All fields are required."
                                    action={<AlertActionCloseButton onClose={this.dismissNotification} />}
                                />
                                <br />
                            </div>
                        )}
                    </div>
                    <Form isHorizontal={true} id="edit_metadata">
                        <FormGroup
                            label="Product Name"
                            isRequired={true}
                            fieldId="product-name"
                        >
                            <InputGroup>
                                <FormSelect value={this.state.productValue} onChange={this.onChangeProduct} aria-label="FormSelect Product">
                                    {this.state.productOptions.map((option, index) => (
                                        <FormSelectOption isDisabled={option.disabled} key={index} value={option.value} label={option.label} />
                                    ))}
                                </FormSelect>
                                <FormSelect value={this.state.versionUUID} onChange={this.onChangeVersion} aria-label="FormSelect Version" id="productVersion">
                                    {verOptions.map((option) => (

                                        <FormSelectOption isDisabled={false} key={option.value} value={option.value} label={option.label} required={false} />
                                    ))}
                                </FormSelect>
                            </InputGroup>
                        </FormGroup>
                        <FormGroup
                            label="Document use case"
                            isRequired={true}
                            fieldId="document-usecase"
                        >
                            <FormSelect value={this.state.usecaseValue} onChange={this.onChangeUsecase} aria-label="FormSelect Usecase">
                                {Versions.USE_CASES.map((option, key) => (
                                    <FormSelectOption key={'usecase_' + key} value={option} label={option} />
                                ))}
                            </FormSelect>
                        </FormGroup>
                        <FormGroup
                            label="Vanity URL fragment"
                            isRequired={true}
                            fieldId="url-fragment"
                        >
                            <InputGroup>
                                <InputGroupText id="slash" aria-label="/">
                                    <span>/</span>
                                </InputGroupText>
                                <TextInput isRequired={true} id="url-fragment" type="text" placeholder="Enter URL" value={this.state.moduleUrl} onChange={this.handleURLInput} />
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
        if (this.props.modulePath !== '') {
            const fetchpath = "/content" + this.props.modulePath + "/en_US.harray.3.json"
            fetch(fetchpath)
                .then(response => response.json())
                .then(responseJSON => {
                    this.setState(updateState => {
                        const releasedTag = responseJSON.released
                        const draftTag = responseJSON.draft
                        const versionCount = responseJSON.__children__.length

                        for (let i = versionCount - 1; i > versionCount - 3 && i >= 0; i--) {
                            const moduleVersion = responseJSON.__children__[i]
                            if (moduleVersion["jcr:uuid"] === draftTag) {
                                this.draft[0].version = "Version " + moduleVersion.__name__
                                this.draft[0].metadata = this.getHarrayChildNamed(moduleVersion, "metadata")
                                this.draft[0].updatedDate = this.draft[0].metadata["pant:dateUploaded"] !== undefined ? this.draft[0].metadata["pant:dateUploaded"] : ''
                                // this.props.modulePath starts with a slash
                                this.draft[0].path = "/content" + this.props.modulePath + "/en_US/" + moduleVersion.__name__
                            }
                            if (moduleVersion["jcr:uuid"] === releasedTag) {
                                this.release[0].version = "Version " + moduleVersion.__name__
                                this.release[0].metadata = this.getHarrayChildNamed(moduleVersion, "metadata")
                                this.release[0].updatedDate = this.release[0].metadata["pant:datePublished"] !== undefined ? this.release[0].metadata["pant:datePublished"] : ''
                                this.release[0].draftUploadDate = this.release[0].metadata["pant:dateUploaded"] !== undefined ? this.release[0].metadata["pant:dateUploaded"] : ''
                                // this.props.modulePath starts with a slash
                                this.release[0].path = "/content" + this.props.modulePath + "/en_US/" + moduleVersion.__name__
                            }
                            if(releasedTag===undefined){
                                this.release[0].updatedDate = "-"
                            }
                            this.props.updateDate((this.draft[0].updatedDate !== "" ? this.draft[0].updatedDate : this.release[0].draftUploadDate),this.release[0].updatedDate,this.release[0]["version"], responseJSON['jcr:uuid'])

                        }
                        return {
                            results: [this.draft, this.release],
                            // tslint:disable-next-line: object-literal-sort-keys
                            metadataPath: this.draft ? this.draft[0].path : this.release[0].path
                        }
                    })
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
        return ''
    }

    private changePublishState = (buttonText) => {
        // Validate productValue before Publish
        if (this.props.productInfo !== undefined && this.props.productInfo.trim() === "" && buttonText === "Publish") {
            this.setState({ canChangePublishState: false, publishAlertVisible: true })
        } else {

            if (this.state.canChangePublishState === true) {
                const formData = new FormData();
                if (buttonText === "Publish") {
                    formData.append(":operation", "pant:release");
                    // console.log('Published file path:', this.props.modulePath)
                    this.draft[0].version = "";
                } else {
                    formData.append(":operation", "pant:unpublish");
                    // console.log('Unpublished file path:', this.props.modulePath);
                    this.release[0].version = "";
                }
                fetch("/content" + this.props.modulePath, {
                    body: formData,
                    method: 'post'
                }).then(response => {
                    if (response.status === 201 || response.status === 200) {
                        console.log(buttonText + " works: " + response.status)
                        this.setState({ publishAlertVisible: false, canChangePublishState: true })
                    } else {
                        console.log(buttonText + " failed " + response.status)
                        this.setState({ publishAlertVisible: true })
                    }
                    this.fetchVersions()
                });
            }
        }
    }

    private onArchiveSelect = event => {
        this.setState({
            isArchiveDropDownOpen: !this.state.isArchiveDropDownOpen
        })
    }

    private onArchiveToggle = (data) => {
        data.isArchiveDropDownOpen = !data.isArchiveDropDownOpen
        this.setState({
            isArchiveDropDownOpen: this.state.isArchiveDropDownOpen
        })
    }

    private onExpandableToggle = (data) => {
        data.isDropdownOpen = !data.isDropdownOpen
        this.forceUpdate()
    }

    private onHeadingToggle = () => {
        this.setState({
            isHeadingToggle: !this.state.isHeadingToggle
        })
    }

    private previewDoc = (buttonText) => {
        let docPath = ""
        if (buttonText === "Preview") {
            docPath = "/content" + this.props.modulePath + ".preview?draft=true"
        } else {
            docPath = "/content" + this.props.modulePath + ".preview"
        }
        // console.log("Preview path: ", docPath)
        return window.open(docPath)
    }

    private handleModalToggle = (event) => {
        this.setState({
            isModalOpen: !this.state.isModalOpen
        })

        // process path
        this.setState({ metadataPath: event.target.id })
    }

    private handleModalClose = () => {
        this.setState({
            isModalOpen: false
        })
    }

    private saveMetadata = (event) => {
        // save form data
        if (this.state.productValue === undefined || this.state.productValue === 'Select a Product' || this.state.productValue === ''
            || this.state.versionUUID === undefined || this.state.versionUUID === 'Select a Version' || this.state.versionUUID === ''
            || this.state.usecaseValue === undefined || this.state.usecaseValue === 'Select Use Case' || this.state.usecaseValue === ''
            || this.state.moduleUrl.trim() === "" || this.state.versionSelected === '') {

                this.setState({ isMissingFields: true })

        } else {
            const hdrs = {
                'Accept': 'application/json',
                'cache-control': 'no-cache'
            }

            const formData = new FormData(event.target.form)

            formData.append("productVersion", this.state.versionUUID)
            formData.append("documentUseCase", this.state.usecaseValue)
            formData.append("urlFragment", "/" + this.state.moduleUrl)
            // console.log("[metadataPath] ", this.state.metadataPath)
            fetch(this.state.metadataPath + '/metadata', {
                body: formData,
                headers: hdrs,
                method: 'post'
            }).then(response => {
                if (response.status === 201 || response.status === 200) {
                    // console.log("successful edit ", response.status)
                    this.handleModalClose()
                    this.setState({ successAlertVisible: true, canChangePublishState: true, publishAlertVisible: false, versionSelected: '' })
                    this.props.onGetProduct(this.state.productValue)
                    this.props.onGetVersion(this.state.versionValue)
                } else if (response.status === 500) {
                    // console.log(" Needs login " + response.status)
                    this.setState({ login: true })
                } else {
                    // console.log(" Failed " + response.status)
                    this.setState({ failedPost: true })
                }
            })
        }
    }
    private onChangeProduct = (productValue) => {
        this.setState({ productValue })
    }
    private onChangeVersion = () => {

        // console.log("[onChangeVersion] event: ", event)
        if (event !== undefined) {
            if (event.target !== null) {
                // tslint:disable-next-line: no-string-literal
                if (this.state.versionUUID !== event.target["selectedOptions"][0].value) {
                    this.setState({
                        // tslint:disable-next-line: no-string-literal
                        versionSelected: event.target["selectedOptions"][0].label,
                        // tslint:disable-next-line: no-string-literal
                        versionUUID: event.target["selectedOptions"][0].value,
                        // tslint:disable-next-line: no-string-literal
                        versionValue: event.target["selectedOptions"][0].label,
                    });
                }
            }
        }
    }

    private onChangeUsecase = (usecaseValue, event) => {
        this.setState({ usecaseValue })
    }

    private handleURLInput = moduleUrl => {
        this.setState({ moduleUrl })
    }

    private fetchProductVersionDetails = () => {

        const path = '/content/products.3.json'
        let key
        const products = new Array()

        fetch(path)
            .then((response) => {
                if (response.ok) {
                    // console.log("[responseJSON] response.ok ", response.json())
                    return response.json()
                } else if (response.status === 404) {
                    // console.log("Something unexpected happen!")
                    return products
                } else {
                    throw new Error(response.statusText)
                }
            })
            .then(responseJSON => {
                // tslint:disable-next-line: prefer-for-of
                for (let i = 0; i < Object.keys(responseJSON).length; i++) {
                    key = Object.keys(responseJSON)[i]
                    const nameKey = "name"
                    const versionKey = "versions"
                    if ((key !== 'jcr:primaryType')) {
                        if (responseJSON[key][nameKey] !== undefined) {
                            const pName = responseJSON[key][nameKey]
                            const versionObj = responseJSON[key][versionKey]

                            if (versionObj) {
                                let vKey
                                const versions = [{ value: '', label: 'Select a Version', disabled: false }]
                                // tslint:disable-next-line: no-shadowed-variable
                                const nameKey = "name"
                                const uuidKey = "jcr:uuid"
                                for (const item in Object.keys(versionObj)) {
                                    if (Object.keys(versionObj)[item] !== undefined) {
                                        vKey = Object.keys(versionObj)[item]

                                        if (vKey !== 'jcr:primaryType') {
                                            if (versionObj[vKey][nameKey]) {
                                                versions.push({ value: versionObj[vKey][uuidKey], label: versionObj[vKey][nameKey], disabled: false })
                                            }
                                        }
                                    }
                                }

                                products[pName] = versions
                            }
                        }
                    }
                }
                this.setState({
                    allProducts: products
                })

                if (products) {
                    const productItems = [{ value: 'Select a Product', label: 'Select a Product', disabled: false }]
                    // tslint:disable-next-line: forin
                    for (const item in products) {
                        // console.log("[render] item ", item)
                        productItems.push({ value: item, label: item, disabled: false })
                    }
                    if (productItems.length > 1) {
                        this.setState({ productOptions: productItems })
                        // console.log("[fetchProductVersionDetails] productOptions: ", this.state.productOptions)
                    }
                }
            })
            .catch((error) => {
                console.log(error)
            })
        return products
    }

    private loginRedirect = () => {
        if (this.state.login) {
            return <Redirect to='/login' />
        } else {
            return ""
        }
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

    private getMetadata = (versionPath) => {

        if (versionPath.trim() !== "") {
            // console.log("[getMetadata] versionPath: ", versionPath)
            this.setState({ metadataInitialLoad: false })
            fetch(versionPath + "/metadata.json")
                .then(response => response.json())
                .then(responseJSON => this.setState({ metadataResults: responseJSON }))
                .then(() => {
                    if (JSON.stringify(this.state.metadataResults) !== "[]") {
                        // Process results
                        // Remove leading slash.
                        // console.log("[metadataResults] ", this.state.metadataResults)
                        if (this.state.metadataResults.urlFragment) {
                            let url = this.state.metadataResults.urlFragment
                            if (url.indexOf('/') === 0) {
                                url = url.replace('/', '')

                            }
                            this.setState({ moduleUrl: url })
                        }
                        this.setState({
                            usecaseValue: this.state.metadataResults.documentUseCase,
                            versionUUID: this.state.metadataResults.productVersion
                        }, () => {
                            // console.log("versionUUID", this.state.versionUUID)
                            // Process versionValue and productValue here.
                            if (this.state.versionUUID !== undefined && this.state.versionUUID.trim() !== "") {

                                if (Object.keys(this.state.allProducts).length > 0) {
                                    // tslint:disable-next-line: forin
                                    for (const item in this.state.allProducts) {
                                        // tslint:disable-next-line: prefer-for-of
                                        for (let j = 0; j < this.state.allProducts[item].length; j++) {
                                            // console.log("[productValue] pName ", item)
                                            // console.log("[productValue] vLabel ", this.state.allProducts[item][j].label)
                                            if (this.state.allProducts[item][j].value === this.state.versionUUID) {

                                                this.setState({
                                                    productValue: item,
                                                    versionValue: this.state.allProducts[item][j].label
                                                })

                                                break
                                            }
                                        }
                                    }

                                }
                            }
                        })

                    }
                })
        }


    }
}

export { Versions }