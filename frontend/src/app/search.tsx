import React, { Component } from 'react';
import {
  Alert, AlertActionCloseButton, TextInput, Label,
  DataList, DataListItem, DataListItemRow, DataListItemCells,
  DataListCell, Button, DataListCheck, Modal,
  Level, LevelItem
} from '@patternfly/react-core';
import '@app/app.css';
import {Pagination} from '@app/Pagination';

export default class Search extends Component {
  public state = {
    alertOneVisible: true ,
    check: false,
    columns: ['Name', 'Description', 'Source Type', 'Source Name', 'Upload Time'],
    countOfCheckedBoxes: 0,
    data: [{ "pant:transientPath": '', "jcr:created": '', "name": "", "jcr:title": "", "jcr:description": "", "sling:transientSource": "", "pant:transientSourceName": "" ,"checkedItem":false}],
    deleteButtonVisible: false,
    deleteState: '',
    input: '*',
    isEmptyResults: false,
    isSortedUp: true,
    redirect: false,
    redirectLocation: '',
    sortKey: '',
    initialLoad: true,
    allPaths: [''],
    isModalOpen: false,
    confirmDelete: false,
    loggedinStatus: false,
    pageLimit: 10,
    page: 1,
    checkNextPageRow: "",
    nextPageRowCount: 1
  };


  public transientPaths : string[] = [];

