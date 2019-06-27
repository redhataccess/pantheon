import React, { Fragment } from 'react';
import { Button, Label, Level, LevelItem } from '@patternfly/react-core';

export interface IProps {
  handleMoveLeft: () => any
  handleMoveRight: () => any
  pageNumber: number
  nextPageRecordCount: number
  noOfRecordsOnPage: number
}

class Pagination extends React.Component<IProps> {

  public render() {

    const blocks = this.fetchBlocks();
    return (
      <Fragment>
        <Level gutter="md">
          <LevelItem>
            <nav aria-label="Countries Pagination">
              {blocks.map((page, index) => {
                if (page === "PREVIOUS") {
                  return (
                    this.props.pageNumber === 1 ?
                      <Button className="pagination-btn" isDisabled={true} href="#" target="_blank" variant="primary" onClick={this.props.handleMoveLeft}>Previous</Button>
                      :
                      <Button className="pagination-btn" href="#" variant="primary" onClick={this.props.handleMoveLeft}>Previous</Button>
                  );
                }
                if (page === "NEXT") {

                  return (
                    this.props.nextPageRecordCount === 0 ?
                      <Button className="pagination-btn" isDisabled={true} href="#" target="_blank" variant="secondary" onClick={this.props.handleMoveRight}>Next</Button>
                      :
                      <Button className="pagination-btn" href="#" variant="secondary" onClick={this.props.handleMoveRight}>Next</Button>
                  );
                }
                return (
                  <Label className="page-lbl" key="pageNumber">Page No: {this.props.pageNumber}</Label>
                );
              })}
            </nav>
          </LevelItem>
          <LevelItem>
            <Label className="page-lbl">No. of records on page: {this.props.noOfRecordsOnPage}</Label>
          </LevelItem>
        </Level>
      </Fragment>
    );

  }

  private fetchBlocks = () => {
    const totalBlocks = 2;
    const PREVIOUS_PAGE = 'PREVIOUS';
    const NEXT_PAGE = 'NEXT';
    const pages = [PREVIOUS_PAGE, NEXT_PAGE, totalBlocks];
    return pages;
  }

}

export { Pagination }