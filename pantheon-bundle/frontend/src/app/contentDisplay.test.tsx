import React from "react"
import { ContentDisplay } from "@app/contentDisplay"
import "@app/fetchMock"

import { mount, shallow } from "enzyme"
import { Button, Card, TextContent, Level, LevelItem, Title, Divider } from "@patternfly/react-core"
import renderer from "react-test-renderer"
import sinon from "sinon"
import { Versions } from "@app/versions"
const anymatch = require("anymatch")

const assemblyProps = {
    match: {
        path:"/assembly/test"
    },
    location: { pathname: "assembly/test" }
}


describe("ContentDisplay tests for Assembly", () => {
    const wrapper = mount(<ContentDisplay {...assemblyProps} />)
    wrapper.setState({isAssembly: true})
    test("should render ContentDisplay component", () => {
        const view = shallow(<ContentDisplay {...assemblyProps} />)
        expect(view).toMatchSnapshot()
    })

    it("should render a Button", () => {
        
        const button = wrapper.find(Button)
        expect(button.exists()).toBe(true)
    })

    it("should render a Card", () => {
        const card = wrapper.find(Card)
        expect(card.exists()).toBe(true)
    })

    it("should render a TextContent Element", () => {
        const textContent = wrapper.find(TextContent)
        expect(textContent.exists()).toBe(true)
    })

    it("should render a Level Element", () => {
        const level = wrapper.find(Level)
        expect(level.exists()).toBe(true)
    })

    it("should render a LevelItem", () => {
        const levelItem = wrapper.find(LevelItem)
        expect(levelItem.exists()).toBe(true)
    })

    it("should render a Versions Element", () => {
        const versions = wrapper.find(Versions)
        expect(versions.exists()).toBe(true)
    })

    it("should render a Divider Element", () => {
        const divider = wrapper.find(Divider)
        expect(divider.exists()).toBe(true)
    })

    it("test fetchModuleDetails function", () => {
        const wrapper = renderer.create(<ContentDisplay {...assemblyProps} />)
        const inst = wrapper.getInstance()
        expect(inst.fetchModuleDetails(assemblyProps)).toMatchSnapshot()
    })

    it("test getVersionUUID function", () => {
        const wrapper = renderer.create(<ContentDisplay {...assemblyProps} />)
        const inst = wrapper.getInstance()
        expect(inst.getVersionUUID("/modules/test")).toMatchSnapshot()
    })

    it("test getProductInitialLoad function", () => {
        const wrapper = renderer.create(<ContentDisplay {...assemblyProps} />)
        const inst = wrapper.getInstance()
        expect(inst.getProductInitialLoad()).toMatchSnapshot()
    })

    it("test componentDidMount function", () => {
        const wrapper = renderer.create(<ContentDisplay {...assemblyProps} />)
        const inst = wrapper.getInstance()
        expect(inst.componentDidMount()).toMatchSnapshot()
    })

    it("test getPortalUrl function", () => {
        const wrapper = renderer.create(<ContentDisplay {...assemblyProps} />)
        const inst = wrapper.getInstance()
        expect(inst.getPortalUrl()).toMatchSnapshot()
    })

    it("has assemblyProps", () => {
        const contentDisplay = mount(<ContentDisplay {...assemblyProps} />).matchesElement
        expect(contentDisplay.length === 1)
    })

    // Value testing with Enzyme.
    it("renders Product heading", () => {
        const sourceTypeText = wrapper.find("#span-source-type-product").first().text()

        // ensure it matches what is expected
        expect(sourceTypeText).toEqual("Product")
    })

    it("renders Published heading", () => {
        const sourceTypeText = wrapper.find("#span-source-type-draft-published").first().text()
        // ensure it matches what is expected
        expect(sourceTypeText).toEqual("Published")
    })

    it("renders Draft Uploaded heading", () => {
        const sourceTypeText = wrapper.find("#span-source-type-draft-uploaded").first().text()

        // ensure it matches what is expected
        expect(sourceTypeText).toEqual("Draft uploaded")
    })

    it("renders View on Customer Portal hotlink", () => {
        wrapper.setState({ "login": true })
        wrapper.setState({ "releaseUpdateDate": "Fri Oct 18 2019 17:35:50 GMT-0400" })
        wrapper.setState({ "variantUUID": "123" })
        wrapper.setState({ "portalUrl": "https://example.com" })
        wrapper.setState({ "portalUrlType": "LIVE" })
        const sourceTypeText = wrapper.find("a").at(0).text()

        // ensure it matches what is expected
        expect(sourceTypeText).toContain("View on Customer Portal")
    })

    it("should check if draftUpdateDate exists", () => {
        const wrapper = shallow(<ContentDisplay {...assemblyProps} />)
        wrapper.setState({ "login": true })
        wrapper.setState({ "draftUpdateDate": "abcd" })
        expect(wrapper.state("draftUpdateDate")).toBeDefined()
    })

    it("should check if releaseUpdateDate exists", () => {
        const wrapper = shallow(<ContentDisplay {...assemblyProps} />)
        wrapper.setState({ "login": true })
        wrapper.setState({ "releaseUpdateDate": "abcd" })
        expect(wrapper.state("releaseUpdateDate")).toBeDefined()
    })

    it("should have a Title", () => {
        const wrapper = shallow(<ContentDisplay {...assemblyProps} />)
        wrapper.setState({ "login": true })
        wrapper.setState({ "moduleTitle": "test title" })

        const sourceText = wrapper.find(Title).first().html()
        // ensure it matches what is expected
        expect(wrapper.state("moduleTitle")).toBeDefined()
        expect(sourceText).toContain("test title")
    })

    it("should have a Text component of TextVariant.small", () => {
        const wrapper = shallow(<ContentDisplay {...assemblyProps} />)
        wrapper.setState({ "login": true })
        expect(wrapper.find('[component="small"]').exists());
    })

    it("should have a versionUUID", () => {
        const wrapper = shallow(<ContentDisplay {...assemblyProps} />)
        wrapper.setState({ "login": true })
        wrapper.setState({ "versionUUID": "122234-1234-1234T" })
        expect(wrapper.state("versionUUID")).toBeDefined()
    })

    it("should have a productValue", () => {
        const wrapper = shallow(<ContentDisplay {...assemblyProps} />)
        wrapper.setState({ "login": true })
        const len = wrapper.setState({ "productValue": "Red Hat Enterprise Linux" })
        expect(wrapper.state("productValue")).toBeDefined()
    })

    it("should have a versionValue", () => {
        const wrapper = shallow(<ContentDisplay {...assemblyProps} />)
        wrapper.setState({ "login": true })
        const len = wrapper.setState({ "versionValue": "8.x" })
        expect(wrapper.state("versionValue")).toBeDefined()
    })

    it("renders Copy permanent URL", () => {
        const wrapper = shallow(<ContentDisplay {...assemblyProps} />)
        wrapper.setState({ "login": true })
        wrapper.setState({ "variantUUID": "somepath" })
        const permanentURL = wrapper.find("a#permanentURL").first()
        expect(permanentURL.exists).toBeTruthy()
    })

    it("renders copySuccess Message", () => {
        const wrapper = shallow(<ContentDisplay {...assemblyProps} />)
        wrapper.setState({ "login": true })
        wrapper.setState({ "releasePath": "somepath" })
        wrapper.setState({ "variantUUID": "1234" })
        wrapper.setState({ "copySuccess": "Copied!" })
        expect(wrapper.state("copySuccess")).toContain("Copied!")
    })

    test("copyToClipboard click event", () => {
        const wrapper = shallow(<ContentDisplay {...assemblyProps} />)
        const instance = wrapper.instance()
        const spy = sinon.spy(instance, "copyToClipboard")

        wrapper.setState({ variantUUID: "1234", releasePath: "yarn" })
        expect(wrapper.find("#permanentURL").exists())
    })

    // it("test fetch api call for portalUrl", async () => {
    //     window.fetch = jest.fn().mockImplementation(async () => {
    //         return new Promise((resolve, reject) => {
    //             resolve({
    //                 ok: true,
    //                 status: 200,
    //                 json: () => new Promise((resolve, reject) => {
    //                     resolve({
    //                         "portalHost": "https://example.com",
    //                     })
    //                 })
    //             })
    //         })
    //         const wrapper = await shallow(<ContentDisplay {...assemblyProps} />)
    //         await wrapper.update()
    //         expect(wrapper.state("portalHost")).toBe("https://example.com")
    //     })
    // })
})



