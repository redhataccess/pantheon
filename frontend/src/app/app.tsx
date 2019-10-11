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
  isNavOpen: boolean
  userAuthenticated: boolean
  username: string
}

class App extends Component<any, IAppState> {
  public static ANON_USER = 'anonymoffffffffffffffus'

  constructor(props) {
    super(props)

    this.state = {
      isNavOpen: true,
      userAuthenticated: false,
      username: App.ANON_USER
    };
  }

  public componentDidMount() {
    fetch("/system/sling/info.sessionInfo.json")
      .then(response => response.json())
      .then(responseJSON => {
        console.log("Got username of: " + responseJSON.userID)
          this.setState({ username: responseJSON.userID, userAuthenticated: responseJSON.userID !== App.ANON_USER })
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
          sidebar={<Sidebar isNavOpen={this.state.isNavOpen} />}>
          <PageSection variant={PageSectionVariants.light}>
            <Routes {...this.state} />
          </PageSection>
        </Page>
      </React.Fragment>
    );
  }
}

export { App }