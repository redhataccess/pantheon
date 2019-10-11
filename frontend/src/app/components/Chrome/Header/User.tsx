import React, { Component } from 'react'
import { Link } from "react-router-dom"
import { IAppState } from '@app/app'

class User extends Component<IAppState, any> {
    constructor(props) {
        super(props)
        this.state = {
            linkText: 'Log In'
        }
    }

    public componentDidMount() {
        console.log("User.tsx username: " + this.props.username)
        if (!this.props.userAuthenticated) {
            if (this.props.username !== 'anonymous') {
                this.setState({ linkText: 'Log Out [' + this.props.username + ']' })
            }
        }
    }

    public render() {
        return (
            <React.Fragment>
                <Link to={this.props.userAuthenticated ? '' : '/login'}
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