import React, { Component } from 'react';
import { Button } from '@patternfly/react-core';
import {
    Alert, AlertActionCloseButton, BaseSizes, Card,DataList, DataListItem, DataListItemRow, DataListItemCells, DataListCell,
    DataListToggle, DataListContent, DataListAction,Dropdown, DropdownToggle, 
    DropdownItem, DropdownSeparator, DropdownPosition, DropdownDirection, Form,
    FormGroup, FormSelect, FormSelectOption, InputGroup, KebabToggle, Modal, InputGroupText, Title, TitleLevel, TextInput  } from '@patternfly/react-core';
import CheckImage from '@app/images/check_image.jpg';
import BlankImage from '@app/images/blank.jpg';
import { Redirect } from 'react-router-dom'

  export interface IProps {
    modulePath: string
    revisionModulePath: string
    draftUpdateDate: (draftUpdateDate,draft,draftPath) => any
    releaseUpdateDate: (releaseUpdateDate,release,releasePath) => any
  }

class Revisions extends Component<IProps> {

    public draft= [{ "icon": BlankImage,"path": "", "revision": "", "publishedState": 'Not published', "updatedDate": '        --', "firstButtonType": 'primary',"secondButtonType": 'secondary', "firstButtonText": 'Publish',"secondButtonText": 'Preview',"isDropdownOpen": false,"isArchiveDropDownOpen": false,"metaData":''}]
    public release= [{ "icon": CheckImage, "path": "", "revision": "", "publishedState": 'Released', "updatedDate": '        --', "firstButtonType": 'secondary',"secondButtonType": 'primary', "firstButtonText": 'Unpublish',"secondButtonText": 'View',"isDropdownOpen": false,"isArchiveDropDownOpen": false,"metaData":''}]

