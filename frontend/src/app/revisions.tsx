import React, { Component } from 'react';
import { Button } from '@patternfly/react-core';
import {
    Card,DataList, DataListItem, DataListItemRow, DataListItemCells, DataListCell,
    DataListToggle, DataListContent, DataListAction,Dropdown, DropdownToggle, 
    DropdownItem, DropdownSeparator, DropdownPosition, DropdownDirection,Grid, 
    GridItem, KebabToggle, Level, LevelItem
  } from '@patternfly/react-core';
import CheckImage from '@app/images/check_image.jpg';
import BlankImage from '@app/images/blank.jpg';

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
        isHeadingToggle: true,
        isOpen: false,
        isRowToggle: false,
        login: false,
        results: [this.draft,this.release]
    };

    public render() {   
        return (
            <React.Fragment>
                {this.state.initialLoad && this.fetchRevisions()}
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
                                                                            <DropdownItem key="archive">Archive</DropdownItem>,
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
                                                                    {this.props.modulePath}
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
            </React.Fragment>

        );
    }

    private fetchRevisions = () => {
            let fetchpath = "/content"+this.props.modulePath+".3.json?"; 
            // TODO : harray.3.json - to process the children
            fetch(fetchpath)
            .then(response => response.json())
            .then(responseJSON => {
                this.setState(updateState => {    
                // console.log("response json:",responseJSON);
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
                            // console.log("1:",this.draft[0]["path"]);  
                            this.props.draftUpdateDate(this.draft[0]["updatedDate"],"draft",this.draft[0]["path"]);                       
                        }
                        if(responseJSON["en_US"][objectKeys[key]]["jcr:uuid"]===releasedTag){
                            this.release[0]["revision"] = "Version "+objectKeys[key];
                            this.release[0]["updatedDate"] = responseJSON["en_US"][objectKeys[key]]["jcr:lastModified"];
                            this.release[0]["metaData"] = responseJSON["en_US"][objectKeys[key]]["metadata"];  
                            this.release[0]["path"] =  "/content/"+this.props.modulePath+"/en_US/"+objectKeys[key];
                            // console.log("2:",this.release[0]["path"]);  
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
            console.log('Published file path:',this.props.modulePath)
            this.draft[0]["revision"] = "";
        }else{
            formData.append(":operation", "pant:unpublish");
            console.log('Unpublished file path:',this.props.modulePath);
            this.release[0]["revision"] = "";
        }
        fetch("/content/"+this.props.modulePath, {
            body: formData,
            method: 'post'
          }).then(response => {
            if (response.status === 201 || response.status === 200) {
              console.log(buttonText+" works: " + response.status)
              this.setState({ initialLoad: true })
            }else {
              console.log(buttonText+" failed " + response.status)
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
            docPath = "/content/"+this.props.modulePath+".preview";
        }
          console.log("Preview path: ", docPath)
          return window.open(docPath);
      }
}

export { Revisions }