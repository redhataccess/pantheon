import React from 'react';

import "@app/app.css";
import { BulkOperationConfirmation } from './bulkOperationConfirmation';
import { Alert, BaseSizes, Button, Form, FormAlert, FormGroup, FormSelect, FormSelectOption, InputGroup, Modal, ModalVariant, TextInput, Title } from '@patternfly/react-core';
import { Fields, Metadata, PathPrefixes } from './Constants';
import { Utils } from './Utils';

export interface IBulkOperationPublishProps {
    documentsSelected: Array<{ cells: [string, { title: { props: { children: string[], href: string } } }, string, string, string], selected: boolean }>
    contentTypeSelected: string
    isBulkPublish: boolean
    updateIsBulkPublish: (isBulkPublish) => any
}

class BulkOperationPublish extends React.Component<IBulkOperationPublishProps, any>{

    constructor(props) {
        super(props);
        this.state = {
            isModalOpen: true,

            alertTitle: "",
            allProducts: [],
            allProductVersions: [],
            isMissingFields: false,
            productValidated: "error",
            productVersionValidated: "error",
            useCaseValidated: "error",
            product: { label: "", value: "" },
            productVersion: { label: "", uuid: "" },
            showBulkEditConfirmation: true,
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
        // this.fetchProducts()
    }

    public render() {
        const { isModalOpen } = this.state;
        const publishHeader = (
          <React.Fragment>
            <Title headingLevel="h1" size={BaseSizes["2xl"]}>
              Publish
            </Title>
          </React.Fragment>
        )
        const publishModal = (
          <React.Fragment>
            <Modal
              variant={ModalVariant.medium}
              title="Publish"
              isOpen={this.state.isModalOpen}
              header={publishHeader}
              aria-label="Publish"
              onClose={this.handleModalClose}
              actions={[
                <Button form="bulk_publish" key="publish" variant="primary" onClick={this.onBulkPublish}>
                  Publish
              </Button>,
                <Button key="cancel" variant="secondary" onClick={this.handleModalToggle}>
                  Cancel
                </Button>
              ]}
            >
              <div>
            {this.props.contentTypeSelected == 'module' ? <div id="publish__module_helper_text"><p>Publishing <b>{this.props.documentsSelected.length}</b> module{this.props.documentsSelected.length > 1 ? 's.' : '.'}</p></div> : <div id="publish__assembly_helper_text"><p>Publishing <b>{this.props.documentsSelected.length}</b> assembl{this.props.documentsSelected.length > 1 ? 'ies.' : 'y.'}</p></div>}
            <br/>
            <p>Publishing modules does <b>not</b> publish assemblies including these modules</p>
            <br/>
            <p>Publishing assemblies <b>does</b> publish all included draft modules. Modules removed from the published assembly are still published as separate modules.</p>
            <br/>
            <p>If metadata is missing from any selected or included docs, they will <b>not</b> be published.</p>
            <br/>
            <p>The publish process may take a while to update all files</p>
            </div>
            </Modal>
          </React.Fragment>
        );

        const publishConfirmationHeader = (
          <React.Fragment>
            <Title headingLevel="h1" size={BaseSizes["2xl"]}>
              Published Message
            </Title>
          </React.Fragment>
        )

        // const publishConfirmationModal = (
        //   <React.Fragment>
        //     <Modal
        //       variant={ModalVariant.medium}
        //       title="Documents Successfully Published"
        //       isOpen={this.state.showPublishMessage}
        //       header={publishConfirmationHeader}
        //       aria-label="Documents Successfully Published"
        //       onClose={this.handlePublishConfirmationModalClose}
        //     >
        //       <div>
        //     {this.state.documentTitles.length > 0 ? this.state.documentTitles.map(title => <p>{title}</p>) : <p>Documents failed to publish. Please ensure no metadata is missing from selected docs.</p>}
        //     </div>
        //     </Modal>
        //   </React.Fragment>
        // );

        return (
            <React.Fragment>
                {/* {this.state.showBulkEditConfirmation &&
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
                        onShowBulkEditConfirmation={this.updateShowBulkEditConfirmation}
                        onMetadataEditError={this.updateMetadataEditError}
                    />} */}

                {this.props.isBulkPublish && publishModal}
            </React.Fragment>
        );
    }

    private handleModalClose = () => {
        this.setState({ isModalOpen: false, showBulkEditConfirmation: false }, () => {
            // User clicked on the close button without saving the metadata
            // if (this.state.documentsSucceeded.length === 0 &&
            //     this.state.documentsFailed.length === 0 &&
            //     this.state.documentsIgnored.length === 0) {
            //     this.props.updateIsEditMetadata(false)
            // }

        })
    }

    private handleModalToggle = (event) => {
        this.setState({ isModalOpen: !this.state.isModalOpen
          // showBulkEditConfirmation: false 
        })
    }

    private onBulkPublish = (event) => {
      console.log('published clicked', event)
        // Validate productValue before Publish
        // if (this.props.productInfo !== undefined && this.props.productInfo.trim() === "") {
        //     this.setState({ canChangePublishState: false})
            //TODO add publish alert
        // } else {
  
            // if (this.state.canChangePublishState === true) {
                const formData = new FormData();
  
                    formData.append(":operation", "pant:publish");
                    // console.log("Published file path:", this.props.modulePath)
                    // this.draft[0].version = "";
                    // this.onPublishEvent()
  
                const hdrs = {
                    "Accept": "application/json",
                    "cache-control": "no-cache",
                    "Access-Control-Allow-Origin": "*",
                }
                this.props.documentsSelected.map((r) => {
                  console.log("[saveMetadata] documentsSelected href =>", r.cells[1].title.props.href)
                  if (r.cells[1].title.props.href) {
                    let href = r.cells[1].title.props.href
                    let documentTitle = r.cells[1].title.props.children[1]
  
                    // let link = href.split("pantheon/#", "?variant=")
                    
                    let variant = href.split("?variant=")[1]
                    
  
  
                    //href part is module path
                    let hrefPart = href.slice(0, href.indexOf("?"))
                    let docPath = hrefPart.match("/repositories/.*") ? hrefPart.match("/repositories/.*") : ""
                    let path = hrefPart.slice(hrefPart.indexOf("/module"))
                    let modulePath = hrefPart.slice(hrefPart.indexOf("/repositories"))
  
                    console.log('path', path)
                    console.log('variant', variant)
                    console.log('hrefPart', hrefPart)
                    console.log('docPath', docPath)
                    console.log('modulePath', modulePath)
                    // this.onPublishEvent(variant, path)
                formData.append("locale", "en_US")
                formData.append("variant", variant)
                fetch("/content" + modulePath, {
                  body: formData,
                  method: "post",
                  headers: hdrs
              }).then(response => {
                  if (response.status === 201 || response.status === 200) {
                      console.log("publish works: " + response.status)
                      this.setState({
                          canChangePublishState: true,
                          // documentTitles: [...this.state.documentTitles, documentTitle],
                          isBulkPublish: false,
                          showPublishMessage: true,
                          // documentsSelected: []
                      })
                  } else {
                      console.log("publish failed " + response.status)
                      this.setState({
                        isBulkPublish: false,
                        showPublishMessage: true,
                      })
  
                      // this.setAlertTitle()
                  // }    this.fetchVersions()
                  // return response.json()
              // }).then(response => this.props.onGetUrl(response.path));
                    }
            })
          }
                })
  }
  
  private getPortalUrl = (path, variant) => {
    const variantPath = "/content" + path + "/en_US/variants/" + variant + ".url.txt"
    console.log('getPortalUrl variantPath', variantPath)
    fetch(variantPath)
        .then(resp => {
            if (resp.ok) {
                resp.text().then(text => {
                    // get portal url from api and set it only if it is not empty
                    if(text.trim() !== "") {
                        this.setState({portalUrl: text})
                    }else{
                        // if portal url is empty, assemble the URL at client side
                        console.log("GetPortalURI API returned empty URI. Falling back to url construction at UI")
                        this.getVersionUUID(path)
                    }
                })
            }else {
                console.log("GetPortalURI API returned error. Falling back to url construction at UI")
                this.getVersionUUID(path)
            }
        })
  }
  
  private getVersionUUID = (path) => {
    // remove /module from path
    path =  path.substring(this.state.contentTypeSelected == "assembly" ? PathPrefixes.ASSEBMLY_PATH_PREFIX.length : PathPrefixes.MODULE_PATH_PREFIX.length)
    // path = "/content" + path + "/en_US/1/metadata.json"
    path = "/content" + path + "/en_US.harray.4.json"
    console.log('VERSION URL', path)
    fetch(path)
        .then(response => response.json())
        .then((responseJSON) => {
            for (const locale of responseJSON.__children__) {
                if (!locale.__children__) {
                    continue
                }
                for (const localeChild of locale.__children__) {
  
                    if (!localeChild.__children__) {
                        continue
                    }
                    for (const variant of localeChild.__children__) {
                        if (!variant.__children__) {
                            continue
                        }
  
                        for (const offspring of variant.__children__) {
                            if (offspring.__name__ === "metadata") {
  
                                if (offspring[Fields.PANT_PRODUCT_VERSION_REF] !== undefined) {
  
                                    this.getProductInitialLoad(offspring[Fields.PANT_PRODUCT_VERSION_REF])
                                }
                            }
                        }
  
                    }
                }
            }
        })
  }
  
  // private getHarrayChildNamed = (object, name) => {
  //   for (const childName in object.__children__) {
  //       if (object.__children__.hasOwnProperty(childName)) { // Not sure what this does, but makes tslin happy
  //           const child = object.__children__[childName]
  //           if (child.__name__ === name) {
  //               return child
  //           }
  //       }
  //   }
  //   return ""
  // }
  
  // private fetchVersions = (modulePath, variant) => {
  //   // TODO: need a better fix for the 404 error.
  //   if (modulePath !== "") {
  //       // fetchpath needs to start from modulePath instead of modulePath/en_US.
  //       // We need extact the module uuid for customer portal url to the module.
  //       const fetchpath = "/content" + modulePath + ".harray.5.json"
  //       fetch(fetchpath)
  //           .then(response => response.json())
  //           .then(responseJSON => {
  //               const en_US = this.getHarrayChildNamed(responseJSON, "en_US")
  //               const source = this.getHarrayChildNamed(en_US, "source")
  //               const variants = this.getHarrayChildNamed(en_US, "variants")
  
  //               const firstVariant = this.getHarrayChildNamed(variants, variant)
  //               // process draftUpdateDate from source/draft
  //               let draftDate = ""
  //               if (source !== "undefined" && source.__name__ === "source") {
  //                   for (const childNode of source.__children__) {
  //                       if (childNode.__name__ === "draft") {
  //                           draftDate = childNode["jcr:created"]
  //                       } else if (childNode.__name__ === "released") {
  //                           draftDate = childNode["jcr:created"]
  //                       }
  //                   }
  //               }
  //               // process variantUUID
  //               let variantUuid = ""
  //               if (firstVariant["jcr:primaryType"] !== "undefined" && (firstVariant["jcr:primaryType"] === "pant:moduleVariant" || firstVariant["jcr:primaryType"] === "pant:assemblyVariant")) {
  //                   variantUuid = firstVariant["jcr:uuid"]
  //               }
  //               const versionCount = firstVariant.__children__.length
  //               for (let i = 0; i < versionCount; i++) {
  //                   const moduleVersion = firstVariant.__children__[i]
  //                   let variantReleased = false
  
  //                   if (moduleVersion.__name__ === "draft") {
  //                       this.draft[0].version = "Version " + moduleVersion.__name__
  //                       this.draft[0].metadata = this.getHarrayChildNamed(moduleVersion, "metadata")
  //                       // get created date from source/draft
  //                       this.draft[0].updatedDate = draftDate !== undefined ? draftDate : ""
  //                       // this.props.modulePath starts with a slash
  //                       this.draft[0].path = "/content" + modulePath + "/en_US/variants/" + firstVariant.__name__ + "/" + moduleVersion.__name__
  //                   }
  //                   if (moduleVersion.__name__ === "released") {
  //                       this.release[0].version = "Version " + moduleVersion.__name__
  //                       this.release[0].metadata = this.getHarrayChildNamed(moduleVersion, "metadata")
  //                       this.release[0].updatedDate = this.release[0].metadata["pant:datePublished"] !== undefined ? this.release[0].metadata["pant:datePublished"] : ""
  //                       // get created date from source/draft
  //                       this.release[0].draftUploadDate = draftDate !== undefined ? draftDate : ""
  //                       // modulePath starts with a slash
  //                       this.release[0].path = "/content" + modulePath + "/en_US/variants/" + firstVariant.__name__ + "/" + moduleVersion.__name__
  //                       variantReleased = true
  //                   }
  //                   if (!variantReleased) {
  //                       this.release[0].updatedDate = "-"
  //                   }
  //                   this.props.updateDate((draftDate !== "" ? draftDate : ""), this.release[0].updatedDate, this.release[0].version, variantUuid)
  //               }
  //               this.setState({
  //                   results: [this.draft, this.release],
  //                   variantPath: "/content" + modulePath + "/en_US/variants/" + variant
  //               })
  
  //               // Check metadata for draft. Show warning icon if metadata missing for draft
  //               if (this.draft && this.draft[0].path.length > 0) {
  //                   if (this.draft[0].metadata !== undefined &&
  //                       this.draft[0].metadata.productVersion === undefined) {
  //                       this.setState({ showMetadataAlertIcon: true })
  //                   } else {
  //                       this.setState({ showMetadataAlertIcon: false })
  //                   }
  //               }
  
  //               // Get documents included in assembly
  //               if (this.state.contentTypeSelected === "assembly") {
  //                   this.getDocumentsIncluded(variantUuid)
  //               }
  //           })
  //   }
  // }
  
  private getProductInitialLoad = (uuid) => {
    const path = "/content/products.harray.3.json"
    fetch(path)
        .then(response => response.json())
        .then(responseJSON => {
            for (const product of responseJSON.__children__) {
                if (!product.__children__) {
                    continue
                }
                for (const productChild of product.__children__) {
                    if (productChild.__name__ !== "versions") {
                        continue
                    }
                    if (productChild.__children__) {
                        for (const productVersion of productChild.__children__) {
                            if (productVersion[Fields.JCR_UUID] === uuid) {
                                this.setState({ productValue: product.name, versionValue: productVersion.name, productUrlFragment: product.urlFragment, versionUrlFragment: productVersion.urlFragment })
                                const isGuideOrTopic = this.props.contentTypeSelected == "assembly" ? '/guide/' : '/topic/'
                                const url = this.state.portalHostUrl + '/documentation/'+this.state.locale.toLocaleLowerCase()+'/' + this.state.productUrlFragment + '/' + this.state.versionUrlFragment + isGuideOrTopic + this.state.variantUUID
                                console.log("Constructed url="+url)
                                if(this.state.productUrlFragment!==""){
                                    this.setState({ portalUrl: url})
                                }
                                break
                            }
                        }
                    }
                }
            }
        })
  
  }
  }
export { BulkOperationPublish }