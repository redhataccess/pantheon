import React from "react";
import { Pagination, PaginationVariant } from "@patternfly/react-core";

export interface IProps {
  itemCount: number,
  contentType: string
  }

class PaginationBottom extends React.Component<IProps> {

  public state = {
      page: 1,
      perPage: 5
  };

  render() {
    return (
      <Pagination
        itemCount={this.props.itemCount}
        widgetId={"pagination-options-menu-top-"+this.props.contentType}
        perPage={this.state.perPage}
        page={this.state.page}
        onSetPage={this.onSetPage}
        onPerPageSelect={this.onPerPageSelect}
        isCompact
      />
    );
  }

  public onSetPage = (_event, pageNumber) => {
    this.setState({
      page: pageNumber
    });
  }

  public onPerPageSelect = (_event, perPage) => {
    this.setState({
      perPage
    });
  }
}

export { PaginationBottom }