import React, { Component } from 'react';
import { Button } from '@patternfly/react-core';
import {
    Alert, AlertActionCloseButton, BaseSizes, Card, DataList, DataListItem, DataListItemRow,
    DataListItemCells, DataListCell, DataListToggle, DataListContent, Dropdown, DropdownItem,
    DropdownPosition, Form, FormGroup, FormSelect, FormSelectOption, InputGroup, KebabToggle,
    Modal, InputGroupText, Title, TitleLevel, TextInput
} from '@patternfly/react-core';
import CheckImage from '@app/images/check_image.jpg';
import BlankImage from '@app/images/blank.jpg';
import { Redirect } from 'react-router-dom'

export interface IProps {
    modulePath: string
    versionModulePath: string
    updateDate: (draftUpdateDate,releaseUpdateDate,releaseVersion) => any
    onGetProduct: (productValue) => any
    onGetVersion: (versionValue) => any
}

class Versions extends Component<IProps, any> {

    public draft = [{ "type":"draft","icon": BlankImage, "path": "", "version": "", "publishedState": 'Not published', "updatedDate": "", "firstButtonType": 'primary', "secondButtonType": 'secondary', "firstButtonText": 'Publish', "secondButtonText": 'Preview', "isDropdownOpen": false, "isArchiveDropDownOpen": false, "metadata": '' }]
    public release = [{ "type":"release","icon": CheckImage, "path": "", "version": "", "publishedState": 'Released', "updatedDate": "", "firstButtonType": 'secondary', "secondButtonType": 'primary', "firstButtonText": 'Unpublish', "secondButtonText": 'View', "isDropdownOpen": false, "isArchiveDropDownOpen": false, "metadata": '',"draftUploadDate": "" }]

    constructor(props) {
        super(props)
        this.state = {
            changePublishState: false,
            initialLoad: true,
            isArchiveDropDownOpen: false,
            isArchiveSelect: false,
            isDropDownOpen: false,
            isHeadingToggle: true,
            isOpen: false,
            isRowToggle: false,
            login: false,
            results: [this.draft, this.release],

            allProducts: [],
            formInvalid: false,

            isEmptyResults: false,
            isMissingFields: false,
            isModalOpen: false,
            isProductDropdownOpen: false,
            isUsecaseDropdownOpen: false,
            isVersionDropdownOpen: false,
            loggedinStatus: false,
            metadataInitialLoad: true,
            metadataPath: '',
            metadataResults: [],
            moduleUrl: '',
            productOptions: [
                { value: '', label: 'Select a Product', disabled: false },
            ],
            productValue: '',
            productVersion: '',
            redirect: false,

            successAlertVisble: false,
            usecaseOptions: [
                { value: '', label: 'Select Use Case', disabled: false }
            ],
            usecaseValue: '',
            usecases: ['Administer', 'Deploy', 'Develop', 'Install', 'Migrate', 'Monitor', 'Network',
                'Plan', 'Provision', 'Release', 'Troubleshoot', 'Optimize'],

            versionOptions: [
                { value: '', label: 'Select a Version', disabled: false },
            ],
            versionSelected: '',
            versionUUID: "",
            versionValue: '',
        };

    }

