import React from 'react'
import { Dropdown } from '@app/Dropdown'
import '@app/fetchMock'
import { mount, shallow } from 'enzyme'
import sinon from 'sinon'
import renderer from 'react-test-renderer'

describe('Tests for Dropdown', () => {

  test('should render Dropdown component', () => {
    const view = shallow(<Dropdown
        perPageValue={move}
        newPerPagevalue={"25 items per page"}
      />
    )
    expect(view).toMatchSnapshot()
  })

  it('should render dropdown list', () => {
    const wrapper = mount(<Dropdown
        perPageValue={move}
        newPerPagevalue={"25 items per page"}
      />
    )
  })

  it('test changeSelection function', () => {
    const wrapper = renderer.create(<Dropdown
      perPageValue={move}
      newPerPagevalue={"25 items per page"}
    />)
    const inst = wrapper.getInstance()
    const spy = sinon.spy(inst, 'changeSelection')
    inst.changeSelection(wrapper)
    sinon.assert.called(spy)

    const spy2 = sinon.spy(inst, 'changeExpandState')
    inst.changeExpandState()
    sinon.assert.called(spy2)
  })

  function move(){
    return "R"
  }
})
