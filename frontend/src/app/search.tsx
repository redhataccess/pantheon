import React, { Component } from 'react';
import {
  Alert, AlertActionCloseButton, TextInput, Label,
  DataList, DataListItem, DataListItemRow, DataListItemCells, DataListCell, Button, Checkbox, DataListCheck, Modal, AlertActionLink
} from '@patternfly/react-core';
import '@app/app.css';

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
    pageCount: 50,
    pageOffset: 1,
    redirect: false,
    redirectLocation: '',
    sortKey: '',
    initialLoad: true,
    allPaths: [''],
    isModalOpen: false,
    confirmDelete: false
  };

  public tpaths : string[] = [];
  public selectallpath : string[] = [];
  public events : any = [];

  public render() {
    const { columns, isEmptyResults, input, isSortedUp,sortKey} = this.state;
    
    return (
      <React.Fragment>
        {this.state.initialLoad && this.doSearch()}
        <div>
          <div>
            <div className="row-view">
              <Label>Search Query:</Label>
              <TextInput id="search" type="text" onKeyDown={this.getRows} onChange={(event) => this.setState({ input: event })} value={this.state.input} />
              <Label>Start At:</Label>
              <TextInput id="pageNum" type="text" pattern="[0-9]*" onKeyDown={this.getRows} onChange={(event) => this.setState({ pageOffset: event })} value={this.state.pageOffset} />
              <Label>Result Count:</Label>
              <TextInput id="pageCount" type="text" pattern="[0-9]*" onKeyDown={this.getRows} onChange={(event) => this.setState({ pageCount: event })} value={this.state.pageCount} />
              <Button onClick={this.doSearch}>Search</Button>
            </div>
            {isEmptyResults && (
              <div className="notification-container">
                <Alert
                  variant="warning"
                  title={"No modules found with your search of: " + this.state.input}
                  action={<AlertActionCloseButton onClose={this.dismissNotification} />}
                />
              </div>
            )}
            <div className="notification-container">
              <Alert
                variant="info"
                title="Search is case sensitive. Type '*' and press 'Enter' for all the modules."
              />
            </div>
            <DataList aria-label="Simple data list example">
              <DataListItem aria-labelledby="simple-item1">
                <DataListItemRow id="data-rows-header" >  
                  <DataListCheck aria-labelledby="width-ex1-check1"
                        isChecked={this.state.check}
                        aria-label="controlled checkbox example"
                        id="check"
                        onClick={this.handleSelectAll}
                        isDisabled={false}
                  />
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
                {this.state.data.map(data => (
                  <DataListItemRow id="data-rows">
                    <DataListCheck aria-labelledby="width-ex3-check1" 
                        isChecked={data["checkedItem"]}
                        aria-label="controlled checkbox example"
                        id={data["pant:transientPath"]}
                        name={data["pant:transientPath"]}
                        onClick={() => this.handleDeleteCheckboxChange(data["pant:transientPath"])}
                    />
                    <DataListItemCells key={data["pant:transientPath"]} onClick={() => this.setPreview(data["pant:transientPath"])} 
                          dataListCells={[      
                                <DataListCell width={2}>
                                  <span>{data["jcr:title"]}</span>
                                </DataListCell>,
                                <DataListCell width={2}>
                                  <span>{data["jcr:description"]===""?"No items found to be displayed":data["jcr:description"]}</span>
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
                <DataListItemRow id="data-rows" key={this.state.data["pant:transientPath"]}>
                    {
                      this.state.deleteButtonVisible?              
                        <Button variant="primary" onClick={() => this.confirmDeleteOperation(event)}>Delete</Button>
                      :null
                    }
                </DataListItemRow>
              </DataListItem>
            </DataList>
            <div className="alert">
              {this.state.confirmDelete===true && <Modal
                    isSmall
                    title="Confirmation"
                    isOpen={!this.state.isModalOpen}
                    onClose={this.hideAlertOne}
                    actions={[<Button key="yes" variant="primary" onClick={() => this.delete(event, this.tpaths)}>Yes</Button>,
                              <Button key="no" variant="secondary" onClick={this.cancelDeleteOperation}>No</Button>]}
                    >
                      Are you sure you want to delete the selected items?
                </Modal>}
              {this.state.deleteState=='positive' && <Modal
                    isSmall
                    title="Success"
                    isOpen={!this.state.isModalOpen}
                    onClose={this.hideAlertOne}
                    actions={[<Button key="cancel" variant="primary" onClick={this.hideAlertOne}>OK</Button>]}
                    >
                      Selected items are deleted!!!
                </Modal>}
                {this.state.deleteState=='negative' && <Modal
                    isSmall
                    title="Failure"
                    isOpen={!this.state.isModalOpen}
                    onClose={this.hideAlertOne}
                    actions={[<Button key="cancel" variant="primary" onClick={this.hideAlertOne}>OK</Button>]}
                    >
                      Selected items are not found!!!
                </Modal>}
                {this.state.deleteState=='unknown' && <Modal
                    isSmall
                    title="Error"
                    isOpen={!this.state.isModalOpen}
                    onClose={this.hideAlertOne}
                    actions={[<Button key="cancel" variant="primary" onClick={this.hideAlertOne}>OK</Button>]}
                    >
                      An unknown error occured, please check if you are logged in!!!
                </Modal>}
            </div>
            <div className="notification-container">
              <Alert
                variant="info"
                title="Search is case sensitive. Type '*' and press 'Enter' for all modules."
              />
            </div>
          </div>
        </div>
      </React.Fragment>
    );
  }
    

  private handleSelectAll = (event) => {
    console.log('handleSelectAll')
    console.log('selecall path:'+this.selectallpath)
    this.setState({check: !this.state.check}, () => {
      this.setState(prevState => {
        const selectAllcheck = this.state.data.map(dataitem => {
              dataitem["checkedItem"] = this.state.check
              console.log(dataitem["pant:transientPath"]+":"+dataitem["checkedItem"])
              this.state.check?this.state.allPaths.push(dataitem["pant:transientPath"]):delete this.state.allPaths[this.state.allPaths.indexOf(dataitem["pant:transientPath"])]
          return dataitem
        })
        this.tpaths=this.state.allPaths
        this.tpaths.map(e => e === "" ? delete this.tpaths[this.tpaths.indexOf(e)] : e)
        console.log('final tpaths:'+this.tpaths)
        console.log('all paths out:'+this.state.allPaths)
        if(this.state.check === true){
          this.setState({countOfCheckedBoxes: this.state.countOfCheckedBoxes+this.state.data.length}, () => {
                console.log('countOfCheckedBoxes: '+this.state.countOfCheckedBoxes)
                if(this.state.countOfCheckedBoxes > 0){
                  this.setState({deleteButtonVisible: true})
                }else{
                  this.setState({deleteButtonVisible: false})
                }
              })
        }else{
          this.setState({countOfCheckedBoxes: this.state.countOfCheckedBoxes-this.state.data.length}, () => {
                console.log('countOfCheckedBoxes: '+this.state.countOfCheckedBoxes)
                if(this.state.countOfCheckedBoxes > 0){
                  this.setState({deleteButtonVisible: true})
                }else{
                  this.setState({deleteButtonVisible: false})
                }
              })
              this.tpaths = []
              console.log('tpaths:'+this.tpaths)
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
            this.tpaths.push(data["pant:transientPath"]);   
            console.log('tpaths:'+this.tpaths)
          }else{
            this.setState({countOfCheckedBoxes: this.state.countOfCheckedBoxes-1}, () => {
              console.log('countOfCheckedBoxes: '+this.state.countOfCheckedBoxes)
              if(this.state.countOfCheckedBoxes > 0){
                this.setState({deleteButtonVisible: true})
              }else{
                this.setState({deleteButtonVisible: false})
              }
            })
            delete this.tpaths[this.tpaths.indexOf(id)]
            console.log('tpaths:'+this.tpaths)
          }
        }
        return data
      })
      return{
        data: updatedData
      }
    })
  };

  delete = (event, keydata) => {
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
          this.setState({ deleteState: 'positive'}) 
          console.log('deleteState:'+this.state.deleteState)
        } else if (response.status == 403) {
          this.setState({ deleteState: 'negative' })
          console.log('deleteState:'+this.state.deleteState)
        } else {
          this.setState({ deleteState: 'unknown' })
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
      .then(responseJSON => this.setState({ data: responseJSON }))
      .then(() => {
        if (JSON.stringify(this.state.data) === "[]") {
          this.setState({
            data: [{ "pant:transientPath": '', "jcr:created": '', "name": "", "jcr:title": "", "jcr:description": "", "sling:transientSource": "", "pant:transientSourceName": "" }],
            isEmptyResults: true
          })
        } else {
          this.setState({ isEmptyResults: false })
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
      .then(responseJSON => this.setState({ data: responseJSON }))
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
    backend += "&offset=" + (this.state.pageOffset - 1) + "&limit=" + this.state.pageCount
    console.log(backend)  
    return backend
  }

  public hideAlertOne = () => this.setState({ alertOneVisible: false }, () => {
      window.location.href = "/pantheon"
    });

  public confirmDeleteOperation = (event) => this.setState({confirmDelete: !this.state.confirmDelete},() =>{
      console.log('confirmDelete:'+this.state.confirmDelete)
    });

  public cancelDeleteOperation = () => this.setState({confirmDelete: !this.state.confirmDelete},() =>{
    console.log('confirmDelete cancelled:'+this.state.confirmDelete)
  });

}