  public render() {
    const { columns, isEmptyResults, input, isSortedUp,sortKey} = this.state;
    
    const id = 'userID';
    if (!this.state.loggedinStatus && this.state.initialLoad==true) {
      fetch("/system/sling/info.sessionInfo.json")
        .then(response => response.json())
        .then(responseJSON => {
          if (responseJSON[id] !== 'anonymous') {
            this.setState({ loggedinStatus: true })
          }
        })
    }
    return (
      <React.Fragment>
        {this.state.initialLoad && this.doSearch()}
        <div>
          <div>
            <div className="row-view">
              <Label>Search Query:</Label>
              <TextInput id="search" type="text" onKeyDown={this.getRows} onChange={(event) => this.setState({ input: event })} value={this.state.input} />
              <Button onClick={this.doSearch}>Search</Button>
            </div>
            <div className="notification-container">
              <Alert
                variant="info"
                title="Search is case sensitive. Type '*' and press 'Enter' for all the modules."
              />
              { console.log("this.state.data: ") }
            { console.log(this.state.data) }
              <Pagination
                handleMoveLeft={() => this.updatePageCounter("L")}
                handleMoveRight={() => this.updatePageCounter("R")}
                pageNumber={this.state.page}
                nextPageRecordCount={this.state.nextPageRowCount}
                noOfRecordsOnPage={this.state.data.length}
              ></Pagination>
            </div>
            <DataList aria-label="Simple data list example">
              <DataListItem aria-labelledby="simple-item1">
                <DataListItemRow id="data-rows-header" > 
                  {this.state.loggedinStatus && !this.state.isEmptyResults &&
                    <DataListCheck aria-labelledby="width-ex1-check1"
                      className="checkbox"
                      isChecked={this.state.check}
                      aria-label="controlled checkbox example"
                      id="check"
                      onClick={this.handleSelectAll}
                      isDisabled={false}
                    />}
                  <DataListItemCells 
                        dataListCells={[
                          <DataListCell width={2} key="title" onClick={() => this.sort("jcr:title")}>
                            <span className="sp-prop" id="span-name">Name</span>
                          </DataListCell>,
                          <DataListCell width={2} key="description" onClick={() => this.sort("jcr:description")}>
                            <span className="sp-prop" id="span-description">Description</span>
                          </DataListCell>,
                          <DataListCell key="resource source">
                            <span className="sp-prop-nosort" id="span-source-type">Source Type</span>
                          </DataListCell>,
                          <DataListCell key="source name">
                            <span className="sp-prop-nosort" id="span-source-name">Source Name</span>
                          </DataListCell>,
                          <DataListCell key="upload time" onClick={() => this.sort("jcr:created")}>
                            <span className="sp-prop" id="span-upload-time">  Upload Time</span>
                          </DataListCell>,
                        ]} 
                  />
                </DataListItemRow>
                {/* Delete button at the top */}
                <DataListItemRow id="data-rows" key={this.state.data["pant:transientPath"]}>
                  {
                    this.state.deleteButtonVisible ?
                      <Button variant="primary" onClick={this.confirmDeleteOperation}>Delete</Button>
                      : null
                  }
                </DataListItemRow>
                {this.state.data.map(data => (
                  <DataListItemRow id="data-rows">
                    {this.state.loggedinStatus && !this.state.isEmptyResults &&
                      <DataListCheck aria-labelledby="width-ex3-check1"
                        className="checkbox"
                        isChecked={data["checkedItem"]}
                        aria-label="controlled checkbox example"
                        id={data["pant:transientPath"]}
                        name={data["pant:transientPath"]}
                        onClick={() => this.handleDeleteCheckboxChange(data["pant:transientPath"])}
                      />}
                    <DataListItemCells key={data["pant:transientPath"]} onClick={() => this.setPreview(data["pant:transientPath"])} 
                          dataListCells={[      
                                <DataListCell width={2}>
                                  <span>{data["jcr:title"]}</span>
                                </DataListCell>,
                                <DataListCell  width={2}>
                                  <span>{data["jcr:description"]}</span>
                                </DataListCell>,
                                <DataListCell>
                                  <span>{data["pant:transientSource"]}</span>
                                </DataListCell>,
                                <DataListCell>
                                  <span>{data["pant:transientSourceName"]}</span>
                                </DataListCell>,
                                <DataListCell>
                                <span >{this.formatDate(new Date(data["jcr:created"]))}</span>
                                </DataListCell>
                          ]} 
                    />      
                  </DataListItemRow>
                ))}
                {/* Delete button at the bottom */}
                <DataListItemRow id="data-rows" key={this.state.data["pant:transientPath"]}>
                    {
                      this.state.deleteButtonVisible?              
                        <Button variant="primary" onClick={this.confirmDeleteOperation}>Delete</Button>
                      :null
                    }
                    
                </DataListItemRow>
                {isEmptyResults && (
                      <Level gutter="md">
                        <LevelItem />
                        <LevelItem>
                          <div className="notification-container">
                      <br />
                      <br />
                        <Alert
                          variant="warning"
                          title={"No modules found with your search of: " + this.state.input}
                          action={<AlertActionCloseButton onClose={this.dismissNotification} />}
                        />
                        <br />
                        <br />
                      </div></LevelItem>
                        <LevelItem />
                      </Level>
                      
                    )}
              </DataListItem>
            </DataList>

            <div className="notification-container">
              <Pagination
                handleMoveLeft={() => this.updatePageCounter("L")}
                handleMoveRight={() => this.updatePageCounter("R")}
                pageNumber={this.state.page}
                nextPageRecordCount={this.state.nextPageRowCount}
                noOfRecordsOnPage={this.state.data.length}
              ></Pagination>
            </div>
            {/* Alert for delete confirmation */}
            <div className="alert">
              {this.state.confirmDelete===true && <Modal
                    isSmall
                    title="Confirmation"
                    isOpen={!this.state.isModalOpen}
                    onClose={this.hideAlertOne}
                    actions={[<Button key="yes" variant="primary" onClick={() => this.delete(this.transientPaths)}>Yes</Button>,
                              <Button key="no" variant="secondary" onClick={this.cancelDeleteOperation}>No</Button>]}
                    >
                      Are you sure you want to delete the selected items?
                </Modal>}
                {/* Alerts after confirmation on delete */}
              {this.state.deleteState=='positive' && <Modal
                    isSmall
                    title="Success"
                    isOpen={!this.state.isModalOpen}
                    onClose={this.hideAlertOne}
                    actions={[<Button key="cancel" variant="primary" onClick={this.hideAlertOne}>OK</Button>]}
                    >
                      Selected items were deleted.
                </Modal>}
                {this.state.deleteState=='negative' && <Modal
                    isSmall
                    title="Failure"
                    isOpen={!this.state.isModalOpen}
                    onClose={this.hideAlertOne}
                    actions={[<Button key="cancel" variant="primary" onClick={this.hideAlertOne}>OK</Button>]}
                    >
                      Selected items were not found.
                </Modal>}
                {this.state.deleteState=='unknown' && <Modal
                    isSmall
                    title="Error"
                    isOpen={!this.state.isModalOpen}
                    onClose={this.hideAlertOne}
                    actions={[<Button key="cancel" variant="primary" onClick={this.hideAlertOne}>OK</Button>]}
                    >
                      An unknown error occured, please check if you are logged in.
                </Modal>}
            </div>
          </div>
        </div>
      </React.Fragment>
    );
  }
    
