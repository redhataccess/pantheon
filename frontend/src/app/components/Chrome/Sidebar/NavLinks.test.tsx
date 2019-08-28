import React from 'react';
import { NavLinks }  from './NavLinks';
import { NavItem } from '@patternfly/react-core';
import { Link } from "react-router-dom";
import { HashRouter as Router } from 'react-router-dom';
import "isomorphic-fetch"

import { mount, shallow } from 'enzyme';

describe('NavLinks tests', () => {
  test('should render NavLinks component', () => {
    const view = shallow(<NavLinks />);
    expect(view).toMatchSnapshot();
  });

  it('should render a NavList', () => {
    const wrapper = mount(<Router><NavLinks/></Router>);
    const navList = wrapper.find(NavItem);
    expect(navList.exists()).toBe(true)
  });

  it('should render a Link component', () => {
    const wrapper = mount(<Router><NavLinks/></Router>);
    const navLinks = wrapper.find(Link);
    expect(navLinks.exists()).toBe(true)
  });

});