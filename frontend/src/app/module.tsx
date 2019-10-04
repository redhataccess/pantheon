import React, { Component } from 'react';
import { Bullseye, Button, Alert, AlertActionCloseButton, FormGroup, TextInput } from '@patternfly/react-core';
import '@app/app.css';
import { Redirect } from 'react-router-dom'

class Module extends Component<any, any> {
  constructor(props) {
    super(props);
    this.state = {
      failedPost: false,
      isMissingFields: false,
      login: false,
      moduleDescription: '',
      moduleFile: File,
      moduleName: '',
      redirect: false
    };
  }

  public componentDidMount() {
    this.checkAuth();
  }

  public render() {
    const { moduleName, moduleDescription, isMissingFields } = this.state;
    return (
      <React.Fragment>
        <Bullseye>
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

              <FormGroup
                label="Module Name"
                fieldId="module-name"
              >
                <TextInput id="module-name" type="text" placeholder="Module Name" value={moduleName} onChange={this.handleNameInput} />
              </FormGroup>
              <br />
              <FormGroup
                label="Module Description"
                fieldId="module-description"
              >
                <TextInput id="module-description" type="text" placeholder="Module Description" value={moduleDescription} onChange={this.handleModuleInput} />
              </FormGroup>
              <br />
              <FormGroup
                label="Upload .adoc file: "
                fieldId="input-file"
              >
                <input id="input" className="input-file" color="#dddddd" type="file" onChange={this.handleFileChange} />
              </FormGroup>
              <br />
              <Button aria-label="Uploads the .adoc file with the Name and Description specified." onClick={this.saveModule}>Save</Button>
              <div>
                {this.loginRedirect()}
                {this.renderRedirect()}
              </div>
            </div>
          </div>
        </Bullseye>
      </React.Fragment>
    );
  }

  private handleNameInput = moduleName => {
    this.setState({ moduleName });
    // console.log("Name " + moduleName)

  };

  private handleModuleInput = moduleDescription => {
    this.setState({ moduleDescription });
    // console.log("Desc " + moduleDescription)
  };

  private handleFileChange = (event) => {
    this.setFile(event.target.files)
  }

  private setFile = selectorFiles => {
    this.setState({ moduleFile: selectorFiles })
    // console.log(selectorFiles);
  }

  private saveModule = (postBody) => {
    // console.log("My data is: " + this.state.moduleName + " and my desc is " + this.state.moduleDescription + " and my files are " + this.state.moduleFile)
    if (this.state.moduleName === "" || this.state.moduleFile[0] === undefined) {
      this.setState({ isMissingFields: true })
    } else {
      const hdrs = {
        'Accept': 'application/json',
        'cache-control': 'no-cache'
      }
      // console.log("The file is: " + this.state.moduleFile)
      const blob = new Blob([this.state.moduleFile[0]])
      const formData = new FormData();
      formData.append("jcr:title", this.state.moduleName)
      formData.append("jcr:description", this.state.moduleDescription)
      formData.append("sling:resourceType", "pantheon/modules")
      formData.append("jcr:primaryType", 'pant:module')
      formData.append("asciidoc@TypeHint", 'nt:file')
      formData.append("asciidoc/jcr:content/jcr:mimeType", "text/x-asciidoc")
      formData.append("asciidoc", blob)
      formData.append(":operation", "pant:newModuleVersion")

      fetch('/content/modules/' + this.state.moduleName, {
        body: formData,
        headers: hdrs,
        method: 'post'
      }).then(response => {
        if (response.status === 201 || response.status === 200) {
          console.log(" Works " + response.status)
          this.setState({ redirect: true })
        } else if (response.status === 500) {
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

  private checkAuth = () => {
    fetch("/system/sling/info.sessionInfo.json")
      .then(response => response.json())
      .then(responseJSON => {
        const key = "userID"
        if (responseJSON[key] === 'anonymous') {
          this.setState({ login: true })
        }
      })
  }

  private dismissNotification = () => {
    this.setState({ isMissingFields: false });
  };

}

export { Module }