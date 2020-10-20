import React from "react"
import { Product } from "@app/product"
import "@app/fetchMock"

import { mount, shallow } from "enzyme"
import { Bullseye, TextInput, FormGroup, Button } from "@patternfly/react-core"
import renderer from "react-test-renderer"

describe("Product tests", () => {
  test("should render Product component", () => {
    const view = shallow(<Product />)
    expect(view).toMatchSnapshot()
  })

  it("should render a Bullseye layout", () => {
    const wrapper = mount(<Product />)
    const bullseyeLayout = wrapper.find(Bullseye)
    expect(bullseyeLayout.exists()).toBe(true)
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

  it("test renderRedirect function", () => {
    const wrapper = renderer.create(<Product />)
    const inst = wrapper.getInstance()
    expect(inst.renderRedirect).toMatchSnapshot()
  })

  it("test loginRedirect function", () => {
    const wrapper = renderer.create(<Product />)
    const inst = wrapper.getInstance()
    expect(inst.loginRedirect).toMatchSnapshot()
  })
 
  it("test checkAuth function", () => {
    const wrapper = renderer.create(<Product />)
    const inst = wrapper.getInstance()
    expect(inst.checkAuth).toMatchSnapshot()
  })
  
  it("test dismissNotification function", () => {
    const wrapper = renderer.create(<Product />)
    const inst = wrapper.getInstance()
    expect(inst.dismissNotification).toMatchSnapshot()
  })

  it("test productExist function", () => {
    const wrapper = renderer.create(<Product />)
    const inst = wrapper.getInstance()
    expect(inst.productExist("Red Hat Enterprise Linux")).toMatchSnapshot()
  })

  it("test handleNameInput function", () => {
    const wrapper = renderer.create(<Product />)
    const inst = wrapper.getInstance()
    expect(inst.handleNameInput("Red Hat Enterprise Linux")).toMatchSnapshot()
  })

  it("test handleProductInput function", () => {
    const wrapper = renderer.create(<Product />)
    const inst = wrapper.getInstance()
    expect(inst.handleProductInput("Linux Platform")).toMatchSnapshot()
  })

  it("test handleUrlInput function", () => {
    const wrapper = renderer.create(<Product />)
    const inst = wrapper.getInstance()
    expect(inst.handleUrlInput("red_hat")).toMatchSnapshot()
  })
})
