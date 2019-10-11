import * as React from 'react'
import { PageSidebar, Nav, NavList } from '@patternfly/react-core'
import { NavLinks }  from './NavLinks'
import { IAppState } from '@app/app'

export interface ISideBarProps {
  isNavOpen: boolean
  appState: React.PropsWithChildren<IAppState>
}

export const Sidebar: React.FunctionComponent<ISideBarProps> = ({isNavOpen, appState}) => {
  const nav = () => (
    <aside className="pf-c-page__sidebar">
      <Nav className="pf-c-nav" id="page-layout-default-nav-primary-nav" aria-label="Primary Nav Default">
        <NavList className="pf-c-nav__list">
          <NavLinks {...appState}/>
        </NavList>
      </Nav>
    </aside>
  )

  return <PageSidebar nav={nav()} isNavOpen={isNavOpen} />;
}