    public state = {
        initialLoad: true,
        isArchiveDropDownOpen: false,
        isArchiveSelect: false,
        isDropDownOpen: false,
        isHeadingToggle: false,
        isOpen: false,
        isRowToggle: false,
        login: false,
        results: [this.draft,this.release],

        productsInitalLoad: true,
        allProducts: '',
        formInvalid: false,
        
        isDup: false,
        isEmptyResults: false,
        isMissingFields: false,
        isModalOpen: false,
        isProductDropdownOpen: false,
        isUsecaseDropdownOpen: false,
        isVersionDropdownOpen: false,
        loggedinStatus: false,
        metadataPath: '',
        metadataResults: [],
        moduleUrl: '',
        moduleUrlresults:[],
        productOptions: [
            { value: 'Select a Product', label: 'Select a Product', disabled: false },
        ],
        productValue: '',
        redirect: false,
        
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
        metadataInitalLoad: true,
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
                
                {this.state.initialLoad && this.fetchRevisions()}
                {this.state.productsInitalLoad && this.fetchProductVersionDetails()}
                {this.state.metadataInitalLoad && this.getMetadata(this.state.metadataPath)}
                <Card>
                    <div>
                        <DataList aria-label="Simple data list example">
                            <DataListItem aria-labelledby="simple-item1" isExpanded={this.state.isHeadingToggle}>
                                <DataListItemRow id="data-rows-header" >
                                    <DataListToggle
                                        onClick={() => this.onHeadingToggle()}
                                        isExpanded={true}
                                        id="width-ex3-toggle1"
                                        aria-controls="width-ex3-expand1"
                                    />
                                    <DataListItemCells
                                        dataListCells={[
                                            <DataListCell key="revision">
                                                <span className="sp-prop-nosort" id="span-source-type">Revision</span>
                                            </DataListCell>,
                                            <DataListCell key="published">
                                                <span className="sp-prop-nosort" id="span-source-type">Published</span>
                                            </DataListCell>,
                                            <DataListCell key="updated">
                                                <span className="sp-prop-nosort" id="span-source-type">Draft Uploaded</span>
                                            </DataListCell>,
                                            <DataListCell key="module_type">
                                                <span className="sp-prop-nosort" id="span-source-name" />
                                            </DataListCell>,
                                            <DataListCell key="module_type">
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
                                {this.state.results.map(type => (
                                    type.map(data => (
                                        data["revision"] !== "" && (
                                            <DataList aria-label="Simple data list example2">
                                                <DataListItem aria-labelledby="simple-item2" isExpanded={data["isDropdownOpen"]}>
                                                    <DataListItemRow>
                                                        <DataListToggle
                                                            onClick={() => this.onExpandableToggle(data)}
                                                            isExpanded={data["isDropdownOpen"]}
                                                            id={data["revision"]}
                                                            aria-controls={data["revision"]}
                                                        /> 
                                                        <DataListItemCells
                                                            dataListCells={[
                                                                <DataListCell key="revision">
                                                                    {/* <img src={CheckImage} width="20px" height="20px"/>                                                         */}
                                                                    {data["revision"]}
                                                                </DataListCell>,
                                                                <DataListCell key="published">
                                                                    {data["publishedState"]}
                                                                </DataListCell>,
                                                                <DataListCell key="updated">
                                                                    {data["updatedDate"].substring(4,15)}
                                                                </DataListCell>,
                                                                <DataListCell key="module_type">
                                                                    <Button variant="primary" onClick={() => this.changePublishState(data["firstButtonText"])}>{data["firstButtonText"]}</Button>{'  '}
                                                                    <Button variant="secondary" onClick={() => this.previewDoc(data["secondButtonText"])}>{data["secondButtonText"]}</Button>{'  '}
                                                                </DataListCell>,
                                                                <DataListCell key="image" width={1}>
                                                                    <Dropdown
                                                                        isPlain={true}
                                                                        position={DropdownPosition.right}
                                                                        isOpen={data["isArchiveDropDownOpen"]}
                                                                        onSelect={this.onArchiveSelect}
                                                                        toggle={<KebabToggle onToggle={() => this.onArchiveToggle(data)} />}
                                                                        dropdownItems={[
                                                                            <DropdownItem key="archive" isDisabled={true}>Archive</DropdownItem>,
                                                                            <DropdownItem id={data["path"]} key={data["path"]} component="button" onClick={this.handleModalToggle}>Edit metadata</DropdownItem>,
                                                                        ]}
                                                                    />
                                                                </DataListCell>
                                                            ]}
                                                        />
                                                    </DataListItemRow>
                                                    <DataListContent
                                                        aria-label={data["revision"]}
                                                        id={data["revision"]}
                                                        isHidden={!data["isDropdownOpen"]}
                                                        noPadding={true}
                                                    >
                                                        {/* this is the content for the inner data list content */}
                                                        <DataListItemCells
                                                            dataListCells={[
                                                                <DataListCell key="File name" width={2}>
                                                                    <span>{' '}</span>
                                                                </DataListCell>,
                                                                <DataListCell key="File name" width={2}>
                                                                    <span className="sp-prop-nosort" id="span-source-type">File Name</span>
                                                                </DataListCell>,
                                                                <DataListCell key="published" width={4}>
                                                                    {"/"+this.props.modulePath}
                                                                </DataListCell>,
                                                                <DataListCell key="updated" width={2}>
                                                                    <span className="sp-prop-nosort" id="span-source-type">Upload Time</span>
                                                                </DataListCell>,
                                                                <DataListCell key="updated" width={4}>
                                                                    {data["updatedDate"]}
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
                                                                    <span className="sp-prop-nosort" id="span-source-type">Module Title</span>
                                                                </DataListCell>,
                                                                <DataListCell key="updated" width={4}>
                                                                    {data["metaData"]["jcr:title"]}
                                                                </DataListCell>,
                                                                <DataListCell key="updated" width={2}>
                                                                    <span className="sp-prop-nosort" id="span-source-type">Context Package</span>
                                                                </DataListCell>,
                                                                <DataListCell key="updated" width={4}>
                                                                    Dumy Context Package
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
                    isOpen={isModalOpen}
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

                                        <FormSelectOption isDisabled={false} key={option.value} value={option.value} label={option.label} required={false}/>
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

    private fetchRevisions = () => {
            console.log('module Path: ', this.props.modulePath);
            fetch("/content/"+this.props.modulePath+".3.json?")
            .then(response => response.json())
            .then(responseJSON => {
                this.setState(updateState => {    
                
                let releasedTag = responseJSON["en_US"]["released"];
                let draftTag = responseJSON["en_US"]["draft"];
                            
                let objectKeys = Object.keys(responseJSON["en_US"]);
    
                for(var key in objectKeys){
                    if(objectKeys[key]==="jcr:primaryType"){
                        break;
                    }
                    else{
                        if(responseJSON["en_US"][objectKeys[key]]["jcr:uuid"]===draftTag){
                            this.draft[0]["revision"] = "Version "+objectKeys[key];
                            this.draft[0]["updatedDate"] = responseJSON["en_US"][objectKeys[key]]["jcr:lastModified"];
                            this.draft[0]["metaData"] = responseJSON["en_US"][objectKeys[key]]["metadata"];  
                            this.draft[0]["path"] =  "/content/"+this.props.modulePath+"/en_US/"+objectKeys[key];
                            console.log("1:",this.draft[0]["path"]);
                            this.props.draftUpdateDate(this.draft[0]["updatedDate"],"draft",this.draft[0]["path"]);                       
                        }
                        if(responseJSON["en_US"][objectKeys[key]]["jcr:uuid"]===releasedTag){
                            this.release[0]["revision"] = "Version "+objectKeys[key];
                            this.release[0]["updatedDate"] = responseJSON["en_US"][objectKeys[key]]["jcr:lastModified"];
                            this.release[0]["metaData"] = responseJSON["en_US"][objectKeys[key]]["metadata"];  
                            this.release[0]["path"] =  "/content/"+this.props.modulePath+"/en_US/"+objectKeys[key];
                            console.log("2:",this.release[0]["path"]);  
                            this.props.releaseUpdateDate(this.release[0]["updatedDate"],"release",this.release[0]["path"])         
                        }                            
                    }
                        
                }
                return {
                    initialLoad: false,
                    results: [this.draft,this.release]                
                }        
            })
        })    
    }

    private changePublishState = (buttonText) =>{
        const formData = new FormData();
        if(buttonText==="Publish"){
            formData.append(":operation", "pant:release");
            console.log('module path:',this.props.modulePath)
            this.draft[0]["revision"] = "";
        }else{
            formData.append(":operation", "pant:draft");
            console.log('module path:',this.props.modulePath)
        }
        fetch("/content/"+this.props.modulePath, {
            body: formData,
            method: 'post'
          }).then(response => {
            if (response.status === 201 || response.status === 200) {
              console.log("Publish Works: " + response.status)
              this.setState({ initialLoad: true })
            }else {
              console.log("Publish Failed " + response.status)
              this.setState({ initialLoad: true })
            }
          });

    }
    
    private onArchiveSelect = event => {
        this.setState({
            isArchiveDropDownOpen: !this.state.isArchiveDropDownOpen
        });
      };

      private onArchiveToggle = (data) => {
        data["isArchiveDropDownOpen"] = !data["isArchiveDropDownOpen"];
        this.setState({ 
            isArchiveDropDownOpen: this.state.isArchiveDropDownOpen
        });
      };

      private onExpandableToggle = (data) => {
        data["isDropdownOpen"] = !data["isDropdownOpen"];
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
        let docPath="";          
        if(buttonText=="Preview"){
            docPath = "/content/"+this.props.modulePath+".preview?draft=true";
        }else{
            docPath = "/content/"+this.props.modulePath+".preview?released=true";
        }
          console.log("Preview path: ", docPath)
          return window.open(docPath);
      }

      private handleModalToggle = (event) => {
        this.setState({
            isModalOpen: !this.state.isModalOpen
        });

        // process path
        this.setState({ metadataPath: event.target.id})
    }

    private handleModalClose = () => {
        this.setState({
            isModalOpen: false
        })
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

            const formData = new FormData(event.target.form);

            formData.append("productVersion", this.state.versionValue)
            formData.append("documentUsecase", this.state.usecaseValue)
            formData.append("urlFragment", "/" + this.state.moduleUrl)

            fetch( this.state.metadataPath + '/metadata', {
                body: formData,
                headers: hdrs,
                method: 'post'
            }).then(response => {
                if (response.status === 201 || response.status === 200) {
                    console.log("successful edit ", response.status)
                    // this.setState({ redirect: true, successAlertVisble: true })
                    this.handleModalClose()
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
            .then(responseJSON => this.setState({ moduleUrlresults: responseJSON.results }))
            .then(() => {
                // console.log("[moduleUrlExist] results breakdown " + JSON.stringify(this.state.results))

                if (JSON.stringify(this.state.moduleUrlresults) === "[]") {
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
        this.setState({ productsInitalLoad: false })
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

        if (this.moduleUrlExist(this.state.moduleUrl) === false) {
            this.setState({ isDup: false });
        }
    }

    private hideSuccessAlert = () => {
        this.setState({ successAlertVisble: false })
    }

    private getMetadata = (revisionPath) => {
        this.setState({ metadataInitalLoad: false })
        if (revisionPath) {
            fetch(revisionPath + "/metadata.json")
            .then(response => response.json())
            .then(responseJSON => this.setState({ metadataResults: responseJSON }))
            .then(() => {

                if (JSON.stringify(this.state.metadataResults) ! === "[]") {
                    // Process results
                    // console.log("[getMetadata] responseJSON ", this.state.metadataResults)
                    // console.log("[getMetadata] urlFragment ", this.state.metadataResults["urlFragment"])
                    // Remove leading slash.
                    if (this.state.metadataResults["urlFragment"]) {
                        let url = this.state.metadataResults["urlFragment"]
                        if (url.indexOf('/') === 0) {
                            url = url.replace('/', '');

                        }
                        this.setState({ moduleUrl: url })
                    }
                    this.setState({ usecaseValue: this.state.metadataResults["documentUsecase"]})
                }
            })
        }
    }
}

export { Revisions }