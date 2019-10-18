import React from 'react';
import { Sidebar }  from './Sidebar';
import { NavList } from '@patternfly/react-core';
import { NavLinks }  from './NavLinks';
import { BuildInfo }  from '../Header/BuildInfo';
import { mount, shallow } from 'enzyme';
import { HashRouter as Router } from 'react-router-dom';
import "isomorphic-fetch"
import { mockStateUser } from '@app/TestResources'

describe('Sidebar tests', () => {
  test('should render Sidebar component', () => {
    const view = shallow(<Sidebar isNavOpen={true} appState={mockStateUser} />);
    expect(view).toMatchSnapshot();
  });

  it('should render a NavList', () => {
    const wrapper = mount(<Router><Sidebar isNavOpen={true} appState={mockStateUser} /></Router>);
    const navList = wrapper.find(NavList);
    expect(navList.exists()).toBe(true)
  });

  it('should render a NavLinks component', () => {
    const wrapper = mount(<Router><Sidebar isNavOpen={true} appState={mockStateUser} /></Router>);
    const navLinks = wrapper.find(NavLinks);
    expect(navLinks.exists()).toBe(true)
  });

});