    public componentDidMount() {
        this.fetchProductVersionDetails()
        if (!this.state.loggedinStatus && this.state.initialLoad === true) {
            fetch("/system/sling/info.sessionInfo.json")
                .then(response => response.json())
                .then(responseJSON => {
                    if (responseJSON.userID) {
                        if (responseJSON.userID !== 'anonymous') {
                            this.setState({ loggedinStatus: true })
                        }
                    }
                })
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
        );
        let verOptions = this.state.versionOptions
        if (this.state.allProducts[this.state.productValue]) {
            verOptions = this.state.allProducts[this.state.productValue]
        }

        const ucOptions = this.state.usecaseOptions
        this.state.usecases.map((item) => (
            ucOptions.push({ value: item, label: item, disabled: false })
        ))

        return (
            <React.Fragment>
                {this.state.successAlertVisble && <Alert
                    variant="success"
                    title="Edit Metadata"
                    action={<AlertActionCloseButton onClose={this.hideSuccessAlert} />}
                >
                    Update Successful!
          </Alert>
                }
                {this.state.initialLoad && this.fetchVersions()}
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
                                            <DataListCell key="version">
                                                <span className="sp-prop-nosort" id="span-source-type-version">Version</span>
                                            </DataListCell>,
                                            <DataListCell key="published">
                                                <span className="sp-prop-nosort" id="span-source-type-version-published">Published</span>
                                            </DataListCell>,
                                            <DataListCell key="updated">
                                                <span className="sp-prop-nosort" id="span-source-type-version-draft-uploaded">Draft Uploaded</span>
                                            </DataListCell>,
                                            <DataListCell key="publish_buttons">
                                                <span className="sp-prop-nosort" id="span-source-name-version-publish-buttons" />
                                            </DataListCell>,
                                            <DataListCell key="module_view_button">
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
                            >
                                {/* this is the data list for the inner row */}
                                {/* {console.log("[results]", this.state.results)} */}
                                {this.state.results.map(type => (
                                    type.map(data => (
                                        data.version !== "" && (
                                            <DataList aria-label="Simple data list2">
                                                <DataListItem aria-labelledby="simple-item2" isExpanded={data.isDropdownOpen}>
                                                    <DataListItemRow>
                                                        <DataListToggle
                                                            // tslint:disable-next-line: jsx-no-lambda
                                                            onClick={() => this.onExpandableToggle(data)}
                                                            isExpanded={data.isDropdownOpen}
                                                            id={data.version}
                                                            aria-controls={data.version}
                                                        />
                                                        <DataListItemCells
                                                            dataListCells={[
                                                                <DataListCell key="version">
                                                                    {/* <img src={CheckImage} width="20px" height="20px"/>                                                         */}
                                                                    {data.version}
                                                                </DataListCell>,
                                                                <DataListCell key="published">
                                                                    {data.publishedState==="Not published" && data.publishedState}
                                                                    {data.publishedState==="Released" && data.updatedDate}
                                                                </DataListCell>,
                                                                <DataListCell key="updated">
                                                                    {data["type"]==="draft" && (data["updatedDate"].trim() !== "" ? data.updatedDate : "-")}
                                                                    {data["type"]==="release" && (data["draftUploadDate"].trim() !== "" ? data.draftUploadDate : "-")}
                                                                </DataListCell>,
                                                                <DataListCell key="publish_buttons">
                                                                    <Button variant="primary" onClick={() => this.changePublishState(data.firstButtonText)}>{data.firstButtonText}</Button>{'  '}
                                                                    <Button variant="secondary" onClick={() => this.previewDoc(data.secondButtonText)}>{data.secondButtonText}</Button>{'  '}
                                                                </DataListCell>,
                                                                <DataListCell key="image" width={1}>
                                                                    <Dropdown
                                                                        isPlain={true}
                                                                        position={DropdownPosition.right}
                                                                        isOpen={data.isArchiveDropDownOpen}
                                                                        onSelect={this.onArchiveSelect}
                                                                        // tslint:disable-next-line: jsx-no-lambda
                                                                        toggle={<KebabToggle onToggle={() => this.onArchiveToggle(data)} />}
                                                                        dropdownItems={[
                                                                            <DropdownItem key="archive" isDisabled={true}>Archive</DropdownItem>,
                                                                            <DropdownItem id={data.path} key={data.path} component="button" onClick={this.handleModalToggle}>Edit metadata</DropdownItem>,
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
                                                    >
                                                        {/* this is the content for the inner data list content */}
                                                        <DataListItemCells
                                                            dataListCells={[
                                                                <DataListCell key="File name" width={2}>
                                                                    <span>{' '}</span>
                                                                </DataListCell>,
                                                                <DataListCell key="File name" width={2}>
                                                                    <span className="sp-prop-nosort" id="span-source-type-filename">File Name</span>
                                                                </DataListCell>,
                                                                <DataListCell key="published" width={4}>
                                                                    {this.props.modulePath}
                                                                </DataListCell>,
                                                                <DataListCell key="updated" width={2}>
                                                                    <span className="sp-prop-nosort" id="span-source-type-upload-time">Upload Time</span>
                                                                </DataListCell>,
                                                                <DataListCell key="updated" width={4}>
                                                                    {data["type"]==="draft" && (data["updatedDate"].trim() !== "" ? data.updatedDate : "-")}
                                                                    {data["type"]==="release" && (data["draftUploadDate"].trim() !== "" ? data.draftUploadDate : "-")}
                                                                </DataListCell>,
                                                            ]}
                                                        />

                                                        <DataListItemCells
                                                            dataListCells={[
                                                                <DataListCell key="File name" width={2}>
                                                                    <span>{' '}</span>
                                                                </DataListCell>,
                                                                <DataListCell key="updated" width={2}>
                                                                    <span>{'  '}</span>
                                                                    <span className="sp-prop-nosort" id="span-source-type-module-title">Module Title</span>
                                                                </DataListCell>,
                                                                <DataListCell key="updated" width={4}>
                                                                    {(data["metadata"]["jcr:title"] !== undefined) ? data["metadata"]["jcr:title"] : '-'}
                                                                </DataListCell>,
                                                                <DataListCell key="updated" width={2}>
                                                                    <span className="sp-prop-nosort" id="span-source-type-context-package">Context Package</span>
                                                                </DataListCell>,
                                                                <DataListCell key="updated" width={4}>
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
                        {this.renderRedirect()}
                    </div>
                    <div className="app-container">

                        {this.state.isMissingFields && (
                            <div className="notification-container">
                                <Alert
                                    variant="warning"
                                    title={this.state.versionSelected === '' ? "Please select a version." : "All fields are required."}
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
                                {ucOptions.map((option, index) => (
                                    <FormSelectOption isDisabled={option.disabled} key={index} value={option.value} label={option.label} />
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

        );
    }

    private fetchVersions = () => {

        // TODO: need a better fix for the 404 error.
        if (this.props.modulePath !== '') {
            const fetchpath = "/content" + this.props.modulePath + "/en_US.harray.3.json";
            fetch(fetchpath)
                .then(response => response.json())
                .then(responseJSON => {
                    this.setState(updateState => {
                        const releasedTag = responseJSON["released"];
                        const draftTag = responseJSON["draft"];
                        const versionCount = responseJSON["__children__"].length

                        for (let i = versionCount - 1; i > versionCount - 3 && i >= 0; i--) {
                            const moduleVersion = responseJSON["__children__"][i]
                            if (moduleVersion["jcr:uuid"] === draftTag) {
                                this.draft[0]["version"] = "Version " + moduleVersion["__name__"];
                                this.draft[0]["metadata"] = this.getHarrayChildNamed(moduleVersion, "metadata")
                                this.draft[0]["updatedDate"] = this.draft[0]["metadata"]["pant:dateUploaded"] !== undefined ? this.draft[0]["metadata"]["pant:dateUploaded"] : '';
                                this.draft[0]["path"] = "/content/" + this.props.modulePath + "/en_US/" + moduleVersion["__name__"];                                
                            }
                            if (moduleVersion["jcr:uuid"] === releasedTag) {
                                this.release[0]["version"] = "Version " + moduleVersion["__name__"];
                                this.release[0]["metadata"] = this.getHarrayChildNamed(moduleVersion, "metadata")
                                this.release[0]["updatedDate"] = this.release[0]["metadata"]["pant:datePublished"] !== undefined ? this.release[0]["metadata"]["pant:datePublished"] : '';
                                this.release[0]["draftUploadDate"] = this.release[0]["metadata"]["pant:dateUploaded"] !== undefined ? this.release[0]["metadata"]["pant:dateUploaded"] : '';
                                this.release[0]["path"] = "/content/" + this.props.modulePath + "/en_US/" + moduleVersion["__name__"];                                
                            }
                            if(releasedTag===undefined){
                                this.release[0].updatedDate = "-";
                            }
                            this.props.updateDate((this.draft[0].updatedDate !== "" ? this.draft[0].updatedDate : this.release[0].draftUploadDate),this.release[0].updatedDate,this.release[0]["version"]);

                        }
                        return {
                            initialLoad: false,
                            results: [this.draft, this.release],
                            // tslint:disable-next-line: object-literal-sort-keys
                            metadatPath: this.draft ? this.draft[0].path : this.release[0].path
                        }
                    })
                })
        }
    }

    private getHarrayChildNamed = (object, name) => {
        for (const child in object["__children__"]) {
            if (object["__children__"][child]["__name__"] === name) {
                return object["__children__"][child]
            }
        }
        return ''
    }

    private changePublishState = (buttonText) => {
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
                // console.log(buttonText + " works: " + response.status)
                this.setState({ initialLoad: true, changePublishState: true })
            } else {
                console.log(buttonText + " failed " + response.status)
                this.setState({ initialLoad: true, changePublishState: true })
            }
        });

    }

    private onArchiveSelect = event => {
        this.setState({
            isArchiveDropDownOpen: !this.state.isArchiveDropDownOpen
        });
    };

    private onArchiveToggle = (data) => {
        data.isArchiveDropDownOpen = !data.isArchiveDropDownOpen;
        this.setState({
            isArchiveDropDownOpen: this.state.isArchiveDropDownOpen
        });
    };

    private onExpandableToggle = (data) => {
        data.isDropdownOpen = !data.isDropdownOpen;
        this.setState({
            isRowToggle: this.state.isRowToggle
        });
    }

    private onHeadingToggle = () => {
        this.setState({
            isHeadingToggle: !this.state.isHeadingToggle
        });
    }

    private previewDoc = (buttonText) => {
        let docPath = "";
        if (buttonText === "Preview") {
            docPath = "/content" + this.props.modulePath + ".preview?draft=true";
        } else {
            docPath = "/content" + this.props.modulePath + ".preview";
        }
        // console.log("Preview path: ", docPath)
        return window.open(docPath);
    }

    private handleModalToggle = (event) => {
        this.setState({
            isModalOpen: !this.state.isModalOpen
        });

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
            this.setState({ formInvalid: true })

        } else {
            const hdrs = {
                'Accept': 'application/json',
                'cache-control': 'no-cache'
            }

            const formData = new FormData(event.target.form);

            formData.append("productVersion", this.state.versionUUID)
            formData.append("documentUsecase", this.state.usecaseValue)
            formData.append("urlFragment", "/" + this.state.moduleUrl)
            // console.log("[metadataPath] ", this.state.metadataPath)
            fetch(this.state.metadataPath + '/metadata', {
                body: formData,
                headers: hdrs,
                method: 'post'
            }).then(response => {
                if (response.status === 201 || response.status === 200) {
                    // console.log("successful edit ", response.status)
                    // this.setState({ redirect: true, successAlertVisble: true })
                    this.handleModalClose()
                    this.setState({ successAlertVisble: true })
                    this.setState({ versionSelected: '' })
                } else if (response.status === 500) {
                    // console.log(" Needs login " + response.status)
                    this.setState({ login: true })
                } else {
                    // console.log(" Failed " + response.status)
                    this.setState({ failedPost: true })
                }
            });
        }
    }
    private onChangeProduct = (productValue) => {
        this.setState({ productValue });
    }
    private onChangeVersion = () => {

        // console.log("[onChangeVersion] event: ", event)
        if (event !== undefined) {
            if (event.target !== null) {
                // tslint:disable-next-line: no-string-literal
                if (this.state.versionUUID !== event.target["selectedOptions"][0].value) {
                    // tslint:disable-next-line: no-string-literal
                    this.setState({
                        versionUUID: event.target["selectedOptions"][0].value,
                        versionValue: event.target["selectedOptions"][0].label,
                        // tslint:disable-next-line: object-literal-sort-keys
                        versionSelected: event.target["selectedOptions"][0].label
                    }, () => {
                        this.props.onGetProduct(this.state.productValue)
                        this.props.onGetVersion(this.state.versionValue)
                    });
                }
            }
        }
    }

    private onChangeUsecase = (usecaseValue, event) => {
        this.setState({ usecaseValue });
    }

    private handleURLInput = moduleUrl => {
        this.setState({ moduleUrl });
    }

    private fetchProductVersionDetails = () => {

        const path = '/content/products.3.json'
        let key
        const products = new Array()

        fetch(path)
            .then((response) => {
                if (response.ok) {
                    // console.log("[responseJSON] response.ok ", response.json())
                    return response.json();
                } else if (response.status === 404) {
                    // console.log("Something unexpected happen!")
                    return products
                } else {
                    throw new Error(response.statusText);
                }
            })
            .then(responseJSON => {
                // tslint:disable-next-line: prefer-for-of
                for (let i = 0; i < Object.keys(responseJSON).length; i++) {
                    key = Object.keys(responseJSON)[i];
                    const nameKey = "name"
                    const versionKey = "versions"
                    if ((key !== 'jcr:primaryType')) {
                        if (responseJSON[key][nameKey] !== undefined) {
                            const pName = responseJSON[key][nameKey]
                            const versionObj = responseJSON[key][versionKey]

                            if (versionObj) {
                                let vKey;
                                const versions = new Array();
                                // tslint:disable-next-line: no-shadowed-variable
                                const nameKey = "name";
                                const uuidKey = "jcr:uuid";
                                for (const item in Object.keys(versionObj)) {
                                    if (Object.keys(versionObj)[item] !== undefined) {
                                        vKey = Object.keys(versionObj)[item]

                                        if (vKey !== 'jcr:primaryType') {
                                            if (versionObj[vKey][nameKey]) {

                                                versions.push({ value: versionObj[vKey][uuidKey], label: versionObj[vKey][nameKey] })
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
            });
        return products;
    };

    private getModuleUrl = (moduleUrl) => {
        const backend = '/content/modules.query.json?nodeType=pant:module&where=[urlFragment]="' + moduleUrl + '"'
        return backend

    }

    private renderRedirect = () => {
        if (this.state.redirect) {
            return <Redirect to='/search' />
        } else {
            return ""
        }
    }

    private loginRedirect = () => {
        if (this.state.login) {
            return <Redirect to='/login' />
        } else {
            return ""
        }
    }

    private dismissNotification = () => {
        if (this.state.isMissingFields === true) {
            this.setState({ isMissingFields: false });
        }
    }

    private hideSuccessAlert = () => {
        this.setState({ successAlertVisble: false })
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
                                url = url.replace('/', '');

                            }
                            this.setState({ moduleUrl: url })
                        }
                        this.setState({
                            usecaseValue: this.state.metadataResults.documentUsecase,
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

                                                break;
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