import React from 'react'
import { GitImport } from '@app/gitImport'
import '@app/fetchMock'

import { mount, shallow } from 'enzyme'
import { Bullseye, TextInput, FormGroup, Button } from '@patternfly/react-core'

describe('Login tests', () => {
  test('should render Login component', () => {
    const view = shallow(<GitImport />)
    expect(view).toMatchSnapshot()
  })

  it('should render a Bullseye layout', () => {
    const wrapper = mount(<GitImport />)
    const bullseyeLayout = wrapper.find(Bullseye)
    expect(bullseyeLayout.exists()).toBe(true)
  })

  it('should render a form group', () => {
    const wrapper = mount(<GitImport />)
    const formGroup = wrapper.find(FormGroup)
    expect(formGroup.exists()).toBe(true)
  })

  it('should render a text input', () => {
    const wrapper = mount(<GitImport />)
    const textInput = wrapper.find(TextInput)
    expect(textInput.exists()).toBe(true)
  })

  it('should render a Button', () => {
    const wrapper = mount(<GitImport />)
    const button = wrapper.find(Button)
    expect(button.exists()).toBe(true)
  })

})
