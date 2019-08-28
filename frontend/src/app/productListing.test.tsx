import React from 'react';
import { ProductListing }  from '@app/productListing';
import "isomorphic-fetch"

import { mount, shallow } from 'enzyme';
import { DataList, FormGroup, TextInput } from '@patternfly/react-core';

 const props = {
   match: exact => true
 }

describe('ProductListing tests', () => {
  test('should render ProductListing component', () => {
    const view = shallow(<ProductListing />);
    expect(view).toMatchSnapshot();
  });

   it('should render a Data List', () => {
     const wrapper = mount(<ProductListing />);
     const dataList = wrapper.find(DataList);
     expect(dataList.exists()).toBe(true)
   });

   it('should render a form group', () => {
     const wrapper = mount(<ProductListing />);
     const formGroup = wrapper.find(FormGroup);
     expect(formGroup.exists()).toBe(true)
   });

   it('should render a text input', () => {
     const wrapper = mount(<ProductListing />);
     const textInput = wrapper.find(TextInput);
     expect(textInput.exists()).toBe(true)
   });

   it('test props', () => {
     const productListing = mount(<ProductListing {...props} />).matchesElement
     expect(productListing.length === 1)
   });
});
