import React from 'react';
import { ProductDetails }  from '@app/productDetails';
import "isomorphic-fetch"

import { mount, shallow } from 'enzyme';
import { Breadcrumb, Button, Form, FormGroup, Level, LevelItem, TextContent, TextInput, Text } from '@patternfly/react-core';

const props = {
  productName: "Red Hat Enterprise Linux"
}

describe('ProductDetails tests', () => {
  test('should render ProductDetails component', () => {
    const view = shallow(<ProductDetails productName="Red Hat Enterprise Linux"/>);
    expect(view).toMatchSnapshot();
  });

  it('should render a form', () => {
    const wrapper = mount(<ProductDetails productName="Red Hat Enterprise Linux"/>);
    const form = wrapper.find(Form);
    expect(form.exists()).toBe(true)
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

  it('should render a breadcrumb', () => {
    const wrapper = mount(<ProductDetails productName="Red Hat Enterprise Linux"/>);
    const breadCrumb = wrapper.find(Breadcrumb);
    expect(breadCrumb.exists()).toBe(true)
  });

  it('should render a Level element', () => {
    const wrapper = mount(<ProductDetails productName="Red Hat Enterprise Linux"/>);
    const level = wrapper.find(Level);
    expect(level.exists()).toBe(true)
  });

  it('should render a LevelItem', () => {
    const wrapper = mount(<ProductDetails productName="Red Hat Enterprise Linux"/>);
    const levelItem = wrapper.find(LevelItem);
    expect(levelItem.exists()).toBe(true)
  });

  it('should render a TextContent element', () => {
    const wrapper = mount(<ProductDetails productName="Red Hat Enterprise Linux"/>);
    const textContent = wrapper.find(TextContent);
    expect(textContent.exists()).toBe(true)
  });

  it('should render a Text element', () => {
    const wrapper = mount(<ProductDetails productName="Red Hat Enterprise Linux"/>);
    const text = wrapper.find(Text);
    expect(text.exists()).toBe(true)
  });

  it('test props', () => {  
    
    const productDetails = mount(<ProductDetails {...props} />).text
    expect(productDetails.length === 1)
  });
});
