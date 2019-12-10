import React from 'react'
import { ModuleDisplay } from '@app/moduleDisplay'
import '@app/fetchMock'

import { mount, shallow } from 'enzyme'
import { Button, Card, DataList, DataListItem, DataListItemCells, DataListItemRow, DataListCell, TextContent, Level, LevelItem, Breadcrumb, BreadcrumbItem } from '@patternfly/react-core'
import renderer from 'react-test-renderer'
import sinon from 'sinon'
import { Versions } from '@app/versions'
const anymatch = require('anymatch')

const props = {
    location: { pathname: "module/test" }
}

describe('ModuleDisplay tests', () => {
    test('should render ModuleDisplay component', () => {
        const view = shallow(<ModuleDisplay {...props} />)
        expect(view).toMatchSnapshot()
    })

    it('should render a Breadcrumb', () => {
        const wrapper = mount(<ModuleDisplay {...props} />)
        const breadcrumb = wrapper.find(Breadcrumb)
        expect(breadcrumb.exists()).toBe(true)
    })

    it('should render a BreadcrumbItem', () => {
        const wrapper = mount(<ModuleDisplay {...props} />)
        const breadcrumbItem = wrapper.find(BreadcrumbItem)
        expect(breadcrumbItem.exists()).toBe(true)
    })

    it('should render a Button', () => {
        const wrapper = mount(<ModuleDisplay {...props} />)
        const button = wrapper.find(Button)
        expect(button.exists()).toBe(true)
    })

    it('should render a Card', () => {
        const wrapper = mount(<ModuleDisplay {...props} />)
        const card = wrapper.find(Card)
        expect(card.exists()).toBe(true)
    })

    it('should render a Data List', () => {
        const wrapper = mount(<ModuleDisplay {...props} />)
        const dataList = wrapper.find(DataList)
        expect(dataList.exists()).toBe(true)
    })

    it('should render a DataListItem', () => {
        const wrapper = mount(<ModuleDisplay {...props} />)
        const dataListItem = wrapper.find(DataListItem)
        expect(dataListItem.exists()).toBe(true)
    })

    it('should render a DataListItemCells Element', () => {
        const wrapper = mount(<ModuleDisplay {...props} />)
        const dataListItemCells = wrapper.find(DataListItemCells)
        expect(dataListItemCells.exists()).toBe(true)
    })

    it('should render a DataListItemRow element', () => {
        const wrapper = mount(<ModuleDisplay {...props} />)
        const dataListItemRow = wrapper.find(DataListItemRow)
        expect(dataListItemRow.exists()).toBe(true)
    })

    it('should render a DataListCell', () => {
        const wrapper = mount(<ModuleDisplay {...props} />)
        const dataListCell = wrapper.find(DataListCell)
        expect(dataListCell.exists()).toBe(true)
        // console.log("[DataListCell] length", dataListCell.length)
        expect(dataListCell.at(0).contains("Product")).toBe(true)
    })

    it('should render a TextContent Element', () => {
        const wrapper = mount(<ModuleDisplay {...props} />)
        const textContent = wrapper.find(TextContent)
        expect(textContent.exists()).toBe(true)
    })

    it('should render a Level Element', () => {
        const wrapper = mount(<ModuleDisplay {...props} />)
        const level = wrapper.find(Level)
        expect(level.exists()).toBe(true)
    })

    it('should render a LevelItem', () => {
        const wrapper = mount(<ModuleDisplay {...props} />)
        const levelItem = wrapper.find(LevelItem)
        expect(levelItem.exists()).toBe(true)
    })

    it('should render a Versions Element', () => {
        const wrapper = mount(<ModuleDisplay {...props} />)
        const versions = wrapper.find(Versions)
        expect(versions.exists()).toBe(true)
    })

    it('should render a h1 component', () => {
        const wrapper = mount(<ModuleDisplay {...props} />)
        const h1 = wrapper.find('[component="h1"]')
        expect(h1.exists()).toBe(true)
    })

    it('test fetchModuleDetails function', () => {
        const wrapper = renderer.create(<ModuleDisplay {...props} />)
        const inst = wrapper.getInstance()
        expect(inst.fetchModuleDetails(props)).toMatchSnapshot()
    })

    it('test getProduct function', () => {
        const wrapper = renderer.create(<ModuleDisplay {...props} />)
        const inst = wrapper.getInstance()
        expect(inst.getProduct()).toMatchSnapshot()
    })

    it('test getVersion function', () => {
        const wrapper = renderer.create(<ModuleDisplay {...props} />)
        const inst = wrapper.getInstance()
        expect(inst.getVersion()).toMatchSnapshot()
    })

    it('test getVersionUUID function', () => {
        const wrapper = renderer.create(<ModuleDisplay {...props} />)
        const inst = wrapper.getInstance()
        expect(inst.getVersionUUID("/modules/test")).toMatchSnapshot()
    })

    it('test getProductInitialLoad function', () => {
        const wrapper = renderer.create(<ModuleDisplay {...props} />)
        const inst = wrapper.getInstance()
        expect(inst.getProductInitialLoad()).toMatchSnapshot()
    })

    it('test componentDidMount function', () => {
        const wrapper = renderer.create(<ModuleDisplay {...props} />)
        const inst = wrapper.getInstance()
        expect(inst.componentDidMount()).toMatchSnapshot()
    })

    it('test getPortalUrl function', () => {
        const wrapper = renderer.create(<ModuleDisplay {...props} />)
        const inst = wrapper.getInstance()
        expect(inst.getPortalUrl()).toMatchSnapshot()
    })

    it('has a props', () => {
        const moduleDisplay = mount(<ModuleDisplay {...props} />).matchesElement
        expect(moduleDisplay.length === 1)
    })

    // Value testing with Enzyme.
    it('renders Product heading', () => {
        const wrapper = mount(<ModuleDisplay {...props} />)
        const sourceTypeText = wrapper.find('#span-source-type-product').first().text()

        // ensure it matches what is expected
        expect(sourceTypeText).toEqual("Product")
    })

    it('renders Published heading', () => {
        const wrapper = mount(<ModuleDisplay {...props} />)
        const sourceTypeText = wrapper.find('#span-source-type-published').first().text()

        // ensure it matches what is expected
        expect(sourceTypeText).toEqual("Published")
    })

    it('renders Draft Uploaded heading', () => {
        const wrapper = mount(<ModuleDisplay {...props} />)
        const sourceTypeText = wrapper.find('#span-source-type-draft-uploaded').first().text()

        // ensure it matches what is expected
        expect(sourceTypeText).toEqual("Draft Uploaded")
    })

    it('renders Module Type heading', () => {
        const wrapper = mount(<ModuleDisplay {...props} />)
        const sourceTypeText = wrapper.find('#span-source-name-module-type').first().text()

        // ensure it matches what is expected
        expect(sourceTypeText).toEqual("Module Type")
    })

    it('renders View on Customer Portal hotlink', () => {
        const wrapper = mount(<ModuleDisplay {...props} />)
        wrapper.setState({ 'login': true })
        wrapper.setState({ 'releaseUpdateDate': "Fri Oct 18 2019 17:35:50 GMT-0400" })
        wrapper.setState({ 'moduleUUID': "123" })
        wrapper.setState({ 'portalHost': "https://example.com" })
        const sourceTypeText = wrapper.find('a').at(2).text()

        // ensure it matches what is expected
        expect(sourceTypeText).toContain("View on Customer Portal")
    })

    it('should check if draftUpdateDate exists', () => {
        const wrapper = shallow(<ModuleDisplay {...props} />)
        wrapper.setState({ 'login': true })
        wrapper.setState({ "draftUpdateDate": "abcd" })
        expect(wrapper.state('draftUpdateDate')).toBeDefined()
    })

    it('should check if releaseUpdateDate exists', () => {
        const wrapper = shallow(<ModuleDisplay {...props} />)
        wrapper.setState({ 'login': true })
        wrapper.setState({ "releaseUpdateDate": "abcd" })
        expect(wrapper.state('releaseUpdateDate')).toBeDefined()
    })

    it('should check if moduleType exists', () => {
        const wrapper = shallow(<ModuleDisplay {...props} />)
        wrapper.setState({ 'moduleType': '' })
        wrapper.setState({ "moduleType": "module" })
        expect(wrapper.state('moduleType')).toBeDefined()
    })

    it('should have a moduleTitle', () => {
        const wrapper = shallow(<ModuleDisplay {...props} />)
        wrapper.setState({ 'login': true })
        wrapper.setState({ "moduleTitle": "test title" })

        const sourceText = wrapper.find('[component="h1"]').first().html()
        // ensure it matches what is expected
        expect(wrapper.state('moduleTitle')).toBeDefined()
        expect(sourceText).toContain("test title")
    })

    it('should have a versionUUID', () => {
        const wrapper = shallow(<ModuleDisplay {...props} />)
        wrapper.setState({ 'login': true })
        wrapper.setState({ "versionUUID": "122234-1234-1234T" })
        expect(wrapper.state('versionUUID')).toBeDefined()
    })

    it('should have a productValue', () => {
        const wrapper = shallow(<ModuleDisplay {...props} />)
        wrapper.setState({ 'login': true })
        const len = wrapper.setState({ "productValue": "Red Hat Enterprise Linux" })
        expect(wrapper.state('productValue')).toBeDefined()
    })

    it('should have a versionValue', () => {
        const wrapper = shallow(<ModuleDisplay {...props} />)
        wrapper.setState({ 'login': true })
        const len = wrapper.setState({ "versionValue": "8.x" })
        expect(wrapper.state('versionValue')).toBeDefined()
    })

    it('renders Copy permanent URL', () => {
        const wrapper = shallow(<ModuleDisplay {...props} />)
        wrapper.setState({ 'login': true })
        wrapper.setState({ 'moduleUUID': "somepath" })
        const permanentURL = wrapper.find('a#permanentURL').first()
        expect(permanentURL.exists).toBeTruthy()
    })

    it('renders copySuccess Message', () => {
        const wrapper = shallow(<ModuleDisplay {...props} />)
        wrapper.setState({ 'login': true })
        wrapper.setState({ 'releasePath': "somepath" })
        wrapper.setState({ 'moduleUUID': "1234" })
        wrapper.setState({ "copySuccess": "Copied!" })
        expect(wrapper.state('copySuccess')).toContain("Copied!")
    })

    test('copyToClipboard click event', () => {
        const wrapper = shallow(<ModuleDisplay {...props} />)
        const instance = wrapper.instance()
        const spy = sinon.spy(instance, 'copyToClipboard')

        wrapper.setState({ moduleUUID: '1234', releasePath: 'yarn' })
        expect(wrapper.find('#permanentURL').exists())
    })

    it('test mouseLeave function', () => {
        const wrapper = renderer.create(<ModuleDisplay {...props} />)
        const inst = wrapper.getInstance()
        expect(inst.mouseLeave()).toMatchSnapshot()
    })

    it('test fetch api call for portalUrl', async () => {
        window.fetch = jest.fn().mockImplementation(async () => {
            return new Promise((resolve, reject) => {
                resolve({
                    ok: true,
                    status: 200,
                    json: () => new Promise((resolve, reject) => {
                        resolve({
                            "portalHost": "https://example.com",
                        })
                    })
                })
            })
            const wrapper = await shallow(<ModuleDisplay {...props} />)
            await wrapper.update()
            expect(wrapper.state('portalHost')).toBe("https://example.com")
        })
    })
})
