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
    loginUrl: string
    logoutUrl: string
}

class User extends Component<IAppState, IState> {
    constructor(props) {
        super(props)
        this.state = {
            helpDropdownOpen: false,
            loginUrl: "/auth/login",
            logoutUrl: "/system/sling/logout"
        }
    }

    public render() {
        const dropdownItems = [
            <DropdownItem key="help" href="/pantheon/docs/assemblies/assembly-pantheon-help.html" target="_blank">Help</DropdownItem>,
            <DropdownItem key="contribute" href="https://github.com/redhataccess/pantheon" target="_blank">Contribute to Pantheon</DropdownItem>
        ]
        return (
            <React.Fragment>
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
                    href={this.props.userAuthenticated ? this.state.logoutUrl : this.state.loginUrl}
                    onClick={this.conditionalRedirect}>
                    {this.props.userAuthenticated ? "[" + this.props.username + "]" : "Log In"}
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

    private conditionalRedirect = () => {
        if (this.props.userAuthenticated) {
            fetch(this.state.logoutUrl)
                .then(response => window.location.href = "/pantheon")
        }
    }
}

export { User }