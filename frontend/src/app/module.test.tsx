import React from 'react';
import { Module } from '@app/module';
import "isomorphic-fetch"

import { mount, shallow } from 'enzyme';
import { Bullseye, TextInput, FormGroup, Button } from '@patternfly/react-core';

describe('Login tests', () => {
  test('should render Login component', () => {
    const view = shallow(<Module />);
    expect(view).toMatchSnapshot();
  });

  it('should render a Bullseye layout', () => {
    const wrapper = mount(<Module />);
    const bullseyeLayout = wrapper.find(Bullseye);
    expect(bullseyeLayout.exists()).toBe(true)
  });


  it('should render a form group', () => {
    const wrapper = mount(<Module />);
    const formGroup = wrapper.find(FormGroup);
    expect(formGroup.exists()).toBe(true)
  });

  it('should render a text input', () => {
    const wrapper = mount(<Module />);
    const textInput = wrapper.find(TextInput);
    expect(textInput.exists()).toBe(true)
  });

  it('should render a Button', () => {
    const wrapper = mount(<Module />);
    const button = wrapper.find(Button);
    expect(button.exists()).toBe(true)
  });

  it('test click event', () => {
    const mockCallBack = jest.fn();
  
    const button = shallow((<Button onClick={mockCallBack}>Submit</Button>));
    button.find('button').simulate('click');
    expect(mockCallBack.mock.calls.length).toEqual(1);
  });
});
