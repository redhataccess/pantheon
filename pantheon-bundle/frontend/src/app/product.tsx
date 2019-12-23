import React, { Component } from 'react'
import { Bullseye, Button, Alert, AlertActionCloseButton, Form, FormGroup, TextInput, ActionGroup } from '@patternfly/react-core'
import '@app/app.css'
import { Redirect } from 'react-router-dom'

interface IState {
  failedPost: boolean
  isDup: boolean
  isMissingFields: boolean
  productDescription: string
  productName: string
  redirect: boolean
}

class Product extends Component<any, IState> {
  constructor(props) {
    super(props)
    this.state = {
      failedPost: false,
      isDup: false,
      isMissingFields: false,
      productDescription: '',
      productName: '',
      redirect: false
    }

  }

  // render method transforms the react components into DOM nodes for the browser.
  public render() {
    return (
      <React.Fragment>
        {/* Bullseye makes sure everyhting is in the middle */}
        <Bullseye>
          <Form>
            <div className="app-container">
              <div>
                {this.state.isMissingFields && 
                  <div className="notification-container">
                    <Alert
                      variant="warning"
                      title="A Product name is required."
                      action={<AlertActionCloseButton onClose={this.dismissNotification} />}
                    />
                  </div>
                }
                {this.state.isDup && 
                  <div className="notification-container">
                    <Alert
                      variant="warning"
                      title="Duplicated Product name."
                      action={<AlertActionCloseButton onClose={this.dismissNotification} />}
                    />
                  </div>
                }
                {this.state.failedPost &&
                  <div className="notification-container">
                    <Alert
                      variant="danger"
                      title="Failed to create product."
                      action={<AlertActionCloseButton onClose={this.dismissNotification} />}
                    >
                    Please check if you are logged in as a publisher.
                    </Alert>
                  </div>
                }
                <FormGroup
                  label="Product Name"
                  isRequired={true}
                  fieldId="product-name"
                >
                  <TextInput isRequired={true} id="product-name" type="text" placeholder="Product Name" value={this.state.productName} onChange={this.handleNameInput} />
                </FormGroup>
                <br />
                <FormGroup
                  label="Product Description"
                  fieldId="product-description"
                >
                  <TextInput id="product-description" type="text" placeholder="Product Description" value={this.state.productDescription} onChange={this.handleProductInput} />
                </FormGroup>
                <br />
                <ActionGroup>
                  <Button aria-label="Creates a new Product Name with Description specified." onClick={this.saveProduct}>Save</Button>
                  <div>
                    {this.renderRedirect()}
                  </div>
                </ActionGroup>
              </div>
            </div>
          </Form>
        </Bullseye>
      </React.Fragment>
    )
  }
  // methods that handle the state changes.
  private handleNameInput = productName => {
    this.setState({ productName })
  }

  private handleProductInput = productDescription => {
    this.setState({ productDescription })
    // console.log("Desc " + productDescription)
  }

  private saveProduct = () => {
    if (this.state.productName === '') {
      this.setState({ isMissingFields: true })
    } else {
      this.productExist().then(exist => {
        if (!exist) {
          const hdrs = {
            'Accept': 'application/json',
            'cache-control': 'no-cache'
          }
          // setup url fragment
          const urlFragment = this.state.productName.toString().toLowerCase().replace(/[^A-Z0-9]+/ig, "_")
          const formData = new FormData()
          formData.append("name", this.state.productName)
          formData.append("description", this.state.productDescription)
          formData.append("sling:resourceType", "pantheon/product")
          formData.append("jcr:primaryType", 'pant:product')
          // currently we don't translate products in Customer Portal.
          formData.append("locale", "en-US")
          formData.append("url", urlFragment)
          // fetch makes the request to create a new product.
          // transfor productName to lower case and replace special chars with _.
          fetch(encodeURI('/content/products/' + urlFragment), {
            body: formData,
            headers: hdrs,
            method: 'post'
          }).then(response => {
            if (response.status === 201 || response.status === 200) {
              this.setState({ redirect: true })
            } else {
              this.setState({ failedPost: true })
            }
          })
        }
      }
      )
    }
  }

  private renderRedirect = () => {
    if (this.state.redirect) {
      return <Redirect to='/products' />
    } else {
      return ""
    }
  }

  private dismissNotification = () => {
    this.setState({ isMissingFields: false, isDup: false })
  }

  private productExist = () => {
    let exists = false
    const backend = '/content/products/' + this.state.productName.toString().toLowerCase().replace(/[^A-Z0-9]+/ig, "_") + '.json'
    return fetch(backend)
      .then(response => {
        if (response.status === 200) {
          exists = true
          this.setState({ isDup: true })
        }
      }).then(() => exists)
  }
}

export { Product }