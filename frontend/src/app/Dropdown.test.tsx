import React from 'react';
import { Dropdown } from '@app/Dropdown';
import "isomorphic-fetch"

import { mount, shallow } from 'enzyme';

describe('Tests for Dropdown', () => {

  test('should render Dropdown component', () => {
    const view = shallow(<Dropdown
        perPageValue={move}
        newPerPagevalue={"25 items per page"}
      />
    );
    expect(view).toMatchSnapshot();
  });

  it('should render button for dropdown list', () => {
    const wrapper = mount(<Dropdown
        perPageValue={move}
        newPerPagevalue={"25 items per page"}
      />
    );
  });  

  function move(){
    return "R"
  }
});
