import React, { Component } from 'react'
import { Button, Level, LevelItem, Text, TextContent, TextVariants, CardHeaderMain, CardActions, Tooltip } from '@patternfly/react-core'
import {
    Alert, AlertActionCloseButton, BaseSizes, Card, CardTitle, CardHeader, CardBody, DataList, DataListItem, DataListItemRow,
    DataListItemCells, DataListCell, DataListToggle, DataListContent, Dropdown, DropdownItem,
    DropdownPosition, Form, FormGroup, FormSelect, FormSelectOption, Grid, GridItem, InputGroup, KebabToggle,
    Modal, InputGroupText, Title, TextInput
} from '@patternfly/react-core'
import CheckImage from '@app/images/check_image.jpg'
import BlankImage from '@app/images/blank.jpg'
import { Redirect } from 'react-router-dom'
import { ExclamationTriangleIcon, TimesIcon, PlusCircleIcon } from '@patternfly/react-icons'

export interface IProps {
    modulePath: string
    productInfo: string
    versionModulePath: string
    variant: string
    attributesFilePath: string
    updateDate: (draftUpdateDate, releaseUpdateDate, releaseVersion, variantUUID) => any
    onGetProduct: (productValue) => any
    onGetVersion: (versionValue) => any
}

interface IState {
    allProducts: any
    allProductVersions: any
    canChangePublishState: boolean
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
    results: any
    successAlertVisible: boolean
    usecaseOptions: any
    usecaseValue: string,
}

class Versions extends Component<IProps, IState> {
    private static USE_CASES = ['Select Use Case', 'Administer', 'Deploy', 'Develop', 'Install', 'Migrate', 'Monitor', 'Network', 'Plan', 'Provision', 'Release', 'Troubleshoot', 'Optimize']

    public draft = [{ 'type': 'draft', 'icon': BlankImage, 'path': '', 'version': '', 'publishedState': 'Not published', 'updatedDate': '', 'firstButtonType': 'primary', 'secondButtonType': 'secondary', 'firstButtonText': 'Publish', 'secondButtonText': 'Preview', 'isDropdownOpen': false, 'isArchiveDropDownOpen': false, 'metadata': '' }]
    public release = [{ 'type': 'release', 'icon': CheckImage, 'path': '', 'version': '', 'publishedState': 'Released', 'updatedDate': '', 'firstButtonType': 'secondary', 'secondButtonType': 'primary', 'firstButtonText': 'Unpublish', 'secondButtonText': 'View', 'isDropdownOpen': false, 'isArchiveDropDownOpen': false, 'metadata': '', 'draftUploadDate': '' }]

