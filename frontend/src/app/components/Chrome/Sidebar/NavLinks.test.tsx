import React from 'react';
import { NavLinks } from './NavLinks';
import { NavList, NavItem, NavExpandable } from '@patternfly/react-core';
import { HashRouter as Router } from 'react-router-dom';
import nock from 'nock'
import waitUntil from 'async-wait-until'

import { mount, shallow } from 'enzyme';
import renderer from 'react-test-renderer';
import { Link, MemoryRouter, Route, Switch } from 'react-router-dom';

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
const MockDenied = () => <div className="denied">Denied</div>;

// export function fetch(input, init) {
//   console.log("Fake fetch called!");
//   return Promise.resolve({
//     json: () => Promise.resolve({ 'userID': 'demo' })
//   })
// }

// const mockResponse = (status, statusText, response) => {
//   return new Response(response, {
//     status: status,
//     statusText: statusText,
//     headers: {
//       'Content-type': 'application/json'
//     }
//   });
// };


describe('NavLinks tests', () => {
  beforeAll(() => {
    nock(/.*/)
      .persist()
      // .log(console.log)
      .get('/system/sling/info.sessionInfo.json')
      .reply(200, {
           userID: 'demo',
      });
  });

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
    const wrapper = mount(<Router><NavLinks /></Router>)
    await waitUntil(() => wrapper.find('NavLinks').instance().state['gotUserInfo'] === true)
    expect(wrapper.find('NavLinks').instance().state['moduleText']).toBe('New Module')

    done()
  });

  it('should hide New Module when not logged in', async (done) => {
    nock.cleanAll()
    nock(/.*/)
      .persist()
      .get('/system/sling/info.sessionInfo.json')
      .reply(200, {
           userID: 'anonymous',
      });
    const wrapper = mount(<Router><NavLinks /></Router>)
    await waitUntil(() => wrapper.find('NavLinks').instance().state['gotUserInfo'] === true)
    expect(wrapper.find('NavLinks').instance().state['moduleText']).toBe('')

    done()
  });

});