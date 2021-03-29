import React from 'react';

import "@app/app.css";
import { BulkOperationConfirmation } from './bulkOperationConfirmation';
import { Alert, BaseSizes, Button, Form, FormAlert, FormGroup, FormSelect, FormSelectOption, InputGroup, Modal, ModalVariant, TextInput, Title } from '@patternfly/react-core';
import { Metadata } from './Constants';
import { Utils } from './Utils';

export interface IBulkOperationMetadataProps {
    documentsSelected: Array<{ cells: [string, { title: { props: { href: string } } }, string, string, string], selected: boolean }>
    contentTypeSelected: string
    isEditMetadata: boolean
    updateIsEditMetadata: (isEditMetadata) => any
}

class BulkOperationMetadata extends React.Component<IBulkOperationMetadataProps, any>{

    constructor(props) {
        super(props);
        this.state = {
            isModalOpen: true,
            contentTypeSelected: "",
            alertTitle: "",
            allProducts: [],
            allProductVersions: [],
            isMissingFields: false,
            productValidated: "error",
            productVersionValidated: "error",
            useCaseValidated: "error",
            product: { label: "", value: "" },
            productVersion: { label: "", uuid: "" },
            showBulkConfirmation: true,
            keywords: "",
            usecaseOptions: [
                { value: "", label: "Select Use Case", disabled: false }
            ],
            usecaseValue: "",
            metadataEditError: "",

            //progress bar
            progressFailureValue: 0,
            progressSuccessValue: 0,
            progressWarningValue: 0,
            bulkUpdateFailure: 0,
            bulkUpdateSuccess: 0,
            bulkUpdateWarning: 0,

            documentsSucceeded: [""],
            documentsFailed: [""],
            documentsIgnored: [""],
            confirmationBody: "",
            confirmationSucceeded: "",
            confirmationIgnored: "",
            confirmationFailed: "",
        };

    }

    public componentDidMount() {
        // fetch products and label for metadata Modal
        this.fetchProducts()
    }

    public render() {
        const { isModalOpen } = this.state;
        const header = (
            <React.Fragment>
                <Title headingLevel="h1" size={BaseSizes["2xl"]}>
                    Edit Metadata
              </Title>
            </React.Fragment>
        )
        const metadataModal = (
            <React.Fragment>
                <Modal
                    variant={ModalVariant.medium}
                    title="Edit metadata"
                    isOpen={this.state.isModalOpen}
                    header={header}
                    aria-label="Edit metadata"
                    onClose={this.handleModalClose}
                    actions={[
                        <Button form="bulk_edit_metadata" isAriaDisabled={this.props.documentsSelected.length === 0 ? true : false} key="confirm" variant="primary" onClick={this.saveMetadata}>
                            Save
                </Button>,
                        <Button data-testid="metadata-modal-cancel-button" key="cancel" variant="secondary" onClick={this.handleModalToggle}>
                            Cancel
                  </Button>
                    ]}
                >
                    <div id="edit_metadata_helper_text"><p>Editing {this.props.documentsSelected.length} {this.props.contentTypeSelected === "module" ? "modules" : "assemblies"}. Changes made apply to all selected docs.</p></div>
                    <br />
                    <Form isWidthLimited={true} id="bulk_edit_metadata">
                        {this.state.isMissingFields
                            && (
                                <FormAlert>
                                    <Alert
                                        variant="danger"
                                        title="You must fill out all required fields before you can proceed."
                                        aria-live="polite"
                                        isInline={true}
                                    />
                                    <br />
                                </FormAlert>
                            )}

                        {this.state.metadataEditError
                            && (
                                <FormAlert>
                                    <Alert
                                        variant="danger"
                                        title={this.state.metadataEditError}
                                        aria-live="polite"
                                        isInline={true}
                                    />
                                    <br />
                                </FormAlert>
                            )}
                        <FormGroup
                            label="Product Name"
                            isRequired={true}
                            fieldId="product-name"
                            validated={this.state.productValidated}
                        >
                            <InputGroup>
                                <FormSelect value={this.state.product.value} onChange={this.onChangeProduct} aria-label="FormSelect Product" name="product" isRequired={true} validated={this.state.productValidated}>
                                    <FormSelectOption label="Select a Product" />
                                    {this.state.allProducts.map((option, key) => (
                                        <FormSelectOption key={key} value={option.value} label={option.label} />
                                    ))}
                                </FormSelect>
                                <FormSelect value={this.state.productVersion.uuid} onChange={this.onChangeVersion} aria-label="FormSelect Version" id="productVersion" name="productVersion" isRequired={true} validated={this.state.productVersionValidated}>
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
                            validated={this.state.useCaseValidated}
                        >
                            <FormSelect value={this.state.usecaseValue} onChange={this.onChangeUsecase} aria-label="FormSelect Usecase" name="useCase" isRequired={true} validated={this.state.useCaseValidated}>
                                {Metadata.USE_CASES.map((option, key) => (
                                    <FormSelectOption key={"usecase_" + key} value={option} label={option} />
                                ))}
                            </FormSelect>
                        </FormGroup>
                        <FormGroup
                            label="Vanity URL fragment"
                            fieldId="url-fragment"
                            helperText="Edit individually to set or change vanity URL."
                        >
                        </FormGroup>
                        <FormGroup
                            label="Search keywords"
                            isRequired={false}
                            fieldId="search-keywords"
                        >
                            <InputGroup>
                                <TextInput isRequired={false} id="search-keywords" type="text" placeholder="cat, dog, bird..." value={this.state.keywords} onChange={this.handleKeywordsInput} name="keywords" />
                            </InputGroup>
                        </FormGroup>
                        <div>
                            <input name="productVersion@TypeHint" type="hidden" value="Reference" />
                        </div>
                    </Form>
                </Modal>
            </React.Fragment>
        );

        return (
            <React.Fragment>
                {this.state.showBulkConfirmation &&
                    <BulkOperationConfirmation
                        header="Bulk Edit"
                        subheading="Documents updated in the bulk operation"
                        updateSucceeded={this.state.confirmationSucceeded}
                        updateIgnored={this.state.confirmationIgnored}
                        updateFailed={this.state.confirmationFailed}
                        footer=""
                        progressSuccessValue={this.state.progressSuccessValue}
                        progressFailureValue={this.state.progressFailureValue}
                        progressWarningValue={this.state.progressWarningValue}
                        onShowBulkOperationConfirmation={this.updateShowBulkEditConfirmation}
                        onBulkOperationError={this.updateBulkOperationError}
                    />}

                {this.props.isEditMetadata && metadataModal}
            </React.Fragment>
        );
    }

