import React, { Component } from 'react';
import { Link } from "react-router-dom";

class Brand extends Component {
    public state = {
        isLoggedIn: false,
        linkText: 'Pantheon'
    };

    public render() {
        return (
            <React.Fragment>
                <Link to={'/search'}>
                    {this.state.linkText}
                </Link>
            </React.Fragment>
        );
    }
}

export { Brand }