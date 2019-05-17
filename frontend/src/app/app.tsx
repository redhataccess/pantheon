import React, { Component } from 'react';
import { BrowserRouter as Router, Route, Link } from "react-router-dom";
import Index from '@app/index';
import Module from '@app/module';
import Login from '@app/login';

function App() {
  return <Routes />
}

class Routes extends React.Component {
  public state = {
    isLoggedIn: false,
    linkText: 'Log In'
  }

  render() {
    if (!this.state.isLoggedIn) {
      fetch("/system/sling/info.sessionInfo.json")
        .then(response => response.json())
        .then(responseJSON => {
          if (responseJSON['userID'] != 'anonymous') {
            this.setState({ linkText: 'Log Out [' + responseJSON['userID'] + ']' })
            this.setState({ isLoggedIn: true })
          }
        })
    }
    return <Router>
      <div>
        <ul>
          <li>
            <Link to="/">Search</Link>
          </li>
          { this.state.isLoggedIn && 
            <li>
              <Link to="/new-module">New Module</Link>
            </li>
          }
          <li id='loginParent'>
            <Link to={this.state.isLoggedIn ? '/logout' : '/login'}
                onClick={this.conditionalRedirect}>
              {this.state.linkText}
            </Link>
          </li>
        </ul>
        <Route exact path="/" component={Index} />
        <Route exact path="/new-module" component={Module} />
        <Route exact path="/login" component={Login} />
      </div>
    </Router>
  }

  conditionalRedirect = () => {
    console.log("Conditional redirect")
    if (this.state.linkText.startsWith("Log Out")) {
      fetch('/system/sling/logout')
          .then(response => window.location.href = "/pantheon")
    }  
  }
}

export default App;
