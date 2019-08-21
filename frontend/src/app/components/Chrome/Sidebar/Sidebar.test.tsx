import React from 'react';
import { Sidebar }  from './Sidebar';
import { NavList } from '@patternfly/react-core';
import { NavLinks }  from './NavLinks';
import { mount, shallow } from 'enzyme';
import { HashRouter as Router } from 'react-router-dom';
import "isomorphic-fetch"

describe('Sidebar tests', () => {
  test('should render Sidebar component', () => {
    const view = shallow(<Sidebar 
      isNavOpen={true}/>);
    expect(view).toMatchSnapshot();
  });

  it('should render a NavList', () => {
    const wrapper = mount(<NavList/>);
    const navList = wrapper.find(NavList);
    expect(navList.exists()).toBe(true)
  });

  it('should render a NavLinks component', () => {
    const wrapper = mount(<Router><NavLinks/></Router>);
    const navLinks = wrapper.find(NavLinks);
    expect(navLinks.exists()).toBe(true)
  });
});
