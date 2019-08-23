import React, { Component } from 'react';
import { Link } from "react-router-dom";

class BuildInfo extends Component {
    public state = {
        buildDate: '',
        commitHash: '',
        commitText: ''
    };

    public render() {
        this.getBuildInfo()
        return (
            <React.Fragment>
                <div className="column-view">
                    <Link to=""
                        onClick={this.commitRedirect}>
                        {this.state.buildDate}
                    </Link>
                </div>
            </React.Fragment>
        );
    }

    private getBuildInfo() {
        const backend = "/pantheon/builddate.json"
        if (this.state.buildDate === '') {
            fetch(backend)
                .then(response => response.json())
                .then(responseJSON => this.setState({ buildDate: "Built Date: " + responseJSON.buildDate, commitHash: responseJSON.commitHash }))
                .then(() => {
                    if (!(this.state.commitHash.includes("not set"))) {
                        this.setState({ commitText: 'commit hash' })
                    }
                })
        }
    };

    private commitRedirect = () => {
        window.location.href = "https://github.com/redhataccess/pantheon/commit/" + this.state.commitHash
    }
}

export { BuildInfo }