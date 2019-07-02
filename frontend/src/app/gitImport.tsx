import React, { Component } from 'react';
import { Bullseye, Button, Alert, Modal, AlertActionCloseButton, FormGroup, TextInput } from '@patternfly/react-core';
import '@app/app.css';
import { Redirect } from 'react-router-dom'

class GitImport extends Component {
  public state = {
    branch: '',
    git2pantheonURL: '',
    isFormSubmitted: false,
    isMissingFields: false,
    isSucess: false,
    login: false,
    redirect: false,
    repository: '',
    submitMsg: ''
  };

  public render() {
    const { branch, repository, isFormSubmitted, isMissingFields, isSucess } = this.state;
    return (
      <React.Fragment>
        <Bullseye>
          <div className="app-container">
            {isFormSubmitted && isSucess && (
              <Modal
                isSmall={true}
                title="Request submitted."
                isOpen={true}
                onClose={this.dismissNotification}
                actions={[<Button key="yes" variant="primary" onClick={this.setRedirect}>Yes</Button>,
                <Button key="no" variant="secondary" onClick={this.dismissNotification}>No</Button>]}>
                {this.state.submitMsg} Do you want to be redirected to the module library?
              </Modal>)}
            <div>
              {isMissingFields && (
                <div className="notification-container">
                  <Alert
                    variant="warning"
                    title="A repository url is required."
                    action={<AlertActionCloseButton onClose={this.dismissNotification} />}
                  />
                </div>
              )}
              {isFormSubmitted && !isSucess && (
                <div className="notification-container">
                  <Alert
                    variant="danger"
                    title={this.state.submitMsg}
                    action={<AlertActionCloseButton onClose={this.dismissNotification} />}
                  />
                </div>
              )}

              <FormGroup
                label="Repository URL"
                fieldId="repository-url"
              >
                <TextInput id="repository-url" type="text" placeholder="Repository" value={repository} onChange={this.handleRepoInput} />
              </FormGroup>
              <br />
              <FormGroup
                label="Branch"
                fieldId="branch-name"
              >
                <TextInput id="branch-name" type="text" placeholder="Branch" value={branch} onChange={this.handleBranchInput} />
              </FormGroup>
              <br />
              <Button aria-label="Submit the repository and branch information to the git integration service." onClick={this.cloneRepo}>Submit</Button>
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

  private handleRepoInput = repository => {
    this.setState({ repository });
    console.log("Repository " + repository)
  };

  private handleBranchInput = branch => {
    this.setState({ branch });
    console.log("Branch " + branch)
  };

  private cloneRepo = (postBody) => {
    console.log("My repo is: " + this.state.repository + " and branch is " + this.state.branch + ".")
    if (this.state.repository === "") {
      this.setState({ isMissingFields: true })
    } else {
      fetch('/content/repositories/pantGitServiceURL')
        .then((resp => {
          if (!resp.ok) {
            this.setState({ isFormSubmitted: true, isSucess: false, msgType: "danger", submitMsg: "Error occurred, could not find the git2pantheon URL configuration." })
          } else {
            resp.text().then((text) => {
              this.setState({ git2pantheonURL: text })
              console.log("The response text from pantGitServiceURL is: " + text)
            })
              .then(() => {
                if (this.state.git2pantheonURL === "") {
                  this.setState({ isFormSubmitted: true, isSucess: false, msgType: "danger", submitMsg: "Error occurred, the git2pantheon URL configuration is blank." })
                } else {
                  console.log("The git2pantheon URL is: " + this.state.git2pantheonURL)
                  const payload = {
                    branch: this.state.branch,
                    repo: this.state.repository
                  };
                  fetch(this.state.git2pantheonURL + "/clone", {
                    body: JSON.stringify(payload),
                    method: "POST"
                  }).then(response => {
                    if (response.status === 201 || response.status === 200) {
                      console.log(" Works " + response.status)
                      this.setState({ isFormSubmitted: true, isSucess: true, msgType: "success", submitMsg: "The git import has been submitted it might take up to 1 minute to see it in the module library." })
                    } else if (response.status === 500) {
                      console.log(" Failed " + response.status)
                      response.text().then((text) => {
                        this.setState({ submitMsg: text })
                      });
                    } else {
                      this.setState({ isSucess: false, msgType: "danger", submitMsg: "The git import submission has failed." })
                    }
                    this.setState({ isFormSubmitted: true })
                  })
                    .catch(err => {
                      this.setState({ isFormSubmitted: true, isSucess: false, msgType: "danger", submitMsg: "Error occurred, it is likely that react could not connect to the git2pantheon URL." })
                      console.log("Error occurred, it is likely that react could not connect to the git2pantheon URL. The error was:", err);
                    });
                }
              })
          }
        }))
    }
  }

  private setRedirect = () => {
    this.setState({ redirect: true });
  };

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
    this.setState({ isMissingFields: false, isFormSubmitted: false });
  };

}

export { GitImport }