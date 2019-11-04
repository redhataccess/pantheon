import React from 'react'
import { Versions, IProps } from '@app/versions'
import '@app/fetchMock'

import { mount, shallow } from 'enzyme'
import { Button, Card, DataList, DataListItem, DataListItemCells, DataListItemRow, DataListToggle, Dropdown, Form, FormGroup, FormSelect, FormSelectOption, InputGroup, InputGroupText, Modal, TextInput, DropdownItem, Title, Alert, AlertActionCloseButton, DataListContent, KebabToggle } from '@patternfly/react-core'
import renderer from 'react-test-renderer'
import sinon from 'sinon'

const anymatch = require('anymatch')

const props = {
    modulePath: "/modules/test",
    onGetProduct: (productValue) => anymatch,
    onGetVersion: (versionValue) => anymatch,
    productInfo: "Red Hat Enterprise Linux",
    updateDate: (draftUpdateDate, releaseUpdateDate, releaseVersion, moduleUUID) => anymatch,
    versionModulePath: "/modules/test/en_US/1"
}

describe('Versions tests', () => {
    test('should render Versions component', () => {
        const view = shallow(<Versions {...props} />)
        expect(view).toMatchSnapshot()
    })

    it('should render a Button', () => {
        const wrapper = mount(<Versions {...props} />)
        const button = wrapper.find(Button)
        expect(button.exists()).toBe(true)
    })

    it('should render a Card', () => {
        const wrapper = mount(<Versions {...props} />)
        const card = wrapper.find(Card)
        expect(card.exists()).toBe(true)
    })

    it('should render a Data List', () => {
        const wrapper = mount(<Versions {...props} />)
        const dataList = wrapper.find(DataList)
        expect(dataList.exists()).toBe(true)
    })

    it('should render a DataListItem', () => {
        const wrapper = mount(<Versions {...props} />)
        const dataListItem = wrapper.find(DataListItem)
        expect(dataListItem.exists()).toBe(true)
    })

    it('should render a DataListItemCells Element', () => {
        const wrapper = mount(<Versions {...props} />)
        const dataListItemCells = wrapper.find(DataListItemCells)
        expect(dataListItemCells.exists()).toBe(true)
    })

    it('should render a DataListItemRow element', () => {
        const wrapper = mount(<Versions {...props} />)
        const dataListItemRow = wrapper.find(DataListItemRow)
        expect(dataListItemRow.exists()).toBe(true)
    })

    it('should render a DataListToggle', () => {
        const wrapper = mount(<Versions {...props} />)
        const dataListToggle = wrapper.find(DataListToggle)
        expect(dataListToggle.exists()).toBe(true)
    })

    it('should render a DataListContent', () => {
        const wrapper = mount(<Versions {...props} />)
        const dataListContent = wrapper.find(DataListContent)
        expect(dataListContent.exists()).toBe(true)
    })

    it('should render a Modal', () => {
        const wrapper = mount(<Versions {...props} />)
        const modal = wrapper.find(Modal)
        expect(modal.exists()).toBe(true)
    })

    it('should render a Form', () => {
        const wrapper = shallow(<Versions {...props} />)
        wrapper.setState({ 'login': true })
        wrapper.setState({ 'isModalOpen': true })
        const form = wrapper.find(Form)
        expect(form.exists()).toBe(true)
    })

    it('should render a FormGroup', () => {
        const wrapper = shallow(<Versions {...props} />)
        wrapper.setState({ 'login': true })
        wrapper.setState({ 'isModalOpen': true })
        const formGroup = wrapper.find(FormGroup)
        expect(formGroup.exists()).toBe(true)
    })

    it('should render a FormSelect', () => {
        const wrapper = shallow(<Versions {...props} />)
        wrapper.setState({ 'login': true })
        wrapper.setState({ 'isModalOpen': true })
        const formSelect = wrapper.find(FormSelect)
        expect(formSelect.exists()).toBe(true)
    })

    it('should render a FormSelectOption', () => {
        const wrapper = shallow(<Versions {...props} />)
        wrapper.setState({ 'login': true })
        wrapper.setState({ 'isModalOpen': true })
        const formSelectOption = wrapper.find(FormSelectOption)
        expect(formSelectOption.exists()).toBe(true)
    })

    it('should render a InputGroup', () => {
        const wrapper = shallow(<Versions {...props} />)
        wrapper.setState({ 'login': true })
        wrapper.setState({ 'isModalOpen': true })
        const inputGroup = wrapper.find(InputGroup)
        expect(inputGroup.exists()).toBe(true)
    })

    it('should render a success Alert', () => {
        const wrapper = shallow(<Versions {...props} />)
        wrapper.setState({ 'login': true })
        wrapper.setState({ 'successAlertVisble': true })
        const alert = wrapper.find(Alert)
        expect(alert.exists()).toBe(true)
    })

    it('should render a KebabToggle element', () => {
        const wrapper = mount(<Versions {...props} />)
        wrapper.setState({ 'login': true })
        wrapper.setState({
            'results': [[{ "type": "draft", "icon": "BlankImage", "path": "/modules/test", "version": "Version 1", "publishedState": 'Not published', "updatedDate": "", "firstButtonType": 'primary', "secondButtonType": 'secondary', "firstButtonText": 'Publish', "secondButtonText": 'Preview', "isDropdownOpen": false, "isArchiveDropDownOpen": false, "metadata": '' }]],
        })
        const kebabToggle = wrapper.find(KebabToggle)
        expect(kebabToggle.exists()).toBe(true)
    })

    it('test fetchVersions function', () => {
        const wrapper = renderer.create(<Versions {...props} />)
        const inst = wrapper.getInstance()
        expect(inst.fetchVersions()).toMatchSnapshot()
    })

    it('test changePublishState function', () => {
        const wrapper = renderer.create(<Versions {...props} />)
        const inst = wrapper.getInstance()
        expect(inst.changePublishState("Publish")).toMatchSnapshot()
    })

    it('test onArchiveSelect function', () => {
        const wrapper = renderer.create(<Versions {...props} />)
        const inst = wrapper.getInstance()
        expect(inst.onArchiveSelect()).toMatchSnapshot()
    })

    it('test onHeadingToggle function', () => {
        const wrapper = renderer.create(<Versions {...props} />)
        const inst = wrapper.getInstance()
        expect(inst.onHeadingToggle()).toMatchSnapshot()
    })

    it('test onExpandableToggle function', () => {
        const wrapper = renderer.create(<Versions {...props} />)
        const inst = wrapper.getInstance()
        const data = [{ "isDropdownOpen": true }]
        expect(inst.onExpandableToggle(data)).toMatchSnapshot()
    })

    it('test onArchiveToggle function', () => {
        const wrapper = renderer.create(<Versions {...props} />)
        const inst = wrapper.getInstance()
        const data = [{ "isDropdownOpen": true }]
        expect(inst.onArchiveToggle(data)).toMatchSnapshot()
    })

    it('test previewDoc function', () => {
        const wrapper = renderer.create(<Versions {...props} />)
        const inst = wrapper.getInstance()
        expect(inst.previewDoc()).toMatchSnapshot()
    })

    it('test saveMetadata function', () => {
        const wrapper = renderer.create(<Versions {...props} />)
        const inst = wrapper.getInstance()
        expect(inst.saveMetadata()).toMatchSnapshot()
    })

    it('test onChangeProduct function', () => {
        const wrapper = renderer.create(<Versions {...props} />)
        const inst = wrapper.getInstance()
        expect(inst.onChangeProduct()).toMatchSnapshot()
    })

    it('test onChangeUsecase function', () => {
        const wrapper = renderer.create(<Versions {...props} />)
        const inst = wrapper.getInstance()
        expect(inst.onChangeUsecase()).toMatchSnapshot()
    })

    it('test handleURLInput function', () => {
        const wrapper = renderer.create(<Versions {...props} />)
        const inst = wrapper.getInstance()
        expect(inst.handleURLInput()).toMatchSnapshot()
    })

    it('test loginRedirect function', () => {
        const wrapper = renderer.create(<Versions {...props} />)
        const inst = wrapper.getInstance()
        expect(inst.loginRedirect()).toMatchSnapshot()
    })

    it('test dismissNotification function', () => {
        const wrapper = renderer.create(<Versions {...props} />)
        const inst = wrapper.getInstance()
        expect(inst.dismissNotification()).toMatchSnapshot()
    })

    it('test getMetadata function', () => {
        const wrapper = shallow(<Versions {...props} />)
        const instance = wrapper.instance()
        wrapper.setState({ 'login': true })
        wrapper.setState({ 'isModalOpen': true })
        // Assuming metadata exists
        const spy = sinon.spy(instance, 'saveMetadata')
        const urlFragment = wrapper.find('input')
        expect(urlFragment.exists()).toBe(true)

        const useCaseValue = wrapper.find('[aria-label="FormSelect Usecase"]').simulate('change', { target: { value: 'Administer' } })
        expect(useCaseValue.exists()).toBe(true)
    })

    it('test getHarrayChildNamed function', () => {
        const wrapper = renderer.create(<Versions {...props} />)
        const inst = wrapper.getInstance()
        expect(inst.getHarrayChildNamed("__children__")).toMatchSnapshot()
    })

    it('test fetchProductVersionDetails function', () => {
        const wrapper = renderer.create(<Versions {...props} />)
        const inst = wrapper.getInstance()
        expect(inst.fetchProductVersionDetails()).toMatchSnapshot()
    })

    it('test hideSuccessAlert function', () => {
        const wrapper = renderer.create(<Versions {...props} />)
        const inst = wrapper.getInstance()
        expect(inst.hideSuccessAlert()).toMatchSnapshot()
    })

    it('test hidePublishAlert function', () => {
        const wrapper = renderer.create(<Versions {...props} />);
        const inst = wrapper.getInstance();
        expect(inst.hidePublishAlert()).toMatchSnapshot();
    });

    test('Version Button click', () => {
        const wrapper = shallow(<Versions {...props} />)
        const instance = wrapper.instance()
        const spy = sinon.spy(instance, 'onHeadingToggle')

        wrapper.find(DataListToggle).simulate('click')
        sinon.assert.called(spy)
    })

    // Value testing with Enzyme.
    it('renders Version heading', () => {
        const wrapper = mount(<Versions {...props} />)
        const sourceTypeText = wrapper.find('#span-source-type-version').text()

        // ensure it matches what is expected
        expect(sourceTypeText).toEqual("Version")
    })

    it('renders Published heading', () => {
        const wrapper = mount(<Versions {...props} />)
        const sourceTypeText = wrapper.find('#span-source-type-version-published').text()

        // ensure it matches what is expected
        expect(sourceTypeText).toEqual("Published")
    })

    it('renders Draft Uploaded heading', () => {
        const wrapper = mount(<Versions {...props} />)
        const sourceTypeText = wrapper.find('#span-source-type-version-draft-uploaded').text()

        // ensure it matches what is expected
        expect(sourceTypeText).toEqual("Draft Uploaded")
    })

    it('has a props', () => {
        const versions = mount(<Versions {...props} />).matchesElement
        expect(versions.length === 1)
    })

    it('test getHarrayChildNamed function', () => {
        const wrapper = renderer.create(<Versions {...props} />)
        const inst = wrapper.getInstance()
        expect(inst.getHarrayChildNamed(anymatch, 'metadata')).toMatchSnapshot()
    })

    it('has a moduleUUID of "1234"', () => {
        const state: IProps = {
            modulePath: "somePath",
            onGetProduct: (productValue) => anymatch,
            onGetVersion: (versionValue) => anymatch,
            productInfo: "Red Hat Enterprise Linux",
            updateDate: (draftUpdateDate, releaseUpdateDate, releaseVersion, moduleUUID) => anymatch,
            versionModulePath: "/modules/test/en_US/1"
        }
        state.updateDate("-", "-", 1, "1234")
        expect(state.modulePath).toEqual('somePath')
        expect(state.versionModulePath).toEqual('versionPath')
    })

    test('changePublishState click', () => {
        const wrapper = mount(<Versions {...props} />)
        const instance = wrapper.instance()
        wrapper.setState({ 'login': true })
        wrapper.setState({
            'results': [[{ "type": "draft", "icon": "BlankImage", "path": "/modules/test", "version": "Version 1", "publishedState": 'Not published', "updatedDate": "", "firstButtonType": 'primary', "secondButtonType": 'secondary', "firstButtonText": 'Publish', "secondButtonText": 'Preview', "isDropdownOpen": false, "isArchiveDropDownOpen": false, "metadata": '' }]],
        })
        const spy = sinon.spy(instance, 'changePublishState')
        wrapper.find(Button).at(2).simulate('click')
        sinon.assert.called(spy)
    })
})