    constructor(props) {
        super(props)
        this.state = {
            allProducts: [],
            // tslint:disable-next-line: object-literal-sort-keys
            allProductVersions: [],
            canChangePublishState: true,
            isArchiveDropDownOpen: false,
            isDropDownOpen: false,
            isHeadingToggle: true,
            isMissingFields: false,
            isModalOpen: false,
            keywords: '',
            login: false,
            metadataPath: '',
            moduleUrl: '',
            product: { label: '', value: '' },
            productVersion: { label: '', uuid: '' },
            publishAlertVisible: false,
            results: [this.draft, this.release],
            successAlertVisible: false,
            usecaseOptions: [
                { value: '', label: 'Select Use Case', disabled: false }
            ],
            usecaseValue: '',
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
                <Title headingLevel="h1" size={BaseSizes['2xl']}>
                    Edit Metadata
              </Title>
            </React.Fragment>
        )

        return (
            <React.Fragment>
                {this.state.successAlertVisible && <Alert
                    variant='success'
                    title='Edit Metadata'
                    actionClose={<AlertActionCloseButton onClose={this.hideSuccessAlert} />}
                >
                    Update Successful!
          </Alert>
                }

                {this.state.publishAlertVisible && <Alert
                    variant='warning'
                    title='Module Versions'
                    actionClose={<AlertActionCloseButton onClose={this.hidePublishAlert} />}
                >
                    Module failed to publish. Check the following:
                    <ul>
                        <li>Are you logged in as a publisher?</li>
                        <li>Does the module have all required metadata?</li>
                    </ul>
                </Alert>
                }

                <Grid hasGutter={true}>
                    {console.log('[results]', this.state.results)}
                    {this.state.results.map((type, key1) => (
                        type.map((data, key2) => (
                            data.version !== '' && data.type === "draft" && (
                                <GridItem span={6}>
                                    <Card className="pf-m-light pf-site-background-medium pf-c-card-draft">
                                        <CardHeader>
                                            <CardHeaderMain><strong>Draft</strong></CardHeaderMain>
                                            <CardActions>{}</CardActions>
                                            {data.metadata !== undefined && data.metadata.productVersion !== undefined &&
                                                <CardActions>
                                                    <Button variant="link" isInline={true} onClick={this.handleModalToggle}>Add metadata</Button>
                                                </CardActions>}
                                            {data.metadata !== undefined && data.metadata.productVersion === undefined &&
                                                <CardActions><i className="pf-icon pf-icon-warning-triangle" />
                                                    <Button variant="link" isInline={true} onClick={this.handleModalToggle}>Add metadata</Button>
                                                </CardActions>}
                                            <CardActions><Button variant="link" isInline={true} onClick={() => this.previewDoc(data.secondButtonText)}>Preview</Button>
                                            </CardActions>
                                            {data.metadata !== undefined && data.metadata.productVersion !== undefined &&
                                                <CardActions>
                                                    <Button variant="primary" isSmall={true} onClick={() => this.changePublishState(data.firstButtonText)}>{data.firstButtonText}</Button>
                                                </CardActions>
                                            }
                                            {data.metadata !== undefined && 
                                                <CardActions>
                                                    <Tooltip content="Add metadata to publish">
                                                        <Button isAriaDisabled={true} variant="primary" isSmall={true} onClick={() => this.changePublishState(data.firstButtonText)}>{data.firstButtonText}</Button>
                                                    </Tooltip>
                                                </CardActions>
                                            }
                                        </CardHeader>

                                        <CardBody>
                                            <TextContent>
                                                <div><Text><strong>Upload time</strong></Text></div>
                                                <div><Text>{data.updatedDate}</Text></div>
                                            </TextContent>
                                            <br />
                                            <TextContent>
                                                <div><Text><strong>Attribute file</strong></Text></div>
                                                <div><Text>{this.props.attributesFilePath}</Text></div>
                                            </TextContent>
                                            <br />
                                            <TextContent>
                                                <div><Text><strong>Modules</strong></Text></div>
                                                <div><Text>{}</Text></div>
                                            </TextContent>
                                        </CardBody>

                                    </Card>
                                </GridItem>)
                        ))
                    ))}
                    {this.state.results.map((type, key1) => (
                        type.map((data, key2) => (
                            data.version !== '' && data.type === "release" && (
                                <GridItem span={6}>
                                    <Card>
                                        <CardHeader>
                                            <CardHeaderMain><strong><span id='span-source-type-version-published'>Published</span></strong></CardHeaderMain>
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
                                                <div><Text><strong>Upload time</strong></Text></div>
                                                <div><Text>{data.updateDate}</Text></div>
                                            </TextContent>
                                            <br />
                                            <TextContent>
                                                <div><Text><strong>Attribute file</strong></Text></div>
                                                <div><Text>{this.props.attributesFilePath}</Text></div>
                                            </TextContent>
                                            <br />
                                            <TextContent>
                                                <div><Text><strong>Modules</strong></Text></div>
                                                <div><Text>{}</Text></div>
                                            </TextContent>
                                        </CardBody>

                                    </Card>
                                </GridItem>)
                        ))
                    ))}
                </Grid>

                <Modal
                    width={'60%'}
                    title='Edit metadata'
                    isOpen={this.state.isModalOpen}
                    header={header}
                    aria-label="Edit metadata"
                    onClose={this.handleModalClose}
                    actions={[
                        <Button form='edit_metadata' key='confirm' variant='primary' onClick={this.saveMetadata}>
                            Save
          </Button>,
                        <Button key='cancel' variant='secondary' onClick={this.handleModalToggle}>
                            Cancel
            </Button>
                    ]}
                >
                    <div>
                        {this.loginRedirect()}
                    </div>
                    <div className='app-container'>

                        {this.state.isMissingFields && (
                            <div className='notification-container'>
                                <Alert
                                    variant='warning'
                                    title=''
                                    actionClose={<AlertActionCloseButton onClose={this.dismissNotification} />}
                                />
                                <br />
                            </div>
                        )}
                    </div>
                    <Form isHorizontal={true} id='edit_metadata'>
                        <FormGroup
                            label='Product Name'
                            isRequired={true}
                            fieldId='product-name'
                        >
                            <InputGroup>
                                <FormSelect value={this.state.product.value} onChange={this.onChangeProduct} aria-label='FormSelect Product'>
                                    <FormSelectOption label='Select a Product' />
                                    {this.state.allProducts.map((option, key) => (
                                        <FormSelectOption key={key} value={option.value} label={option.label} />
                                    ))}
                                </FormSelect>
                                <FormSelect value={this.state.productVersion.uuid} onChange={this.onChangeVersion} aria-label='FormSelect Version' id='productVersion'>
                                    <FormSelectOption label='Select a Version' />
                                    {this.state.allProductVersions.map((option, key) => (
                                        <FormSelectOption key={key} value={option['jcr:uuid']} label={option.name} />
                                    ))}
                                </FormSelect>
                            </InputGroup>
                        </FormGroup>
                        <FormGroup
                            label='Document use case'
                            isRequired={true}
                            fieldId='document-usecase'
                            helperText="Explanations of document user cases included in documentation."
                        >
                            <FormSelect value={this.state.usecaseValue} onChange={this.onChangeUsecase} aria-label='FormSelect Usecase'>
                                {Versions.USE_CASES.map((option, key) => (
                                    <FormSelectOption key={'usecase_' + key} value={option} label={option} />
                                ))}
                            </FormSelect>
                        </FormGroup>
                        <FormGroup
                            label='Vanity URL fragment'
                            fieldId='url-fragment'
                        >
                            <InputGroup>
                                <InputGroupText id='slash' aria-label='/'>
                                    <span>/</span>
                                </InputGroupText>
                                <TextInput isRequired={false} id='url-fragment' type='text' placeholder='Enter URL' value={this.state.moduleUrl} onChange={this.handleURLInput} />
                            </InputGroup>
                        </FormGroup>
                        <FormGroup
                            label='Search keywords'
                            isRequired={false}
                            fieldId='search-keywords'
                        >
                            <InputGroup>
                                <TextInput isRequired={false} id='search-keywords' type='text' placeholder='cat, dog, bird...' value={this.state.keywords} onChange={this.handleKeywordsInput} />
                            </InputGroup>
                        </FormGroup>
                        <div>
                            <input name='productVersion@TypeHint' type='hidden' value='Reference' />
                        </div>
                    </Form>
                </Modal>
            </React.Fragment>

        )
    }