  onChangePage(newPage){
    this.setState({page: newPage},()=>{console.log("New page number: "+this.state.page)})
  }

  private handleSelectAll = (event) => {
    console.log('handleSelectAll')
    this.setState({check: !this.state.check}, () => {
      this.setState(prevState => {
        this.state.allPaths=[]
        this.transientPaths=[]
        const selectAllcheck = this.state.data.map(dataitem => {
              dataitem["checkedItem"] = this.state.check
              console.log(dataitem["pant:transientPath"]+":"+dataitem["checkedItem"])
              if(this.state.check){
                this.state.allPaths.push(dataitem["pant:transientPath"])
              }
          return dataitem
        })
        this.transientPaths=this.state.allPaths
        if(this.state.check === true){
          this.setState({countOfCheckedBoxes: this.state.data.length}, () => {
            console.log('countOfCheckedBoxes: '+this.state.countOfCheckedBoxes)
                if(this.state.countOfCheckedBoxes > 0){
                  this.setState({deleteButtonVisible: true})
                }else{
                  this.setState({deleteButtonVisible: false})
                }
                console.log('transientPaths:'+this.transientPaths)
              })
        }else{
          this.setState({countOfCheckedBoxes: 0}, () => {
            console.log('countOfCheckedBoxes: '+this.state.countOfCheckedBoxes)
            this.transientPaths = []
            console.log('transientPaths:'+this.transientPaths)
            this.setState({deleteButtonVisible: false})
          })
        }
        return{
          data: selectAllcheck
        }
      })

    }) 
  }

  private handleDeleteCheckboxChange = (id) => {
    this.setState(prevState => {
      const updatedData = this.state.data.map(data => {
        if(data["pant:transientPath"] === id){
          data["checkedItem"] = !data["checkedItem"]
          if(data["checkedItem"] === true){
            this.setState({countOfCheckedBoxes: this.state.countOfCheckedBoxes+1}, () => {
              console.log('countOfCheckedBoxes: '+this.state.countOfCheckedBoxes)
              if(this.state.countOfCheckedBoxes > 0){
                this.setState({deleteButtonVisible: true})
              }else{
                this.setState({deleteButtonVisible: false})
              }
            })
            this.transientPaths.push(data["pant:transientPath"]);   
            console.log('transientPaths:'+this.transientPaths)
            console.log('all Paths:'+this.state.allPaths)
          }else{
            this.setState({countOfCheckedBoxes: this.state.countOfCheckedBoxes-1}, () => {
              console.log('countOfCheckedBoxes: '+this.state.countOfCheckedBoxes)
              if(this.state.countOfCheckedBoxes > 0){
                this.setState({deleteButtonVisible: true})
              }else{
                this.setState({deleteButtonVisible: false})
              }
            })
            this.transientPaths.splice(this.transientPaths.indexOf(id),1)
            console.log('transientPaths:'+this.transientPaths)
            console.log('all Paths:'+this.state.allPaths)
          }
        }
        return data
      })
      return{
        data: updatedData
      }
    })
  };

  private delete = (keydata) => {
    console.log(keydata)
    console.log('in the delete function')
      const formData = new FormData();
      formData.append(':operation', 'delete')
    for(var i=0;i<keydata.length;i++){
      formData.append(':applyTo', '/content/'+keydata[i])
    }
      fetch('/content/'+keydata[0], {
        method: 'post',
        body: formData
      }).then(response => {
        if (response.status == 200) {
          this.setState({ deleteState: 'positive'},() => 
          this.transientPaths=[]) 
          console.log('deleteState:'+this.state.deleteState)
        } else if (response.status == 403) {
          this.setState({ deleteState: 'negative'},() => 
          this.transientPaths=[])
          console.log('deleteState:'+this.state.deleteState)
        } else {
          this.setState({ deleteState: 'unknown'},() => 
          this.transientPaths=[])
          console.log('deleteState:'+this.state.deleteState)
        }
      });
  }

