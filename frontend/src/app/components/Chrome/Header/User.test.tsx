import React from 'react';
import { User } from './User';
import "isomorphic-fetch"
import { Link } from "react-router-dom";
import { HashRouter as Router } from 'react-router-dom';
import { shallow, mount } from 'enzyme';
import renderer from 'react-test-renderer';
import { mockStateUser } from '@app/TestResources'

describe('User tests', () => {
  test('should render User component', () => {
    const view = shallow(<User {...mockStateUser} />);
    expect(view).toMatchSnapshot();
  });

  it('should render a Link component', () => {
    const wrapper = mount(<Router><User {...mockStateUser} /></Router>);
    const navLinks = wrapper.find(Link);
    expect(navLinks.exists()).toBe(true)
  });

  it('test render function', () => {
    const wrapper = renderer.create(<Router><User {...mockStateUser} /></Router>);
    const inst = wrapper.getInstance();
    expect(inst.render).toMatchSnapshot();
  });

  it('test conditionalRedirect function', () => {
    const wrapper = renderer.create(<Router><User {...mockStateUser} /></Router>);
    const inst = wrapper.getInstance();
    expect(inst.conditionalRedirect).toMatchSnapshot();
  });

  it('test componentDidMount function', () => {
    const wrapper = renderer.create(<Router><User {...mockStateUser} /></Router>);
    const inst = wrapper.getInstance();
    expect(inst.componentDidMount).toMatchSnapshot();
  });
});
