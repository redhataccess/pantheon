import React from 'react'
import { Module } from '@app/module'
import '@app/fetchMock'

import { mount, shallow } from 'enzyme'
import { Bullseye, TextInput, FormGroup, Button } from '@patternfly/react-core'

import sinon from "sinon"
import renderer from 'react-test-renderer'

describe('Login tests', () => {
  test('should render Login component', () => {
    const view = shallow(<Module />)
    expect(view).toMatchSnapshot()
  })

  it('should render a Bullseye layout', () => {
    const wrapper = mount(<Module />)
    const bullseyeLayout = wrapper.find(Bullseye)
    expect(bullseyeLayout.exists()).toBe(true)
  })


  it('should render a form group', () => {
    const wrapper = mount(<Module />)
    const formGroup = wrapper.find(FormGroup)
    expect(formGroup.exists()).toBe(true)
  })

  it('should render a text input', () => {
    const wrapper = mount(<Module />)
    const textInput = wrapper.find(TextInput)
    expect(textInput.exists()).toBe(true)
  })

  it('should render a Button', () => {
    const wrapper = mount(<Module />)
    const button = wrapper.find(Button)
    expect(button.exists()).toBe(true)
  })

  test('saveModule() click event', () => {
    const wrapper = shallow(<Module />)
    const instance = wrapper.instance()
    const spy = sinon.spy(instance, 'saveModule')

    wrapper.setState({ buildDate: '', commitHash: '', commitText: '' })
    wrapper.find(Button).at(0).simulate('click')
    sinon.assert.calledOnce(spy)
  })

  it('test handleNameInput function', () => {
    const wrapper = renderer.create(<Module />)
    const inst = wrapper.getInstance()
    expect(inst.handleNameInput()).toMatchSnapshot()
  })

  it('test handleModuleInput function', () => {
    const wrapper = renderer.create(<Module />)
    const inst = wrapper.getInstance()
    expect(inst.handleModuleInput()).toMatchSnapshot()
  })

  it('test handleFileChange function', () => {
    const wrapper = renderer.create(<Module />)
    const inst = wrapper.getInstance()
    const mockedEvent = { target: {} }
    expect(inst.handleFileChange(mockedEvent)).toMatchSnapshot()
  })

  it('test setFile function', () => {
    const wrapper = renderer.create(<Module />)
    const inst = wrapper.getInstance()
    expect(inst.setFile()).toMatchSnapshot()
  })

  it('test renderRedirect function', () => {
    const wrapper = renderer.create(<Module />)
    const inst = wrapper.getInstance()
    expect(inst.renderRedirect()).toMatchSnapshot()
  })

  it('test dismissNotification function', () => {
    const wrapper = renderer.create(<Module />)
    const inst = wrapper.getInstance()
    expect(inst.dismissNotification()).toMatchSnapshot()
  })
})
