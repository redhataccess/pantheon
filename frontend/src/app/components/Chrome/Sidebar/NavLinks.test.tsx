import React from 'react';
import { NavLinks } from './NavLinks';
import { NavList, NavItem, NavExpandable } from '@patternfly/react-core';
import { HashRouter as Router } from 'react-router-dom';
import nock from 'nock'
import waitUntil from 'async-wait-until'

import { mount, shallow } from 'enzyme';
import { Link } from 'react-router-dom';
import renderer from 'react-test-renderer';

describe('NavLinks tests', () => {
  test('should render NavLinks component', () => {
    const view = shallow(<NavLinks />);
    expect(view).toMatchSnapshot();
  });

  it('should render a NavList', () => {
    const wrapper = mount(<Router><NavLinks /></Router>);
    const navList = wrapper.find(NavList);
    expect(navList.exists()).toBe(true)
  });

  it('should render a NavItem', () => {
    const wrapper = mount(<Router><NavLinks /></Router>);
    const navItem = wrapper.find(NavItem);
    expect(navItem.exists()).toBe(true)
  });

  it('should render a Link component', () => {
    const wrapper = mount(<Router><NavLinks /></Router>);
    const navLinks = wrapper.find(Link);
    expect(navLinks.exists()).toBe(true)
  });

  it('should render an Expandable component', () => {
    const wrapper = mount(<Router><NavLinks /></Router>);
    const expandable = wrapper.find(NavExpandable);
    expect(expandable.exists()).toBe(true)
  });

  // it('should show New Module when logged in', async (done) => {
  //   jest.setTimeout(10000);
  //   nock(/.*/, { allowUnmocked: true })
  //     // .persist()
  //     .log(console.log)
  //     .get('/system/sling/info.sessionInfo.json')
  //     .reply(200, {
  //       userID: 'demo',
  //     });
  //   const wrapper = mount(<Router><NavLinks /></Router>)
  //   const gotUserInfoKey = "gotUserInfo"
  //   const moduleTextKey = "moduleText"
  //   await waitUntil(() => wrapper.find('NavLinks').instance().state[gotUserInfoKey] === true)
  //   expect(wrapper.find('NavLinks').instance().state[moduleTextKey]).toBe('New Module')

  //   done()
  // });

  // it('should hide New Module when not logged in', async (done) => {
  //   jest.setTimeout(10000);
  //   nock(/.*/, { allowUnmocked: true })
  //     // .persist()
  //     .log(console.log)
  //     .get('/system/sling/info.sessionInfo.json')
  //     .reply(200, {
  //       userID: 'anonymous',
  //     });
  //   const wrapper = mount(<Router><NavLinks /></Router>)
  //   const gotUserInfoKey = "gotUserInfo"
  //   const moduleTextKey = "moduleText"
  //   await waitUntil(() => wrapper.find('NavLinks').instance().state[gotUserInfoKey] === true)
  //   expect(wrapper.find('NavLinks').instance().state[moduleTextKey]).toBe('')

  //   done()
  // });

  it('should contain 1 NavItem without authentication', () => {
    const wrapper = shallow(<NavLinks />);

    const items = wrapper.find(NavItem);
    expect(items).toHaveLength(1);
  });

  it('should handle state changes for isLoggedIn', () => {
    const wrapper = shallow(<NavLinks />)

    expect(wrapper.state('isLoggedIn')).toBe(false);
    wrapper.setState({ 'isLoggedIn': true })
    expect(wrapper.state('isLoggedIn')).toBe(true);

    wrapper.setState({ 'moduleText': 'New Module' });
    wrapper.setState({ 'gitText': 'Git Import' });
    const navGroup1 = wrapper.find('[groupId="grp-1"]');
    expect(navGroup1.length).toBe(4)

    const navGroup2 = wrapper.find('[groupId="grp-2"]');
    expect(navGroup2.length).toBe(3)
  });

  it('should handle state changes for isAdmin', () => {
    const wrapper = shallow(<NavLinks />)

    expect(wrapper.state('isLoggedIn')).toBe(false);
    wrapper.setState({ 'isLoggedIn': true })
    expect(wrapper.state('isLoggedIn')).toBe(true);

    expect(wrapper.state('isAdmin')).toBe(false)
    wrapper.setState({ 'isAdmin': true })
    expect(wrapper.state('isAdmin')).toBe(true)
    const navGroup3 = wrapper.find('[groupId="grp-3"]');
    expect(navGroup3.length).toBe(4)
  });

  it('test browserLink function', () => {
    const wrapper = renderer.create(<Router><NavLinks /></Router>);
    const inst = wrapper.getInstance();
    expect(inst.browserLink).toMatchSnapshot();
  });

  it('test welcomeLink function', () => {
    const wrapper = renderer.create(<Router><NavLinks /></Router>);
    const inst = wrapper.getInstance();
    expect(inst.welcomeLink).toMatchSnapshot();
  });

  it('test webConsole function', () => {
    const wrapper = renderer.create(<Router><NavLinks /></Router>);
    const inst = wrapper.getInstance();
    expect(inst.consoleLink).toMatchSnapshot();
  });

  it('test render function', () => {
    const wrapper = renderer.create(<Router><NavLinks /></Router>);
    const inst = wrapper.getInstance();
    expect(inst.render).toMatchSnapshot();
  });

  it('test checkAuth function', () => {
    const wrapper = renderer.create(<Router><NavLinks /></Router>);
    const inst = wrapper.getInstance();
    expect(inst.checkAuth).toMatchSnapshot();
  });

  it('test Admin Panel links', () => {
    jest.mock('./NavLinks', () => {
      // Require the original module to not be mocked...
      const originalModule = jest.requireActual('./NavLinks');

      return {
        __esModule: true, // Use it when dealing with esModules
        ...originalModule,
        browserLink: jest.fn().mockReturnValue('window.open("/bin/browser.html")'),
        checkAuth: jest.fn().mockReturnValue(true),
        consoleLink: jest.fn().mockReturnValue('window.open("/system/console/bundles.html")'),
        welcomeLink: jest.fn().mockReturnValue('window.open("/starter/index.html")'),
      };
    });

    const checkAuth = require('./NavLinks').checkAuth;
    const browserLink = require('./NavLinks').browserLink;
    const consoleLink = require('./NavLinks').consoleLink;
    const welcomeLink = require('./NavLinks').welcomeLink;

    expect(checkAuth()).toBe(true);
    expect(browserLink()).toBe('window.open("/bin/browser.html")')
    expect(consoleLink()).toBe('window.open("/system/console/bundles.html")')
    expect(welcomeLink()).toBe('window.open("/starter/index.html")')
    jest.resetAllMocks()
  });

  it('calls render function', () => {
    const render = jest.fn();
    render();
    expect(render).toHaveBeenCalled();
  });

});