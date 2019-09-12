import React, { Component } from 'react';
import {
    Alert, AlertActionCloseButton, Breadcrumb, BreadcrumbItem, BreadcrumbHeading, BaseSizes, Button, DataList, DataListItem, DataListItemRow, DataListItemCells,
    DataListCell, Card, Text, TextContent, TextVariants, Grid, GridItem, Dropdown, DropdownToggle, DropdownItem, DropdownSeparator, Form,
    FormGroup, FormSelect, FormSelectOption, FormSelectOptionGroup, InputGroup, InputGroupText, Level, LevelItem, Modal, Title, TitleLevel, TextInput, Tooltip
} from '@patternfly/react-core';
import { Link } from "react-router-dom";
import { Revisions } from '@app/revisions';
import { HelpIcon, ThIcon, CaretDownIcon } from '@patternfly/react-icons';
import { isTemplateElement } from '@babel/types';
import { Redirect } from 'react-router-dom'

export interface IProps {
    moduleName: string
    modulePath: string
    moduleType: string
    updated: string
}

class ModuleDisplay extends Component<IProps> {
    public state = {
        allProducts: '',
        formInvalid: false,
        initialLoad: true,
        isDup: false,
        isEmptyResults: false,
        isMissingFields: false,
        isModalOpen: false,
        isProductDropdownOpen: false,
        isUsecaseDropdownOpen: false,
        isVersionDropdownOpen: false,
        loggedinStatus: false,
        login: false,
        moduleUrl: '',

        productOptions: [
            { value: 'Select a Product', label: 'Select a Product', disabled: false },
        ],
        productValue: '',
        redirect: false,
        results: [],
        successAlertVisble: false,
        usecaseOptions: [
            { value: 'Select Use Case', label: 'Select Use Case', disabled: false }
        ],
        usecaseValue: '',
        usecases: ['Administer', 'Deploy', 'Develop', 'Install', 'Migrate', 'Monitor', 'Network',
            'Plan', 'Provision', 'Release', 'Troubleshoot', 'Optimize'],

        versionOptions: [
            { value: 'Select a Version', label: 'Select a Version', disabled: false },
        ],
        versionValue: '',
        draftPath: '',
        draftUpdateDate: '',
        releasePath: '',
        releaseUpdateDate: ''
    };

