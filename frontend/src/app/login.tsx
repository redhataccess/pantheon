import React, { Component } from 'react';
import { Button, Alert, AlertActionCloseButton, BackgroundImage, BackgroundImageSrc, TextInput } from '@patternfly/react-core';
import '@app/app.css';

export default class Login extends Component {
  public state = {
    authMessage: '',
    currentLogin: 'anonymous',
    password: '',
    username: ''
  };

  public render() {
    const { username, password } = this.state;
    return (
      <React.Fragment>
        <div className="app-container">
          <div>
            {this.failedAuthMessage()}
            <TextInput id="username" type="text" placeholder="Username" value={username} onChange={this.onUsernameChange} onKeyPress={this.onLoginKeyPress} />
            <TextInput id="password" type="password" placeholder="Password" value={password} onChange={this.onPasswordChange} onKeyPress={this.onLoginKeyPress} />
            <div>
              {this.checkAuth()}
              <Button onClick={this.login}>Log In</Button>
            </div>
          </div>
        </div>
      </React.Fragment>
    );
  }

  private onLoginKeyPress = (event) => {
    if (event.key == 'Enter') {
      this.login()
    }
  }

  private failedAuthMessage = () => {
    return this.state.authMessage.length > 0 && <div className="notification-container">
      <Alert variant="danger"
          title={this.state.authMessage}
        action={<AlertActionCloseButton onClose={() => { this.setState({ authMessage: '' })}} />} />
    </div>
  }

  private onUsernameChange = username => {
    this.setState({ username });

  };
  private onPasswordChange = password => {
    this.setState({ password });
  };

  private login = () => {
    const formData = new FormData();
    formData.append("j_username", this.state.username)
    formData.append("j_password", this.state.password)

    fetch('/j_security_check', {
      body: formData,
      method: 'post',
    }).then(response => {
      if (response.status === 200) {
        console.log(" Works " + response.status)
        window.location.href = "/pantheon"
      } else if (response.status === 403) {
        this.setState({ authMessage: "Login failed, please try again." })
      } else {
        this.setState({ authMessage: "Unknown failure - HTTP " + response.status + ": " + response.statusText})
      }
    });
  }

  private checkAuth = () => {
    console.log('Check auth: ' + this.state.currentLogin)
    if (this.state.currentLogin === 'anonymous') {
      fetch("/system/sling/info.sessionInfo.json")
        .then(response => response.json())
        .then(responseJSON => {
          if (responseJSON["userID"] != 'anonymous') {
            this.setState({ currentLogin: responseJSON["userID"] })
          }
        })
    }
  }
}
