import React, { Component } from 'react';
import {
  Alert, AlertActionCloseButton, TextInput, Label,
  DataList, DataListItem, DataListItemRow, DataListItemCells, DataListCell, Button, Checkbox, DataListCheck
} from '@patternfly/react-core';
import '@app/app.css';

export default class Search extends Component {
  public state = {
    alertOneVisible: true ,
    check: false,
    columns: ['Name', 'Description', 'Source Type', 'Source Name', 'Upload Time'],
    countOfCheckedBoxes: 0,
    data: [{ "pant:transientPath": '', "jcr:created": '', "name": "", "jcr:title": "", "jcr:description": "", "sling:transientSource": "", "pant:transientSourceName": "" }],
    deleteButtonVisible: false,
    delstate: '',
    input: '*',
    isEmptyResults: false,
    isSortedUp: true,
    pageCount: 50,
    pageOffset: 1,
    redirect: false,
    redirectLocation: '',
    sortKey: '',
    initialLoad: true,
  };

  public hideAlertOne = () => this.setState({ alertOneVisible: false }, () => {
    window.location.href = "/pantheon"
  });

  public tpaths : string[] = [];

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
                  <DataListItemCells 
                        dataListCells={[
                          <DataListCell key="checkbox" aria-labelledby="width-ex1-check1">
                            <span className="sp-prop" id="span-check">Select</span>
                          </DataListCell>,
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
                            <span className="sp-prop" id="span-upload-time">Upload Time</span>
                          </DataListCell>,
                        ]} 
                  />
                </DataListItemRow>
                {this.state.data.map(data => (
                  <DataListItemRow id="data-rows">
                    <DataListCheck aria-labelledby="width-ex1-check1"
                        isChecked={this.state.check}
                        onChange={this.handleDeleteCheckboxChange}
                        aria-label="controlled checkbox example"
                        id="check"
                        name={data["pant:transientPath"]}
                    />
                    <DataListItemCells key={data["pant:transientPath"]} onClick={() => this.setPreview(data["pant:transientPath"])}
                          dataListCells={[      
                                <DataListCell width={2}>
                                  <span>{data["jcr:title"]}</span>
                                </DataListCell>,
                                <DataListCell width={2}>
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
                <DataListItemRow id="data-rows" key={this.state.data["pant:transientPath"]}>
                    {
                      this.state.deleteButtonVisible?              
                        <Button variant="primary" onClick={() => this.delete(event, this.tpaths)}>Delete</Button>
                      :null
                    }
                </DataListItemRow>
              </DataListItem>
            </DataList>
            <div className="alert">
              {this.state.delstate=='positive' && <Alert
                    variant="success"
                    title="Success"
                    action={<AlertActionCloseButton onClose={this.hideAlertOne} />}
                    >
                      Selected items are deleted!!!
                </Alert>}
                {this.state.delstate=='negative' && <Alert
                    variant="danger"
                    title="Failure"
                    action={<AlertActionCloseButton onClose={this.hideAlertOne} />}
                    >
                      Selected items not found!!!
                </Alert>}
                {this.state.delstate=='unknown' && <Alert
                    variant="danger"
                    title="Failure"
                    action={<AlertActionCloseButton onClose={this.hideAlertOne} />}
                    >
                      An error has occured, please check if you are logged in!!!
                </Alert>}
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

  private handleDeleteCheckboxChange = (checked, event) => {
    const target = event.target;
    const value = target.type === 'checkbox' ? target.checked : target.value;
    const name = target.name;
    this.setState({ [name]: value }, ()=> {
      if(checked === true){
        this.setState({countOfCheckedBoxes: this.state.countOfCheckedBoxes+1}, () => {
          if(this.state.countOfCheckedBoxes === 0){
            this.setState({deleteButtonVisible: false})
          }else{
            this.setState({deleteButtonVisible: true})
          }
        })
        this.tpaths.push(name);   
        console.log('tpaths:'+this.tpaths)
      }else{
        this.setState({countOfCheckedBoxes: this.state.countOfCheckedBoxes-1}, () => {
          if(this.state.countOfCheckedBoxes === 0){
            this.setState({deleteButtonVisible: false})
          }else{
            this.setState({deleteButtonVisible: true})
          }
        })
        this.tpaths.splice(this.tpaths.indexOf(name))
        console.log('tpaths:'+this.tpaths)
      }
    });
  };

  delete = (event, keydata) => {
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
          this.setState({ delstate: 'positive' })
          console.log('delstate:'+this.state.delstate)
        } else if (response.status == 403) {
          this.setState({ delstate: 'negative' })
          console.log('delstate:'+this.state.delstate)
        } else {
          this.setState({ delstate: 'unknown' })
          console.log('delstate:'+this.state.delstate)
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
        // console.log("JSON string is " + JSON.stringify(this.state.data))
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
        // console.log("JSON string is " + JSON.stringify(this.state.data))
        if (JSON.stringify(this.state.data) === "[]") {
          this.setState({
            data: [{ "pant:transientPath": '', "jcr:created": '', "name": "", "jcr:title": "", "jcr:description": "", "sling:transientSource": "", "pant:transientSourceName": "" }],
            isEmptyResults: true
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
}
