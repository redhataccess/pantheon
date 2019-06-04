import React, { Component } from 'react';
import {
  Alert, AlertActionCloseButton, Button, TextInput, Label,
  DataList, DataListItem, DataListItemRow, DataListItemCells, DataListCell
} from '@patternfly/react-core';
import '@app/app.css';

export default class Search extends Component {
  public state = {
    columns: ['Name', 'Description', 'Source Type', 'Source Name', 'Upload Time'],
    data: [{ "pant:transientPath": '', "jcr:created": '', "name": "", "jcr:title": "", "jcr:description": "", "sling:transientSource": "", "pant:transientSourceName": "" }],
    initialLoad: true,
    input: '*',
    isEmptyResults: false,
    isSortedUp: true,
    pageCount: 50,
    pageOffset: 1,
    redirect: false,
    redirectLocation: '',
    sortKey: ''
  };
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
                      <DataListCell width={4} key="title" onClick={() => this.sort("jcr:title")}>
                        <span className="sp-prop" id="span-name">Name</span>
                      </DataListCell>,
                      <DataListCell width={4} key="description" onClick={() => this.sort("jcr:description")}>
                        <span className="sp-prop" id="span-description">Description</span>
                      </DataListCell>,
                      <DataListCell key="resource source">
                        <span className="sp-prop-nosort" id="span-source-type">Source Type</span>
                      </DataListCell>,
                      <DataListCell key="source name">
                        <span className="sp-prop-nosort" id="span-source-name">Source Name</span>
                      </DataListCell>,
                      <DataListCell width={2} key="upload time" onClick={() => this.sort("jcr:created")}>
                        <span className="sp-prop" id="span-upload-time">Upload Time</span>
                      </DataListCell>,]} />
                </DataListItemRow>
                {this.state.data.map(data => (
                  <DataListItemRow id="data-rows" key={data["pant:transientPath"]} onClick={() => this.setPreview(data["pant:transientPath"])}>
                    <DataListItemCells
                      dataListCells={[
                        <DataListCell width={4} key="title">
                          {data["jcr:title"]}
                        </DataListCell>,
                        <DataListCell width={4} key="description">
                          {data["jcr:description"]}
                        </DataListCell>,
                        <DataListCell key="resource source">
                          {data["pant:transientSource"]}
                        </DataListCell>,
                        <DataListCell key="source name">
                          {data["pant:transientSourceName"]}
                        </DataListCell>,
                        <DataListCell width={2} key="upload time">
                          {this.formatDate(new Date(data["jcr:created"]))}
                        </DataListCell>,]} />
                  </DataListItemRow>
                ))}
              </DataListItem>
            </DataList>
          </div>
        </div>
      </React.Fragment>
    );
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
