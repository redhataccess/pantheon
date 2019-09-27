import React, { Component } from 'react';
import { NavItem, NavExpandable, NavList } from '@patternfly/react-core';
import { Link } from "react-router-dom";
import fetch from 'isomorphic-fetch';

// BASE is used in the fetch call to check if isLoggedIn or isAdmin. It currently breaks the Navlinks.
// only search is displayed when BASE is consumed.
// const BASE = process.env.BROWSER? '': `http://localhost`;
class NavLinks extends Component<any, any> {

  constructor(props) {
    super(props)
    this.state = {
      activeGroup: '',
      activeItem: '',
      gotUserInfo: false,
      isAdmin: false,
      isDropdownOpen: false,
      isKebabDropdownOpen: false,
      isLoggedIn: false,
    }
  }

  public componentDidMount() {
    if (!this.state.isLoggedIn) {
      fetch("/system/sling/info.sessionInfo.json")
        .then(response => response.json())
        .then(responseJSON => {
          if (responseJSON.userID !== 'anonymous') {
            this.setState({ moduleText: 'New Module' })
            this.setState({ isLoggedIn: true })
          }
          if (responseJSON.userID === 'admin') {
            this.setState({ isAdmin: true })
          }
        })
    }
    this.setState({ activeGroup: 'grp-1', activeItem: 'grp-1_itm-1' });
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
            <NavItem groupId="grp-3" itemId="grp-3_itm-1" isActive={this.state.activeItem === 'grp-3_itm-1'} preventDefault={false} component='a'>
              <a href='/starter/index.html' target='_blank'>{slingHomeText}</a>
            </NavItem>
            <NavItem groupId="grp-3" itemId="grp-3_itm-2" isActive={this.state.activeItem === 'grp-3_itm-2'} preventDefault={false} component='a'>
              <a href='/bin/browser.html' target='_blank'>{browserText}</a>
            </NavItem>
            <NavItem groupId="grp-3" itemId="grp-3_itm-3" isActive={this.state.activeItem === 'grp-3_itm-3'} preventDefault={false} component='a'>
              <a href='/system/console/bundles.html' target='_blank'>{consoleText}</a>
            </NavItem>
          </NavExpandable>)}
        </NavList>
      </React.Fragment>
    );
  }
}

export { NavLinks }