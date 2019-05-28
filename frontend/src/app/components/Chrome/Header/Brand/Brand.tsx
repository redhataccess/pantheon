import React, { Component } from 'react';
import { Link } from "react-router-dom";

export default class Brand extends Component {
    public state = {
        isLoggedIn: false,
        linkText: 'Pantheon | Log In'
    };

    public render() {
        if (!this.state.isLoggedIn) {
            fetch("/system/sling/info.sessionInfo.json")
                .then(response => response.json())
                .then(responseJSON => {
                    if (responseJSON['userID'] !== 'anonymous') {
                        this.setState({ linkText: 'Pantheon | Log Out [' + responseJSON['userID'] + ']' })
                        this.setState({ isLoggedIn: true })
                    }
                })
        }

        return (
            <React.Fragment>
                <Link to={this.state.isLoggedIn ? '/logout' : '/login'}
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