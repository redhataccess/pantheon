import React, { Fragment } from 'react';
import { Level, LevelItem, Button } from '@patternfly/react-core';
import { ContextSelector, ContextSelectorItem } from '@patternfly/react-core';

export interface IProps {
  handleMoveLeft: () => any
  handleMoveRight: () => any
  handleMoveToFirst: () => any
  pageNumber: number
  nextPageRecordCount: number
  noOfRecordsOnPage: number
}

class Paginate extends React.Component<IProps> {

  public dropdownItems = [
    '10 items per page',
    '20 items per page',
    '50 items per page',
    '100 items per page',
  ];

  public state = {
    filteredItems: this.dropdownItems, 
    isOpen: false,
    searchValue: '',
    selected: this.dropdownItems[0]
  };

  public render() {
    const { isOpen, selected, searchValue, filteredItems } = this.state;

    return (
      <Fragment>
        <Level gutter="md">
          <LevelItem /><LevelItem />
          <LevelItem>
            <nav aria-label="Countries Pagination">
              <div>
                {this.props.pageNumber === 1 &&
                  <div className="example">
                    <div className="ws-preview">
                      <div className="pf-c-pagination" id="pagination-options-menu-top">
                      <div className="pf-c-dropdown">
                        <div className="pf-c-options-menu__toggle pf-m-plain pf-m-text">
                        Showing <ContextSelector
                          toggleText={selected}
                          onSearchInputChange={this.onSearchInputChange}
                          isOpen={isOpen}
                          searchInputValue={searchValue}
                          onToggle={this.onToggle}
                          onSelect={this.onSelect}
                          onSearchButtonClick={this.onSearchButtonClick}
                          screenReaderLabel="Selected Project:"
                        >
                          {filteredItems.map((item, index) => (
                            <ContextSelectorItem key={index}>{item}</ContextSelectorItem>
                          ))}
                        </ContextSelector>
                            <button disabled={true} data-action="first" aria-label="Go to first page" className="pf-c-button pf-m-plain" type="button" onClick={this.props.handleMoveToFirst}><svg fill="currentColor" height="1em" width="1em" viewBox="0 0 448 512" aria-hidden="true" role="img">
                              <path d="M223.7 239l136-136c9.4-9.4 24.6-9.4 33.9 0l22.6 22.6c9.4 9.4 9.4 24.6 0 33.9L319.9 256l96.4 96.4c9.4 9.4 9.4 24.6 0 33.9L393.7 409c-9.4 9.4-24.6 9.4-33.9 0l-136-136c-9.5-9.4-9.5-24.6-.1-34zm-192 34l136 136c9.4 9.4 24.6 9.4 33.9 0l22.6-22.6c9.4-9.4 9.4-24.6 0-33.9L127.9 256l96.4-96.4c9.4-9.4 9.4-24.6 0-33.9L201.7 103c-9.4-9.4-24.6-9.4-33.9 0l-136 136c-9.5 9.4-9.5 24.6-.1 34z" transform="" /></svg></button>
                            <button disabled={true} data-action="previous" aria-label="Go to previous page" className="pf-c-button pf-m-plain" type="button" onClick={this.props.handleMoveLeft}><svg fill="currentColor" height="1em" width="1em" viewBox="0 0 256 512" aria-hidden="true" role="img">
                              <path d="M31.7 239l136-136c9.4-9.4 24.6-9.4 33.9 0l22.6 22.6c9.4 9.4 9.4 24.6 0 33.9L127.9 256l96.4 96.4c9.4 9.4 9.4 24.6 0 33.9L201.7 409c-9.4 9.4-24.6 9.4-33.9 0l-136-136c-9.5-9.4-9.5-24.6-.1-34z" transform="" /></svg></button>
                            Page No: {this.props.pageNumber}
                            <button disabled={false} data-action="next" aria-label="Go to next page" className="pf-c-button pf-m-plain" type="button" onClick={this.props.handleMoveRight}><svg fill="currentColor" height="1em" width="1em" viewBox="0 0 256 512" aria-hidden="true" role="img">
                              <path d="M224.3 273l-136 136c-9.4 9.4-24.6 9.4-33.9 0l-22.6-22.6c-9.4-9.4-9.4-24.6 0-33.9l96.4-96.4-96.4-96.4c-9.4-9.4-9.4-24.6 0-33.9L54.3 103c9.4-9.4 24.6-9.4 33.9 0l136 136c9.5 9.4 9.5 24.6.1 34z" transform="" /></svg></button>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                }
                {this.props.nextPageRecordCount === 0 &&
                  <div>
                    <div className="example">
                      <div className="ws-preview">
                        <div className="pf-c-pagination" id="pagination-options-menu-top">
                          <div className="pf-c-dropdown">
                            <div className="pf-c-options-menu__toggle pf-m-plain pf-m-text">
                            Showing <ContextSelector
                              toggleText={selected}
                              onSearchInputChange={this.onSearchInputChange}
                              isOpen={isOpen}
                              searchInputValue={searchValue}
                              onToggle={this.onToggle}
                              onSelect={this.onSelect}
                              onSearchButtonClick={this.onSearchButtonClick}
                              screenReaderLabel="Selected Project:"
                            >
                              {filteredItems.map((item, index) => (
                                <ContextSelectorItem key={index}>{item}</ContextSelectorItem>
                              ))}
                            </ContextSelector>
                              <button disabled={false} data-action="first" aria-label="Go to first page" className="pf-c-button pf-m-plain" type="button" onClick={this.props.handleMoveToFirst}><svg fill="currentColor" height="1em" width="1em" viewBox="0 0 448 512" aria-hidden="true" role="img">
                                <path d="M223.7 239l136-136c9.4-9.4 24.6-9.4 33.9 0l22.6 22.6c9.4 9.4 9.4 24.6 0 33.9L319.9 256l96.4 96.4c9.4 9.4 9.4 24.6 0 33.9L393.7 409c-9.4 9.4-24.6 9.4-33.9 0l-136-136c-9.5-9.4-9.5-24.6-.1-34zm-192 34l136 136c9.4 9.4 24.6 9.4 33.9 0l22.6-22.6c9.4-9.4 9.4-24.6 0-33.9L127.9 256l96.4-96.4c9.4-9.4 9.4-24.6 0-33.9L201.7 103c-9.4-9.4-24.6-9.4-33.9 0l-136 136c-9.5 9.4-9.5 24.6-.1 34z" transform="" /></svg></button>
                              <button disabled={false} data-action="previous" aria-label="Go to previous page" className="pf-c-button pf-m-plain" type="button" onClick={this.props.handleMoveLeft}><svg fill="currentColor" height="1em" width="1em" viewBox="0 0 256 512" aria-hidden="true" role="img">
                                <path d="M31.7 239l136-136c9.4-9.4 24.6-9.4 33.9 0l22.6 22.6c9.4 9.4 9.4 24.6 0 33.9L127.9 256l96.4 96.4c9.4 9.4 9.4 24.6 0 33.9L201.7 409c-9.4 9.4-24.6 9.4-33.9 0l-136-136c-9.5-9.4-9.5-24.6-.1-34z" transform="" /></svg></button>
                              Page No: {this.props.pageNumber}
                              <button disabled={true} data-action="next" aria-label="Go to next page" className="pf-c-button pf-m-plain" type="button" onClick={this.props.handleMoveRight}><svg fill="currentColor" height="1em" width="1em" viewBox="0 0 256 512" aria-hidden="true" role="img">
                                <path d="M224.3 273l-136 136c-9.4 9.4-24.6 9.4-33.9 0l-22.6-22.6c-9.4-9.4-9.4-24.6 0-33.9l96.4-96.4-96.4-96.4c-9.4-9.4-9.4-24.6 0-33.9L54.3 103c9.4-9.4 24.6-9.4 33.9 0l136 136c9.5 9.4 9.5 24.6.1 34z" transform="" /></svg></button>
                            </div>
                          </div>
                        </div>
                      </div>
                    </div> </div>
                }
                {
                  this.props.pageNumber !== 1 && this.props.nextPageRecordCount !== 0 &&
                  <div>
                    <div className="example">
                      <div className="ws-preview">
                        <div className="pf-c-pagination" id="pagination-options-menu-top">
                          <div className="pf-c-dropdown">
                            <div className="pf-c-options-menu__toggle pf-m-plain pf-m-text">
                              Showing <ContextSelector
                                toggleText={selected}
                                onSearchInputChange={this.onSearchInputChange}
                                isOpen={isOpen}
                                searchInputValue={searchValue}
                                onToggle={this.onToggle}
                                onSelect={this.onSelect}
                                onSearchButtonClick={this.onSearchButtonClick}
                                screenReaderLabel="Selected Project:"
                              >
                                {filteredItems.map((item, index) => (
                                  <ContextSelectorItem key={index}>{item}</ContextSelectorItem>
                                ))}
                              </ContextSelector>
                              <button disabled={false} data-action="first" aria-label="Go to first page" className="pf-c-button pf-m-plain" type="button" onClick={this.props.handleMoveToFirst}><svg fill="currentColor" height="1em" width="1em" viewBox="0 0 448 512" aria-hidden="true" role="img">
                                <path d="M223.7 239l136-136c9.4-9.4 24.6-9.4 33.9 0l22.6 22.6c9.4 9.4 9.4 24.6 0 33.9L319.9 256l96.4 96.4c9.4 9.4 9.4 24.6 0 33.9L393.7 409c-9.4 9.4-24.6 9.4-33.9 0l-136-136c-9.5-9.4-9.5-24.6-.1-34zm-192 34l136 136c9.4 9.4 24.6 9.4 33.9 0l22.6-22.6c9.4-9.4 9.4-24.6 0-33.9L127.9 256l96.4-96.4c9.4-9.4 9.4-24.6 0-33.9L201.7 103c-9.4-9.4-24.6-9.4-33.9 0l-136 136c-9.5 9.4-9.5 24.6-.1 34z" transform="" /></svg></button>
                              <button disabled={false} data-action="previous" aria-label="Go to previous page" className="pf-c-button pf-m-plain" type="button" onClick={this.props.handleMoveLeft}><svg fill="currentColor" height="1em" width="1em" viewBox="0 0 256 512" aria-hidden="true" role="img">
                                <path d="M31.7 239l136-136c9.4-9.4 24.6-9.4 33.9 0l22.6 22.6c9.4 9.4 9.4 24.6 0 33.9L127.9 256l96.4 96.4c9.4 9.4 9.4 24.6 0 33.9L201.7 409c-9.4 9.4-24.6 9.4-33.9 0l-136-136c-9.5-9.4-9.5-24.6-.1-34z" transform="" /></svg></button>
                              Page No: {this.props.pageNumber}
                              <button disabled={false} data-action="next" aria-label="Go to next page" className="pf-c-button pf-m-plain" type="button" onClick={this.props.handleMoveRight}><svg fill="currentColor" height="1em" width="1em" viewBox="0 0 256 512" aria-hidden="true" role="img">
                                <path d="M224.3 273l-136 136c-9.4 9.4-24.6 9.4-33.9 0l-22.6-22.6c-9.4-9.4-9.4-24.6 0-33.9l96.4-96.4-96.4-96.4c-9.4-9.4-9.4-24.6 0-33.9L54.3 103c9.4-9.4 24.6-9.4 33.9 0l136 136c9.5 9.4 9.5 24.6.1 34z" transform="" /></svg></button>
                            </div>
                          </div>
                        </div>
                      </div>
                    </div></div>
                }
              </div>
            </nav>
          </LevelItem>
        </Level>
      </Fragment>
    );
  }

  private onToggle = isOpen => {
    this.setState({
      isOpen
    });
  };

  private onSelect = (event,value) => {
    this.setState({
      isOpen: !this.state.isOpen,
      selected: value
    });
  };

  private onSearchInputChange = value => {
    this.setState({ searchValue: value });
  };

  private onSearchButtonClick = event => {
    const filtered =
      this.state.searchValue === ''
        ? this.dropdownItems
        : this.dropdownItems.filter(str => str.toLowerCase().indexOf(this.state.searchValue.toLowerCase()) !== -1);
    this.setState({ filteredItems: filtered || [] });
  };

}

export { Paginate }