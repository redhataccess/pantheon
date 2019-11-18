import React, { Component } from 'react';
import {
  Button, ButtonVariant, TextInput, InputGroup
} from '@patternfly/react-core';
import '@app/app.css';
import { SearchIcon, SortAlphaDownIcon, SortAlphaUpIcon } from '@patternfly/react-icons';

export default class SearchFilter extends Component<any, any> {
  constructor(props) {
    super(props);
    this.state = {};
  }

  public render() {
    const {} = this.state;
    return (
      <React.Fragment>
        <div className="row-filter" >
          <InputGroup className="small-margin">
            <TextInput id="searchFilterInput" type="text" onKeyDown={this.props.onKeyDown} value={this.props.value} onChange={this.props.onChange} />
            <Button onClick={this.props.onClick} variant={ButtonVariant.control} aria-label="search button for search input">
              <SearchIcon />
            </Button>
          </InputGroup>

          <Button onClick={this.props.onSort} variant={ButtonVariant.control} aria-label="search button for search input">
          {this.props.isSortedUp ? <SortAlphaDownIcon /> : <SortAlphaUpIcon /> }
          </Button>
        </div>
      </React.Fragment>
    );
  }

}

export { SearchFilter }; 