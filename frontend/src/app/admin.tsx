import React, { Component } from 'react';
import { Redirect } from 'react-router-dom';
import { Button } from '@patternfly/react-core';
import { Grid, GridItem } from '@patternfly/react-core';
import Browseri from '@app/images/browseri.jpg';
import Consolei from '@app/images/consolei.jpg';
import Welcomei from '@app/images/welcomei.jpg';
import { Card, CardHeader, CardBody } from '@patternfly/react-core';

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
                {this.checkAuth()}
                {this.loginRedirect()}
              <Grid gutter="md">
                <GridItem span={12}/>
                <GridItem span={12}/>
                <GridItem span={12}/>
                <GridItem span={12}/>
                <GridItem span={12}/>
                <GridItem span={12}/>
                <GridItem span={12}/>
                <GridItem span={12}/>
                <GridItem span={12}/>
                <GridItem span={12}/>
                <GridItem span={12}/>
                <GridItem span={3} rowSpan={2}/>
                <GridItem span={2} rowSpan={2}>
                  <Card>
                    <CardHeader><Button isBlock={true} variant={"secondary"} onClick={this.browserLink()}><img src={Browseri} style={{height: "100px"}}/></Button></CardHeader>
                    <CardBody style={{fontSize: "16px"}}>Browser link</CardBody>
                  </Card>
                </GridItem>
                <GridItem span={2} rowSpan={2}>
                  <Card>
                    <CardHeader><Button isBlock={true} variant={"secondary"} onClick={this.consoleLink()}><img src={Consolei} style={{height: "100px"}}/></Button></CardHeader>
                    <CardBody style={{fontSize: "16px"}}>Web Console Link</CardBody>
                  </Card>
                </GridItem>
                <GridItem span={2} rowSpan={2}>
                  <Card>
                    <CardHeader><Button isBlock={true} variant={"secondary"} onClick={this.welcomeLink()}><img src={Welcomei} style={{height: "100px"}}/></Button></CardHeader>
                    <CardBody style={{fontSize: "16px",alignItems:'center'}}>Sling Welcome Link</CardBody>
                  </Card>
                </GridItem>
                <GridItem span={3} rowSpan={2}/>
                <GridItem span={12} rowSpan={2}/>
                <GridItem span={3}/><GridItem span={2}/><GridItem span={2} rowSpan={2} style={{fontSize: "16px",alignItems: 'center'}}><div>Build Date:</div><div>{this.state.buildDate}</div></GridItem><GridItem span={2}/><GridItem span={3}/>
              </Grid>
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