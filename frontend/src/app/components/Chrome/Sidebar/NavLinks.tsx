import React, { Component } from 'react';
import { NavItem } from '@patternfly/react-core';
import { Link } from "react-router-dom";

class NavLinks extends Component {
    public state = {
        gitText: '',
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
                        this.setState({ moduleText: 'New Module' })
                        this.setState({ gitText: 'Git Import' })
                        this.setState({ isLoggedIn: true })
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

            </React.Fragment>
        );
    }
}

export { NavLinks }