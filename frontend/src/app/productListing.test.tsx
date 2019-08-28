import React from 'react';
import { ProductListing }  from '@app/productListing';
import "isomorphic-fetch"

import { mount, shallow } from 'enzyme';
import { DataList, DataListItem, DataListItemCells, DataListItemRow, FormGroup, TextInput } from '@patternfly/react-core';

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

   it('should render a DataListItem', () => {
    const wrapper = mount(<ProductListing />);
    const dataListItem = wrapper.find(DataListItem);
    expect(dataListItem.exists()).toBe(true)
  });

  it('should render a DataListItemCells Element', () => {
    const wrapper = mount(<ProductListing />);
    const dataListItemCells = wrapper.find(DataListItemCells);
    expect(dataListItemCells.exists()).toBe(true)
  });

  it('should render a DataListItemRow element', () => {
    const wrapper = mount(<ProductListing />);
    const dataListItemRow = wrapper.find(DataListItemRow);
    expect(dataListItemRow.exists()).toBe(true)
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
