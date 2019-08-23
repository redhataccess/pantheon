import React, { Component } from 'react';
import {
  Alert, AlertActionCloseButton, TextInput,
  DataList, DataListItem, DataListItemRow, DataListItemCells,
  DataListCell, FormGroup, Button, DataListCheck, Modal,
  Level, LevelItem
} from '@patternfly/react-core';
import '@app/app.css';
import { BuildInfo } from './components/Chrome/Header/BuildInfo'
import { Pagination } from '@app/Pagination';

export default class Search extends Component {
  public state = {
    alertOneVisible: true ,
    allPaths: [''],
    check: false,
    checkNextPageRow: "",
    checkedItemKey: "checkedItem",
    columns: ['Name', 'Description', 'Source Type', 'Source Name', 'Upload Time'],
    confirmDelete: false,
    countOfCheckedBoxes: 0,
    deleteButtonVisible: false,
    deleteState: '',
    initialLoad: true,
    input: '',
    isEmptyResults: false,
    isModalOpen: false,
    isSortedUp: true,
    loggedinStatus: false,
    nextPageRowCount: 1,
    page: 1,
    pageLimit: 25,
    redirect: false,
    redirectLocation: '',
    results: [{ "pant:transientPath": '', "jcr:created": '', "name": "", "jcr:title": "", "jcr:description": "", "sling:transientSource": "", "pant:transientSourceName": "" ,"checkedItem":false}],
    showDropdownOptions: true,
    sortKey: ''
  };

  public transientPaths : string[] = [];

