import React, { Fragment } from 'react';
import { Button, Badge, Level, LevelItem } from '@patternfly/react-core';

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
                      <Button isDisabled={true} href="#" target="_blank" variant="primary" onClick={this.props.handleMoveLeft}>Previous</Button>
                      :
                      <Button href="#" variant="primary" onClick={this.props.handleMoveLeft}>Previous</Button>
                  );
                }
                if (page === "NEXT") {

                  return (
                    this.props.nextPageRecordCount === 0 ?
                      <Button isDisabled={true} href="#" target="_blank" variant="secondary" onClick={this.props.handleMoveRight}>Next</Button>
                      :
                      <Button href="#" variant="secondary" onClick={this.props.handleMoveRight}>Next</Button>
                  );
                }
                return (
                  <Badge key="pageNumber">Page No: {this.props.pageNumber}</Badge>
                );
              })}
            </nav>
          </LevelItem>
          <LevelItem>
            <Badge>No. of records on page: {this.props.noOfRecordsOnPage}</Badge>
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