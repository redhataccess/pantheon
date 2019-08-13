import React, { Component } from 'react';
import { Bullseye, Button, Alert, AlertActionCloseButton, Form, FormGroup, TextInput,TextArea, ActionGroup } from '@patternfly/react-core';
import '@app/app.css';
import { Redirect } from 'react-router-dom'

class Product extends Component {
  public state = {
    failedPost: false,
    isMissingFields: false,
    login: false,
    productDescription: '',
    productName: '',
    redirect: false
  };
  
  // render method transforms the react components into DOM nodes for the browser.
  public render() {
    const { productName, productDescription, isMissingFields } = this.state;
    return (
      <React.Fragment>
        {/* Bullseye makes sure everyhting is in the middle */}
        <Bullseye>
          <Form>
          <div className="app-container">
            <div>
              {isMissingFields && (
                <div className="notification-container">
                  <Alert
                    variant="warning"
                    title="A Product name is required."
                    action={<AlertActionCloseButton onClose={this.dismissNotification} />}
                  />
                </div>
              )}

              <FormGroup
                label="Product Name"
                isRequired
                fieldId="product-name"
              >
                <TextInput isRequired id="product-name" type="text" placeholder="Product Name" value={productName} onChange={this.handleNameInput} />
              </FormGroup>
              
              <FormGroup
                label="Product Description"
                fieldId="product-description"
              >
                <TextInput id="product-description" type="text" placeholder="Product Description" value={productDescription} onChange={this.handleProductInput} />
              </FormGroup>
              
              <ActionGroup>
              <Button aria-label="Creates a new Product Name with Description specified." onClick={this.saveProduct}>Save</Button>
              <div>
                {this.checkAuth()}
                {this.loginRedirect()}
                {this.renderRedirect()}
              </div>
              </ActionGroup>
            </div>
          </div>
          </Form>
        </Bullseye>
      </React.Fragment>
    );
  }
  //methods that handle the state changes.
  private handleNameInput = productName => {
    this.setState({ productName });
    console.log("Name " + productName)

  };

  private handleProductInput = productDescription => {
    this.setState({ productDescription });
    console.log("Desc " + productDescription)
  };

  private saveProduct = (postBody) => {
    console.log("My data is: " + this.state.productName + " and my desc is " + this.state.productDescription)
    if (this.state.productName === "" ) {
      this.setState({ isMissingFields: true })
    } else {
      const hdrs = {
        'Accept': 'application/json',
        'cache-control': 'no-cache'
      }
      
      // setup url fragment
      let url_fragment = this.state.productName.toString().toLowerCase().replace(/[^A-Z0-9]+/ig, "_");
      
      const formData = new FormData();
      formData.append("name", this.state.productName)
      formData.append("description", this.state.productDescription)
      formData.append("sling:resourceType", "pantheon/product")
      formData.append("jcr:primaryType", 'pant:product')
      // currently we don't translate products in Customer Portal.
      formData.append("locale", "en-US")
      formData.append("url", url_fragment)
      // fetch makes the request to create a new product.
      // transfor productName to lower case and replace special chars with _.
      fetch('/content/products/' + url_fragment, {
        body: formData,
        headers: hdrs,
        method: 'post'
      }).then(response => {
        if (response.status === 201 || response.status === 200) {
          console.log(" Works " + response.status)
          this.setState({ redirect: true })
        } else if (response.status === 500) {
          console.log(" Needs login " + response.status)
          this.setState({ login: true })
        } else {
          console.log(" Failed " + response.status)
          this.setState({ failedPost: true })
        }
      });
    }
  }

  private renderRedirect = () => {
    if (this.state.redirect) {
      return <Redirect to='/' />
    } else {
      return ""
    }
  }

  private loginRedirect = () => {
    if (this.state.login) {
      return <Redirect to='/login' />
    } else {
      return ""
    }
  }

  private checkAuth = () => {
    fetch("/system/sling/info.sessionInfo.json")
      .then(response => response.json())
      .then(responseJSON => {
        const key = "userID"
        if (responseJSON[key] === 'anonymous') {
          this.setState({ login: true })
        }
      })
  }
  
  private dismissNotification = () => {
    this.setState({ isMissingFields: false });
  };

}

export { Product }