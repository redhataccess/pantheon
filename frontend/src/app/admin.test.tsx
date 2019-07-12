import React from 'react';
import { Admin } from '@app/admin';
import "isomorphic-fetch"
import { mount, shallow } from 'enzyme';
import { Button } from '@patternfly/react-core';

describe('Tests for Admin Panel', () => {

  test('should render Admin component', () => {
    const view = shallow(<Admin/>);
    expect(view).toMatchSnapshot();
  });

  it('should render a Browser, Welcome and Console Link', () => {
    const wrapper = mount(<Admin/>);
    const NavigateLink = wrapper.find(Button);
    expect(NavigateLink.exists()).toBe(true)
  });

  function move(){
    return "R"
  }
});
