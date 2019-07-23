import React from 'react';
import Search from '@app/search';
import "isomorphic-fetch"

import { mount, shallow } from 'enzyme';
import { DataList, Button, TextInput } from '@patternfly/react-core';

describe('Search tests', () => {
  test('should render default Search component', () => {
    const view = shallow(<Search/>);
    expect(view).toMatchSnapshot();
  });

  it('should render a Button', () => {
    const wrapper = mount(<Search/>);
    const button = wrapper.find(Button);
    expect(button.exists()).toBe(true)
  });

  it('should render a DataList', () => {
    const wrapper = mount(<Search/>);
    const dataList = wrapper.find(DataList);
    expect(dataList.exists()).toBe(true)
  });

  it('should render a TextInput', () => {
    const wrapper = mount(<Search/>);
    const textInput = wrapper.find(TextInput);
    expect(textInput.exists()).toBe(true)
  });

});
