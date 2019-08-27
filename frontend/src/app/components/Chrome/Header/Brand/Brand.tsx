import React, { Component } from 'react';
import { Link } from "react-router-dom";
import logo from '../../../../images/Pantheon2-logo.png';

class Brand extends Component {
    public state = {
        isLoggedIn: false,
        linkText: 'Pantheon'
    };

    public render() {
        const logo1 = require('../../../../images/Pantheon2-logo-white.png');
        return (
            <React.Fragment>
                <Link to={'/search'}>
                <div className="logo"><img src={logo1} alt={this.state.linkText} width="220" /></div>
                </Link>
            </React.Fragment>
        );
    }
}

export { Brand }