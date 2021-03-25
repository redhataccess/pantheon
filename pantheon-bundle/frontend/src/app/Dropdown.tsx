import React, { Component } from "react";
import "@app/app.css";

export interface IProps {
  perPageValue: (itemsPerPage) => any
  newPerPagevalue: string
}

class Dropdown extends Component<IProps> {

  public dropdownItems = [
    "5 items per page",
    "25 items per page",
    "50 items per page",
    "100 items per page"
  ];

  public state = {
    filteredItems: this.dropdownItems,
    isOpen: false,
    selectedItem: this.dropdownItems[0]
  };

  public render() {

    return (
      <React.Fragment>
        {!this.state.isOpen &&
          <div className="pf-c-context-selector">
            <button aria-labelledby="pf-context-selector-label-id-0 pf-context-selector-toggle-id-0" id="pf-context-selector-toggle-id-0" className="pf-c-context-selector__toggle" type="button" aria-expanded="false"
              onClick={this.changeExpandState}>
              <span className="pf-c-context-selector__toggle-text">{this.props.newPerPagevalue}</span>
              <svg fill="currentColor" height="1em" width="1em" viewBox="0 0 320 512" aria-hidden="true" role="img" className="pf-c-context-selector__toggle-icon" >
                <path d="M31.3 192h257.3c17.8 0 26.7 21.5 14.1 34.1L174.1 354.8c-7.8 7.8-20.5 7.8-28.3 0L17.2 226.1C4.6 213.5 13.5 192 31.3 192z" transform="" />
              </svg>
            </button>
          </div>
        }
        {this.state.isOpen &&
          <div className="pf-c-context-selector pf-m-expanded">
            <button aria-labelledby="pf-context-selector-label-id-0 pf-context-selector-toggle-id-0" id="pf-context-selector-toggle-id-0" className="pf-c-context-selector__toggle" type="button" aria-expanded="true"
              onClick={this.changeExpandState}>
              <span className="pf-c-context-selector__toggle-text">{this.props.newPerPagevalue}</span>
              <svg fill="currentColor" height="1em" width="1em" viewBox="0 0 320 512" aria-hidden="true" role="img" className="pf-c-context-selector__toggle-icon" >
                <path d="M31.3 192h257.3c17.8 0 26.7 21.5 14.1 34.1L174.1 354.8c-7.8 7.8-20.5 7.8-28.3 0L17.2 226.1C4.6 213.5 13.5 192 31.3 192z" transform="" />
              </svg>
            </button>
            <div className="pf-c-context-selector__menu">
              <div>
                <ul className="pf-c-context-selector__menu-list" role="menu">
                  <li role="none"><button className="pf-c-context-selector__menu-list-item" onClick={() => this.changeSelection(this.dropdownItems[0])} id="items_5">{this.dropdownItems[0]}</button></li>
                  <li role="none"><button className="pf-c-context-selector__menu-list-item" onClick={() => this.changeSelection(this.dropdownItems[1])} id="items_25">{this.dropdownItems[1]}</button></li>
                  <li role="none"><button className="pf-c-context-selector__menu-list-item" onClick={() => this.changeSelection(this.dropdownItems[2])} id="items_50">{this.dropdownItems[2]}</button></li>
                  <li role="none"><button className="pf-c-context-selector__menu-list-item" onClick={() => this.changeSelection(this.dropdownItems[3])} id="items_100">{this.dropdownItems[3]}</button></li>
                </ul>
              </div>
            </div>
          </div>
        }
      </React.Fragment>
    );
  }

  private changeExpandState = () => {
    this.setState({
      isOpen: !this.state.isOpen
    })
  };

  private changeSelection = (item) => {
    this.setState({
      isOpen: !this.state.isOpen,
      selectedItem: item
    }, () => {
      this.props.perPageValue(this.state.selectedItem);
    })
  };


}

export { Dropdown }