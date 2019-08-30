import React from 'react';
import Search from '@app/search';
import { BuildInfo } from './components/Chrome/Header/BuildInfo';
import "isomorphic-fetch"
import { HashRouter as Router } from 'react-router-dom';
import { mount, shallow } from 'enzyme';
import { DataList, Button, TextInput } from '@patternfly/react-core';
import renderer from 'react-test-renderer';

describe('Search tests', () => {
  test('should render default Search component', () => {
    const view = shallow(<Search />);
    expect(view).toMatchSnapshot();
  });

  it('should render a Button', () => {
    const wrapper = mount(<Router><Search /></Router>);
    const button = wrapper.find(Button);
    expect(button.exists()).toBe(true)
  });

  it('should render a DataList', () => {
    const wrapper = mount(<Router><Search /></Router>);
    const dataList = wrapper.find(DataList);
    expect(dataList.exists()).toBe(true)
  });

  it('should render a TextInput', () => {
    const wrapper = mount(<Router><Search /></Router>);
    const textInput = wrapper.find(TextInput);
    expect(textInput.exists()).toBe(true)
  });

  it('should render a BuildInfo component', () => {
    const wrapper = mount(<Router><Search /></Router>);
    const buildInfo = wrapper.find(BuildInfo);
    expect(buildInfo.exists()).toBe(true)
  });

  it('test click event', () => {
    const mockCallBack = jest.fn();

    const button = shallow((<Button onClick={mockCallBack}>Submit</Button>));
    button.find('button').simulate('click');
    expect(mockCallBack.mock.calls.length).toEqual(1);
  });

  it('test getRows function', () => {
    const wrapper = renderer.create(<Router><Search /></Router>);
    const inst = wrapper.getInstance();
    expect(inst.getRows).toMatchSnapshot();
  });

  it('test buildSearchUrl function', () => {
    const wrapper = renderer.create(<Router><Search /></Router>);
    const inst = wrapper.getInstance();
    expect(inst.buildSearchUrl).toMatchSnapshot();
  });

  it('test hideAlertOne function', () => {
    const wrapper = renderer.create(<Router><Search /></Router>);
    const inst = wrapper.getInstance();
    expect(inst.hideAlertOne).toMatchSnapshot();
  });

  it('test confirmDeleteOperation function', () => {
    const wrapper = renderer.create(<Router><Search /></Router>);
    const inst = wrapper.getInstance();
    expect(inst.confirmDeleteOperation).toMatchSnapshot();
  });

  it('test cancelDeleteOperation function', () => {
    const wrapper = renderer.create(<Router><Search /></Router>);
    const inst = wrapper.getInstance();
    expect(inst.cancelDeleteOperation).toMatchSnapshot();
  });
});
