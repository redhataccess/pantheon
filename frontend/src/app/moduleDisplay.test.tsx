import React from 'react';
import { ModuleDisplay } from '@app/moduleDisplay';
import "isomorphic-fetch"

import { mount, shallow } from 'enzyme';
import { Breadcrumb, Button, Card, DataList, DataListItem, DataListItemCells, DataListItemRow, FormGroup, FormSelect, Modal, TextInput } from '@patternfly/react-core';
import renderer from 'react-test-renderer';
import sinon from 'sinon'

const props = {
    moduleName: "Red Hat Enterprise Linux",
    modulePath: "module/red_hat_enterprise_linux",
    moduleType: "REFERENCE",
    updated: "Tue Aug 27 2019 11:28:26 GMT-0400"
}

describe('ModuleDisplay tests', () => {
    test('should render ModuleDisplay component', () => {
        const view = shallow(<ModuleDisplay {...props} />);
        expect(view).toMatchSnapshot();
    });

    it('should render a Breadcrumb', () => {
        const wrapper = mount(<ModuleDisplay {...props} />);
        const breadcrum = wrapper.find(Breadcrumb);
        expect(breadcrum.exists()).toBe(true)
    });

    it('should render a Button', () => {
        const wrapper = mount(<ModuleDisplay {...props} />);
        const button = wrapper.find(Button);
        expect(button.exists()).toBe(true)
    });

    it('should render a Card', () => {
        const wrapper = mount(<ModuleDisplay {...props} />);
        const card = wrapper.find(Card);
        expect(card.exists()).toBe(true)
    });

    it('should render a Data List', () => {
        const wrapper = mount(<ModuleDisplay {...props} />);
        const dataList = wrapper.find(DataList);
        expect(dataList.exists()).toBe(true)
    });

    it('should render a DataListItem', () => {
        const wrapper = mount(<ModuleDisplay {...props} />);
        const dataListItem = wrapper.find(DataListItem);
        expect(dataListItem.exists()).toBe(true)
    });

    it('should render a DataListItemCells Element', () => {
        const wrapper = mount(<ModuleDisplay {...props} />);
        const dataListItemCells = wrapper.find(DataListItemCells);
        expect(dataListItemCells.exists()).toBe(true)
    });

    it('should render a DataListItemRow element', () => {
        const wrapper = mount(<ModuleDisplay {...props} />);
        const dataListItemRow = wrapper.find(DataListItemRow);
        expect(dataListItemRow.exists()).toBe(true)
    });

    // it('should render a form group', () => {
    //     const wrapper = mount(<ModuleDisplay {...props} />);
    //     const formGroup = wrapper.find(FormGroup);
    //     expect(formGroup.exists()).toBe(true)
    // });

    it('should render a Modal', () => {
        const wrapper = mount(<ModuleDisplay {...props} />);
        const modal = wrapper.find(Modal);
        expect(modal.exists()).toBe(true)
    });

    // it('should render a FormSelect Element', () => {
    //     const wrapper = mount(<ModuleDisplay {...props} />);
    //     wrapper.setState({moduleName: "test", modulePath: "modules/test", login: true})
    //     const formSelect = wrapper.find(FormSelect);
    //     expect(formSelect.exists()).toBe(true)
    // });

    // it('should render a text input', () => {
    //     const wrapper = mount(<ModuleDisplay {...props} />);
    //     wrapper.setState({moduleName: "test", modulePath: "modules/test"})
    //     const textInput = wrapper.find(TextInput);
    //     expect(textInput.exists()).toBe(true)
    // });

    // it('test onChangeProduct function', () => {
    //     const wrapper = renderer.create(<ModuleDisplay {...props} />);
    //     wrapper.setState({moduleName: "test", modulePath: "modules/test"})
    //     const inst = wrapper.getInstance();
    //     expect(inst.onChangeProduct("testProduct", event)).toMatchSnapshot();
    // });

    // it('test onChangeVersion function', () => {
    //     const wrapper = renderer.create(<ModuleDisplay {...props} />);
    //     wrapper.setState({moduleName: "test", modulePath: "modules/test"})
    //     const inst = wrapper.getInstance();
    //     expect(inst.onChangeVersion("testVersion", event)).toMatchSnapshot();
    // });

    // it('test onChangeUsecase function', () => {
    //     const wrapper = renderer.create(<ModuleDisplay {...props} />);
    //     wrapper.setState({moduleName: "test", modulePath: "modules/test"})
    //     const inst = wrapper.getInstance();
    //     expect(inst.onChangeUsecase("testUsecase", event)).toMatchSnapshot();
    // });

    // it('test handleURLInput function', () => {
    //     const wrapper = renderer.create(<ModuleDisplay {...props} />);
    //     wrapper.setState({moduleName: "test", modulePath: "modules/test"})
    //     const inst = wrapper.getInstance();
    //     expect(inst.handleURLInput("testURL")).toMatchSnapshot();
    // });

    // it('test productUrlExist function', () => {
    //     const wrapper = renderer.create(<ModuleDisplay {...props} />);
    //     wrapper.setState({moduleName: "test", modulePath: "modules/test"})
    //     const inst = wrapper.getInstance();
    //     expect(inst.productUrlExist("testURL")).toMatchSnapshot();
    // });

    // it('test getProductUrl function', () => {
    //     const wrapper = renderer.create(<ModuleDisplay {...props} />);
    //     wrapper.setState({moduleName: "test", modulePath: "modules/test"})
    //     const inst = wrapper.getInstance();
    //     expect(inst.getProductUrl("testurl")).toMatchSnapshot();
    // });

    // it('test fetchProductVersionDetails function', () => {
    //     const wrapper = renderer.create(<ModuleDisplay {...props} />);
    //     wrapper.setState({moduleName: "test", modulePath: "modules/test"})
    //     const inst = wrapper.getInstance();
    //     expect(inst.fetchProductVersionDetails()).toMatchSnapshot();
    // });
    
    // it('test handleModalToggle function', () => {
    //     const wrapper = renderer.create(<ModuleDisplay {...props} />);
    //     const inst = wrapper.getInstance();
    //     expect(inst.handleModalToggle()).toMatchSnapshot();
    // });

    // it('test loginRedirect function', () => {
    //     const wrapper = renderer.create(<ModuleDisplay {...props} />);
    //     const inst = wrapper.getInstance();
    //     expect(inst.loginRedirect).toMatchSnapshot();
    // });

    // it('test checkAuth function', () => {
    //     const wrapper = renderer.create(<ModuleDisplay {...props} />);
    //     const inst = wrapper.getInstance();
    //     expect(inst.checkAuth).toMatchSnapshot();
    // });

    // it('should handle state changes for login', () => {
    //     const wrapper = shallow(<ModuleDisplay {...props} />)

    //     expect(wrapper.state('login')).toBe(false)
    //     wrapper.setState({ 'login': true })
    //     expect(wrapper.state('login')).toBe(true)
    // });

});