import React from 'react';
import { NavLinks } from './NavLinks';
import { NavList, NavItem, NavExpandable } from '@patternfly/react-core';
import { HashRouter as Router } from 'react-router-dom';
import nock from 'nock'
import waitUntil from 'async-wait-until'

import { mount, shallow } from 'enzyme';
import { Link, MemoryRouter, Route, Switch } from 'react-router-dom';
import renderer from 'react-test-renderer';

const Home = () => <div>Pantheon</div>;
const MockComp = () => (
  <div className="test">
    <NavList>
      <NavExpandable title="Modules" isExpanded={true}>
        <NavItem groupId="grp-1" itemId="grp-1_itm-1" isActive={true}>
          <Link to='/search' data-testid="navLink_search">Search</Link>
        </NavItem>
        <NavItem groupId="grp-1" itemId="grp-1_itm-2" isActive={false}>
          <Link to='/module' data-testid="navLink_module_protected">New Module</Link>
        </NavItem>
      </NavExpandable>
    </NavList>
  </div>
);

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

  it("contains correct passed prop", () => {
    const comp = (
      <Link to="/search">
        Search
        </Link>
    );
    const wrapper = shallow(comp);
    // Received string: Search
    expect(wrapper.instance().props.children).toHaveLength(6)
  });

  test('Clicking link will render component associated with path', () => {
    const wrapper = mount(
      <MemoryRouter>
        <div>
          <Link to="/pantheon" />
          <Switch>
            <Route path="/search" component={MockComp} />
            <Route path="/" component={Home} />
          </Switch>
        </div>
      </MemoryRouter>
    );
    wrapper.find('a').simulate('click', { button: 0 });
    expect(wrapper.find('.test')).toBeTruthy();
    expect(wrapper.find('groupId').getElements()).toBeDefined();
  });

  it('should show New Module when logged in', async (done) => {
    nock(/.*/, { allowUnmocked: true })
      // .persist()
      // .log(console.log)
      .get('/system/sling/info.sessionInfo.json')
      .reply(200, {
           userID: 'demo',
      });
    const wrapper = mount(<Router><NavLinks /></Router>)
    await waitUntil(() => wrapper.find('NavLinks').instance().state['gotUserInfo'] === true)
    expect(wrapper.find('NavLinks').instance().state['moduleText']).toBe('New Module')

    done()
  });

  it('should hide New Module when not logged in', async (done) => {
    nock(/.*/, { allowUnmocked: true })
      // .persist()
      // .log(console.log)
      .get('/system/sling/info.sessionInfo.json')
      .reply(200, {
           userID: 'anonymous',
      });
    const wrapper = mount(<Router><NavLinks /></Router>)
    await waitUntil(() => wrapper.find('NavLinks').instance().state['gotUserInfo'] === true)
    expect(wrapper.find('NavLinks').instance().state['moduleText']).toBe('')

    done()
  });

  it('should contain 1 NavItem without authentication', () => {
    const renderedComponent = shallow(<NavLinks />);

    const items = renderedComponent.find(NavItem);
    expect(items).toHaveLength(1);
  });


  it('should be possible to toggle a LinkItem', () => {
    const wrapper = mount(<Router><NavLinks /></Router>)
    const expandables = wrapper.find('.pf-c-nav__link')

    expect(expandables).toHaveLength(3)
    wrapper.unmount();
  });

  it('should handle state changes for isLoggedIn', () => {
    const wrapper = shallow(<NavLinks />)
    const instance = wrapper.instance();

    expect(wrapper.state('isLoggedIn')).toBe(false);
    wrapper.setState({ 'isLoggedIn': true })
    expect(wrapper.state('isLoggedIn')).toBe(true);

    wrapper.setState({ 'moduleText': 'New Module' });
    wrapper.setState({ 'gitText': 'Git Import' });
    const navGroup1 = wrapper.find('[groupId="grp-1"]');
    expect(navGroup1.length).toBe(4)

    const navGroup2 = wrapper.find('[groupId="grp-2"]');
    expect(navGroup2.length).toBe(3)

    wrapper.setState({ 'isAdmin': true })
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

});