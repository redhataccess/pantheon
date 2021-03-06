import React, { Component } from "react"
import {
  Page,
  PageSection,
  PageSectionVariants,
  PageSidebar
} from "@patternfly/react-core"
import { Header } from "@app/components/Chrome/Header/Header"
import { Sidebar } from "@app/components/Chrome/Sidebar/Sidebar"
import { Routes } from "@app/routes"
import "@app/app.css"

export interface IAppState {
  isAdmin: boolean
  isNavOpen: boolean
  userAuthenticated: boolean
  username: string
}

class App extends Component<any, IAppState> {
  public static ANON_USER = "anonymous"
  public static ADMIN_USER = "admin"
  public static ADMIN_GROUP = "pantheon-administrators"

  public static thisApp: App

  constructor(props) {
    super(props)

    this.state = {
      isAdmin: false,
      isNavOpen: true,
      userAuthenticated: false,
      username: App.ANON_USER,
    };
    App.thisApp = this
  }

  public componentDidMount() {
    fetch("/api/userinfo.json")
      .then(response => response.json())
      .then(responseJSON => {
          this.setState({
            isAdmin: responseJSON.userID === App.ADMIN_USER || responseJSON.groups.includes(App.ADMIN_GROUP),
            userAuthenticated: responseJSON.userID !== App.ANON_USER,
            username: responseJSON.userID
          })
    })
  }

  public onNavToggle() {
    // No idea why this roundabout setState is necessary, but if we replace this with the simpler "this.setState",
    // then we get a console error saying "`this` is undefined" - which I don"t even understand how that"s
    // possible - but this works around it.
    App.thisApp.setState({
      isNavOpen: !App.thisApp.state.isNavOpen
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