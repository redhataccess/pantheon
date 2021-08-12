import React from "react";
import { mount } from "enzyme";
import { ErrorBoundary } from "./ErrorBoundary"


const Something = () => null;

describe('ErrorBoundary', () => {
  it('should display an ErrorMessage if wrapped component throws', () => {
    const wrapper = mount(
      <ErrorBoundary hasError={true}>
        <Something />
      </ErrorBoundary>
    );

    const error = new Error('test');

    wrapper.find(Something).simulateError(error);
  })
})