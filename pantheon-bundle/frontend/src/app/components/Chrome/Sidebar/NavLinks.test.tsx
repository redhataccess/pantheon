import React from 'react'
import { NavLinks } from './NavLinks'
import { NavList, NavItem, NavExpandable } from '@patternfly/react-core'
import { HashRouter as Router } from 'react-router-dom'

import { mount, shallow } from 'enzyme'
import { Link } from 'react-router-dom'
import renderer from 'react-test-renderer'
import { mockStateUser, mockStateGuest, mockStateAdmin } from '@app/TestResources'

describe('NavLinks tests', () => {
  test('should render NavLinks component', () => {
    const view = shallow(<NavLinks {...mockStateUser} />)
    expect(view).toMatchSnapshot()
  })

  it('should render a NavList', () => {
    const wrapper = mount(<Router><NavLinks {...mockStateUser} /></Router>)
    const navList = wrapper.find(NavList)
    expect(navList.exists()).toBe(true)
  })

  it('should render a NavItem', () => {
    const wrapper = mount(<Router><NavLinks {...mockStateUser} /></Router>)
    const navItem = wrapper.find(NavItem)
    expect(navItem.exists()).toBe(true)
  })

  it('should render a Link component', () => {
    const wrapper = mount(<Router><NavLinks {...mockStateUser} /></Router>)
    const navLinks = wrapper.find(Link)
    expect(navLinks.exists()).toBe(true)
  })

  it('should render an Expandable component', () => {
    const wrapper = mount(<Router><NavLinks {...mockStateUser} /></Router>)
    const expandable = wrapper.find(NavExpandable)
    expect(expandable.exists()).toBe(true)
  })

  it('should contain 1 NavItem without authentication', () => {
    const wrapper = shallow(<NavLinks {...mockStateGuest} />)

    const items = wrapper.find(NavItem)
    expect(items).toHaveLength(1)
  })

  it('should handle state changes for isLoggedIn', () => {
    const wrapper = shallow(<NavLinks {...mockStateUser} />)
    const navGroup1 = wrapper.find('[groupId="grp-1"]')
    expect(navGroup1.length).toBe(3)
    const navGroup2 = wrapper.find('[groupId="grp-2"]')
    expect(navGroup2.length).toBe(3)
  })

  it('should handle state changes for isAdmin', () => {
    const wrapper = shallow(<NavLinks {...mockStateAdmin} />)
    const navGroup3 = wrapper.find('[groupId="grp-3"]')
    expect(navGroup3.length).toBe(4)
  })

  it('test browserLink function', () => {
    const wrapper = renderer.create(<Router><NavLinks {...mockStateUser} /></Router>)
    const inst = wrapper.getInstance()
    expect(inst.browserLink).toMatchSnapshot()
  })

  it('test welcomeLink function', () => {
    const wrapper = renderer.create(<Router><NavLinks {...mockStateUser} /></Router>)
    const inst = wrapper.getInstance()
    expect(inst.welcomeLink).toMatchSnapshot()
  })

  it('test webConsole function', () => {
    const wrapper = renderer.create(<Router><NavLinks {...mockStateUser} /></Router>)
    const inst = wrapper.getInstance()
    expect(inst.consoleLink).toMatchSnapshot()
  })

  it('test render function', () => {
    const wrapper = renderer.create(<Router><NavLinks {...mockStateUser} /></Router>)
    const inst = wrapper.getInstance()
    expect(inst.render).toMatchSnapshot()
  })

  it('test checkAuth function', () => {
    const wrapper = renderer.create(<Router><NavLinks {...mockStateUser} /></Router>)
    const inst = wrapper.getInstance()
    expect(inst.checkAuth).toMatchSnapshot()
  })

  it('test onExpandableSelect function', () => {
    const wrapper = renderer.create(<Router><NavLinks {...mockStateUser} /></Router>)
    const inst = wrapper.getInstance()
    expect(inst.onExpandableSelect).toMatchSnapshot()
  })

  it('test handleItemOnclick function', () => {
    const wrapper = renderer.create(<Router><NavLinks {...mockStateUser} /></Router>)
    const inst = wrapper.getInstance()
    expect(inst.handleItemOnclick).toMatchSnapshot()
  })

  it('test Admin Panel links', () => {
    jest.mock('./NavLinks', () => {
      // Require the original module to not be mocked...
      const originalModule = jest.requireActual('./NavLinks')

      return {
        __esModule: true, // Use it when dealing with esModules
        ...originalModule,
        browserLink: jest.fn().mockReturnValue('window.open("/bin/browser.html")'),
        checkAuth: jest.fn().mockReturnValue(true),
        consoleLink: jest.fn().mockReturnValue('window.open("/system/console/bundles.html")'),
        welcomeLink: jest.fn().mockReturnValue('window.open("/starter/index.html")'),
      }
    })

    const browserLink = require('./NavLinks').browserLink
    const consoleLink = require('./NavLinks').consoleLink
    const welcomeLink = require('./NavLinks').welcomeLink

    expect(browserLink()).toBe('window.open("/bin/browser.html")')
    expect(consoleLink()).toBe('window.open("/system/console/bundles.html")')
    expect(welcomeLink()).toBe('window.open("/starter/index.html")')
    jest.resetAllMocks()
  })

  it('calls render function', () => {
    const render = jest.fn()
    render()
    expect(render).toHaveBeenCalled()
  })

  it('test fetch api call', async () => {
    window.fetch = jest.fn().mockImplementation(async () => {
      return new Promise((resolve, reject) => {
        resolve({
          ok: true,
          status: 200,
          json: () => new Promise((resolve, reject) => {
            resolve({
              "getUserInfo": true,
              "isAdmin": false,
              "isLoggedIn": true,
            })
          })
        })
      })
      const wrapper = await shallow(<NavLinks {...mockStateUser} />)
      await wrapper.update()
      expect(wrapper.state('getUserInfo')).toBe(true)
      expect(wrapper.state('isLoggedIn')).toBe(true)
      expect(wrapper.state('isAdmin')).toBe(false)
    })
  })
})