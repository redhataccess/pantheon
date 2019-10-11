import React, { Component } from 'react'
import {
  Page,
  PageSection,
  PageSectionVariants
} from '@patternfly/react-core'
import { Header } from '@app/components/Chrome/Header/Header'
import { Sidebar } from '@app/components/Chrome/Sidebar/Sidebar'
import { Routes } from '@app/routes'
import '@app/app.css'

export interface IAppState {
  isAdmin: boolean
  isNavOpen: boolean
  userAuthenticated: boolean
  username: string
}

class App extends Component<any, IAppState> {
  public static ANON_USER = 'anonymous'
  public static ADMIN_USER = 'admin'

  constructor(props) {
    super(props)

    this.state = {
      isAdmin: false,
      isNavOpen: true,
      userAuthenticated: false,
      username: App.ANON_USER
    };
  }

  public componentDidMount() {
    fetch("/system/sling/info.sessionInfo.json")
      .then(response => response.json())
      .then(responseJSON => {
          this.setState({ 
            isAdmin: responseJSON.userID === App.ADMIN_USER ,
            userAuthenticated: responseJSON.userID !== App.ANON_USER,
            username: responseJSON.userID
          })
    })
  }

  public onNavToggle() {
    this.setState({
      isNavOpen: !this.state.isNavOpen
    })
  }
  
  public render() {
    return (
      <React.Fragment>
       <Page
          header={<Header isNavOpen={this.state.isNavOpen} onNavToggle={this.onNavToggle} appState={this.state} />}
          sidebar={<Sidebar isNavOpen={this.state.isNavOpen} appState={this.state} />}>
          <PageSection variant={PageSectionVariants.light}>
            <Routes {...this.state} />
          </PageSection>
        </Page>
      </React.Fragment>
    );
  }
}

export { App }