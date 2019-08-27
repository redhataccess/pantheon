import React from 'react';
import { Brand }  from './Brand';
import "isomorphic-fetch"

import { shallow, mount } from 'enzyme';

describe('Brand tests', () => {
  test('should render Brand component', () => {
    const view = shallow(<Brand />);
    expect(view).toMatchSnapshot();
  });
});
