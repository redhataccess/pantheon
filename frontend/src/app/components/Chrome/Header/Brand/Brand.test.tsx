import React from 'react'
import { Brand }  from './Brand'
import '@app/fetchMock'

import { shallow } from 'enzyme'

describe('Brand tests', () => {
  test('should render Brand component', () => {
    const view = shallow(<Brand />)
    expect(view).toMatchSnapshot()
  })
})