  public render() {
    const { columns, isEmptyResults, input, isSortedUp,sortKey} = this.state;

    const id = 'userID';
    if (!this.state.loggedinStatus && this.state.initialLoad===true) {
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
          <FormGroup
              label="Search Query"
              fieldId="search"
              helperText="Search is case sensitive. An empty search will show all modules."
            >
              <div className="row-view">
                <TextInput id="search" type="text" onKeyDown={this.getRows} onChange={this.setInput} value={this.state.input} />
                <Button onClick={this.newSearch}>Search</Button>
              </div>
            </FormGroup>
            <div className="notification-container">
              { console.log("this.state.results: ") }
            { console.log(this.state.results) }
              <Pagination
                handleMoveLeft={this.updatePageCounter("L")}
                handleMoveRight={this.updatePageCounter("R")}
                handleMoveToFirst={this.updatePageCounter("F")}
                pageNumber={this.state.page}
                nextPageRecordCount={this.state.nextPageRowCount}
                handlePerPageLimit={this.changePerPageLimit}
                perPageLimit={this.state.pageLimit}
                showDropdownOptions={this.state.showDropdownOptions}
                bottom={false}
              />
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
                          <DataListCell width={2} key="title">
                            <button onClick={this.sortByName} className="sp-prop" id="span-name" aria-label="sort column by name">Name</button>
                          </DataListCell>,
                          <DataListCell width={2} key="description">
                            <button onClick={this.sortByDescription} className="sp-prop" id="span-name" aria-label="sort column by description">Description</button>
                          </DataListCell>,
                          <DataListCell key="resource source">
                            <span className="sp-prop-nosort" id="span-source-type">Source Type</span>
                          </DataListCell>,
                          <DataListCell key="source name">
                            <span className="sp-prop-nosort" id="span-source-name">Source Name</span>
                          </DataListCell>,
                          <DataListCell key="upload time">
                            <button onClick={this.sortByUploadTime} className="sp-prop" id="span-name" aria-label="sort column by upload time">Upload Time</button>
                          </DataListCell>,
                        ]}
                  />
                </DataListItemRow>
                {/* Delete button at the top */}
                <DataListItemRow id="data-rows" key={this.state.results["pant:transientPath"]}>
                  {
                    this.state.deleteButtonVisible ?
                      <Button variant="primary" onClick={this.confirmDeleteOperation}>Delete</Button>
                      : null
                  }
                </DataListItemRow>
                {this.state.results.map(data => (
                  <DataListItemRow id="data-rows">
                    {this.state.loggedinStatus && !this.state.isEmptyResults &&
                      <DataListCheck aria-labelledby="width-ex3-check1"
                        className="checkbox"
                        isChecked={data[this.state.checkedItemKey]}
                        aria-label="controlled checkbox example"
                        id={data["pant:transientPath"]}
                        name={data["pant:transientPath"]}
                        onClick={this.handleDeleteCheckboxChange(data["pant:transientPath"])}
                      />}
                    <DataListItemCells key={data["pant:transientPath"]} onClick={this.setPreview(data["pant:transientPath"])}
                          dataListCells={[
                                <DataListCell key="div-title" width={2}>
                                  <span>{data["jcr:title"]}</span>
                                </DataListCell>,
                                <DataListCell  key="div-description" width={2}>
                                  <span>{data["jcr:description"]===""?"No items found to be displayed":data["jcr:description"]}</span>
                                </DataListCell>,
                                <DataListCell key="div-transient-source">
                                  <span>{data["pant:transientSource"]}</span>
                                </DataListCell>,
                                <DataListCell key="div-transient-source-name">
                                  <span>{data["pant:transientSourceName"]}</span>
                                </DataListCell>,
                                <DataListCell key="div-created">
                                <span >{this.formatDate(new Date(data["jcr:created"]))}</span>
                                </DataListCell>
                          ]}
                    />
                  </DataListItemRow>
                ))}
                {/* Delete button at the bottom */}
                <DataListItemRow id="data-rows" key={this.state.results["pant:transientPath"]}>
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
                handleMoveLeft={this.updatePageCounter("L")}
                handleMoveRight={this.updatePageCounter("R")}
                handleMoveToFirst={this.updatePageCounter("F")}
                pageNumber={this.state.page}
                nextPageRecordCount={this.state.nextPageRowCount}
                handlePerPageLimit={this.changePerPageLimit}
                perPageLimit={this.state.pageLimit}
                showDropdownOptions={!this.state.showDropdownOptions}
                bottom={true}
              />
            </div>
            <BuildInfo/>
            {/* Alert for delete confirmation */}
            <div className="alert">
              {this.state.confirmDelete===true && <Modal
                    isSmall={true}
                    title="Confirmation"
                    isOpen={!this.state.isModalOpen}
                    onClose={this.hideAlertOne}
                    actions={[<Button key="yes" variant="primary" onClick={this.delete(this.transientPaths)}>Yes</Button>,
                              <Button key="no" variant="secondary" onClick={this.cancelDeleteOperation}>No</Button>]}
                    >
                      Are you sure you want to delete the selected items?
                </Modal>}
                {/* Alerts after confirmation on delete */}
              {this.state.deleteState==='positive' && <Modal
                    isSmall={true}
                    title="Success"
                    isOpen={!this.state.isModalOpen}
                    onClose={this.hideAlertOne}
                    actions={[<Button key="cancel" variant="primary" onClick={this.hideAlertOne}>OK</Button>]}
                    >
                      Selected items were deleted.
                </Modal>}
                {this.state.deleteState==='negative' && <Modal
                    isSmall={true}
                    title="Failure"
                    isOpen={!this.state.isModalOpen}
                    onClose={this.hideAlertOne}
                    actions={[<Button key="cancel" variant="primary" onClick={this.hideAlertOne}>OK</Button>]}
                    >
                      Selected items were not found.
                </Modal>}
                {this.state.deleteState==='unknown' && <Modal
                    isSmall={true}
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

  private setInput = (event) => this.setState({ input: event });

  private handleSelectAll = (event) => {
    console.log('handleSelectAll')
    this.setState({check: !this.state.check}, () => {
      this.setState(prevState => {
        this.state.allPaths=[]
        this.transientPaths=[]
        const selectAllcheck = this.state.results.map(dataitem => {
              dataitem[this.state.checkedItemKey] = this.state.check
              console.log(dataitem["pant:transientPath"]+":"+dataitem[this.state.checkedItemKey])
              if(this.state.check){
                this.state.allPaths.push(dataitem["pant:transientPath"])
              }
          return dataitem
        })
        this.transientPaths=this.state.allPaths
        if(this.state.check === true){
          this.setState({countOfCheckedBoxes: this.state.results.length}, () => {
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

  private handleDeleteCheckboxChange = (id) => (event: any) => {
    this.setState(prevState => {
      const updatedData = this.state.results.map(data => {
        if(data["pant:transientPath"] === id){
          data[this.state.checkedItemKey] = !data[this.state.checkedItemKey]
          if(data[this.state.checkedItemKey] === true){
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

  private delete = (keydata) => (event: any) =>  {
    console.log(keydata)
    console.log('in the delete function')
      const formData = new FormData();
      formData.append(':operation', 'delete')
      for (const item of keydata){
        formData.append(':applyTo', '/content/'+item)
      }
      fetch('/content/'+keydata[0], {
        body: formData,
        method: 'post'
      }).then(response => {
        if (response.status === 200) {
          this.setState({ deleteState: 'positive'},() =>
          this.transientPaths=[])
          console.log('deleteState:'+this.state.deleteState)
        } else if (response.status === 403) {
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
      this.setState({page: 1, initialLoad: true},()=>{
        this.doSearch()
      })
    }
  };

  private newSearch = () => {
    this.setState({page: 1, initialLoad: true},()=>{
      this.doSearch()
    })
  }

  private doSearch = () => {
    this.setState({ initialLoad: false })
    fetch(this.buildSearchUrl())
      .then(response => response.json())
      .then(responseJSON => this.setState({ results: responseJSON.results, nextPageRowCount: responseJSON.hasNextPage ? 1 : 0 }))
      .then(() => {
        if (JSON.stringify(this.state.results) === "[]") {
          this.setState({
            check: false,
            deleteButtonVisible: false,
            isEmptyResults: true
          })
        } else {
          this.setState({
            check: false,
            countOfCheckedBoxes: 0,
            deleteButtonVisible: false,
            isEmptyResults: false
           })
        }
      })
    }

    private setPreview = (path: string) => (event: any) =>  {
      console.log("what do I see when you click ? " + path)
      if (path !== "") {
        return window.open("/" + path + ".preview?draft=true");
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

  private sortByName = () => {
    this.sort("jcr:title")
  }

  private sortByDescription = () => {
    this.sort("jcr:description")
  }

  private sortByUploadTime = () => {
    this.sort("jcr:created")
  }

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
      .then(responseJSON => this.setState({ results: responseJSON.results, nextPageRowCount: responseJSON.hasNextPage ? 1 : 0  }))
      .then(() => {
        if (JSON.stringify(this.state.results) === "[]") {
          this.setState({
            data: [{ "pant:transientPath": '', "jcr:created": '', "name": "", "jcr:title": "", "jcr:description": "", "sling:transientSource": "", "pant:transientSourceName": "" }],
            isEmptyResults: true
          }, () =>{
            console.log('transient path:'+this.state.results[0])
          })
        } else {
          this.setState({ isEmptyResults: false })
        }
      })
  };

  private buildSearchUrl() {
    let backend = "/modules.json?search="
    if (this.state.input != null) {
      backend += this.state.input
    }
    backend += "&key=" + this.state.sortKey + "&direction=" + (this.state.isSortedUp ? "desc" : "asc")
    backend += "&offset=" + ((this.state.page - 1)*this.state.pageLimit) + "&limit=" + this.state.pageLimit
    console.log('itemsPerPaeProp: '+this.state.pageLimit)
    console.log(backend)
    return backend
  }

  private hideAlertOne = () => this.setState({ alertOneVisible: false }, () => {
    this.setState({
      confirmDelete: false,
      deleteState: '',
      initialLoad: true,
      page: 1
    });
  });

  private confirmDeleteOperation = () => this.setState({confirmDelete: !this.state.confirmDelete},() =>{
      console.log('confirmDelete:'+this.state.confirmDelete)
    });

  private cancelDeleteOperation = () => this.setState({confirmDelete: !this.state.confirmDelete},() =>{
    console.log('confirmDelete cancelled:'+this.state.confirmDelete)
  });

  private updatePageCounter = (direction: string) => () =>  {
    if(direction==="L" && this.state.page>1){
      this.setState({page: this.state.page - 1, initialLoad: true})
    }else if(direction==="R"){
      this.setState({page: this.state.page + 1, initialLoad: true})
    }else if(direction==="F"){
      this.setState({page: 1, initialLoad: true})
    }
  }

  private changePerPageLimit = (pageLimitValue) => {
    this.setState({pageLimit: pageLimitValue, initialLoad: true,page: 1},()=>{
      console.log("pageLImit value on calling changePerPageLimit function: "+this.state.pageLimit)
      return (this.state.pageLimit+" items per page")
    })
  }

}
