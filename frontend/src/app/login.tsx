import React, { Component } from 'react';
import { Bullseye, Button, Alert, AlertActionCloseButton, FormGroup, TextInput } from '@patternfly/react-core';
import '@app/app.css';

class Login extends Component<any, any, any> {
  constructor(props) {
    super(props)
    this.state = {
      authMessage: '',
      currentLogin: 'anonymous',
      password: '',
      username: ''
    };
  }

  public componentDidMount() {
    this.checkAuth()
  }

  public render() {
    return (
      <React.Fragment>
        <Bullseye>
          <div className="app-container">
            <div>
              {this.failedAuthMessage()}
              <FormGroup
                label="Username:"
                fieldId="username"
              >
                <TextInput id="username" type="text" placeholder="Username" value={this.state.username} onChange={this.onUsernameChange} onKeyPress={this.onLoginKeyPress} />
              </FormGroup>
              <br />
              <FormGroup
                label="Password:"
                fieldId="password"
              >
                <TextInput id="password" type="password" placeholder="Password" value={this.state.password} onChange={this.onPasswordChange} onKeyPress={this.onLoginKeyPress} />
              </FormGroup>
              <br />
              <div>
                <Button onClick={this.login}>Log In</Button>
              </div>
            </div>
          </div>
        </Bullseye>
      </React.Fragment>
    );
  }

  private onLoginKeyPress = (event) => {
    if (event.key === 'Enter') {
      this.login()
    }
  }

  private failedAuthMessage = () => {
    return this.state.authMessage.length > 0 && <div className="notification-container">
      <Alert variant="danger"
        title={this.state.authMessage}
        action={<AlertActionCloseButton onClose={this.resetAuthMessage} />} />
    </div>
  }

  private resetAuthMessage = () => {
    this.setState({ authMessage: '' })
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
        this.setState({ authMessage: "Unknown failure - HTTP " + response.status + ": " + response.statusText })
      }
    });
  }

  private checkAuth = () => {
    console.log('Check auth: ' + this.state.currentLogin)
    if (this.state.currentLogin === 'anonymous') {
      fetch("/system/sling/info.sessionInfo.json")
        .then(response => response.json())
        .then(responseJSON => {
          const key = "userID"
          if (responseJSON[key] !== 'anonymous') {
            this.setState({ currentLogin: responseJSON[key] })
          }
        })
    }
  }
}

export { Login }