    private handleModalClose = () => {
        this.setState({ isModalOpen: false, showBulkConfirmation: false }, () => {
            // User clicked on the close button without saving the metadata
            if (this.state.documentsSucceeded.length === 0 &&
                this.state.documentsFailed.length === 0 &&
                this.state.documentsIgnored.length === 0) {
                this.props.updateIsEditMetadata(false)
            }

        })
    }

    private handleModalToggle = (event) => {
        this.setState({ isModalOpen: !this.state.isModalOpen, showBulkConfirmation: false }, () => {
            this.props.updateIsEditMetadata(false)
        })
    }

    private onChangeProduct = (productValue: string, event: React.FormEvent<HTMLSelectElement>) => {
        let productLabel = ""
        const target = event.nativeEvent.target
        if (target !== null) {
            // Necessary because target.selectedOptions produces a compiler error but is valid
            // tslint:disable-next-line: no-string-literal
            productLabel = target["selectedOptions"][0].label
        } else {
            this.setState({ productValidated: "error" })
        }
        this.setState({
            product: { label: productLabel, value: productValue },
            productVersion: { label: "", uuid: "" }
        })

        if (productValue.length > 0) {
            this.setState({ productValidated: "success" })
        } else {
            this.setState({ productValidated: "error" })
        }
        this.populateProductVersions(productValue)
    }

