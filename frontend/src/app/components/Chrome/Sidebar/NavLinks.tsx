import React, { Component } from 'react';
import { NavItem, NavExpandable, NavList, Nav, PageSidebar, Page } from '@patternfly/react-core';
import { Link } from "react-router-dom";

class NavLinks extends Component {
    public state = {
        adminPage: 'Admin Panel',
        gitText: '',
        isAdmin: false,
        isLoggedIn: false,
        moduleText: '',
        productText: '',
        productsText: '',
        browserText: '',
        consoleText: '',
        slingHomeText: '',
        searchText: 'Search',
        isDropdownOpen: false,
        isKebabDropdownOpen: false,
        activeGroup: 'grp-1',
        activeItem: 'grp-1_itm-1'
      };
    
    onDropdownToggle = isDropdownOpen => {
        this.setState({
          isDropdownOpen
        });
      };
    
      onDropdownSelect = event => {
        this.setState({
          isDropdownOpen: !this.state.isDropdownOpen
        });
      };
    
      onKebabDropdownToggle = isKebabDropdownOpen => {
        this.setState({
          isKebabDropdownOpen
        });
      };
    
      onKebabDropdownSelect = event => {
        this.setState({
          isKebabDropdownOpen: !this.state.isKebabDropdownOpen
        });
      };
    
      onNavSelect = result => {
        this.setState({
          activeItem: result.itemId,
          activeGroup: result.groupId
        });
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
                        this.setState({ productText: 'New Product' })
                        this.setState({ productsText: 'All Products' })
                        this.setState({ gitText: 'Git Import' })
                        this.setState({ browserText: 'Content Browser' })
                        this.setState({ slingHomeText: 'Sling Welcome' })
                        this.setState({ consoleText: 'Web Console' })
                        this.setState({ isLoggedIn: true })
                    }
                    if(responseJSON[id] === 'admin'){
                        this.setState({isAdmin: true})
                    }
                })
        }
        return (
            <React.Fragment>  
              <NavList>
                <NavExpandable title="Modules" groupId="grp-1" isActive={this.state.activeGroup === 'grp-1'} isExpanded>
                  <NavItem groupId="grp-1" itemId="grp-1_itm-1" isActive={this.state.activeItem === 'grp-1_itm-1'}>
                    <Link to='/search'>{this.state.searchText}</Link>
                  </NavItem>
                  {(this.state.moduleText.length > 0) && (<NavItem groupId="grp-1" itemId="grp-1_itm-2" isActive={this.state.activeItem === 'grp-1_itm-2'}>
                    <Link to='/module'>{this.state.moduleText}</Link>
                  </NavItem>)}
                  {(this.state.gitText.length > 0) && (<NavItem groupId="grp-1" itemId="grp-1_itm-3" isActive={this.state.activeItem === 'grp-1_itm-3'}>
                    <Link to='/git'>{this.state.gitText}</Link>
                  </NavItem> )}
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
    private browserLink = () => (event: any) =>  {
      return window.open("/bin/browser.html");
    };

    private welcomeLink = () => (event: any) =>  {
      return window.open("/starter/index.html");
    };

    private consoleLink = () => (event: any) =>  {
      return window.open("/system/console/bundles.html");
    };
}

export { NavLinks }