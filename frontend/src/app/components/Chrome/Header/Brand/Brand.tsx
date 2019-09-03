import React, { Component } from 'react';
import { Link } from "react-router-dom";
import logo from '../../../../images/Pantheon2-logo-white.png';

class Brand extends Component {
    public state = {
        isLoggedIn: false,
        linkText: 'Pantheon',
        logoWidth: 220
    };

    public render() {

        return (
            <React.Fragment>
                <Link to={'/search'}>
                <div className="logo"><img src={logo} alt={this.state.linkText} width={this.state.logoWidth} /></div>
                </Link>
            </React.Fragment>
        );
    }
}

export { Brand }