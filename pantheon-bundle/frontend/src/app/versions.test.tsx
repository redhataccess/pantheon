import React from "react"
import { Versions, IProps } from "@app/versions"
import "@app/fetchMock"

import { mount, shallow } from "enzyme"
import {
    Button, Form, FormGroup, FormSelect, FormSelectOption, InputGroup,
    InputGroupText, Modal, Title, Alert, AlertActionCloseButton, Grid
} from "@patternfly/react-core"
import renderer from "react-test-renderer"
import sinon from "sinon"
import { any } from "prop-types";

const anymatch = require("anymatch")

const props = {
    assemblies: [],
    attributesFilePath: "/repositories/testRepo/attributes.adoc",
    contentType: "module",
    modulePath: "/modules/test",
    onGetProduct: (productValue) => anymatch,
    onGetVersion: (versionValue) => anymatch,
    onGetUrl: (url) => anymatch,
    productInfo: "Red Hat Enterprise Linux",
    updateDate: (draftUpdateDate, releaseUpdateDate, releaseVersion, variantUUID) => anymatch,
    variant: "test",
    variantUUID: "abcd-1234",
    versionModulePath: "/modules/test_module/en_US/variants/test/draft",
}

describe("Versions tests", () => {
    test("should render Versions component", () => {
        const view = shallow(<Versions {...props} />)
        expect(view).toMatchSnapshot()
    })

    it("should render a Modal", () => {
        const wrapper = mount(<Versions {...props} />)
        // console.log(wrapper.debug())
        const modal = wrapper.find(Modal)
        expect(modal.exists()).toBe(true)
    })

    it("should render a Grid", () => {
        const wrapper = mount(<Versions {...props} />)
        const grid = wrapper.find(Grid)
        expect(grid.exists()).toBe(true)
    })

    it("should render a Form", () => {
        const wrapper = shallow(<Versions {...props} />)
        wrapper.setState({ "login": true })
        wrapper.setState({ "isModalOpen": true })
        const form = wrapper.find(Form)
        expect(form.exists()).toBe(true)
    })

    it("should render a FormGroup", () => {
        const wrapper = shallow(<Versions {...props} />)
        wrapper.setState({ "login": true })
        wrapper.setState({ "isModalOpen": true })
        const formGroup = wrapper.find(FormGroup)
        expect(formGroup.exists()).toBe(true)
    })

    it("should render a FormSelect", () => {
        const wrapper = shallow(<Versions {...props} />)
        wrapper.setState({ "login": true })
        wrapper.setState({ "isModalOpen": true })
        const formSelect = wrapper.find(FormSelect)
        expect(formSelect.exists()).toBe(true)
    })

    it("should render a FormSelectOption", () => {
        const wrapper = shallow(<Versions {...props} />)
        wrapper.setState({ "login": true })
        wrapper.setState({ "isModalOpen": true })
        const formSelectOption = wrapper.find(FormSelectOption)
        expect(formSelectOption.exists()).toBe(true)
    })

    it("should render a InputGroup", () => {
        const wrapper = shallow(<Versions {...props} />)
        wrapper.setState({ "login": true })
        wrapper.setState({ "isModalOpen": true })
        const inputGroup = wrapper.find(InputGroup)
        expect(inputGroup.exists()).toBe(true)
    })

    it("should render a success Alert", () => {
        const wrapper = shallow(<Versions {...props} />)
        wrapper.setState({ "login": true })
        wrapper.setState({ "successAlertVisible": true })
        const alert = wrapper.find(Alert)
        expect(alert.exists()).toBe(true)
    })

    it("test fetchVersions function", () => {
        const wrapper = renderer.create(<Versions {...props} />)
        const inst = wrapper.getInstance()
        expect(inst.fetchVersions()).toMatchSnapshot()
    })

    it("test changePublishState function", () => {
        const wrapper = renderer.create(<Versions {...props} />)
        const inst = wrapper.getInstance()
        expect(inst.changePublishState("Publish")).toMatchSnapshot()
    })

    it("test previewDoc function", () => {
        const wrapper = renderer.create(<Versions {...props} />)
        const inst = wrapper.getInstance()
        expect(inst.previewDoc()).toMatchSnapshot()
    })

    it("test saveMetadata function", () => {
        const wrapper = renderer.create(<Versions {...props} />)
        const inst = wrapper.getInstance()
        expect(inst.saveMetadata()).toMatchSnapshot()
    })

    it("test onChangeUsecase function", () => {
        const wrapper = renderer.create(<Versions {...props} />)
        const inst = wrapper.getInstance()
        expect(inst.onChangeUsecase()).toMatchSnapshot()
    })

    it("test handleURLInput function", () => {
        const wrapper = renderer.create(<Versions {...props} />)
        const inst = wrapper.getInstance()
        expect(inst.handleURLInput()).toMatchSnapshot()
    })

    it("test dismissNotification function", () => {
        const wrapper = renderer.create(<Versions {...props} />)
        const inst = wrapper.getInstance()
        expect(inst.dismissNotification()).toMatchSnapshot()
    })

    it("test getMetadata function", () => {
        const wrapper = shallow(<Versions {...props} />)
        const instance = wrapper.instance()
        wrapper.setState({ "login": true })
        wrapper.setState({ "isModalOpen": true })
        // Assuming metadata exists
        const spy = sinon.spy(instance, "saveMetadata")
        const urlFragment = wrapper.find("input")
        expect(urlFragment.exists()).toBe(true)

        const useCaseValue = wrapper.find("[aria-label='FormSelect Usecase']").simulate("change", { target: { value: "Administer" } })
        expect(useCaseValue.exists()).toBe(true)
    })

    it("test getHarrayChildNamed function", () => {
        const wrapper = renderer.create(<Versions {...props} />)
        const inst = wrapper.getInstance()
        expect(inst.getHarrayChildNamed("__children__")).toMatchSnapshot()
    })

    it("test hideSuccessAlert function", () => {
        const wrapper = renderer.create(<Versions {...props} />)
        const inst = wrapper.getInstance()
        expect(inst.hideSuccessAlert()).toMatchSnapshot()
    })

    it("test hidePublishAlert function", () => {
        const wrapper = renderer.create(<Versions {...props} />);
        const inst = wrapper.getInstance();
        expect(inst.hidePublishAlert()).toMatchSnapshot();
    });

    it("test hideUppublishAlertForModule function", () => {
        const wrapper = renderer.create(<Versions {...props} />)
        const inst = wrapper.getInstance()
        expect(inst.hideUppublishAlertForModule()).toMatchSnapshot()
    })

    it("has a props", () => {
        const versions = mount(<Versions {...props} />).matchesElement
        expect(versions.length === 1)
    })

    it("test getHarrayChildNamed function", () => {
        const wrapper = renderer.create(<Versions {...props} />)
        const inst = wrapper.getInstance()
        expect(inst.getHarrayChildNamed(anymatch, "metadata")).toMatchSnapshot()
    })

    it("has a variantUUID of '1234'", () => {
        const state: IProps = {
            attributesFilePath: "/repositories/testRepo/attributes.adoc",
            contentType: "module",
            modulePath: "somePath",
            onGetProduct: (productValue) => anymatch,
            onGetVersion: (versionValue) => anymatch,
            onGetUrl: (url) => anymatch,
            productInfo: "Red Hat Enterprise Linux",
            updateDate: (draftUpdateDate, releaseUpdateDate, releaseVersion, variantUUID) => anymatch,
            variant: "DEFAULT",
            variantUUID: "abcd-1234",
            versionModulePath: "versionPath",
        }
        state.updateDate("-", "-", 1, "1234")
        expect(state.modulePath).toEqual("somePath")
        expect(state.versionModulePath).toEqual("versionPath")
    })

    test("changePublishState click Publish", () => {
        const wrapper = mount(<Versions {...props} />)
        const instance = wrapper.instance()
        wrapper.setState({
            "login": true,
            "showMetadataAlertIcon": false,
            "results": [[{ "type": "draft", "icon": "BlankImage", "path": "/modules/test", "version": "Version 1", "publishedState": "Not published", "updatedDate": "", "firstButtonType": "primary", "secondButtonType": "secondary", "firstButtonText": "Publish", "secondButtonText": "Preview", "isDropdownOpen": false, "isArchiveDropDownOpen": false, "metadata": { productVersion: { label: "test", uuid: 1234 } }, "validation":[] }]],
        })
        const spy = sinon.spy(instance, "changePublishState")
        wrapper.find(Button).at(2).simulate("click")
        sinon.assert.called(spy)
    })

    test("changePublishState click Unpublish", () => {
        const wrapper = mount(<Versions {...props} />)
        const instance = wrapper.instance()
        wrapper.setState({
            "login": true,
            "results": [[{ "type": "release", "icon": "BlankImage", "path": "/modules/test", "version": "Version 1", "publishedState": "Released", "updatedDate": "", "firstButtonType": "primary", "secondButtonType": "secondary", "firstButtonText": "Publish", "secondButtonText": "Preview", "isDropdownOpen": false, "isArchiveDropDownOpen": false, "metadata": { productVersion: { label: "test", uuid: 1234 } }, "validation":[] }]],
        })
        const spy = sinon.spy(instance, "changePublishState")
        wrapper.find(Button).at(2).simulate("click")
        sinon.assert.called(spy)
    })

    it("test fetchProducts function", () => {
        const wrapper = renderer.create(<Versions {...props} />)
        const inst = wrapper.getInstance()
        const spy = sinon.spy(inst, "fetchProducts")
        inst.componentDidMount()
        sinon.assert.called(spy)
    })

    it("test fetchVersions function", () => {
        const wrapper = renderer.create(<Versions {...props} />)
        const inst = wrapper.getInstance()
        const spy = sinon.spy(inst, "fetchVersions")
        inst.fetchVersions()
        sinon.assert.called(spy)
    })

    it("test changePublishState function", () => {
        const wrapper = renderer.create(<Versions {...props} />)
        const inst = wrapper.getInstance()
        const spy = sinon.spy(inst, "changePublishState")
        inst.changePublishState("ds")
        sinon.assert.called(spy)
    })

    it("test getDocumentsIncluded function", () => {
        const wrapper = renderer.create(<Versions {...props} />)
        const inst = wrapper.getInstance()
        const spy = sinon.spy(inst, "getDocumentsIncluded")
        inst.getDocumentsIncluded()
        sinon.assert.called(spy)
    })

    it("test capitalize function", () => {
        const wrapper = renderer.create(<Versions {...props} />)
        const inst = wrapper.getInstance()
        const spy = sinon.spy(inst, "capitalize")
        inst.capitalize()
        sinon.assert.called(spy)
    })

    it("test setAlertTitle function", () => {
        const wrapper = renderer.create(<Versions {...props} />)
        const inst = wrapper.getInstance()
        const spy = sinon.spy(inst, "setAlertTitle")
        inst.setAlertTitle()
        sinon.assert.called(spy)
    })
})
