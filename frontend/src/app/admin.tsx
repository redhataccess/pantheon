import React, { Component } from 'react';
import { Button } from '@patternfly/react-core';
import { Redirect } from 'react-router-dom';

class Admin extends Component {
    public state = {
        buildDate: '',
        login: false
    };

    public render() {
        const id = 'userID';
        if(this.state.buildDate===''){
            fetch("/pantheon/builddate.json?")
            .then(response => response.json())
            .then(responseJSON => {
                    this.setState({ buildDate: responseJSON.buildDate }, () => {
                        console.log('Build date: ' + this.state.buildDate)
                    })
            })
        }

        return (  
            <React.Fragment>
            <div>
                {this.checkAuth()}
                {this.loginRedirect()}
                {/* {this.renderRedirect()} */}
            
                        <Button isBlock={true} onClick={this.browserLink()}>Browser link</Button>{'\n'}
                        <Button isBlock={true} variant="secondary" onClick={this.welcomeLink()}>Welcome link</Button>{'\n'}
                        <Button isBlock={true} onClick={this.consoleLink()}>Console link</Button>
                        Build Date: {this.state.buildDate}               
            </div>
            </React.Fragment>

        );
    }

    private browserLink = () => (event: any) =>  {
          return window.open("/bin/browser.html");
      };

    private welcomeLink = () => (event: any) =>  {
        return window.open("/starter/index.html");
    };

    private consoleLink = () => (event: any) =>  {
        return window.open("/system/console/bundles.html");
    };
    
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
            if (responseJSON[key] !== 'admin') {
              this.setState({ login: true })
            }
          })
      }
}

export { Admin }