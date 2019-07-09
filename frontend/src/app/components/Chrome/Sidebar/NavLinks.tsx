import React, { Component } from 'react';
import { NavItem } from '@patternfly/react-core';
import { Link } from "react-router-dom";

class NavLinks extends Component {
    public state = {
        adminPage: 'Admin Panel',
        gitText: '',
        isAdmin: false,
        isLoggedIn: false,
        moduleText: '',
        searchText: 'Search'
    };

    public render() {
        const id = 'userID';
        if (!this.state.isLoggedIn) {
            fetch("/system/sling/info.sessionInfo.json")
                .then(response => response.json())
                .then(responseJSON => {
                    if (responseJSON[id] !== 'anonymous') {
                        console.log('response[id] in navlinks: '+responseJSON[id])
                        this.setState({ moduleText: 'New Module' })
                        this.setState({ gitText: 'Git Import' })
                        this.setState({ isLoggedIn: true })
                    }
                    if(responseJSON[id] === 'admin'){
                        this.setState({isAdmin: true})
                    }
                })
        }

        return (
            <React.Fragment>
                <NavItem>
                    <Link to='/search'>
                        {this.state.searchText}
                    </Link>
                </NavItem>
                {(this.state.moduleText.length > 0) && (<NavItem>
                    <Link to='/module'>
                        {this.state.moduleText}
                    </Link>
                </NavItem>)}
                {(this.state.gitText.length > 0) && (<NavItem>
                    <Link to='/git'>
                        {this.state.gitText}
                    </Link>
                </NavItem>)}

                {(this.state.isLoggedIn) && (this.state.isAdmin) && (
                <NavItem>
                    <Link to='/admin'>
                        {this.state.adminPage}
                    </Link>
                </NavItem>)
                }
            </React.Fragment>
        );
    }
}

export { NavLinks }