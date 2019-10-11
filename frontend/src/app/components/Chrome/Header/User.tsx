import React, { Component } from 'react'
import { Link } from "react-router-dom"
import { IAppState } from '@app/app'

class User extends Component<IAppState, any> {
    public render() {
        return (
            <React.Fragment>
                <Link to={this.props.userAuthenticated ? '' : '/login'}
                    onClick={this.conditionalRedirect}>
                    {this.props.userAuthenticated ? 'Log Out [' + this.props.username + ']' : 'Log In'}
                </Link>
            </React.Fragment>
        );
    }

    private conditionalRedirect = () => {
        if (this.props.userAuthenticated) {
            fetch('/system/sling/logout')
                .then(response => window.location.href = "/pantheon")
        }
    }
}

export { User }