import React from 'react';
import { User }  from './User';
import "isomorphic-fetch"
import { Link } from "react-router-dom";
import { HashRouter as Router } from 'react-router-dom';
import { shallow, mount } from 'enzyme';

describe('User tests', () => {
  test('should render User component', () => {
    const view = shallow(<User />);
    expect(view).toMatchSnapshot();
  });

  it('should render a Link component', () => {
    const wrapper = mount(<Router><User/></Router>);
    const navLinks = wrapper.find(Link);
    expect(navLinks.exists()).toBe(true)
  });

});
