import React, { Component } from 'react';
import {
  Alert, AlertActionCloseButton, TextInput, Label,
  DataList, DataListItem, DataListItemRow, DataListItemCells, DataListCell
} from '@patternfly/react-core';
import '@app/app.css';

export default class Index extends Component {
  public state = {
    columns: ['Name', 'Description', 'Source Type', 'Source Name', 'Upload Time'],
    data: [{ "pant:transientPath": '', "jcr:created": '', "name": "", "jcr:title": "", "jcr:description": "", "sling:transientSource": "", "pant:transientSourceName": "" }],
    input: '',
    isEmptyResults: false,
    isSortedUp: true,
    redirect: false,
    redirectLocation: '',
    sortKey: ''
  };
  public render() {
    const { columns, isEmptyResults, input, isSortedUp,sortKey } = this.state;
    return (
      <React.Fragment>
        <div className="app-container">
          <div>
            {isEmptyResults && (
              <div className="notification-container">
                <Alert
                  variant="warning"
                  title={"No modules found with your search of: " + this.state.input}
                  action={<AlertActionCloseButton onClose={this.dismissNotification} />}
                />
              </div>
            )}
            <div className="row-view">
              <Label>Search:</Label>
              <TextInput id="search" onKeyDown={(event) => this.getRows(event)} type="text" />
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
                      <DataListCell key="upload time" onClick={() => this.sort("jcr:created")}>
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
                        <DataListCell key="upload time">
                          {this.formatDate(new Date(data["jcr:created"]))}
                        </DataListCell>,]} />
                  </DataListItemRow>
                ))}
              </DataListItem>
            </DataList>
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

  private getRows = (event) => {
    if (event.key === 'Enter') {
      console.log("what do I see? " + event.target.value)
      this.setState({
        input: event.target.value
      }, () => {
        console.log("Now I get the expected value down " + this.state.input)
        let backend = "/modules.json?search="
        if (this.state.input != null && this.state.input != "" && this.state.input != "*") {
          backend = backend + this.state.input + "&key=" + "jcr:created" + "&direction=" + "desc"
          console.log(backend)
        }
        fetch(backend)
          .then(response => response.json())
          .then(responseJSON => this.setState({ data: responseJSON }))
          .then(() => {
            // console.log("JSON string is " + JSON.stringify(this.state.data))
            if (JSON.stringify(this.state.data) == "[]") {
              this.setState({
                data: [{ "pant:transientPath": '', "jcr:created": '', "name": "", "jcr:title": "", "jcr:description": "", "sling:transientSource": "", "pant:transientSourceName": "" }],
                isEmptyResults: true
              })
            } else {
              this.setState({ isEmptyResults: false })
            }
          })
      })
    }
  };

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
    console.log("Now I get the expected value down " + this.state.input)
    let direction = ""
    if (this.state.isSortedUp) {
      direction = "asc"
    }else {
      direction = "desc"
    }
    let backend = "/modules.json?search="+ "&key=" + this.state.sortKey + "&direction=" + direction
    if (this.state.input != null && this.state.input !== "" && this.state.input !== "*") {
      backend = "/modules.json?search=" + this.state.input + "&key=" + this.state.sortKey + "&direction=" + direction
      console.log(backend)
    }
    fetch(backend)
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
}