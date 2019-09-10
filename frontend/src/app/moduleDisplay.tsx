import React, { Component } from 'react';
import {
    Breadcrumb, BreadcrumbItem, BreadcrumbHeading, BaseSizes, Button, DataList, DataListItem, DataListItemRow, DataListItemCells,
    DataListCell, Card, Text, TextContent, TextVariants, Grid, GridItem, Dropdown, DropdownToggle, DropdownItem, DropdownSeparator, Form,
    FormGroup, FormSelect, FormSelectOption, FormSelectOptionGroup, InputGroup, InputGroupText, Level, LevelItem, Modal, Title, TitleLevel, TextInput, Tooltip
} from '@patternfly/react-core';
import { Link } from "react-router-dom";
import { Revisions } from '@app/revisions';
import { HelpIcon, ThIcon, CaretDownIcon } from '@patternfly/react-icons';
import { isTemplateElement } from '@babel/types';

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
        productOptions: [
            { value: 'Select a Product', label: 'Select a Product', disabled: false },
        ],
        productUrl: '',
        productValue: '',
        results: [],
        usecaseValue: '',
        versionOptions: [
            { value: 'Select a Version', label: 'Select a Version', disabled: false },

        ],
        versionValue: '',


    };

    public render() {
        const { isModalOpen, productOptions, productUrl, productValue, usecaseValue, versionOptions, versionValue } = this.state;
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

        // console.log("verOptions ", verOptions)
        const usecaseOptions = [
            { value: 'Select Use Case', label: 'Select Use Case', disabled: false },
            { value: 'CONCEPT', label: 'Concept', disabled: false },
            { value: 'PROCEDURE', label: 'Procedure', disabled: false },
            { value: 'REFERENCE', label: 'Reference', disabled: false }
        ];

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
                                            <span className="sp-prop-nosort" id="span-source-type">Updated</span>
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
                                            <span>Dummy Publish</span>
                                        </DataListCell>,
                                        <DataListCell key="updated">
                                            <span>{this.props.updated}</span>
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
                        <Button key="confirm" variant="primary" onClick={this.handleModalToggle}>
                            Save
          </Button>,
                        <Button key="cancel" variant="secondary" onClick={this.handleModalToggle}>
                            Cancel
            </Button>
                    ]}
                >
                    <Form isHorizontal={true}>
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
                                <FormSelect value={versionValue} onChange={this.onChangeVersion} aria-label="FormSelect Version">
                                    {verOptions.map((option) => (
                                        <FormSelectOption isDisabled={false} key={option.value} value={option.value} label={option.value} />
                                    ))}
                                </FormSelect>
                            </InputGroup>
                        </FormGroup>
                        <FormGroup
                            label="Document use case"
                            isRequired={true}
                            fieldId="document-usecase"
                        >
                            <FormSelect value={usecaseValue} onChange={this.onChangeUsecase} aria-label="FormSelect Product">
                                {usecaseOptions.map((option, index) => (
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
                                <TextInput isRequired={true} id="url-fragment" type="text" placeholder="Enter URL" value={productUrl} onChange={this.handleURLInput} />
                            </InputGroup>
                        </FormGroup>
                    </Form>
                </Modal>
            </React.Fragment>

        );
    }

    private handleModalToggle = () => {
        this.setState({
            isModalOpen: !this.state.isModalOpen
        });
        // save form data
        if (this.state.productUrl === "") {
            this.setState({ isMissingFields: true })
            this.setState({ formInvalid: true })

        } else if (this.productUrlExist(this.state.productUrl)) {
            this.setState({ isDup: true })
            this.setState({ formInvalid: true })
        } else {
            const hdrs = {
                'Accept': 'application/json',
                'cache-control': 'no-cache'
            }

            const formData = new FormData();
            formData.append("productName", this.state.productValue)
            formData.append("versionName", this.state.versionValue)
            formData.append("moduleType", this.state.usecaseValue)

            formData.append("urlFragment", "/" + this.state.productUrl)

            fetch('/content/' + this.props.modulePath + '/metadata', {
                body: formData,
                headers: hdrs,
                method: 'post'
            }).then(response => {
                if (response.status === 201 || response.status === 200) {
                    this.setState({ redirect: true })
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

    private handleURLInput = productUrl => {
        this.setState({ productUrl });

        // check for duplcated product URL.
        this.productUrlExist(this.state.productUrl);
        if (this.state.isDup) {
            this.setState({ formInvalid: true });
        }
    }

    private productUrlExist = (productUrl) => {
        this.setState({ initialLoad: false })
        fetch(this.getProductUrl(productUrl))
            .then(response => response.json())
            .then(responseJSON => this.setState({ results: responseJSON.results }))
            .then(() => {
                // console.log("[productUrlExist] results breakdown " + JSON.stringify(this.state.results))

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
                                for (const item in Object.keys(versionObj)) {
                                    if (Object.keys(versionObj)[item] !== undefined) {
                                        vKey = Object.keys(versionObj)[item]

                                        if (vKey !== 'jcr:primaryType') {
                                            if (versionObj[vKey][nameKey]) {

                                                versions.push({ id: vKey, value: versionObj[vKey][nameKey] })
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
                    // generate options for versions.
                    // console.log("[generate versionOptions] productValue ", this.state.productValue)

                    if (products[this.state.productValue]) {
                        const versions = products[this.state.productValue];
                        // console.log("[generate versionOptions] versions ", versions)
                        if (versions) {
                            const versionItems = [{ value: 'Select a Version', label: 'Select a Version', disabled: false }]
                            for (const item in versions) {
                                if (item !== undefined) {
                                    versionItems.push({ value: item, label: item, disabled: false })
                                }

                            }

                            if (versionItems.length > 1) {
                                this.setState({ versionOptions: versionItems })
                            }
                        }

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

    private getProductUrl = (productUrl) => {
        const backend = '/content/products.query.json?nodeType=pant:product&where=[url]="' + productUrl + '"'
        return backend

    }
}

export { ModuleDisplay }