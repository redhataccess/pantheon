import React from 'react';
import { ProductDetails }  from '@app/productDetails';
import "isomorphic-fetch"

import { mount, shallow } from 'enzyme';
import { Button, FormGroup, TextInput } from '@patternfly/react-core';

const props = {
  productName: "Red Hat Enterprise Linux"
}

describe('ProductDetails tests', () => {
  test('should render ProductDetails component', () => {
    const view = shallow(<ProductDetails productName="Red Hat Enterprise Linux"/>);
    expect(view).toMatchSnapshot();
  });

  it('should render a form group', () => {
    const wrapper = mount(<ProductDetails productName="Red Hat Enterprise Linux"/>);
    const formGroup = wrapper.find(FormGroup);
    expect(formGroup.exists()).toBe(true)
  });

  it('should render a text input', () => {
    const wrapper = mount(<ProductDetails productName="Red Hat Enterprise Linux"/>);
    const textInput = wrapper.find(TextInput);
    expect(textInput.exists()).toBe(true)
  });

  it('should render a Button', () => {
    const wrapper = mount(<ProductDetails productName="Red Hat Enterprise Linux"/>);
    const button = wrapper.find(Button);
    expect(button.exists()).toBe(true)
  });

  it('test props', () => {  
    
    const productDetails = mount(<ProductDetails {...props} />).text
    expect(productDetails.length === 1)
  });
});
