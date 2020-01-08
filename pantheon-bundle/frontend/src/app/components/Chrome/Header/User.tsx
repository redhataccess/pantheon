import React, { Component } from 'react'
import { Link } from "react-router-dom"
import {
    Dropdown,
    DropdownToggle,
    DropdownItem,
    DropdownSeparator,
    DropdownPosition,
    DropdownDirection,
    KebabToggle
  } from '@patternfly/react-core'
import { HelpIcon } from '@patternfly/react-icons'

import { IAppState } from '@app/app'

class User extends Component<IAppState, any> {
    constructor(props) {
        super(props)
        this.state = {
          isOpen: false
        }
      }


      onToggle = isOpen => {
        this.setState({
          isOpen
        })
      }
      onSelect = event => {
        this.setState({
          isOpen: !this.state.isOpen
        })
      }

    public render() {
        const dropdownItems = [
            <DropdownItem key="link">Link</DropdownItem>,
            <DropdownItem key="action" component="button">
              Action
            </DropdownItem>,
            <DropdownItem key="disabled link" isDisabled>
              Disabled Link
            </DropdownItem>,
            <DropdownItem key="disabled action" isDisabled component="button">
              Disabled Action
            </DropdownItem>,
            <DropdownSeparator key="separator" />,
            <DropdownItem key="separated link">Separated Link</DropdownItem>,
            <DropdownItem key="separated action" component="button">
              Separated Action
            </DropdownItem>
          ]
        return (
            <React.Fragment>
                <Dropdown
                    onSelect={this.onSelect}
                    toggle={
                    <DropdownToggle iconComponent={null} onToggle={this.onToggle} aria-label="Applications" id="toggle-id-7">
                        <HelpIcon />
                    </DropdownToggle>
                    }
                    isOpen={this.state.isOpen}
                    isPlain
                    dropdownItems={dropdownItems}
                />
                <Link to={this.props.userAuthenticated ? '' : '/login'}
                    onClick={this.conditionalRedirect}>
                    {this.props.userAuthenticated ? 'Log Out [' + this.props.username + ']' : 'Log In'}
                </Link>
            </React.Fragment>
        )
    }

    private conditionalRedirect = () => {
        if (this.props.userAuthenticated) {
            fetch('/system/sling/logout')
                .then(response => window.location.href = "/pantheon")
        }
    }
}

export { User }