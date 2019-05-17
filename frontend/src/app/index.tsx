import React, { Component } from 'react';
import { Alert, AlertActionCloseButton, TextInput, Label } from '@patternfly/react-core';
import { Table, TableHeader, TableBody } from '@patternfly/react-table';
import '@app/app.css';

export default class Index extends Component {
  public state = {
    redirect: false,
    redirectLocation: '',
    input: '',
    columns: ['Name', 'Description', 'Source Type', 'Source Name', 'Upload Time'],
    data: [{ "pant:transientPath": '', "jcr:created": '', "name": "search_intro_message_thn", "jcr:title": "Search is case sensitive. Type '*' and press 'Enter' for all modules.", "jcr:description": "", "sling:transientSource": "", "pant:transientSourceName": "" }],
    isEmptyResults: false
  };
  public render() {
    const { columns, isEmptyResults, input } = this.state;
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
              >
              </Alert>
            </div>
          )}
            <div className="row-view">
              <Label>Search:</Label>
              <TextInput id="search" onKeyDown={(event) => this.getRows(event)} type="text" />
            </div>
            <Table aria-label="table-header" rows={[]} cells={columns} >
              <TableHeader />
            </Table>
            {this.state.data.map(data => (
              <Table id="table-rows" aria-label="table-data" key={data["pant:transientPath"]} rows={[[data["jcr:title"], data["jcr:description"], data["pant:transientSource"], data["pant:transientSourceName"], this.formatDate(new Date(data["jcr:created"]))]]} cells={columns} >
                <TableBody onRowClick={() => this.setPreview(data["pant:transientPath"])} />
              </Table>
            ))}
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
        var backend = "/modules.json?search="
        if (this.state.input != null && this.state.input != "" && this.state.input != "*") {
          backend = backend + this.state.input
          console.log(backend)
        }
        fetch(backend)
          .then(response => response.json())
          .then(responseJSON => this.setState({ data: responseJSON }))
          .then(() => {
           // console.log("JSON string is " + JSON.stringify(this.state.data))
            if (JSON.stringify(this.state.data) == "[]"){
              this.setState({isEmptyResults: true,
              data: [{ "pant:transientPath": '', "jcr:created": '', "name": "search_intro_message_thn", "jcr:title": "Search is case sensitive. Type '*' and press 'Enter' for all modules.", "jcr:description": "", "sling:transientSource": "", "pant:transientSourceName": "" }],
              })
            } else {
              this.setState({isEmptyResults: false})
            }
          })
      })
    }
  };

  private setPreview(path: string) {
    console.log("what do I see when you click ? " + path)
    if (path != "") {
      return window.open("/" + path + ".preview");
    } else {
      return ""
    }
  };

  private formatDate(date: Date) {
    //2019/05/07 14:21:36
    var dateStr = date.getFullYear().toString() + "/" +
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

}