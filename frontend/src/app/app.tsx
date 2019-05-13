import React, { Component } from 'react';
import { BrowserRouter as Router, Route, Link } from "react-router-dom";
import Index from '@app/index';
import Module from '@app/module';

function App() {
  return (
    <Router>
      <div>
        <Header />
        <Route exact path="/" component={Index} />
        <Route exact path="/new-module" component={Module} />
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
    </ul>
  );
}

export default App;
