import React, { Component } from 'react';
import { Button, Alert, AlertActionCloseButton, TextInput } from '@patternfly/react-core';
import '@app/app.css';
import { Redirect } from 'react-router-dom'

export default class Module extends Component {
  public state = {
    failedPost: false,
    isMissingFields: false,
    login: false,
    moduleDescription: '',
    moduleFile: File,
    moduleName: '',
    redirect: false
  };

  public render() {
    const { moduleName, moduleDescription, isMissingFields } = this.state;
    return (
      <React.Fragment>
        <div className="app-container">
          <div>
          {isMissingFields && (
            <div className="notification-container">
              <Alert
                variant="warning"
                  title="A module name and choosing a file is required."
                action={<AlertActionCloseButton onClose={this.dismissNotification} />}
              />
            </div>
          )}
            <TextInput id="module-name" type="text" placeholder="Module Name" value={moduleName} onChange={this.handleNameInput} />
            <TextInput id="module-description" type="text" placeholder="Module Description" value={moduleDescription} onChange={this.handleModuleInput} />
            <input id="input" className="input-file" color="#dddddd" type="file" onChange={(e) => this.handleFileChange(e.target.files)} />
            <div>
              {this.loginRedirect()}
              {this.renderRedirect()}
              <Button onClick={this.saveModule}>Save</Button>
            </div>
          </div>
        </div>
      </React.Fragment>
    );
  }

  private handleNameInput = moduleName => {
    this.setState({ moduleName });
    console.log("Name " + moduleName)

  };

  private handleModuleInput = moduleDescription => {
    this.setState({ moduleDescription });
    console.log("Desc " + moduleDescription)
  };

  private handleFileChange = selectorFiles => {
    this.setState({ moduleFile: selectorFiles })
    console.log(selectorFiles);
  }

  private saveModule = (postBody) => {
    console.log("My data is: " + this.state.moduleName + " and my desc is " + this.state.moduleDescription + " and my files are " + this.state.moduleFile)
    if (this.state.moduleName == "" || this.state.moduleFile[0] == undefined){
      this.setState({ isMissingFields: true })
    } else {
    const hdrs = {
      'cache-control': 'no-cache',
      'Accept': 'application/json'
    }
    console.log("The file is: " + this.state.moduleFile)
    const blob = new Blob([this.state.moduleFile[0]])
    const formData = new FormData();
    formData.append("jcr:title", this.state.moduleName)
    formData.append("jcr:description", this.state.moduleDescription)
    formData.append("sling:resourceType", "pantheon/modules")
    formData.append("jcr:primaryType", 'pant:module')
    formData.append("asciidoc@TypeHint", 'nt:file')
    formData.append("asciidoc/jcr:content/jcr:mimeType", "text/x-asciidoc")
    formData.append("asciidoc", blob)

    fetch('/content/modules/' + this.state.moduleName, {
      body: formData,
      headers: hdrs,
      method: 'post'
    }).then(response => {
      if (response.status == 201 || response.status == 200) {
        console.log(" Works " + response.status)
        this.setState({ redirect: true })
      } else  if (response.status == 500) {
        console.log(" Needs login " + response.status)
        this.setState({ login: true })
      } else {
        console.log(" Failed " + response.status)
        this.setState({ failedPost: true })
      }
    });
   }
  }

  private renderRedirect = () => {
    if (this.state.redirect) {
      return <Redirect to='/' />
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
    this.setState({ isMissingFields: false });
  };

}
