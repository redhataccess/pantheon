import React, { Component } from 'react';
import { Button, BackgroundImage, BackgroundImageSrc, TextInput } from '@patternfly/react-core';
import '@app/app.css';
import { Redirect } from 'react-router-dom'

export default class Login extends Component {
  public state = {
    username: '',
    password: '',
    currentLogin: 'anonymous'
  };

  public render() {
    const { username, password } = this.state;
    return (
      <React.Fragment>
        <div className="app-container">
          <div>
            <TextInput id="username" type="text" placeholder="Username" value={username} onChange={this.handleTextInputChange1} />
            <TextInput id="password" type="text" placeholder="Password" value={username} onChange={this.handleTextInputChange2} />
            <div>
              {this.loginRedirect()}
              {this.renderRedirect()}
              {this.checkAuth()}
              <Button onClick={this.login}>Log In</Button>
            </div>
          </div>
        </div>
      </React.Fragment>
    );
  }

  handleTextInputChange1 = moduleName => {
    this.setState({ moduleName });
    console.log("Name " + moduleName)

  };
  handleTextInputChange2 = moduleDescription => {
    this.setState({ moduleDescription });
    console.log("Desc " + moduleDescription)
  };

  login = (postBody) => {
    console.log("My name is: " + this.state.username + " and my pw is " + this.state.password + " and my current login is " + this.state.currentLogin)

    const hdrs = {
      'cache-control': 'no-cache',
      'Accept': 'application/json'
    }
    const formData = new FormData();
    formData.append("jcr:title", this.state.moduleName)
    formData.append("jcr:description", this.state.moduleDescription)
    formData.append("sling:resourceType", "pantheon/modules")
    formData.append("jcr:primaryType", 'pant:module')
    formData.append("asciidoc@TypeHint", 'nt:file')
    formData.append("asciidoc/jcr:content/jcr:mimeType", "text/x-asciidoc")
    formData.append("asciidoc", blob)

    fetch('/content/modules/' + this.state.moduleName, {
      method: 'post',
      headers: hdrs,
      body: formData
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

  checkAuth = () => {
    console.log('Check auth: ' + this.state.username)
    if (this.state.username == 'anonymous') {
      fetch("/system/sling/info.sessionInfo.json")
        .then(response => response.json())
        .then(responseJSON => {
          if (responseJSON["userID"] != 'anonymous') {
            this.setState({ username: responseJSON["userID"] })
          }
        })
    }
  }
}
