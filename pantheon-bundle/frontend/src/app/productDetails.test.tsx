import React from "react"
import "@app/fetchMock"
import { mount, shallow } from "enzyme"
import { Breadcrumb, Button, Form, FormGroup, Level, LevelItem, TextContent, TextInput, Text } from "@patternfly/react-core"
import { ProductDetails } from "@app/productDetails"
import { ProductContext, IProduct } from "./contexts/ProductContext"


const date = new Date();
const allProducts: IProduct[] = [{
  description: "descrip",
  isOpen: false,
  ["jcr:created"]: date,
  ['jcr:createdBy']: "admin",
  ['jcr:lastModified']: date,
  ['jcr:lastModifiedBy']: "admin",
  ['jcr:primaryType']: "pant:product",
  ['jcr:uuid']: "5df8d913-79b9-42fd-a17b-ffc917add446",
  locale: "en-US",
  name: "product 11/16",
  ['sling:resourceType']: "pantheon/product",
  urlFragment: "product_url",
}, {
  description: "test descrip",
  isOpen: false,
  ['jcr:created']: date,
  ['jcr:createdBy']: "admin",
  ['jcr:lastModified']: date,
  ['jcr:lastModifiedBy']: "admin",
  ['jcr:primaryType']: "pant:product",
  ['jcr:uuid']: "242d4187-d2f4-4df5-917b-e09bf4ff45e9",
  locale: "en-US",
  name: "test 11/17",
  ['sling:resourceType']: "pantheon/product",
  urlFragment: "testurl",
}];

const props =
{
  match:
  {
    params: {
      id: '5df8d913-79b9-42fd-a17b-ffc917add446'
    }
  }
}
const container = mount(<ProductContext.Provider value={allProducts}><ProductDetails {...props} /></ProductContext.Provider>)

describe("ProductDetails tests", () => {
  test("should render ProductDetails component", () => {
    const view = shallow(<ProductDetails {...props} />)
    expect(view).toMatchSnapshot()
  })

  it("should render a form", () => {
    const form = container.find(Form)
    expect(form.exists()).toBe(true)
  })

  it("should render a form group", () => {
    const formGroup = container.find(FormGroup)
    expect(formGroup.exists()).toBe(true)
  })

  it("should render a text input", () => {
    const textInput = container.find(TextInput)
    expect(textInput.exists()).toBe(true)
  })

  it("should render a Button", () => {
    const button = container.find(Button)
    expect(button.exists()).toBe(true)
  })

  it("should render a breadcrumb", () => {
    const breadCrumb = container.find(Breadcrumb)
    expect(breadCrumb.exists()).toBe(true)
  })

  it("should render a Level element", () => {
    const level = container.find(Level)
    expect(level.exists()).toBe(true)
  })

  it("should render a LevelItem", () => {
    const levelItem = container.find(LevelItem)
    expect(levelItem.exists()).toBe(true)
  })

  it("should render a TextContent element", () => {
    const textContent = container.find(TextContent)
    expect(textContent.exists()).toBe(true)
  })

  it("should render a Text element", () => {
    const text = container.find(Text)
    expect(text.exists()).toBe(true)
  })
})

describe('fetchProductVersions', () => {
  it('fetches data and returns a successful response', done => {
    const mockSuccessResponse = {};
    const mockJsonPromise = Promise.resolve(mockSuccessResponse);
    const mockFetchPromise = Promise.resolve({
      json: () => mockJsonPromise,
    });
    global.fetch = jest.fn().mockImplementation(() => mockFetchPromise);
    mount(<ProductContext.Provider value={allProducts}><ProductDetails {...props} /></ProductContext.Provider>)
    expect(global.fetch).toHaveBeenCalledTimes(1);
    expect(global.fetch).toHaveBeenCalledWith('/content/products/product_11_16/versions.2.json');
    done();
  });
});
