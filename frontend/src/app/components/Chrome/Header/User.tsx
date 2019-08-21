import React, { Component } from 'react';
import { Link } from "react-router-dom";

class User extends Component {
    public state = {
        buildDate: '',
        commitHash: '',
        commitText: '',
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
                        if (responseJSON[id] === 'admin') {
                            this.getBuildInfo()
                        }
                    }
                })
        }

        return (
            <React.Fragment>
                <div className="column-view">
                    <Link to={this.state.isLoggedIn ? '/logout' : '/login'}
                        onClick={this.conditionalRedirect}>
                        {this.state.linkText}
                    </Link>
                    <Link to=""
                        onClick={this.commitRedirect}>
                        {this.state.commitText}
                    </Link>
                    <div>{this.state.buildDate}</div>
                </div>
            </React.Fragment>
        );
    }

    private conditionalRedirect = () => {
        if (this.state.linkText.includes("Log Out")) {
            fetch('/system/sling/logout')
                .then(response => window.location.href = "/pantheon")
        }
    }

    private getBuildInfo() {
        const backend = "/pantheon/builddate.json"
        fetch(backend)
            .then(response => response.json())
            .then(responseJSON => this.setState({ buildDate: "Build Date: " + responseJSON.buildDate, commitHash: responseJSON.commitHash }))
            .then(() => {
                if (!(this.state.commitHash.includes("not set"))) {
                    this.setState({ commitText: 'commit hash' })
                }
            })

    };

    private commitRedirect = () => {
        window.location.href = "https://github.com/redhataccess/pantheon/commit/" + this.state.commitHash
    }

}

export { User }