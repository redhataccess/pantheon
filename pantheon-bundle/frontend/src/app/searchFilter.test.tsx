import React from 'react';
import { SearchFilter } from '@app/searchFilter';
import { mount, shallow } from 'enzyme';
import { Button, InputGroup } from '@patternfly/react-core';
import '@app/fetchMock'

describe('SearchFilter tests', () => {
  test('should render default Search component', () => {
    const view = shallow(<SearchFilter />);
    expect(view).toMatchSnapshot();
  });

  it('should render a Button', () => {
    const wrapper = mount(<SearchFilter />);
    const button = wrapper.find(Button);
    expect(button.exists()).toBe(true)
  });

  it('should render a Button', () => {
    const wrapper = mount(<SearchFilter />);
    const input = wrapper.find(InputGroup);
    expect(input.exists()).toBe(true)
  });

});
