import React, { Fragment } from 'react';
import {Button} from '@patternfly/react-core';
import { Badge } from '@patternfly/react-core';

export interface IProps{
    handleMoveLeft: Function
    handleMoveRight: Function
    pageNumber : number
    isNextPageRequied: number
}

export class Pagination extends React.Component<IProps> {

private fetchBlocks = () => {
    const totalBlocks = 2;
    const LEFT_PAGE = 'LEFT';
    const RIGHT_PAGE = 'RIGHT';
    const pages = [LEFT_PAGE,RIGHT_PAGE,totalBlocks];
    return pages;
}

render() {

    const blocks = this.fetchBlocks();
    return (
      <Fragment>
        <nav aria-label="Countries Pagination">
            { blocks.map((page, index) => {
              if (page === "LEFT") return (
                this.props.pageNumber ===1 ?
                <Button isDisabled href="#" target="_blank" variant="primary" onClick={() => this.props.handleMoveLeft()}>Previous</Button>
                : 
                <Button href="#" variant="primary" onClick={() => this.props.handleMoveLeft()}>Previous</Button>
              );

              if (page === "RIGHT") return (
                this.props.isNextPageRequied<10?
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