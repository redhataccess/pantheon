import React, { Component } from 'react';
import { Link } from "react-router-dom";

class NavLinks extends Component {
    public state = {
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
                        this.setState({ isLoggedIn: true })
                    }
                })
        }

        return (
            <React.Fragment>
                <li>
                    <Link to='/search'>
                        {this.state.searchText}
                    </Link>
                </li>
                {(this.state.moduleText.length > 0) && (<li>
                    <Link to='/module'>
                        {this.state.moduleText}
                    </Link>
                </li>)}
            </React.Fragment>
        );
    }
}

export { NavLinks }