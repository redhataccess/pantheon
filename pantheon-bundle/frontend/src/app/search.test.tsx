import React from 'react'
import { Search } from '@app/search'
import { BuildInfo } from './components/Chrome/Header/BuildInfo'
import { HashRouter as Router, Link } from 'react-router-dom'
import { mount, shallow } from 'enzyme'
import { DataList, Button, TextInput, Level, LevelItem, Checkbox, Alert } from '@patternfly/react-core'
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

  it('should render a Level component', () => {
    const wrapper = mount(<Router><Search {...mockStateUser} /></Router>)
    const level = wrapper.find(Level)
    expect(level.exists()).toBe(true)
  })

  it('should render a levelItem component', () => {
    const wrapper = mount(<Router><Search {...mockStateUser} /></Router>)
    const levelItem = wrapper.find(LevelItem)
    expect(levelItem.exists()).toBe(true)
  })

  it('should not render a dangerAlert for null or positive search results', () => {
    const wrapper = mount(<Router><Search {...mockStateUser} /></Router>)
    const dangerAlert = wrapper.find(Alert)
    expect(dangerAlert.exists()).toBe(false)
  })

  it('should handle state changes for displayLoadIcon', () => {
    const wrapper = shallow(<Router><Search {...mockStateUser} /></Router>)
    wrapper.setState({ 'displayLoadIcon': true })
    expect(wrapper.state('displayLoadIcon')).toBe(true)
    wrapper.setState({ 'displayLoadIcon': false })
    expect(wrapper.state('displayLoadIcon')).toBe(false)
  })

  it('should handle state changes for isSearchException', () => {
    const wrapper = shallow(<Router><Search {...mockStateUser} /></Router>)
    wrapper.setState({ 'isSearchException': false })
    expect(wrapper.state('isSearchException')).toBe(false)
    wrapper.setState({ 'isSearchException': true })
    expect(wrapper.state('isSearchException')).toBe(true)
  })

  it('should handle state changes for empty results for search', () => {
    const wrapper = shallow(<Router><Search {...mockStateUser} /></Router>)
    wrapper.setState({ 'isEmptyResults': false })
    expect(wrapper.state('isEmptyResults')).toBe(false)
    wrapper.setState({ 'isEmptyResults': true })
    expect(wrapper.state('isEmptyResults')).toBe(true)
  })

  it('should render a level component', () => {
    const wrapper = mount(<Router><Search {...mockStateUser} /></Router>)
    const level = wrapper.find(Level)
    expect(level.exists()).toBe(true)
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

  it('test changePerPageLimit function', () => {
    const wrapper = renderer.create(<Router><Search {...mockStateUser} /></Router>)
    const inst = wrapper.getInstance()
    expect(inst.changePerPageLimit).toMatchSnapshot()
  })

  it('test buildTransientPathArray function', () => {
    const wrapper = renderer.create(<Router><Search {...mockStateUser} /></Router>)
    const inst = wrapper.getInstance()
    expect(inst.buildTransientPathArray).toMatchSnapshot()
  })

})
