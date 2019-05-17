import React from 'react';
import Search from '@app/search';
import "isomorphic-fetch"

import { mount, shallow } from 'enzyme';
import { Label } from '@patternfly/react-core';

describe('App tests', () => {
  test('should render default App component', () => {
    const view = shallow(<Search />);
    expect(view).toMatchSnapshot();
  });

  it('should render a search Label', () => {
    const wrapper = mount(<Search />);
    const button = wrapper.find(Label);
    expect(button.exists()).toBe(true)
  });

});