describe("ContentDisplay tests for Module", () => {
    const props = {
        match: {
            path:"/module/test"
        },
        location: { pathname: "module/test" }
    }
    const wrapper = mount(<ContentDisplay {...props} />)
    test("should render ModuleDisplay component", () => {
        const view = shallow(<ContentDisplay {...props} />)
        expect(view).toMatchSnapshot()
    })

    it("should render a Button", () => {
        const button = wrapper.find(Button)
        expect(button.exists()).toBe(true)
    })

    it("should render a Card", () => {
        const card = wrapper.find(Card)
        expect(card.exists()).toBe(true)
    })

    it("should render a TextContent Element", () => {
        const textContent = wrapper.find(TextContent)
        expect(textContent.exists()).toBe(true)
    })

    it("should render a Level Element", () => {
        const level = wrapper.find(Level)
        expect(level.exists()).toBe(true)
    })

    it("should render a LevelItem", () => {
        const levelItem = wrapper.find(LevelItem)
        expect(levelItem.exists()).toBe(true)
    })

    it("should render a Versions Element", () => {
        const versions = wrapper.find(Versions)
        expect(versions.exists()).toBe(true)
    })

    it("should render a Divider Element", () => {
        const divider = wrapper.find(Divider)
        expect(divider.exists()).toBe(true)
    })

    it("test fetchModuleDetails function", () => {
        const wrapper = renderer.create(<ContentDisplay {...props} />)
        const inst = wrapper.getInstance()
        expect(inst.fetchModuleDetails(props)).toMatchSnapshot()
    })

    it("test getProduct function", () => {
        const wrapper = renderer.create(<ContentDisplay {...props} />)
        const inst = wrapper.getInstance()
        expect(inst.getProduct()).toMatchSnapshot()
    })

    it("test getVersion function", () => {
        const wrapper = renderer.create(<ContentDisplay {...props} />)
        const inst = wrapper.getInstance()
        expect(inst.getVersion()).toMatchSnapshot()
    })

    it("test getVersionUUID function", () => {
        const wrapper = renderer.create(<ContentDisplay {...props} />)
        const inst = wrapper.getInstance()
        expect(inst.getVersionUUID("/modules/test")).toMatchSnapshot()
    })

    it("test getProductInitialLoad function", () => {
        const wrapper = renderer.create(<ContentDisplay {...props} />)
        const inst = wrapper.getInstance()
        expect(inst.getProductInitialLoad()).toMatchSnapshot()
    })

    it("test componentDidMount function", () => {
        const wrapper = renderer.create(<ContentDisplay {...props} />)
        const inst = wrapper.getInstance()
        expect(inst.componentDidMount()).toMatchSnapshot()
    })

    it("test getPortalUrl function", () => {
        const wrapper = renderer.create(<ContentDisplay {...props} />)
        const inst = wrapper.getInstance()
        expect(inst.getPortalUrl()).toMatchSnapshot()
    })

    it("has a props", () => {
        const moduleDisplay = mount(<ContentDisplay {...props} />).matchesElement
        expect(moduleDisplay.length === 1)
    })

    // Value testing with Enzyme.
    it("renders Product heading", () => {
        const sourceTypeText = wrapper.find("#span-source-type-product").first().text()

        // ensure it matches what is expected
        expect(sourceTypeText).toEqual("Product")
    })

    it("renders Published heading", () => {
        const sourceTypeText = wrapper.find("#span-source-type-published").first().text()

        // ensure it matches what is expected
        expect(sourceTypeText).toEqual("Published")
    })

    it("renders Draft Uploaded heading", () => {
        const sourceTypeText = wrapper.find("#span-source-type-draft-uploaded").first().text()

        // ensure it matches what is expected
        expect(sourceTypeText).toEqual("Draft uploaded")
    })

    it("renders Module Type heading", () => {
        const sourceTypeText = wrapper.find("#span-source-name-module-type").first().text()

        // ensure it matches what is expected
        expect(sourceTypeText).toEqual("Module type")
    })

    it("renders View on Customer Portal hotlink", () => {
        wrapper.setState({ "login": true })
        wrapper.setState({ "releaseUpdateDate": "Fri Oct 18 2019 17:35:50 GMT-0400" })
        wrapper.setState({ "variantUUID": "123" })
        wrapper.setState({ "portalUrl": "https://example.com" })
        wrapper.setState({ "portalUrlType": "LIVE" })
        const sourceTypeText = wrapper.find("a").at(0).text()

        // ensure it matches what is expected
        expect(sourceTypeText).toContain("View on Customer Portal")
    })

    it("should check if draftUpdateDate exists", () => {
        const wrapper = shallow(<ContentDisplay {...props} />)
        wrapper.setState({ "login": true })
        wrapper.setState({ "draftUpdateDate": "abcd" })
        expect(wrapper.state("draftUpdateDate")).toBeDefined()
    })

    it("should check if releaseUpdateDate exists", () => {
        const wrapper = shallow(<ContentDisplay {...props} />)
        wrapper.setState({ "login": true })
        wrapper.setState({ "releaseUpdateDate": "abcd" })
        expect(wrapper.state("releaseUpdateDate")).toBeDefined()
    })

    it("should check if moduleType exists", () => {
        const wrapper = shallow(<ContentDisplay {...props} />)
        wrapper.setState({ "moduleType": "" })
        wrapper.setState({ "moduleType": "module" })
        expect(wrapper.state("moduleType")).toBeDefined()
    })

    it("should have a Title", () => {
        const wrapper = shallow(<ContentDisplay {...props} />)
        wrapper.setState({ "login": true })
        wrapper.setState({ "moduleTitle": "test title" })

        const sourceText = wrapper.find(Title).first().html()
        // ensure it matches what is expected
        expect(wrapper.state("moduleTitle")).toBeDefined()
        expect(sourceText).toContain("test title")
    })

    it("should have a Text component of TextVariant.small", () => {
        const wrapper = shallow(<ContentDisplay {...props} />)
        wrapper.setState({ "login": true })
        expect(wrapper.find("[component='small']").exists());
    })

    it("should have a versionUUID", () => {
        const wrapper = shallow(<ContentDisplay {...props} />)
        wrapper.setState({ "login": true })
        wrapper.setState({ "versionUUID": "122234-1234-1234T" })
        expect(wrapper.state("versionUUID")).toBeDefined()
    })

    it("should have a productValue", () => {
        const wrapper = shallow(<ContentDisplay {...props} />)
        wrapper.setState({ "login": true })
        const len = wrapper.setState({ "productValue": "Red Hat Enterprise Linux" })
        expect(wrapper.state("productValue")).toBeDefined()
    })

    it("should have a versionValue", () => {
        const wrapper = shallow(<ContentDisplay {...props} />)
        wrapper.setState({ "login": true })
        const len = wrapper.setState({ "versionValue": "8.x" })
        expect(wrapper.state("versionValue")).toBeDefined()
    })

    it("renders Copy permanent URL", () => {
        const wrapper = shallow(<ContentDisplay {...props} />)
        wrapper.setState({ "login": true })
        wrapper.setState({ "variantUUID": "somepath" })
        const permanentURL = wrapper.find("a#permanentURL").first()
        expect(permanentURL.exists).toBeTruthy()
    })

    it("renders copySuccess Message", () => {
        const wrapper = shallow(<ContentDisplay {...props} />)
        wrapper.setState({ "login": true })
        wrapper.setState({ "releasePath": "somepath" })
        wrapper.setState({ "variantUUID": "1234" })
        wrapper.setState({ "copySuccess": "Copied!" })
        expect(wrapper.state("copySuccess")).toContain("Copied!")
    })

    test("copyToClipboard click event", () => {
        const wrapper = shallow(<ContentDisplay {...props} />)
        const instance = wrapper.instance()
        const spy = sinon.spy(instance, "copyToClipboard")

        wrapper.setState({ variantUUID: "1234", releasePath: "yarn" })
        expect(wrapper.find("#permanentURL").exists())
    })

    it("test mouseLeave function", () => {
        const wrapper = renderer.create(<ContentDisplay {...props} />)
        const inst = wrapper.getInstance()
        expect(inst.mouseLeave()).toMatchSnapshot()
    })

    it("test fetch api call for portalUrl", async () => {
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
            const wrapper = await shallow(<ContentDisplay {...props} />)
            await wrapper.update()
            expect(wrapper.state("portalHost")).toBe("https://example.com")
        })
    })
})