import React, { Component } from "react"
import { Link } from "react-router-dom"
import {
    Button, Dropdown,
    DropdownToggle,
    DropdownItem, Modal, DropdownPosition
} from "@patternfly/react-core"
import { HelpIcon } from "@patternfly/react-icons"
import "@app/app.css"
import { IAppState } from "@app/app"

interface IState {
    helpDropdownOpen: boolean
    placeholderDialogOpen: boolean
}

class User extends Component<IAppState, IState> {
    constructor(props) {
        super(props)
        this.state = {
            helpDropdownOpen: false,
            placeholderDialogOpen: false
        }
    }

    public render() {
        const dropdownItems = [
            <DropdownItem key="help" onClick={this.onPlaceholderShow}>Help</DropdownItem>,
            <DropdownItem key="contribute" href="https://github.com/redhataccess/pantheon" target="_blank">Contribute to Pantheon</DropdownItem>
        ]
        const placeHolderModalButtons = [
            <Button key="placeholderOk" onClick={this.onPlaceholderClose}>OK</Button>
        ]
        return (
            <React.Fragment>
                <Modal width={"60%"}
                        title="Placeholder dialog"
                        isOpen={this.state.placeholderDialogOpen}
                        onClose={this.onPlaceholderClose}
                        actions={placeHolderModalButtons}>
                    This feature has not yet been implemented.
                </Modal>
                <Dropdown onSelect={this.onHelpSelect}
                        toggle={
                            <DropdownToggle toggleIndicator={null} onToggle={this.onHelpToggle}>
                                <HelpIcon />
                            </DropdownToggle>
                        }
                        isPlain={true}
                        isOpen={this.state.helpDropdownOpen}
                        dropdownItems={dropdownItems}
                        position={DropdownPosition.right}
                />
                <a className="p2-header__login"
                        href={this.props.userAuthenticated ? "" : "https://sso.redhat.com/auth/realms/redhat-external/protocol/openid-connect/auth?client_id=pantheon&redirect_uri=https%3A%2F%2F" + window.location.hostname
                        +"&login=true&response_type=code&scope=openid"}
                        onClick={this.conditionalRedirect}>
                    {this.props.userAuthenticated ? "Log Out [" + this.props.username + "]" : "Log In"}
                </a>
            </React.Fragment>
        )
    }

    private onHelpToggle = helpDropdownOpen => {
        this.setState({ helpDropdownOpen })
    }

    private onHelpSelect = () => {
        this.setState({
            helpDropdownOpen: !this.state.helpDropdownOpen
        })
    }

    private onPlaceholderClose = () => {
        this.setState({ placeholderDialogOpen: false })
    }

    private onPlaceholderShow = () => {
        this.setState({ placeholderDialogOpen: true })
    }

    private conditionalRedirect = () => {
        if (this.props.userAuthenticated) {
            fetch("/system/sling/logout")
                .then(response => window.location.href = "/pantheon")
        }
    }
}

export { User }