  private getRows = (event) => {
    if (event.key === 'Enter') {
      this.doSearch()
    }
  };

  private doSearch = () => {
    this.setState({ initialLoad: false })
    fetch(this.buildSearchUrl())
      .then(response => response.json())
      .then(responseJSON => this.setState({ data: responseJSON.data, nextPageRowCount: responseJSON.hasNextPage ? 1 : 0 }))
      .then(() => {
        if (JSON.stringify(this.state.data) === "[]") {
          this.setState({
            isEmptyResults: true,
            deleteButtonVisible: false,
            check: false
          })
        } else {
          this.setState({ isEmptyResults: false,
            deleteButtonVisible: false,
            countOfCheckedBoxes: 0,
            check: false
           })
        }
      })
    }

  private setPreview(path: string) {
    console.log("what do I see when you click ? " + path)
    if (path !== "") {
      return window.open("/" + path + ".preview");
    } else {
      return ""
    }
  };

  private formatDate(date: Date) {
    // 2019/05/07 14:21:36
    let dateStr = date.getFullYear().toString() + "/" +
      (date.getMonth()+1).toString() + "/" +
      date.getDate().toString() + " " +
      date.getHours().toString() + ":" +
      date.getMinutes().toString() + ":" +
      date.getSeconds().toString()

    if (dateStr.includes("NaN")) {
      dateStr = ""
    }
    return dateStr
  };

  private dismissNotification = () => {
    this.setState({ isEmptyResults: false });
  };

  private sort(key: string) {
    console.log("My Sort Key is: " + key)
    // Switch the direction each time some clicks.
    this.setState({ isSortedUp: !this.state.isSortedUp, sortKey: key }, () => {
      this.getSortedRows()
    })
  };

  private getSortedRows() {
    fetch(this.buildSearchUrl())
      .then(response => response.json())
      .then(responseJSON => this.setState({ data: responseJSON.data, nextPageRowCount: responseJSON.hasNextPage ? 1 : 0  }))
      .then(() => {
        if (JSON.stringify(this.state.data) === "[]") {
          this.setState({
            data: [{ "pant:transientPath": '', "jcr:created": '', "name": "", "jcr:title": "", "jcr:description": "", "sling:transientSource": "", "pant:transientSourceName": "" }],
            isEmptyResults: true
          }, () =>{
            console.log('transient path:'+this.state.data[0])
          })
        } else {
          this.setState({ isEmptyResults: false })
        }
      })
  };

  private buildSearchUrl() {
    let backend = "/modules.json?search="
    if (this.state.input != null && this.state.input !== "") {
      backend += this.state.input
    } else {
      backend += "*"
    }
    backend += "&key=" + this.state.sortKey + "&direction=" + (this.state.isSortedUp ? "desc" : "asc")
    backend += "&offset=" + ((this.state.page - 1)*this.state.pageLimit) + "&limit=" + this.state.pageLimit
    console.log(backend)  
    return backend
  }

  private hideAlertOne = () => this.setState({ alertOneVisible: false }, () => {
    this.setState({
      initialLoad: true, page: 1, deleteState: '', confirmDelete: false
    }, () => { this.doSearch });
  });

  private confirmDeleteOperation = () => this.setState({confirmDelete: !this.state.confirmDelete},() =>{
      console.log('confirmDelete:'+this.state.confirmDelete)
    });

  private cancelDeleteOperation = () => this.setState({confirmDelete: !this.state.confirmDelete},() =>{
    console.log('confirmDelete cancelled:'+this.state.confirmDelete)
  });

  private updatePageCounter = (direction) => {
    if(direction==="L" && this.state.page>1){
      this.setState({page: this.state.page - 1, initialLoad: true})
    }else if(direction==="R"){
      this.setState({page: this.state.page + 1, initialLoad: true})      
    }
  }

}
