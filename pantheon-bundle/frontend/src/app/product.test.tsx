import React from "react"
import { Product } from "@app/product"
import "@app/fetchMock"

import { mount, shallow } from "enzyme"
import { TextInput, FormGroup, Button } from "@patternfly/react-core"
import { render, fireEvent } from '@testing-library/react'

describe("Product tests", () => {
  test("should render Product component", () => {
    const view = shallow(<Product />)
    expect(view).toMatchSnapshot()
  })

  it("should render a form group", () => {
    const wrapper = mount(<Product />)
    const formGroup = wrapper.find(FormGroup)
    expect(formGroup.exists()).toBe(true)
  })

  it("should render a text input", () => {
    const wrapper = mount(<Product />)
    const textInput = wrapper.find(TextInput)
    expect(textInput.exists()).toBe(true)
  })

  it("should render a Button", () => {
    const wrapper = mount(<Product />)
    const button = wrapper.find(Button)
    expect(button.exists()).toBe(true)
  })
 
  it("test handleNameInput function", () => {
    const container = shallow(<Product />)
    const productInput = container.find('#product_name_text')
    productInput.simulate('change', 'test_name');
    container.update()
    expect(container.find('#product_name_text').prop('value')).toEqual(
      'test_name',
    );
  })
  it("test handleProductInput function", () => {
    const container = shallow(<Product />)
    const input = container.find('#product_description_text')
    input.simulate('change', 'test_prod_descrip');
    container.update()
    expect(container.find('#product_description_text').prop('value')).toEqual(
      'test_prod_descrip',
    );
  })

  it("test handleUrlInput function - valid url provided", () => {
    const container = shallow(<Product />)
    const input = container.find('#product_url_fragment_text')
    input.simulate('change', 'test_prod_url');
    container.update()
    expect(container.find('#product_url_fragment_text').prop('value')).toEqual(
      'test_prod_url',
    );
  })
  it("test handleUrlInput function - invalid url provided", () => {
    const container = shallow(<Product />)
    const input = container.find('#product_url_fragment_text')
    input.simulate('change', '');
    container.update()
    expect(container.find('#product_url_fragment_text').prop('value')).toEqual(
      '',
    );
  })
  it("test handleTextInputChange function", () => {
    const container = shallow(<Product />)
    const input = container.find('#new_version_name_text')
    input.simulate('change', 'test_version_name');
    container.update()
    expect(container.find('#new_version_name_text').prop('value')).toEqual(
      'test_version_name',
    );
  })
  it("test handleUrlInputChange function - valid URL", () => {
    const container = shallow(<Product />)
    const input = container.find('#new_version_url_fragment')
    input.simulate('change', 'test_version_url');
    container.update()
    expect(container.find('#new_version_url_fragment').prop('value')).toEqual(
      'test_version_url',
    );
  })
  it("test handleUrlInputChange function - invalid URL", () => {
    const container = shallow(<Product />)
    const input = container.find('#new_version_url_fragment')
    input.simulate('change', '');
    container.update()
    expect(container.find('#new_version_url_fragment').prop('value')).toEqual(
      '',
    );
  })
  test('test alert message that appears when form is missing fields', () => {
    const handleClose = jest.fn()
    const { queryByText, getByText, getByLabelText } = render(<Product onClose={handleClose} />)
    fireEvent.click(getByText('Save'))
    expect(queryByText("Fields indicated by * are mandatory")).toBeTruthy()
    const closeButton = getByLabelText('Close Warning alert: alert: Fields indicated by * are mandatory')
    fireEvent.click(closeButton)

  })
  test('test alert message that appears when invalid input is entered for URL Fragment', () => {
    const { queryByText, getByPlaceholderText } = render(<Product />)
    let input = getByPlaceholderText('URL Fragment')
    fireEvent.change(input, { target: { value: '*' } })
    expect(queryByText("Allowed input for Product ulrFragment: alphanumeric, hyphen, period and underscore")).toBeTruthy()
  })
  test('test filling out product form and clicking save product button', async () => {
    const { getByText, getByPlaceholderText } = render(<Product exist={false} />)
    let productInput = getByPlaceholderText('Product Name')
    fireEvent.change(productInput, { target: { value: 'test_name' } })
    let urlInput = getByPlaceholderText('URL Fragment')
    fireEvent.change(urlInput, { target: { value: 'url' } })
    let prodVersionInput = getByPlaceholderText('Product Version')
    fireEvent.change(prodVersionInput, { target: { value: 'test_version' } })
    let versionUrlInput = getByPlaceholderText('Version URL Fragment')
    fireEvent.change(versionUrlInput, { target: { value: 'test_url' } })
    fireEvent.click(getByText('Save'))
  })
})