    public render() {
        const { isModalOpen, isDup, isMissingFields, productOptions, moduleUrl, successAlertVisble, productValue, usecaseValue, usecaseOptions, usecases, versionOptions, versionValue } = this.state;
        const id = 'userID';

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

        let verOptions = versionOptions
        if (this.state.allProducts[productValue]) {
            verOptions = this.state.allProducts[productValue]
        }

        const ucOptions = usecaseOptions
        usecases.map((item) => (
            ucOptions.push({ value: item, label: item, disabled: false })
        ))

        if (!this.state.loggedinStatus && this.state.initialLoad === true) {
            fetch("/system/sling/info.sessionInfo.json")
                .then(response => response.json())
                .then(responseJSON => {
                    if (responseJSON[id]) {
                        if (responseJSON[id] !== 'anonymous') {
                            this.setState({ loggedinStatus: true })
                        }
                    }
                })
        }

        return (
            <React.Fragment>
                {successAlertVisble && <Alert
                    variant="success"
                    title="Edit Metadata"
                    action={<AlertActionCloseButton onClose={this.hideSuccessAlert} />}
                >
                    Update Successful!
          </Alert>
                }
                {this.state.initialLoad && this.fetchProductVersionDetails()}
                <div>
                    <Breadcrumb>
                        <BreadcrumbItem to="#">Modules</BreadcrumbItem>
                        <BreadcrumbItem to="#" isActive={true}>
                            {this.props.moduleName}
                        </BreadcrumbItem>
                    </Breadcrumb>
                </div>
                <div>
                    <Level gutter="md">
                        <LevelItem>
                            <TextContent>
                                <Text component={TextVariants.h1}>{this.props.moduleName}{'  '}
                                    <Tooltip
                                        position="right"
                                        content={
                                            <div>Title updated in latest revision</div>
                                        }>
                                        <span><HelpIcon /></span>
                                    </Tooltip>
                                </Text>
                            </TextContent>
                        </LevelItem>
                        <LevelItem />
                        <LevelItem>
                            <Button variant="secondary" onClick={this.handleModalToggle}>Edit metadata</Button>
                        </LevelItem>
                    </Level>
                </div>
                <div>
                    <a href='http://access.redhat.com'>View on Customer Portal</a>
                </div>
                <div>
                    <DataList aria-label="single action data list example ">
                        <DataListItem aria-labelledby="simple-item1">
                            <DataListItemRow id="data-rows-header" >
                                <DataListItemCells
                                    dataListCells={[
                                        <DataListCell width={2} key="products">
                                            <span className="sp-prop-nosort" id="span-source-type">Products</span>
                                        </DataListCell>,
                                        <DataListCell key="published">
                                            <span className="sp-prop-nosort" id="span-source-type">Published</span>
                                        </DataListCell>,
                                        <DataListCell key="updated">
                                            <span className="sp-prop-nosort" id="span-source-type">Draft Uploaded</span>
                                        </DataListCell>,
                                        <DataListCell key="module_type">
                                            <span className="sp-prop-nosort" id="span-source-name">Module Type</span>
                                        </DataListCell>
                                    ]}
                                />
                            </DataListItemRow>

                            <DataListItemRow>
                                <DataListItemCells
                                    dataListCells={[
                                        <DataListCell width={2} key="products">
                                            <span>Dummy Product Name</span>
                                        </DataListCell>,
                                        <DataListCell key="published">
                                            <span>{this.state.releaseUpdateDate.substring(4,15)}</span>
                                        </DataListCell>,
                                        <DataListCell key="updated">
                                            <span>{this.state.draftUpdateDate.substring(4,15)}</span>
                                        </DataListCell>,
                                        <DataListCell key="module_type">
                                            <span>{this.props.moduleType}</span>
                                        </DataListCell>,
                                    ]}
                                />
                            </DataListItemRow>
                            ))}
                    </DataListItem>
                    </DataList>
                </div>
                <div>
                    <Card>
                        <Revisions
                            modulePath={this.props.modulePath}
                            revisionModulePath={this.props.moduleName}
                            draftUpdateDate={this.updateDate}
                            releaseUpdateDate={this.updateDate}
                        />
                    </Card>
                </div>
                <Modal
                    width={'60%'}
                    title="Edit metadata"
                    isOpen={isModalOpen}
                    header={header}
                    ariaDescribedById="edit-metadata"
                    onClose={this.handleModalToggle}
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
                        {/* {this.checkAuth()} */}
                        {this.loginRedirect()}
                        {this.renderRedirect()}
                    </div>
                    <div className="app-container">

                        {isMissingFields && (
                            <div className="notification-container">
                                <Alert
                                    variant="warning"
                                    title="all fields are required."
                                    action={<AlertActionCloseButton onClose={this.dismissNotification} />}
                                />
                            </div>
                        )}
                        {isDup && (
                            <div className="notification-container">
                                <Alert
                                    variant="warning"
                                    title="Duplicated url fragment."
                                    action={<AlertActionCloseButton onClose={this.dismissNotification} />}
                                />
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
                                <FormSelect value={productValue} onChange={this.onChangeProduct} aria-label="FormSelect Product">
                                    {productOptions.map((option, index) => (
                                        <FormSelectOption isDisabled={option.disabled} key={index} value={option.value} label={option.label} />
                                    ))}
                                </FormSelect>
                                <FormSelect value={versionValue} onChange={this.onChangeVersion} aria-label="FormSelect Version" id="productVersion">
                                    {verOptions.map((option) => (

                                        <FormSelectOption isDisabled={false} key={option.value} value={option.value} label={option.label} />
                                    ))}
                                </FormSelect>
                            </InputGroup>
                        </FormGroup>
                        <FormGroup
                            label="Document use case"
                            isRequired={true}
                            fieldId="document-usecase"
                        >
                            <FormSelect value={usecaseValue} onChange={this.onChangeUsecase} aria-label="FormSelect Usecase">
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
                                <TextInput isRequired={true} id="url-fragment" type="text" placeholder="Enter URL" value={moduleUrl} onChange={this.handleURLInput} />
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

    private handleModalToggle = () => {
        this.setState({
            isModalOpen: !this.state.isModalOpen
        });
    }

    private saveMetadata = (event) => {
        // save form data
        if (this.state.productValue === "" || this.state.versionValue === "" ||
            this.state.usecaseValue === "" || this.state.moduleUrl === "") {
            this.setState({ isMissingFields: true })
            this.setState({ formInvalid: true })

        } else if (this.moduleUrlExist(this.state.moduleUrl)) {
            this.setState({ isDup: true })
            this.setState({ formInvalid: true })
        } else {
            const hdrs = {
                'Accept': 'application/json',
                'cache-control': 'no-cache'
            }

            // console.log("event.target ", event.target.form)
            const formData = new FormData(event.target.form);

            formData.append("productVersion", this.state.versionValue)
            formData.append("documentUsecase", this.state.usecaseValue)
            formData.append("urlFragment", "/" + this.state.moduleUrl)

            fetch('/content/' + this.props.modulePath + '/metadata', {
                body: formData,
                headers: hdrs,
                method: 'post'
            }).then(response => {
                if (response.status === 201 || response.status === 200) {
                    console.log("successful edit ", response.status)
                    // this.setState({ redirect: true, successAlertVisble: true })
                    this.handleModalToggle()
                    this.setState({ successAlertVisble: true })
                } else if (response.status === 500) {
                    // console.log(" Needs login " + response.status)
                    this.setState({ login: true })
                } else {
                    console.log(" Failed " + response.status)
                    this.setState({ failedPost: true })
                }
            });
        }
    }
    private onChangeProduct = (productValue, event) => {
        this.setState({ productValue });
    }

    private onChangeVersion = (versionValue, event) => {
        this.setState({ versionValue });
    }

    private onChangeUsecase = (usecaseValue, event) => {
        this.setState({ usecaseValue });
    }

    private handleURLInput = moduleUrl => {
        this.setState({ moduleUrl });

        // check for duplcated product URL.
        this.moduleUrlExist(this.state.moduleUrl);
        if (this.state.isDup) {
            this.setState({ formInvalid: true });
        }
    }

    private moduleUrlExist = (moduleUrl) => {
        this.setState({ initialLoad: false })
        fetch(this.getModuleUrl(moduleUrl))
            .then(response => response.json())
            .then(responseJSON => this.setState({ results: responseJSON.results }))
            .then(() => {
                // console.log("[moduleUrlExist] results breakdown " + JSON.stringify(this.state.results))

                if (JSON.stringify(this.state.results) === "[]") {
                    this.setState({
                        isDup: false
                    });
                } else {
                    this.setState({
                        isDup: true
                    });
                }
            })
        return this.state.isDup
    }

    private fetchProductVersionDetails = () => {
        this.setState({ initialLoad: false })
        const path = '/content/products.3.json'
        let key
        const products = new Array()

        fetch(path)
            .then((response) => {
                if (response.ok) {
                    // console.log("[responseJSON] response.ok ", response.json())
                    return response.json();
                } else if (response.status === 404) {
                    console.log("Something unexpected happen!")
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
                    }
                }
            })
            .catch((error) => {
                console.log(error)
            });
        // console.log("products: ", products)
        // console.log("productOptions ", this.state.productOptions)
        // console.log("versionOptions ", this.state.versionOptions)
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

    private checkAuth = () => {
        fetch("/system/sling/info.sessionInfo.json")
            .then(response => response.json())
            .then(responseJSON => {
                const key = "userID"
                if (responseJSON[key] === 'anonymous') {
                    this.setState({ login: true })
                }
            })
    }

    private dismissNotification = () => {
        if (this.state.isMissingFields === true) {
            this.setState({ isMissingFields: false });
        }

        if (this.moduleUrlExist(this.state.moduleUrl) === false) {
            this.setState({ isDup: false });
        }
    }

    private hideSuccessAlert = () => {
        this.setState({ successAlertVisble: false })
    }
    private updateDate = (date,type,path) => {
        if(type==="draft"){
            this.setState({
                draftUpdateDate: date,
                draftPath: path
            },() => {
                console.log('changed draft date: ', this.state.draftUpdateDate, "version path: ",this.state.draftPath)
            });    
        }
        else{
            this.setState({
                releaseUpdateDate: date,
                releasePath: path
            },() => {
                console.log('changed release date: ', this.state.releaseUpdateDate, "version path: ",this.state.releasePath)
            });
        }
      };
}

export { ModuleDisplay }