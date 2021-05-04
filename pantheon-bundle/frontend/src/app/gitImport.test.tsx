import React from 'react'
import GitImport from "./gitImport"
import '@app/fetchMock'

import { mount, shallow } from 'enzyme'
import { Bullseye, TextInput, FormGroup, Button } from '@patternfly/react-core'

import sinon from 'sinon'
import renderer from 'react-test-renderer'

describe("Login tests", () => {
  test("should render Login component", () => {
    const view = shallow(<GitImport />)
    expect(view).toMatchSnapshot()
  })

  it("should render a Bullseye layout", () => {
    const wrapper = mount(<GitImport />)
    const bullseyeLayout = wrapper.find(Bullseye)
    expect(bullseyeLayout.exists()).toBe(true)
  })

  it("should render a form group", () => {
    const wrapper = mount(<GitImport />)
    const formGroup = wrapper.find(FormGroup)
    expect(formGroup.exists()).toBe(true)
  })

  it("should render a text input", () => {
    const wrapper = mount(<GitImport />)
    const textInput = wrapper.find(TextInput)
    expect(textInput.exists()).toBe(true)
  })

  it("should render a Button", () => {
    const wrapper = mount(<GitImport />)
    const button = wrapper.find(Button)
    expect(button.exists()).toBe(true)
  })

  it("test onChangeSort function", () => {
    const wrapper = renderer.create(<GitImport />)
    const inst = wrapper.getInstance()
    const spy = sinon.spy(inst, "cloneRepo")
    inst.cloneRepo()
    sinon.assert.called(spy)
    inst.setState({ repository: "http" })
    inst.cloneRepo()
    sinon.assert.called(spy)
  })

})
