import React from 'react';
import { SearchFilter } from '@app/searchFilter';
import { mount, shallow } from 'enzyme';
import sinon from 'sinon'
import { InputGroup, FormSelect, ChipGroup, Button } from '@patternfly/react-core';
import renderer from 'react-test-renderer'
import '@app/fetchMock'

const props = {
  filterQuery: () => ("any"),
}

describe('SearchFilter tests', () => {
  test('should render default Search component', () => {
    const view = shallow(<SearchFilter />);
    expect(view).toMatchSnapshot();
  });


  it('test fetchProductVersionDetails function', () => {
    const wrapper = renderer.create(<SearchFilter />);
    const inst = wrapper.getInstance()
    const spy = sinon.spy(inst, 'fetchProductVersionDetails')
    inst.componentDidMount()
    sinon.assert.called(spy)
  })

  it('test setQuery function', () => {
    const wrapper = renderer.create(<SearchFilter {...props} />)
    const inst = wrapper.getInstance()
    const spy = sinon.spy(inst, 'setQuery')
    inst.setQuery()
    sinon.assert.called(spy)
  })

  it('test onChangeProduct function', () => {
    const wrapper = renderer.create(<SearchFilter {...props} />)
    const inst = wrapper.getInstance()
    const spy = sinon.spy(inst, 'onChangeProduct')
    inst.onChangeProduct("prod")
    sinon.assert.called(spy)
  })

  it('test onChangeSort function', () => {
    const wrapper = renderer.create(<SearchFilter {...props} />)
    const inst = wrapper.getInstance()
    const spy = sinon.spy(inst, 'onChangeSort')
    inst.onChangeSort("asc")
    sinon.assert.called(spy)
  })


  it('test onChangeModuleType function', () => {
    const wrapper = renderer.create(<SearchFilter {...props} />)
    const inst = wrapper.getInstance()
    const spy = sinon.spy(inst, 'onChangeModuleType')
    inst.onChangeModuleType("type")
    sinon.assert.called(spy)
  })

  it('test deleteItem function', () => {
    const wrapper = renderer.create(<SearchFilter {...props} />)
    const inst = wrapper.getInstance()
    const spy = sinon.spy(inst, 'deleteItem')

    const products = new Array()
    const pName = "productName";
    const versions = [{ value: '', label: 'Select a Version', disabled: false }, { value: 'All', label: 'All', disabled: false },];
    products[pName] = versions;
    inst.setState({ productValue: "productName", allProducts: products, chipGroups: [], })

    inst.deleteItem()
    sinon.assert.called(spy)
  })

  it('test addChipItem function', () => {
    const wrapper = renderer.create(<SearchFilter {...props} />)
    const inst = wrapper.getInstance()
    const spy = sinon.spy(inst, 'addChipItem')

    const products = new Array()
    const pName = "productName";
    const versions = [{ value: 'x', label: 'x', disabled: false }, { value: 'All', label: 'All', disabled: false },];
    products[pName] = versions;
    inst.setState({ versionValue:"x",productValue: "productName", allProducts: products, chipGroups: [], })

    inst.addChipItem()
    sinon.assert.called(spy)
  })
});
