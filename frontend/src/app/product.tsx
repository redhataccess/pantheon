import React, { Component } from 'react';
<<<<<<< HEAD
import { Bullseye, Button, Alert, AlertActionCloseButton, Form, FormGroup, TextInput,TextArea, ActionGroup } from '@patternfly/react-core';
=======
import { Bullseye, Button, Alert, AlertActionCloseButton, Form, FormGroup, TextInput, ActionGroup } from '@patternfly/react-core';
>>>>>>> master
import '@app/app.css';
import { Redirect } from 'react-router-dom'

class Product extends Component {
  public state = {
    failedPost: false,
<<<<<<< HEAD
=======
    formInvalid: false,
    isDup: false,
>>>>>>> master
    isMissingFields: false,
    login: false,
    productDescription: '',
    productName: '',
<<<<<<< HEAD
    redirect: false
  };
  
  // render method transforms the react components into DOM nodes for the browser.
  public render() {
    const { productName, productDescription, isMissingFields } = this.state;
=======
    redirect: false,
    results: [],
  };

  // render method transforms the react components into DOM nodes for the browser.
  public render() {
    const { productName, productDescription, isMissingFields, isDup } = this.state;
>>>>>>> master
    return (
      <React.Fragment>
        {/* Bullseye makes sure everyhting is in the middle */}
        <Bullseye>
          <Form>
<<<<<<< HEAD
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
=======
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
                {isDup && (
                  <div className="notification-container">
                    <Alert
                      variant="warning"
                      title="Duplicated Product name."
                      action={<AlertActionCloseButton onClose={this.dismissNotification} />}
                    />
                  </div>
                )}
                <FormGroup
                  label="Product Name"
                  isRequired={true}
                  fieldId="product-name"
                >
                  <TextInput isRequired={true} id="product-name" type="text" placeholder="Product Name" value={productName} onChange={this.handleNameInput} />
                </FormGroup>
                <br />
                <FormGroup
                  label="Product Description"
                  fieldId="product-description"
                >
                  <TextInput id="product-description" type="text" placeholder="Product Description" value={productDescription} onChange={this.handleProductInput} />
                </FormGroup>
                <br />
                <ActionGroup>
                  <Button aria-label="Creates a new Product Name with Description specified." onClick={this.saveProduct} disabled={this.state.formInvalid}>Save</Button>
                  <div>
                    {this.checkAuth()}
                    {this.loginRedirect()}
                    {this.renderRedirect()}
                  </div>
                </ActionGroup>
              </div>
            </div>
>>>>>>> master
          </Form>
        </Bullseye>
      </React.Fragment>
    );
  }
<<<<<<< HEAD
  //methods that handle the state changes.
  private handleNameInput = productName => {
    this.setState({ productName });
    console.log("Name " + productName)

=======
  // methods that handle the state changes.
  private handleNameInput = productName => {
    this.setState({ productName });

    // check for duplcated product name.
    this.productExist(this.state.productName);
    if (this.state.isDup) {
      this.setState({ formInvalid: true });
    }
>>>>>>> master
  };

  private handleProductInput = productDescription => {
    this.setState({ productDescription });
<<<<<<< HEAD
    console.log("Desc " + productDescription)
  };

  private saveProduct = (postBody) => {
    console.log("My data is: " + this.state.productName + " and my desc is " + this.state.productDescription)
    if (this.state.productName === "" ) {
      this.setState({ isMissingFields: true })
=======
    // console.log("Desc " + productDescription)

    // check for duplcated product name.
    this.productExist(this.state.productName);
    if (this.state.isDup) {
      this.setState({ formInvalid: true });
    }
  };

  private saveProduct = (postBody) => {
    if (this.state.productName === "") {
      this.setState({ isMissingFields: true })
      this.setState({ formInvalid: true })

    } else if (this.productExist(this.state.productName)) {
      this.setState({ isDup: true })
      this.setState({ formInvalid: true })
>>>>>>> master
    } else {
      const hdrs = {
        'Accept': 'application/json',
        'cache-control': 'no-cache'
      }
<<<<<<< HEAD
      
      // setup url fragment
      let url_fragment = this.state.productName.toString().toLowerCase().replace(/[^A-Z0-9]+/ig, "_");
      
=======

      // setup url fragment
      const urlFragment = this.state.productName.toString().toLowerCase().replace(/[^A-Z0-9]+/ig, "_");

>>>>>>> master
      const formData = new FormData();
      formData.append("name", this.state.productName)
      formData.append("description", this.state.productDescription)
      formData.append("sling:resourceType", "pantheon/product")
      formData.append("jcr:primaryType", 'pant:product')
      // currently we don't translate products in Customer Portal.
      formData.append("locale", "en-US")
<<<<<<< HEAD
      formData.append("url", url_fragment)
      // fetch makes the request to create a new product.
      // transfor productName to lower case and replace special chars with _.
      fetch('/content/products/' + url_fragment, {
=======
      formData.append("url", urlFragment)
      // fetch makes the request to create a new product.
      // transfor productName to lower case and replace special chars with _.
      fetch('/content/products/' + urlFragment, {
>>>>>>> master
        body: formData,
        headers: hdrs,
        method: 'post'
      }).then(response => {
        if (response.status === 201 || response.status === 200) {
<<<<<<< HEAD
          console.log(" Works " + response.status)
          this.setState({ redirect: true })
        } else if (response.status === 500) {
          console.log(" Needs login " + response.status)
=======
          this.setState({ redirect: true })
        } else if (response.status === 500) {
          // console.log(" Needs login " + response.status)
>>>>>>> master
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
<<<<<<< HEAD
      return <Redirect to='/' />
=======
      return <Redirect to='/products' />
>>>>>>> master
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
<<<<<<< HEAD
  
  private dismissNotification = () => {
    this.setState({ isMissingFields: false });
  };
=======

  private dismissNotification = () => {
    if (this.state.isMissingFields === true) {
      this.setState({ isMissingFields: false });
    }

    if (this.productExist(this.state.productName) === false) {
      this.setState({ isDup: false });
    }
  }

  private productExist = (productName) => {
    this.setState({ initialLoad: false })
    fetch(this.getProductsUrl(productName))
      .then(response => response.json())
      .then(responseJSON => this.setState({ results: responseJSON.results }))
      .then(() => {
        // console.log("[productExist] results breakdown " + JSON.stringify(this.state.results))

        if (JSON.stringify(this.state.results) === "[]") {
          this.setState({
            isDup: false
          });
        } else {
          this.setState({
            isDup: true
          });
        }
      })
    return this.state.isDup
  }

  private getProductsUrl = (productName) => {
    const backend = '/content/products.query.json?nodeType=pant:product&where=[name]="' + productName + '"'
    return backend
  }
>>>>>>> master

}

export { Product }