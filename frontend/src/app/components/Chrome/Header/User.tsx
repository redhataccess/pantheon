import React, { Component } from 'react';
import { Link } from "react-router-dom";

class User extends Component<any, any> {
    constructor(props) {
        super(props);
        this.state = {
            isLoggedIn: false,
            linkText: 'Log In'
        };
    }

    public componentDidMount() {

        if (!this.state.isLoggedIn) {
            fetch("/system/sling/info.sessionInfo.json")
                .then(response => response.json())
                .then(responseJSON => {
                    if (responseJSON.userID !== 'anonymous') {
                        this.setState({ linkText: 'Log Out [' + responseJSON.userID + ']' })
                        this.setState({ isLoggedIn: true })
                    }
                })
        }
    }

    public render() {
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

export { User }