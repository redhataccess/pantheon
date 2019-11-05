import React from 'react'
import { ProductListing }  from '@app/productListing'
import '@app/fetchMock'

import { mount, shallow } from 'enzyme'
import { DataList, DataListItem, DataListItemCells, DataListItemRow, FormGroup, TextInput } from '@patternfly/react-core'
import renderer from 'react-test-renderer'

 const props = {
   match: exact => true
 }

describe('ProductListing tests', () => {
  test('should render ProductListing component', () => {
    const view = shallow(<ProductListing />)
    expect(view).toMatchSnapshot()
  })

   it('should render a Data List', () => {
     const wrapper = mount(<ProductListing />)
     const dataList = wrapper.find(DataList)
     expect(dataList.exists()).toBe(true)
   })

   it('should render a DataListItem', () => {
    const wrapper = mount(<ProductListing />)
    const dataListItem = wrapper.find(DataListItem)
    expect(dataListItem.exists()).toBe(true)
  })

  it('should render a DataListItemCells Element', () => {
    const wrapper = mount(<ProductListing />)
    const dataListItemCells = wrapper.find(DataListItemCells)
    expect(dataListItemCells.exists()).toBe(true)
  })

  it('should render a DataListItemRow element', () => {
    const wrapper = mount(<ProductListing />)
    const dataListItemRow = wrapper.find(DataListItemRow)
    expect(dataListItemRow.exists()).toBe(true)
  })

   it('should render a form group', () => {
     const wrapper = mount(<ProductListing />)
     const formGroup = wrapper.find(FormGroup)
     expect(formGroup.exists()).toBe(true)
   })

   it('should render a text input', () => {
     const wrapper = mount(<ProductListing />)
     const textInput = wrapper.find(TextInput)
     expect(textInput.exists()).toBe(true)
   })

   it('test props', () => {
     const productListing = mount(<ProductListing {...props} />).matchesElement
     expect(productListing.length === 1)
   })

   
   it('test getProducts function', () => {
    const wrapper = renderer.create(<ProductListing />)
    const inst = wrapper.getInstance()
    expect(inst.getProducts([{"product1": "product1 name"}])).toMatchSnapshot()
  })

   it('test getProductsUrl function', () => {
    const wrapper = renderer.create(<ProductListing />)
    const inst = wrapper.getInstance()
    expect(inst.getProductsUrl("/content/products.query.json?nodeType=pant:product&orderby=name")).toMatchSnapshot()
  })
   
   it('test setInput function', () => {
    const wrapper = renderer.create(<ProductListing />)
    const inst = wrapper.getInstance()
    expect(inst.setInput("test input")).toMatchSnapshot()
  })

   it('test loginRedirect function', () => {
    const wrapper = renderer.create(<ProductListing />)
    const inst = wrapper.getInstance()
    expect(inst.loginRedirect).toMatchSnapshot()
  })
 
  it('test checkAuth function', () => {
    const wrapper = renderer.create(<ProductListing />)
    const inst = wrapper.getInstance()
    expect(inst.checkAuth).toMatchSnapshot()
  })

  it('test componentDidMount function', () => {
    const wrapper = renderer.create(<ProductListing />)
    const inst = wrapper.getInstance()
    expect(inst.componentDidMount).toMatchSnapshot()
  })
})
