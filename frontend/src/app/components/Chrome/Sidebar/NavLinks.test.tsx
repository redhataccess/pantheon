import React from 'react';
import { NavLinks } from './NavLinks';
import { NavList, NavItem, NavExpandable } from '@patternfly/react-core';
import { HashRouter as Router } from 'react-router-dom';
import "isomorphic-fetch"

import { mount, shallow } from 'enzyme';
import { Link, MemoryRouter, Route, Switch } from 'react-router-dom';
import { createMemoryHistory } from 'history'
import renderer from 'react-test-renderer';

// beforeAll(() => {
//   window.fetch = jest.fn();
// });

// let wrapper1;

// beforeEach(() => {
//   wrapper1 = shallow(<NavLinks />, { disableLifecycleMethods: true });
// });

// afterEach(() => {
//   wrapper1.unmount();
// });

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
// const MockDenied = () => <div className="denied">Denied</div>;
const onClickMode = jest.fn();
const history = createMemoryHistory()

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

  it('should contain 1 NavItem without authentication', () => {
    const renderedComponent = shallow(<NavLinks />);

    const items = renderedComponent.find(NavItem);
    expect(items).toHaveLength(1);
  });

  // it('should handle click events', () => {
  //   const wrapper = mount(<Router><NavLinks /></Router>);
  //   const links = wrapper.find('a');

  //   links.first().simulate('click', { a: 0 })
  //   expect(wrapper.find('.pf-c-nav__link pf-m-current')).toBeCalledTimes(1);
  // });

  // it('fetches data from server when server returns a successful response', done => { // 1
    // const mockSuccessResponse = {"userID":"demo"};
    // // const mockSuccessResponse = {};
    // const mockJsonPromise = Promise.resolve(mockSuccessResponse); // 2
    // const mockFetchPromise = Promise.resolve({ // 3
    //   json: () => mockJsonPromise,
    // });
    // // jest.spyOn(window, 'fetch').mockImplementation(() => Promise.resolve(Response)); // 4
    // jest.spyOn(window, 'fetch').mockImplementation(() => Promise.resolve({ok: true, UserID: 'demo'})); // 4
    // // window.fetch = jest.fn().mockImplementation(() => Promise.resolve({ok: true, Id: '123'}));
    

    // const wrapper = shallow(<NavLinks />); // 5
                          
    // // expect(global.fetch).toHaveBeenCalledTimes(1);
    // expect(window.fetch).toHaveBeenCalledTimes(1);
    // expect(window.fetch).toHaveBeenCalledWith('/system/sling/info.sessionInfo.json');
    
    // process.nextTick(() => { // 6
    //   expect(wrapper.state()).toEqual({
    //     // ... assert the set state
    //     isLoggedIn: true
    //   });

    //   window.fetch.mockClear(); // 7
      
    //   //delete global.fetch;
    //   done(); // 8
    // });
   // });

   it('should be possible to toggle a LinkItem', () => {
    const wrapper = mount(<Router><NavLinks /></Router>)
    const expandables = wrapper.find('.pf-c-nav__link')
    
    expect(expandables).toHaveLength(3)
    wrapper.unmount();
  });

  // test('it calls start Web Console on click', () => {
  //   const mockConsoleLink = jest.fn();
  //   const wrapper = shallow(<NavItem onClick={mockConsoleLink}/>);
  //   wrapper.find('.pf-c-nav__link').at(7).simulate('click');
  //   expect(mockConsoleLink).toHaveBeenCalled();
  // });

  
  // it('should handle state changes', () => {
  //   const wrapper = mount(<Router><NavLinks /></Router>)
  //   expect(wrapper.state('isLoggedIn')).toEqual(false);
  //   //wrapper.simulate('click');
  //   //expect(wrapper.state().clicked).toEqual(true);
  // });

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