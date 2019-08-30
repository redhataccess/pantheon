import React, { Component } from 'react';
import { NavItem, NavExpandable, NavList } from '@patternfly/react-core';
import { Link } from "react-router-dom";
import fetch from 'isomorphic-fetch';

const BASE = process.env.BROWSER? '': `http://localhost`;

class NavLinks extends Component {

  public state = {
    activeGroup: 'grp-1',
    activeItem: 'grp-1_itm-1',
    adminPage: 'Admin Panel',
    browserText: '',
    consoleText: '',
    gitText: '',
    gotUserInfo: false,
    isAdmin: false,
    isDropdownOpen: false,
    isKebabDropdownOpen: false,
    isLoggedIn: false,
    moduleText: '',
    productText: '',
    productsText: '',
    searchText: 'Search',
    slingHomeText: ''
  }

  public render() {
    const id = 'userID';
    if (!this.state.isLoggedIn) {
      fetch(BASE + "/system/sling/info.sessionInfo.json")
        .then(response => response.json())
        .then(responseJSON => {
          this.setState({ gotUserInfo: true })
          if (responseJSON[id] !== 'anonymous') {

            this.setState({ moduleText: 'New Module' })
            this.setState({ productText: 'New Product' })
            this.setState({ productsText: 'All Products' })
            this.setState({ gitText: 'Git Import' })
            this.setState({ browserText: 'Content Browser' })
            this.setState({ slingHomeText: 'Sling Welcome' })
            this.setState({ consoleText: 'Web Console' })
            this.setState({ isLoggedIn: true })
          }
          if (responseJSON[id] === 'admin') {
            this.setState({ isAdmin: true })
          }
        })
        .catch(() => {})
    }
    return (
      <React.Fragment>
        <NavList>
          <NavExpandable title="Modules" groupId="grp-1" isActive={this.state.activeGroup === 'grp-1'} isExpanded={true} data-testid="navLink_modules">
            <NavItem groupId="grp-1" itemId="grp-1_itm-1" isActive={this.state.activeItem === 'grp-1_itm-1'}>
              <Link to='/search' data-testid="navLink_search">{this.state.searchText}</Link>
            </NavItem>
            {(this.state.moduleText.length > 0) && (<NavItem groupId="grp-1" itemId="grp-1_itm-2" isActive={this.state.activeItem === 'grp-1_itm-2'}>
              <Link to='/module'>{this.state.moduleText}</Link>
            </NavItem>)}
            {(this.state.gitText.length > 0) && (<NavItem groupId="grp-1" itemId="grp-1_itm-3" isActive={this.state.activeItem === 'grp-1_itm-3'}>
              <Link to='/git'>{this.state.gitText}</Link>
            </NavItem>)}
          </NavExpandable>
          {(this.state.isLoggedIn) && (<NavExpandable title="Products" groupId="grp-2" isActive={this.state.activeGroup === 'grp-2'}>
            <NavItem groupId="grp-2" itemId="grp-2_itm-1" isActive={this.state.activeItem === 'grp-2_itm-1'}>
              <Link to='/products'>{this.state.productsText}</Link>
            </NavItem>
            {(this.state.gitText.length > 0) && (<NavItem groupId="grp-2" itemId="grp-2_itm-2" isActive={this.state.activeItem === 'grp-2_itm-2'}>
              <Link to='/product'>{this.state.productText}</Link>
            </NavItem>)}
          </NavExpandable>)}
          {(this.state.isLoggedIn) && (this.state.isAdmin) && (<NavExpandable title="Admin Panel" groupId="grp-3" isActive={this.state.activeGroup === 'grp-3'}>
            <NavItem groupId="grp-3" itemId="grp-3_itm-1" isActive={this.state.activeItem === 'grp-3_itm-1'} onClick={this.welcomeLink()}>
              <Link to='/starter/index.html'>{this.state.slingHomeText}</Link>
            </NavItem>
            <NavItem groupId="grp-3" itemId="grp-3_itm-2" isActive={this.state.activeItem === 'grp-3_itm-2'} onClick={this.browserLink()}>
              <Link to='/bin/browser.html' >{this.state.browserText}</Link>
            </NavItem>
            <NavItem groupId="grp-3" itemId="grp-3_itm-3" isActive={this.state.activeItem === 'grp-3_itm-3'} onClick={this.consoleLink()}>
              <Link to='/system/console/bundles.html'>{this.state.consoleText}</Link>
            </NavItem>
          </NavExpandable>)}
        </NavList>
      </React.Fragment>
    );
  }

  private browserLink = () => (event: any) => {
    return window.open("/bin/browser.html");
  };

  private welcomeLink = () => (event: any) => {
    return window.open("/starter/index.html");
  };

  private consoleLink = () => (event: any) => {
    return window.open("/system/console/bundles.html");
  };
}

export { NavLinks }