    private fetchVersions = () => {
        // TODO: need a better fix for the 404 error.
        if (this.props.modulePath !== '') {
            // fetchpath needs to start from modulePath instead of modulePath/en_US.
            // We need extact the module uuid for customer portal url to the module.
            const fetchpath = '/content' + this.props.modulePath + '.harray.5.json'
            fetch(fetchpath)
                .then(response => response.json())
                .then(responseJSON => {
                    const en_US = this.getHarrayChildNamed(responseJSON, 'en_US')
                    const source = this.getHarrayChildNamed(en_US, 'source')
                    const variants = this.getHarrayChildNamed(en_US, 'variants')

                    const firstVariant = this.getHarrayChildNamed(variants, this.props.variant)
                    // process draftUpdateDate from source/draft
                    let draftDate = ''
                    if (source !== 'undefined' && source.__name__ === 'source') {
                        for (const childNode of source.__children__) {
                            if (childNode.__name__ === 'draft') {
                                draftDate = childNode["jcr:created"]
                            } else if (childNode.__name__ === 'released') {
                                draftDate = childNode["jcr:created"]
                            }
                        }
                    }
                    // process variantUUID
                    let variantUuid = ''
                    if (firstVariant["jcr:primaryType"] !== "undefined" && firstVariant["jcr:primaryType"] === "pant:moduleVariant") {
                        variantUuid = firstVariant["jcr:uuid"]
                    }
                    const versionCount = firstVariant.__children__.length
                    for (let i = 0; i < versionCount; i++) {
                        const moduleVersion = firstVariant.__children__[i]
                        let variantReleased = false
                        // console.log("[versions] moduleVersion => ", moduleVersion)
                        if (moduleVersion.__name__ === 'draft') {
                            this.draft[0].version = 'Version ' + moduleVersion.__name__
                            this.draft[0].metadata = this.getHarrayChildNamed(moduleVersion, 'metadata')
                            // get created date from source/draft
                            this.draft[0].updatedDate = draftDate !== undefined ? draftDate : ''
                            // this.props.modulePath starts with a slash
                            this.draft[0].path = '/content' + this.props.modulePath + '/en_US/variants/' + firstVariant.__name__ + '/' + moduleVersion.__name__
                        }
                        if (moduleVersion.__name__ === 'released') {
                            this.release[0].version = 'Version ' + moduleVersion.__name__
                            this.release[0].metadata = this.getHarrayChildNamed(moduleVersion, 'metadata')
                            this.release[0].updatedDate = this.release[0].metadata['pant:datePublished'] !== undefined ? this.release[0].metadata['pant:datePublished'] : ''
                            // get created date from source/draft
                            this.release[0].draftUploadDate = draftDate !== undefined ? draftDate : ''
                            // this.props.modulePath starts with a slash
                            this.release[0].path = '/content' + this.props.modulePath + '/en_US/variants/' + firstVariant.__name__ + '/' + moduleVersion.__name__
                            variantReleased = true
                        }
                        if (!variantReleased) {
                            this.release[0].updatedDate = '-'
                        }
                        this.props.updateDate((draftDate !== '' ? draftDate : ''), this.release[0].updatedDate, this.release[0].version, variantUuid)
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
        if (this.props.productInfo !== undefined && this.props.productInfo.trim() === '' && buttonText === 'Publish') {
            this.setState({ canChangePublishState: false, publishAlertVisible: true })
        } else {

            if (this.state.canChangePublishState === true) {
                const formData = new FormData();
                if (buttonText === 'Publish') {
                    formData.append(':operation', 'pant:publish');
                    // console.log('Published file path:', this.props.modulePath)
                    this.draft[0].version = '';
                } else {
                    formData.append(':operation', 'pant:unpublish');
                    // console.log('Unpublished file path:', this.props.modulePath);
                    this.release[0].version = '';
                }
                formData.append('locale', 'en_US')
                formData.append('variant', this.props.variant)
                fetch('/content' + this.props.modulePath, {
                    body: formData,
                    method: 'post'
                }).then(response => {
                    if (response.status === 201 || response.status === 200) {
                        console.log(buttonText + ' works: ' + response.status)
                        this.setState({ publishAlertVisible: false, canChangePublishState: true })
                    } else {
                        console.log(buttonText + ' failed ' + response.status)
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
        let docPath = ''
        if (buttonText === 'Preview') {
            docPath = '/content' + this.props.modulePath + '.preview?draft=true&variant=' + this.props.variant
        } else {
            docPath = '/content' + this.props.modulePath + '.preview?variant=' + this.props.variant
        }
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
        if (this.state.product.value === undefined || this.state.product.value === 'Select a Product' || this.state.product.value === ''
            || this.state.productVersion.uuid === undefined || this.state.productVersion.label === 'Select a Version' || this.state.productVersion.uuid === ''
            || this.state.usecaseValue === undefined || this.state.usecaseValue === 'Select Use Case' || this.state.usecaseValue === '') {

            this.setState({ isMissingFields: true })
        } else {
            const hdrs = {
                'Accept': 'application/json',
                'cache-control': 'no-cache'
            }

            const formData = new FormData(event.target.form)
            formData.append('productVersion', this.state.productVersion.uuid)
            formData.append('documentUsecase', this.state.usecaseValue)
            formData.append('urlFragment', this.state.moduleUrl.trim().length > 0 ? '/' + this.state.moduleUrl.trim() : '')
            formData.append('searchKeywords', this.state.keywords === undefined ? '' : this.state.keywords)

            fetch(this.state.metadataPath + '/metadata', {
                body: formData,
                headers: hdrs,
                method: 'post'
            }).then(response => {
                if (response.status === 201 || response.status === 200) {
                    // console.log('successful edit ', response.status)
                    this.handleModalClose()
                    this.setState({ successAlertVisible: true, canChangePublishState: true, publishAlertVisible: false })
                    this.props.onGetProduct(this.state.product.label)
                    this.props.onGetVersion(this.state.productVersion.label)
                } else if (response.status === 500) {
                    // console.log(' Needs login ' + response.status)
                    this.setState({ login: true })
                }
            })
        }
    }
    private onChangeProduct = (productValue: string, event: React.FormEvent<HTMLSelectElement>) => {
        let productLabel = ''
        const target = event.nativeEvent.target
        if (target !== null) {
            // Necessary because target.selectedOptions produces a compiler error but is valid
            // tslint:disable-next-line: no-string-literal
            productLabel = target['selectedOptions'][0].label
        }
        this.setState({
            product: { label: productLabel, value: productValue },
            productVersion: { label: '', uuid: '' }
        })
        this.populateProductVersions(productValue)
    }

    private populateProductVersions(productValue) {
        fetch('/content/products/' + productValue + '/versions.harray.1.json')
            .then(response => response.json())
            .then(json => {
                this.setState({ allProductVersions: json.__children__ })
            })
    }

    private onChangeVersion = (value: string, event: React.FormEvent<HTMLSelectElement>) => {
        if (event.target !== null) {
            // Necessary because target.selectedOptions produces a compiler error but is valid
            // tslint:disable-next-line: no-string-literal
            const selectedOption = event.target['selectedOptions'][0]
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

        const path = '/content/products.harray.1.json'
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

    private loginRedirect = () => {
        if (this.state.login) {
            return <Redirect to='/login' />
        } else {
            return ''
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
        if (versionPath.trim() !== '') {
            fetch(versionPath + '/metadata.json')
                .then(response => response.json())
                .then(metadataResults => {
                    if (JSON.stringify(metadataResults) !== '[]') {
                        // Process results
                        // Remove leading slash.
                        if (metadataResults.urlFragment) {
                            let url = metadataResults.urlFragment
                            if (url.indexOf('/') === 0) {
                                url = url.replace('/', '')

                            }
                            this.setState({ moduleUrl: url })
                        }
                        this.setState({
                            keywords: metadataResults.searchKeywords,
                            productVersion: { label: '', uuid: metadataResults.productVersion },
                            usecaseValue: metadataResults.documentUsecase
                        })
                        if (metadataResults.productVersion !== undefined) {
                            this.getProductFromVersionUuid(metadataResults.productVersion)
                        }
                    }
                })
        }
    }

    private getProductFromVersionUuid(versionUuid) {
        fetch('/pantheon/internal/node.json?ancestors=2&uuid=' + versionUuid)
            .then(response => response.json())
            .then(responseJSON => {
                this.setState({
                    product: { label: responseJSON.ancestors[1].name, value: responseJSON.ancestors[1].__name__ },
                    productVersion: { label: responseJSON.name, uuid: responseJSON['jcr:uuid'] }
                })
                this.populateProductVersions(this.state.product.value)
                this.props.onGetProduct(this.state.product.label)
                this.props.onGetVersion(this.state.productVersion.label)
            })
    }
}

export { Versions }