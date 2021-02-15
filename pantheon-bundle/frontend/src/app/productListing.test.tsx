import React from "react"
import "@app/fetchMock"
import { mount, shallow } from "enzyme"
import { render, fireEvent } from '@testing-library/react'
import { DataList, DataListItem, DataListItemCells, DataListItemRow, FormGroup, TextInput } from "@patternfly/react-core"
import ProductListing from "./productListing"
import { ProductContext, IProduct } from "./contexts/ProductContext"

const date = new Date();

const allProducts: IProduct[] = [{
  description: "descrip",
  isOpen: false,
  ["jcr:created"]: date,
  ['jcr:createdBy']: "admin",
  ['jcr:lastModified']: date,
  ['jcr:lastModifiedBy']: "admin",
  ['jcr:primaryType']: "pant:product",
  ['jcr:uuid']: "5df8d913-79b9-42fd-a17b-ffc917add446",
  locale: "en-US",
  name: "product 11/16",
  ['sling:resourceType']: "pantheon/product",
  urlFragment: "product_url",
}, {
  description: "test descrip",
  isOpen: false,
  ['jcr:created']: date,
  ['jcr:createdBy']: "admin",
  ['jcr:lastModified']: date,
  ['jcr:lastModifiedBy']: "admin",
  ['jcr:primaryType']: "pant:product",
  ['jcr:uuid']: "242d4187-d2f4-4df5-917b-e09bf4ff45e9",
  locale: "en-US",
  name: "test 11/17",
  ['sling:resourceType']: "pantheon/product",
  urlFragment: "testurl",
}];

describe("ProductListing tests", () => {
  test("should render ProductListing component", () => {
    const view = shallow(<ProductListing />)
    expect(view).toMatchSnapshot()
  })

  it("should render a Data List", () => {
    const wrapper = mount(<ProductListing />)
    const dataList = wrapper.find(DataList)
    expect(dataList.exists()).toBe(true)
  })

  it("should render a DataListItem", () => {
    const wrapper = mount(<ProductListing />)
    const dataListItem = wrapper.find(DataListItem)
    expect(dataListItem.exists()).toBe(true)
  })

  it("should render a DataListItemCells Element", () => {
    const wrapper = mount(<ProductListing />)
    const dataListItemCells = wrapper.find(DataListItemCells)
    expect(dataListItemCells.exists()).toBe(true)
  })

  it("should render a DataListItemRow element", () => {
    const wrapper = mount(<ProductListing />)
    const dataListItemRow = wrapper.find(DataListItemRow)
    expect(dataListItemRow.exists()).toBe(true)
  })

  it("should render a form group", () => {
    const wrapper = mount(<ProductListing />)
    const formGroup = wrapper.find(FormGroup)
    expect(formGroup.exists()).toBe(true)
  })

  it("should render a text input", () => {
    const wrapper = mount(<ProductListing />)
    const textInput = wrapper.find(TextInput)
    expect(textInput.exists()).toBe(true)
  })


  it("should update input and list of products on text change", () => {
    const { getByPlaceholderText } = render(<ProductContext.Provider value={allProducts}><ProductListing /></ProductContext.Provider>)
    let productInput = getByPlaceholderText('Type product name to search') as HTMLInputElement
    expect(productInput.value).toBe('')
    fireEvent.change(productInput, { target: { value: 'test_name' } })
    expect(productInput.value).toBe('test_name')
  })

  it("should test toggling caret to display product details dropdown option", () => {
    const { getByTestId, getByText } = render(<ProductContext.Provider value={allProducts}><ProductListing /></ProductContext.Provider>)
    const caret = getByTestId('product-5df8d913-79b9-42fd-a17b-ffc917add446-button')
    fireEvent.click(caret)
    expect(getByText('Product Details')).toBeTruthy()
    const productDetailButton = getByText('Product Details')
    fireEvent.click(productDetailButton)
  })
})
