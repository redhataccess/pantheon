import React, { Component } from 'react';
import { NavItem, NavExpandable, NavList } from '@patternfly/react-core';
import { Link } from "react-router-dom";
import fetch from 'isomorphic-fetch';

// BASE is used in the fetch call to check if isLoggedIn or isAdmin. It currently breaks the Navlinks.
// only search is displayed when BASE is consumed.
// const BASE = process.env.BROWSER? '': `http://localhost`;
class NavLinks extends Component {

  public state = {
    activeGroup: 'grp-1',
    activeItem: 'grp-1_itm-1',
    gotUserInfo: false,
    isAdmin: false,
    isDropdownOpen: false,
    isKebabDropdownOpen: false,
    isLoggedIn: false,
  }

  public render() {
    const browserText = 'Content Browser'
    const consoleText = 'Web Console'
    const gitText = 'Git Import'
    const moduleText = 'New Module'
    const productText = 'New Product'
    const productsText = 'Product Listing'
    const searchText = 'Search'
    const slingHomeText = 'Sling Welcome'
    if (!this.state.isLoggedIn) {
      this.checkAuth();
    }
    return (
      <React.Fragment>
        <NavList>
          <NavExpandable title="Modules" groupId="grp-1" isActive={this.state.activeGroup === 'grp-1'} isExpanded={true} data-testid="navLink_modules">
            <NavItem groupId="grp-1" itemId="grp-1_itm-1" isActive={this.state.activeItem === 'grp-1_itm-1'}>
              <Link to='/search' data-testid="navLink_search">{searchText}</Link>
            </NavItem>
            {(this.state.isLoggedIn) && (<NavItem groupId="grp-1" itemId="grp-1_itm-2" isActive={this.state.activeItem === 'grp-1_itm-2'}>
              <Link to='/module'>{moduleText}</Link>
            </NavItem>)}
            {(this.state.isLoggedIn) && (<NavItem groupId="grp-1" itemId="grp-1_itm-3" isActive={this.state.activeItem === 'grp-1_itm-3'}>
              <Link to='/git'>{gitText}</Link>
            </NavItem>)}
          </NavExpandable>
          {(this.state.isLoggedIn) && (<NavExpandable title="Products" groupId="grp-2" isActive={this.state.activeGroup === 'grp-2'}>
            <NavItem groupId="grp-2" itemId="grp-2_itm-1" isActive={this.state.activeItem === 'grp-2_itm-1'}>
              <Link to='/products'>{productsText}</Link>
            </NavItem>
            {(productText.length > 0) && (<NavItem groupId="grp-2" itemId="grp-2_itm-2" isActive={this.state.activeItem === 'grp-2_itm-2'}>
              <Link to='/product'>{productText}</Link>
            </NavItem>)}
          </NavExpandable>)}
          {(this.state.isLoggedIn) && (this.state.isAdmin) && (<NavExpandable title="Admin Panel" groupId="grp-3" isActive={this.state.activeGroup === 'grp-3'}>
            <NavItem groupId="grp-3" itemId="grp-3_itm-1" isActive={this.state.activeItem === 'grp-3_itm-1'} onClick={this.welcomeLink()}>
              <Link to='/starter/index.html'>{slingHomeText}</Link>
            </NavItem>
            <NavItem groupId="grp-3" itemId="grp-3_itm-2" isActive={this.state.activeItem === 'grp-3_itm-2'} onClick={this.browserLink()}>
              <Link to='/bin/browser.html' >{browserText}</Link>
            </NavItem>
            <NavItem groupId="grp-3" itemId="grp-3_itm-3" isActive={this.state.activeItem === 'grp-3_itm-3'} onClick={this.consoleLink()}>
              <Link to='/system/console/bundles.html'>{consoleText}</Link>
            </NavItem>
          </NavExpandable>)}
        </NavList>
      </React.Fragment>
    );
  }

  private checkAuth = () => {
    const id = 'userID';
    // console.log("BASE: ", BASE)
    // fetch(BASE + "/system/sling/info.sessionInfo.json")
    fetch("/system/sling/info.sessionInfo.json")
      .then(response => response.json())
      .then(responseJSON => {
        this.setState({ gotUserInfo: true })
        if (responseJSON[id] !== 'anonymous') {
          this.setState({ isLoggedIn: true })
        }
        if (responseJSON[id] === 'admin') {
          this.setState({ isAdmin: true })
        }
      })
      .catch(() => { })
  };

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