    private populateProductVersions = (productValue) => {
        if (productValue.length > 0) {
            fetch("/content/products/" + productValue + "/versions.harray.1.json")
                .then(response => response.json())
                .then(json => {
                    this.setState({ allProductVersions: json.__children__ })
                })
        }
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
            if (selectedOption.value !== "") {
                this.setState({ productVersionValidated: "success" })
            } else {
                this.setState({ productVersionValidated: "error" })
            }
        }
    }

    private onChangeUsecase = (usecaseValue, event) => {
        if (event != undefined) {
            this.setState({ usecaseValue: event.target.value })

            if (event.target.value !== "" && event.target.value.trim() !== "Select Use Case") {
                this.setState({ useCaseValidated: "success" })
            } else {
                this.setState({ useCaseValidated: "error" })
            }
        } else {
            this.setState({ useCaseValidated: "error" })
        }
    }

    private handleKeywordsInput = (keywords, event) => {
        if (event != undefined) {
            this.setState({ keywords: event.target.value })
        }
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
                if (responseJSON.__children__ !== undefined) {
                    for (const product of responseJSON.__children__) {
                        if (product.name !== undefined) {
                            products.push({ label: product.name, value: product.__name__ })
                        }
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

    private saveMetadata = (event) => {
        if (this.state.productValidated === "error"
            || this.state.productVersionValidated === "error"
            || this.state.useCaseValidated === "error") {
            this.setState({ isMissingFields: true })
        } else {
            const hdrs = {
                "Accept": "application/json",
                "cache-control": "no-cache"
            }

            const metadataForm = event.target.form
            const formData = new FormData(metadataForm)

            formData.append("documentUsecase", this.state.usecaseValue)
            formData.append("searchKeywords", this.state.keywords === undefined ? "" : this.state.keywords)

            this.props.documentsSelected.map((r) => {
                if (r.cells[1].title.props.href) {
                    let href = r.cells[1].title.props.href

                    let variant = href.split("?variant=")[1]
                    let hrefPart = href.slice(0, href.indexOf("?"))
                    let docPath = hrefPart.match("/repositories/.*") ? hrefPart.match("/repositories/.*") : ""

                    // check draft version
                    const backend = "/content" + docPath + "/en_US/variants/" + variant + "/draft/metadata"
                    console.log('METADATA BACKEND VALUE', backend)
                    this.setState({ showBulkConfirmation: true })
                    Utils.draftExist(backend).then((exist) => {
                        if (exist) {
                            // Process form for each docPath
                            fetch(backend, {
                                body: formData,
                                headers: hdrs,
                                method: "post"
                            }).then(response => {
                                if (response.status === 201 || response.status === 200) {

                                    let docs = new Array()
                                    docs = this.state.documentsSucceeded
                                    docs.push(docPath)
                                    this.setState({
                                        documentsSucceeded: docs,
                                        usecaseValue: "",
                                        product: { label: "", value: "" },
                                        productVersion: { label: "", uuid: "" },
                                        keywords: "",
                                        productValidated: "error",
                                        productVersionValidated: "error",
                                        useCaseValidated: "error",
                                        bulkUpdateSuccess: this.state.bulkUpdateSuccess + 1,
                                    }, () => {
                                        this.handleModalClose()

                                        this.calculateSuccessProgress(this.state.bulkUpdateSuccess)
                                    })
                                } else {

                                    let docs = new Array()
                                    docs = this.state.documentsFailed
                                    docs.push(docPath)
                                    // update state for progressbar
                                    this.setState({ bulkUpdateFailure: this.state.bulkUpdateFailure + 1, documentsFailed: docs }, () => {
                                        this.calculateFailureProgress(this.state.bulkUpdateFailure)
                                    })

                                }
                            })
                        } else {
                            // draft does not exist
                            let docs = new Array()
                            docs = this.state.documentsIgnored
                            docs.push(docPath)
                            this.setState({ bulkUpdateWarning: this.state.bulkUpdateWarning + 1, documentsIgnored: docs }, () => {

                                this.calculateWarningProgress(this.state.bulkUpdateWarning)
                                if (this.state.bulkUpdateWarning > 0 && this.state.bulkUpdateWarning === this.props.documentsSelected.length) {

                                    this.setState({ metadataEditError: "No draft versions found on selected items. Unable to save metadata." })
                                }
                            })
                        }
                    })
                }
            })
        }
    }


    private updateShowBulkEditConfirmation = (showBulkConfirmation) => {
        this.setState({ showBulkConfirmation })
    }

    private updateBulkOperationError = (metadataEditError) => {
        this.setState({ metadataEditError })
    }

    private calculateFailureProgress = (num: number) => {
        if (num >= 0) {
            let stat = (num) / this.props.documentsSelected.length * 100
            this.setState({ progressFailureValue: stat, showBulkConfirmation: true }, () => {
                this.getDocumentFailed()
            })
        }
    }

    private calculateSuccessProgress = (num: number) => {
        if (num >= 0) {
            let stat = (num) / this.props.documentsSelected.length * 100
            this.setState({ progressSuccessValue: stat, showBulkConfirmation: true }, () => {
                this.getDocumentsSucceeded()
            })
        }
    }

    private calculateWarningProgress = (num: number) => {
        if (num >= 0) {
            let stat = (num) / this.props.documentsSelected.length * 100
            this.setState({ progressWarningValue: stat, showBulkConfirmation: true }, () => {
                this.getDocumentIgnored()
            })
        }
    }
    private getDocumentsSucceeded = () => {
        if (this.state.documentsSucceeded.length > 0) {
            let succeeded = this.state.documentsSucceeded.join(",")
            this.setState({ confirmationSucceeded: succeeded })
        }
    }

    private getDocumentIgnored = () => {
        if (this.state.documentsIgnored.length > 0) {
            let ignored = this.state.documentsIgnored.join(",")
            this.setState({ confirmationIgnored: ignored })
        }
    }

    private getDocumentFailed = () => {
        if (this.state.documentsFailed.length > 0) {
            let failed = this.state.documentsFailed.join(",")
            this.setState({ confirmationFailed: failed })
        }
    }
}
export { BulkOperationMetadata }