import React from 'react'
import { Search } from '@app/search'
import { BuildInfo } from './components/Chrome/Header/BuildInfo'
import { HashRouter as Router } from 'react-router-dom'
import { mount, shallow } from 'enzyme'
import { DataList, Button, TextInput, Level, LevelItem } from '@patternfly/react-core'
import renderer from 'react-test-renderer'
import sinon from "sinon"
import { mockStateUser } from '@app/TestResources'
import '@app/fetchMock'

describe('Search tests', () => {
  test('should render default Search component', () => {
    const view = shallow(<Search {...mockStateUser} />)
    expect(view).toMatchSnapshot()
  })

  it('should render a Button', () => {
    const wrapper = mount(<Router><Search {...mockStateUser} /></Router>)
    const button = wrapper.find(Button)
    expect(button.exists()).toBe(true)
  })

  it('should render a DataList', () => {
    const wrapper = mount(<Router><Search {...mockStateUser} /></Router>)
    const dataList = wrapper.find(DataList)
    expect(dataList.exists()).toBe(true)
  })

  it('should render a TextInput', () => {
    const wrapper = mount(<Router><Search {...mockStateUser} /></Router>)
    const textInput = wrapper.find(TextInput)
    expect(textInput.exists()).toBe(true)
  })

  it('should render a BuildInfo component', () => {
    const wrapper = mount(<Router><Search {...mockStateUser} /></Router>)
    const buildInfo = wrapper.find(BuildInfo)
    expect(buildInfo.exists()).toBe(true)
  })

  it('should render a level component', () => {
    const wrapper = mount(<Router><Search {...mockStateUser} /></Router>)
    const level = wrapper.find(Level)
    expect(level.exists()).toBe(true)
  })

  it('should render a levelItem component', () => {
    const wrapper = mount(<Router><Search {...mockStateUser} /></Router>)
    const levelItem = wrapper.find(LevelItem)
    expect(levelItem.exists()).toBe(true)
  })

  it('test getRows function', () => {
    const wrapper = renderer.create(<Router><Search {...mockStateUser} /></Router>)
    const inst = wrapper.getInstance()
    expect(inst.getRows).toMatchSnapshot()
  })

  it('test buildSearchUrl function', () => {
    const wrapper = renderer.create(<Router><Search {...mockStateUser} /></Router>)
    const inst = wrapper.getInstance()
    expect(inst.buildSearchUrl).toMatchSnapshot()
  })

  it('test hideAlertOne function', () => {
    const wrapper = renderer.create(<Router><Search {...mockStateUser} /></Router>)
    const inst = wrapper.getInstance()
    expect(inst.hideAlertOne).toMatchSnapshot()
  })

  it('test confirmDeleteOperation function', () => {
    const wrapper = renderer.create(<Router><Search {...mockStateUser} /></Router>)
    const inst = wrapper.getInstance()
    expect(inst.confirmDeleteOperation).toMatchSnapshot()
  })

  it('test cancelDeleteOperation function', () => {
    const wrapper = renderer.create(<Router><Search {...mockStateUser} /></Router>)
    const inst = wrapper.getInstance()
    expect(inst.cancelDeleteOperation).toMatchSnapshot()
  })

  it('test sortByUploadTime function', () => {
    const wrapper = renderer.create(<Router><Search {...mockStateUser} /></Router>)
    const inst = wrapper.getInstance()
    expect(inst.sortByUploadTime).toMatchSnapshot()
  })

  it('test sortByDescription function', () => {
    const wrapper = renderer.create(<Router><Search {...mockStateUser} /></Router>)
    const inst = wrapper.getInstance()
    expect(inst.sortByDescription).toMatchSnapshot()
  })

  it('test sortByName function', () => {
    const wrapper = renderer.create(<Router><Search {...mockStateUser} /></Router>)
    const inst = wrapper.getInstance()
    expect(inst.sortByName).toMatchSnapshot()
  })

  it('test dismissNotification function', () => {
    const wrapper = renderer.create(<Router><Search {...mockStateUser} /></Router>)
    const inst = wrapper.getInstance()
    expect(inst.dismissNotification).toMatchSnapshot()
  })

  it('test doSearch function', () => {
    const wrapper = renderer.create(<Router><Search {...mockStateUser} /></Router>)
    const inst = wrapper.getInstance()
    expect(inst.doSearch).toMatchSnapshot()
  })

  it('test newSearch function', () => {
    const wrapper = renderer.create(<Router><Search {...mockStateUser} /></Router>)
    const inst = wrapper.getInstance()
    expect(inst.newSearch).toMatchSnapshot()
  })

  it('test delete function', () => {
    const wrapper = renderer.create(<Router><Search {...mockStateUser} /></Router>)
    const inst = wrapper.getInstance()
    expect(inst.delete).toMatchSnapshot()
  })

  it('test handleDeleteCheckboxChange function', () => {
    const wrapper = renderer.create(<Router><Search {...mockStateUser} /></Router>)
    const inst = wrapper.getInstance()
    expect(inst.handleDeleteCheckboxChange).toMatchSnapshot()
  })

  it('test handleSelectAll function', () => {
    const wrapper = renderer.create(<Router><Search {...mockStateUser} /></Router>)
    const inst = wrapper.getInstance()
    expect(inst.handleSelectAll).toMatchSnapshot()
  })

  it('test setInput function', () => {
    const wrapper = renderer.create(<Router><Search {...mockStateUser} /></Router>)
    const inst = wrapper.getInstance()
    expect(inst.setInput).toMatchSnapshot()
  })

  it('test fetchTimeout function', () => {
    const wrapper = renderer.create(<Router><Search {...mockStateUser} /></Router>)
    const inst = wrapper.getInstance()
    expect(inst.fetchTimeout).toMatchSnapshot()
  })

})
