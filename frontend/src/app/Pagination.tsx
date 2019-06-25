import React, { Fragment } from 'react';
import {Button} from '@patternfly/react-core';
import { Badge } from '@patternfly/react-core';

export interface IProps{
    handleMoveLeft: Function
    handleMoveRight: Function
    pageNumber : number
    nextPageRecordCount: number
}

export class Pagination extends React.Component<IProps> {

private fetchBlocks = () => {
    const totalBlocks = 2;
    const PREVIOUS_PAGE = 'PREVIOUS';
    const NEXT_PAGE = 'NEXT';
    const pages = [PREVIOUS_PAGE,NEXT_PAGE,totalBlocks];
    return pages;
}

render() {

    const blocks = this.fetchBlocks();
    return (
      <Fragment>
        <nav aria-label="Countries Pagination">
            { blocks.map((page, index) => {
              if (page === "PREVIOUS") return (
                this.props.pageNumber ===1 ?
                <Button isDisabled href="#" target="_blank" variant="primary" onClick={() => this.props.handleMoveLeft()}>Previous</Button>
                : 
                <Button href="#" variant="primary" onClick={() => this.props.handleMoveLeft()}>Previous</Button>
              );

              if (page === "NEXT") return (
                this.props.nextPageRecordCount<10?
                <Button isDisabled href="#" target="_blank" variant="secondary" onClick={() => this.props.handleMoveRight()}>Next</Button>
                : 
                <Button href="#" variant="secondary" onClick={() => this.props.handleMoveRight()}>Next</Button>
              );

              return (
                <Badge>Page No: {this.props.pageNumber}</Badge>
              );
            }) }
        </nav>
      </Fragment>
    );

  }
} 

export default IProps;