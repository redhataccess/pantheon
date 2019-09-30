import React, { Component } from 'react';
import { Link } from "react-router-dom";

class User extends Component {
    public state = {
        isLoggedIn: false,
        linkText: 'Log In'
    };

    public render() {
        const id = 'userID';
        if (!this.state.isLoggedIn) {
            fetch("/system/sling/info.sessionInfo.json")
                .then(response => response.json())
                .then(responseJSON => {
                    if (responseJSON[id] !== 'anonymous') {
                        this.setState({ linkText: 'Log Out [' + responseJSON[id] + ']' })
                        this.setState({ isLoggedIn: true })
                    }
                })
        }

        return (
            <React.Fragment>
                <Link to={this.state.isLoggedIn ? '' : '/login'}
                    onClick={this.conditionalRedirect}>
                    {this.state.linkText}
                </Link>
            </React.Fragment>
        );
    }

    private conditionalRedirect = () => {
        if (this.state.linkText.includes("Log Out")) {
            fetch('/system/sling/logout')
                .then(response => window.location.href = "/pantheon")
        }
    }
}

export { User }