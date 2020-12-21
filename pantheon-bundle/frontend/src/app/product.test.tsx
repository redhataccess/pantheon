import React from "react"
import { Product } from "@app/product"
import "@app/fetchMock"

import { mount, shallow } from "enzyme"
import { TextInput, FormGroup, Button, Alert } from "@patternfly/react-core"
import renderer from "react-test-renderer"
import { spy } from 'sinon';

describe("Product tests", () => {
  test("should render Product component", () => {
    const view = shallow(<Product />)
    expect(view).toMatchSnapshot()
  })

  it("should render a form group", () => {
    const wrapper = mount(<Product />)
    const formGroup = wrapper.find(FormGroup)
    expect(formGroup.exists()).toBe(true)
  })

  it("should render a text input", () => {
    const wrapper = mount(<Product />)
    const textInput = wrapper.find(TextInput)
    expect(textInput.exists()).toBe(true)
  })

  it("should render a Button", () => {
    const wrapper = mount(<Product />)
    const button = wrapper.find(Button)
    expect(button.exists()).toBe(true)
  })
  // expect(counter.find('p').text()).toBe('Counter value is: 0');
  // const incButton = counter.find('button');
  // incButton.simulate('click');
  // expect(counter.find('p').text()).toBe('Counter value is: 1');


  // it.only("test renderRedirect function", () => {
    // const wrapper = shallow(<Product />)
    it("test handleNameInput function", () => {
      // const container = mount(<Product />)
      const container = shallow(<Product />)
      const searchInput = container.find('#product_name_text')

      // const searchInput = container.find('#product_name_text').at(2)
      console.log('input b4 simulate change', searchInput.debug())
      // searchInput.simulate('change', {
      //   target: {
      //     value: 'test_name',
      //   },
      // });
      searchInput.simulate('change', 'test_name');
      container.update()
      // console.log('input after simulate change', container.find('#product_name_text').at(1).html())
      console.log('input after simulate change', container.find('#product_name_text').html())
      expect(container.find('#product_name_text').prop('value')).toEqual(
        'test_name',
      );
      // expect(container.find('#product_name_text').at(1).prop('value')).toEqual(
      //   'test_name',
      // );
  
  })
  it("test handleNameInput function", () => {
    const container = shallow(<Product />)
    const productInput = container.find('#product_name_text')
    productInput.simulate('change', 'test_name');
    container.update()
    expect(container.find('#product_name_text').prop('value')).toEqual(
      'test_name',
    );
  })
  it("test handleProductInput function", () => {
    const container = shallow(<Product />)
    const productInput = container.find('#product_description_text')
    productInput.simulate('change', 'test_prod_descrip');
    container.update()
    expect(container.find('#product_description_text').prop('value')).toEqual(
      'test_prod_descrip',
    );
  })

  it("test handleUrlInput function - valid url provided", () => {
    const container = shallow(<Product />)
    const productInput = container.find('#product_url_fragment_text')
    productInput.simulate('change', 'test_prod_url');
    container.update()
    expect(container.find('#product_url_fragment_text').prop('value')).toEqual(
      'test_prod_url',
    );
  })
  it("test handleUrlInput function - invalid url provided", () => {
    const container = shallow(<Product />)
    const productInput = container.find('#product_url_fragment_text')
    productInput.simulate('change', '');
    container.update()
    expect(container.find('#product_url_fragment_text').prop('value')).toEqual(
      '',
    );
  })
  it("test handleTextInputChange function", () => {
    const container = shallow(<Product />)
    const productInput = container.find('#new_version_name_text')
    productInput.simulate('change', 'test_version_name');
    container.update()
    expect(container.find('#new_version_name_text').prop('value')).toEqual(
      'test_version_name',
    );
  })
  it("test handleUrlInputChange function - valid URL", () => {
    const container = shallow(<Product />)
    const productInput = container.find('#new_version_url_fragment')
    productInput.simulate('change', 'test_version_url');
    container.update()
    expect(container.find('#new_version_url_fragment').prop('value')).toEqual(
      'test_version_url',
    );
  })
  it("test handleUrlInputChange function - invalid URL", () => {
    const container = shallow(<Product />)
    const productInput = container.find('#new_version_url_fragment')
    productInput.simulate('change', '');
    container.update()
    expect(container.find('#new_version_url_fragment').prop('value')).toEqual(
      '',
    );
  })

  it('should fail if no credentials are provided', () => {
    const onSubmit = spy();
const wrapper = mount(
    <Product onSubmit={onSubmit} />
);
const button = wrapper.find('#form-submit-button').at(0);
button.simulate('submit');
console.log(wrapper.debug())
});

  // it("test loginRedirect function", () => {
  //   const wrapper = renderer.create(<Product />)
  //   const inst = wrapper.getInstance()
  //   expect(inst.loginRedirect).toMatchSnapshot()
  // })
 
  // it("test checkAuth function", () => {
  //   const wrapper = renderer.create(<Product />)
  //   const inst = wrapper.getInstance()
  //   expect(inst.checkAuth).toMatchSnapshot()
  // })
  
  // it("test dismissNotification function", () => {
  //   const wrapper = renderer.create(<Product />)
  //   const inst = wrapper.getInstance()
  //   expect(inst.dismissNotification).toMatchSnapshot()
  // })

  // it("test productExist function", () => {
  //   const wrapper = renderer.create(<Product />)
  //   const inst = wrapper.getInstance()
  //   expect(inst.productExist("Red Hat Enterprise Linux")).toMatchSnapshot()
  // })

  // it("test handleNameInput function", () => {
  //   const wrapper = renderer.create(<Product />)
  //   const inst = wrapper.getInstance()
  //   expect(inst.handleNameInput("Red Hat Enterprise Linux")).toMatchSnapshot()
  // })

  // it("test handleProductInput function", () => {
  //   const wrapper = renderer.create(<Product />)
  //   const inst = wrapper.getInstance()
  //   expect(inst.handleProductInput("Linux Platform")).toMatchSnapshot()
  // })

  // it("test handleUrlInput function", () => {
  //   const wrapper = renderer.create(<Product />)
  //   const inst = wrapper.getInstance()
  //   expect(inst.handleUrlInput("red_hat")).toMatchSnapshot()
  // })
  // it("test handleTextInputChange function", () => {
  //   const wrapper = renderer.create(<Product />)
  //   const inst = wrapper.getInstance()
  //   expect(inst.handleTextInputChange("1")).toMatchSnapshot()
  // })
  // it("test handleUrlInputChange function", () => {
  //   const wrapper = renderer.create(<Product />)
  //   const inst = wrapper.getInstance()
  //   expect(inst.handleUrlInputChange("test_url")).toMatchSnapshot()
  // })
  // it("test saveVersion function", () => {
  //   const wrapper = renderer.create(<Product />)
  //   const inst = wrapper.getInstance()
  //   expect(inst.saveVersion).toMatchSnapshot()
  // })
  // it("test createVersionsPath function", () => {
  //   const wrapper = renderer.create(<Product />)
  //   const inst = wrapper.getInstance()
  //   expect(inst.createVersionsPath).toMatchSnapshot()
  // })
})
