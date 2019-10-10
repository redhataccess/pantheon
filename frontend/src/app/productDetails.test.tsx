import React from 'react';
import { ProductDetails } from '@app/productDetails';
import "isomorphic-fetch"

import { mount, shallow } from 'enzyme';
import { Breadcrumb, Button, Form, FormGroup, Level, LevelItem, TextContent, TextInput, Text } from '@patternfly/react-core';
import renderer from 'react-test-renderer';
import sinon from 'sinon'

const props = {
  productName: "Red Hat Enterprise Linux"
}

describe('ProductDetails tests', () => {
  test('should render ProductDetails component', () => {
    const view = shallow(<ProductDetails productName="Red Hat Enterprise Linux" />);
    expect(view).toMatchSnapshot();
  });

  it('should render a form', () => {
    const wrapper = mount(<ProductDetails productName="Red Hat Enterprise Linux" />);
    const form = wrapper.find(Form);
    expect(form.exists()).toBe(true)
  });

  it('should render a form group', () => {
    const wrapper = mount(<ProductDetails productName="Red Hat Enterprise Linux" />);
    const formGroup = wrapper.find(FormGroup);
    expect(formGroup.exists()).toBe(true)
  });

  it('should render a text input', () => {
    const wrapper = mount(<ProductDetails productName="Red Hat Enterprise Linux" />);
    const textInput = wrapper.find(TextInput);
    expect(textInput.exists()).toBe(true)
  });

  it('should render a Button', () => {
    const wrapper = mount(<ProductDetails productName="Red Hat Enterprise Linux" />);
    const button = wrapper.find(Button);
    expect(button.exists()).toBe(true)
  });

  it('should render a breadcrumb', () => {
    const wrapper = mount(<ProductDetails productName="Red Hat Enterprise Linux" />);
    const breadCrumb = wrapper.find(Breadcrumb);
    expect(breadCrumb.exists()).toBe(true)
  });

  it('should render a Level element', () => {
    const wrapper = mount(<ProductDetails productName="Red Hat Enterprise Linux" />);
    const level = wrapper.find(Level);
    expect(level.exists()).toBe(true)
  });

  it('should render a LevelItem', () => {
    const wrapper = mount(<ProductDetails productName="Red Hat Enterprise Linux" />);
    const levelItem = wrapper.find(LevelItem);
    expect(levelItem.exists()).toBe(true)
  });

  it('should render a TextContent element', () => {
    const wrapper = mount(<ProductDetails productName="Red Hat Enterprise Linux" />);
    const textContent = wrapper.find(TextContent);
    expect(textContent.exists()).toBe(true)
  });

  it('should render a Text element', () => {
    const wrapper = mount(<ProductDetails productName="Red Hat Enterprise Linux" />);
    const text = wrapper.find(Text);
    expect(text.exists()).toBe(true)
  });

  it('test props', () => {

    const productDetails = mount(<ProductDetails {...props} />).text
    expect(productDetails.length === 1)
  });

  it('test click event', () => {
    const mockCallBack = jest.fn();

    const button = shallow((<Button onClick={mockCallBack}>Submit</Button>));
    button.find('button').simulate('click');
    expect(mockCallBack.mock.calls.length).toEqual(1);
  });

  it('test fetchProductDetails function', () => {
    const wrapper = renderer.create(<ProductDetails {...props} />);
    const inst = wrapper.getInstance();
    expect(inst.fetchProductDetails([])).toMatchSnapshot();
  });

  it('test handleTextInputChange function', () => {
    const wrapper = renderer.create(<ProductDetails {...props} />);
    const inst = wrapper.getInstance();
    expect(inst.handleTextInputChange("1.1")).toMatchSnapshot();
  });

  it('test saveVersion function', () => {
    const wrapper = renderer.create(<ProductDetails {...props} />);
    const inst = wrapper.getInstance();
    expect(inst.saveVersion).toMatchSnapshot();
  });

  it('test createVersionsPath function', () => {
    const wrapper = renderer.create(<ProductDetails {...props} />);
    const inst = wrapper.getInstance();
    expect(inst.createVersionsPath).toMatchSnapshot();
  });

  test('newSearch() click event', () => {
    const wrapper = shallow(<ProductDetails {...props} />);
    const instance = wrapper.instance();
    const spy = sinon.spy(instance, 'saveVersion');

    wrapper.setState({ "newVersion": "1.1" })
    wrapper.find(Button).simulate('click');
    sinon.assert.called(spy);
  });

  it('test componentWillReceiveProps function', () => {
    const wrapper = renderer.create(<ProductDetails {...props} />);
    const inst = wrapper.getInstance();
    expect(inst.componentWillReceiveProps({...props})).toMatchSnapshot();
  });
});