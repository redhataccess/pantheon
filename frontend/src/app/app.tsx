import React, { Component } from 'react';
import {
  Page,
  PageSection,
  PageSectionVariants
} from '@patternfly/react-core';
import { Header } from '@app/components/Chrome/Header/Header';
import { Sidebar } from '@app/components/Chrome/Sidebar/Sidebar';
import { SubHeader } from '@app/components/Chrome/SubHeader/SubHeader';
import { Routes } from '@app/routes';
import '@app/app.css';

export default class App extends Component {
  public state = {
    isNavOpen: true,
    setIsNavOpen: Boolean
  };
  public render() {
    const { isNavOpen, setIsNavOpen} = this.state;
      const onNavToggle = () => {
        this.setState({
          isNavOpen: !isNavOpen
        })
      }
    
    return (
      <React.Fragment>
       <Page
          header={<Header isNavOpen={isNavOpen} onNavToggle={onNavToggle} />}
          sidebar={<Sidebar isNavOpen={isNavOpen} />}>
          <PageSection variant={PageSectionVariants.light}>
            <Routes />
          </PageSection>
        </Page>
      </React.Fragment>
    );
  }
}
