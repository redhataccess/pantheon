import React, { Fragment } from "react";
import { DropdownItem, Level, LevelItem } from "@patternfly/react-core";
import { ContextSelector, ContextSelectorItem } from "@patternfly/react-core";
import { Dropdown } from "@app/Dropdown";

export interface IProps {
  handleMoveLeft: () => any
  handleMoveRight: () => any
  handleMoveToFirst: () => any
  handlePerPageLimit: (itemsPerPage) => any
  handleItemsPerPage: (itemsPerPage) => any
  pageNumber: number
  nextPageRecordCount: number
  perPageLimit: number
  showDropdownOptions: boolean
  bottom: boolean
  className?: string
  currentBulkOperation: string
}

class Pagination extends React.Component<IProps> {

  public dropdownItems = [
    "5 items per page",
    "25 items per page",
    "50 items per page",
    "100 items per page"
  ];

  public state = {
    filteredItems: this.dropdownItems,
    isExpanded: false,
    isOpen: false,
    renderSearch: false,
    searchValue: "",
    selected: this.dropdownItems[0]

  };

  public firstPageButton = true;
  public previousPageButton = true;
  public nextPageButton = true;

  public render() {
    const { isOpen, filteredItems } = this.state;

    if (this.props.pageNumber === 1 && this.props.nextPageRecordCount !== 0) {
      this.firstPageButton = true;
      this.previousPageButton = true;
      this.nextPageButton = false;
    }
    if (this.props.nextPageRecordCount === 0 && this.props.pageNumber !== 1) {
      this.firstPageButton = false;
      this.previousPageButton = false;
      this.nextPageButton = true;
    }
    if (this.props.pageNumber !== 1 && this.props.nextPageRecordCount !== 0) {
      this.firstPageButton = false;
      this.previousPageButton = false;
      this.nextPageButton = false;
    }
    if (this.props.pageNumber === 1 && this.props.nextPageRecordCount === 0) {
      this.firstPageButton = true;
      this.previousPageButton = true;
      this.nextPageButton = true;
    }
    if(this.props.currentBulkOperation !== ""){
      this.firstPageButton = true;
      this.previousPageButton = true;
      this.nextPageButton = true;
    }

    return (
      <Fragment>
        <Level>
          <LevelItem />
          <LevelItem />
          <LevelItem>
            <nav aria-label="Pagination" className={this.props.className}>
              <div>
                <div>
                  <div className="example">
                    <div className="ws-preview">
                      <div className="pf-c-pagination" id="pagination-options-menu-top">
                        <div className="pf-c-dropdown">
                          <div className="pf-c-options-menu__toggle pf-m-plain pf-m-text">
                            {!this.props.showDropdownOptions && !this.props.bottom &&
                              <ContextSelector
                                isOpen={isOpen}
                                onToggle={this.onToggle}
                              />}
                            {this.props.showDropdownOptions &&
                              <Dropdown
                                perPageValue={this.dropDownValue}
                                newPerPagevalue={this.props.perPageLimit + " items per page"}
                              />
                            }
                            <button disabled={this.firstPageButton} data-action="first" aria-label="Go to first page" className="pf-c-button pf-m-plain" type="button" onClick={this.props.handleMoveToFirst}><svg fill="currentColor" height="1em" width="1em" viewBox="0 0 448 512" aria-hidden="true" role="img">
                              <path d="M223.7 239l136-136c9.4-9.4 24.6-9.4 33.9 0l22.6 22.6c9.4 9.4 9.4 24.6 0 33.9L319.9 256l96.4 96.4c9.4 9.4 9.4 24.6 0 33.9L393.7 409c-9.4 9.4-24.6 9.4-33.9 0l-136-136c-9.5-9.4-9.5-24.6-.1-34zm-192 34l136 136c9.4 9.4 24.6 9.4 33.9 0l22.6-22.6c9.4-9.4 9.4-24.6 0-33.9L127.9 256l96.4-96.4c9.4-9.4 9.4-24.6 0-33.9L201.7 103c-9.4-9.4-24.6-9.4-33.9 0l-136 136c-9.5 9.4-9.5 24.6-.1 34z" transform="" /></svg></button>
                            <button disabled={this.previousPageButton} data-action="previous" aria-label="Go to previous page" className="pf-c-button pf-m-plain" type="button" onClick={this.props.handleMoveLeft}><svg fill="currentColor" height="1em" width="1em" viewBox="0 0 256 512" aria-hidden="true" role="img">
                              <path d="M31.7 239l136-136c9.4-9.4 24.6-9.4 33.9 0l22.6 22.6c9.4 9.4 9.4 24.6 0 33.9L127.9 256l96.4 96.4c9.4 9.4 9.4 24.6 0 33.9L201.7 409c-9.4 9.4-24.6 9.4-33.9 0l-136-136c-9.5-9.4-9.5-24.6-.1-34z" transform="" /></svg></button>
                            Page: {this.props.pageNumber}
                            <button disabled={this.nextPageButton} data-action="next" aria-label="Go to next page" className="pf-c-button pf-m-plain" type="button" onClick={this.props.handleMoveRight}><svg fill="currentColor" height="1em" width="1em" viewBox="0 0 256 512" aria-hidden="true" role="img">
                              <path d="M224.3 273l-136 136c-9.4 9.4-24.6 9.4-33.9 0l-22.6-22.6c-9.4-9.4-9.4-24.6 0-33.9l96.4-96.4-96.4-96.4c-9.4-9.4-9.4-24.6 0-33.9L54.3 103c9.4-9.4 24.6-9.4 33.9 0l136 136c9.5 9.4 9.5 24.6.1 34z" transform="" /></svg></button>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div></div>
              </div>
            </nav>
          </LevelItem>
        </Level>
      </Fragment>
    );
  }

  private onToggle = isOpen => {
    this.setState({
      isOpen: !this.state.isOpen
    });
  };

  private dropDownValue = (value) => {
    this.setState({
      isOpen: !this.state.isOpen,
      itemsPerPage: Number(value.substr(0, value.indexOf(" "))),
      selected: value
    }, () => {
      this.props.handlePerPageLimit(Number(value.substr(0, value.indexOf(" "))))
      this.props.handleItemsPerPage(Number(value.substr(0, value.indexOf(" "))))
    });
  };
}

export { Pagination }