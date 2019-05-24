import * as React from 'react';
import { NavLink } from 'react-router-dom';
import {
  PageSidebar,
} from '@patternfly/react-core';
import { routes } from '../../../routes';

export interface ISideBarProps {
  isNavOpen: boolean;
}

export const Sidebar: React.FunctionComponent<ISideBarProps> = ({isNavOpen}) => {
  const outputNavLinks = () => {
    return routes.map(route => {
      return (
        <li key={route.path}>
          <NavLink to={route.path} className="pf-c-nav__link">{route.label}</NavLink>
        </li>
      );
    });
  }

  const nav = () => (
    <aside className="pf-c-page__sidebar">
      <nav className="pf-c-nav" id="page-layout-default-nav-primary-nav" aria-label="Primary Nav Default Example">
        <ul className="pf-c-nav__list">
          {outputNavLinks()}
        </ul>
      </nav>
    </aside>
  );

  return <PageSidebar nav={nav()} isNavOpen={isNavOpen} />;
}