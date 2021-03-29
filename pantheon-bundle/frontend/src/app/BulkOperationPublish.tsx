import React from 'react';

import "@app/app.css";
import { BulkPublishConfirmation } from './BulkPublishConfirmation';
import { BaseSizes, Button, Modal, ModalVariant, Title } from '@patternfly/react-core';
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

            showBulkConfirmation: false,
            metadataEditError: "",

            //progress bar
            progressFailureValue: 0,
            progressSuccessValue: 0,
            progressWarningValue: 0,
            bulkUpdateFailure: 0,
            bulkUpdateSuccess: 0,
            bulkUpdateWarning: 0,

            documentTitles: [],
            documentsSucceeded: [],
            documentsFailed: [""],
            documentsIgnored: [""],
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
                <Button key="cancel" variant="secondary" onClick={this.handleModalClose}>
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
                {this.state.showBulkConfirmation &&
                    <BulkPublishConfirmation
                        header="Bulk Publish"
                        subheading="Documents updated in the bulk operation"
                        updateSucceeded={this.state.confirmationSucceeded}
                        updateIgnored={this.state.confirmationIgnored}
                        updateFailed={this.state.confirmationFailed}
                        footer=""
                        progressSuccessValue={this.state.progressSuccessValue}
                        progressFailureValue={this.state.progressFailureValue}
                        progressWarningValue={this.state.progressWarningValue}
                        onShowBulkOperationConfirmation={this.updateShowBulkPublishConfirmation}
                        onBulkOperationError={this.updateBulkOperationError}
                    />}

                {this.props.isBulkPublish && publishModal}
            </React.Fragment>
        );
    }

    private handleModalClose = () => {
        this.setState({ isModalOpen: false })
    }

    private onBulkPublish = (event) => {
                const formData = new FormData();
  
                    formData.append(":operation", "pant:publish");

  
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
                    const backend = "/content" + modulePath + "/en_US/variants/unified/draft"
                    console.log('PUBLISH BACKEND VALUE', backend)

                formData.append("locale", "en_US")
                formData.append("variant", variant)
                Utils.draftExist(backend).then((exist) => {
                if(exist){
                    fetch("/content" + modulePath, {
                        body: formData,
                        method: "post",
                        headers: hdrs
                    }).then(response => {
                        if (response.status === 201 || response.status === 200) {
                            console.log("publish works: " + response.status)
                            this.setState({
                                canChangePublishState: true,
                                documentsSucceeded: [...this.state.documentsSucceeded, modulePath],
                                isBulkPublish: false,
                                showPublishMessage: true,
                                bulkUpdateSuccess: this.state.bulkUpdateSuccess + 1,
                            },() =>{
                              this.calculateSuccessProgress(this.state.bulkUpdateSuccess)
      
                            })
                        } else {
                            console.log("publish failed " + response.status)
                            this.setState({ bulkUpdateFailure: this.state.bulkUpdateFailure + 1, documentsFailed: [...this.state.documentsFailed, modulePath] }, () => {
                              this.calculateFailureProgress(this.state.bulkUpdateFailure)
                          })
        
                       
                          }
                        })
                  
                }else{
                    console.log('no draft exists')
                    this.setState({ bulkUpdateWarning: this.state.bulkUpdateWarning + 1, documentsIgnored: [...this.state.documentsFailed, modulePath] }, () => {
                        this.calculateWarningProgress(this.state.bulkUpdateWarning)
                    })
                }
       
          })
        }

    })
                
                // this.calculateFailureProgress(this.state.bulkUpdateFailure)


                this.setState( {isModalOpen: false, showBulkConfirmation: true})
      

            
  }
  

  

  
 

  //functions for success & failure messages
  private updateShowBulkPublishConfirmation = (showBulkConfirmation) => {
    this.setState({ showBulkConfirmation })
}

private updateBulkOperationError = (metadataEditError) => {
    this.setState({ metadataEditError: "string" })
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
export { BulkOperationPublish }