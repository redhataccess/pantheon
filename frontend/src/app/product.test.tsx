import React from 'react';
import { Product } from '@app/product';
import "isomorphic-fetch"

import { mount, shallow } from 'enzyme';
import { Bullseye, TextInput, FormGroup, Button } from '@patternfly/react-core';

describe('Product tests', () => {
  test('should render Product component', () => {
    const view = shallow(<Product />);
    expect(view).toMatchSnapshot();
  });

  it('should render a Bullseye layout', () => {
    const wrapper = mount(<Product />);
    const bullseyeLayout = wrapper.find(Bullseye);
    expect(bullseyeLayout.exists()).toBe(true)
  });


  it('should render a form group', () => {
    const wrapper = mount(<Product />);
    const formGroup = wrapper.find(FormGroup);
    expect(formGroup.exists()).toBe(true)
  });

  it('should render a text input', () => {
    const wrapper = mount(<Product />);
    const textInput = wrapper.find(TextInput);
    expect(textInput.exists()).toBe(true)
  });

  it('should render a Button', () => {
    const wrapper = mount(<Product />);
    const button = wrapper.find(Button);
    expect(button.exists()).toBe(true)
  });

});
