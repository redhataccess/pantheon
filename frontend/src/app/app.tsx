import React, { Component } from 'react';
import { BrowserRouter as Router, Route, Link } from "react-router-dom";
import Index from '@app/index';
import Module from '@app/module';
import Login from '@app/login';

function App() {
  return (
    <Router>
      <div>
        <Header />
        <Route exact path="/" component={Index} />
        <Route exact path="/new-module" component={Module} />
        <Route exact path="/login" component={Login} />
      </div>
    </Router>
  );
}

function Home() {
  return <h2>Search</h2>;
}

function New() {
  return <h2>New Module</h2>;
}

function Header() {
  return (
    <ul>
      <li>
        <Link to="/">Search</Link>
      </li>
      <li>
        <Link to="/new-module">New Module</Link>
      </li>
      <li id='loginParent'>
        <LoginLink />
      </li>
    </ul>
  );
}
class LoginLink extends React.Component {
  public state = {
    linkText: 'Log In'
  }

  render() {
    if (this.state.linkText == 'Log In') {
      fetch("/system/sling/info.sessionInfo.json")
        .then(response => response.json())
        .then(responseJSON => {
          if (responseJSON['userID'] != 'anonymous') {
            this.setState({ linkText: 'Log Out [' + responseJSON['userID'] + ']' })
          }
        })
    }
    return <Link to="/login">{this.state.linkText}</Link>
  }
}

export default App;
