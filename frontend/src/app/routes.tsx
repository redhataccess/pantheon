import React from 'react';
import { Route, RouteComponentProps, Switch, Redirect } from 'react-router-dom';
import Search from '@app/search';
import { Module } from '@app/module';
import { Login } from '@app/login';
import { GitImport } from './gitImport';
import { Admin } from '@app/admin';

export interface IAppRoute {
  label: string;
  component: | React.ComponentType<RouteComponentProps<any>> | React.ComponentType<any>;
  icon: any;
  exact?: boolean;
  path: string;
}

const routes: IAppRoute[] = [
  {
    component: Search,
    exact: true,
    icon: null,
    label: 'Search',
    path: '/search'
  },
  {
    component: Module,
    exact: true,
    icon: null,
    label: '',
    path: '/module'
  },
  {
    component: GitImport,
    exact: true,
    icon: null,
    label: '',
    path: '/git'
  },
  {
    component: Login,
    exact: true,
    icon: null,
    label: '', // Empty because we are using the Brand component to render the text.
    path: '/login'
  },
  {
    component: Admin,
    exact: true,
    icon: null,
    label: '', // Using the Admin component to render the text.
    path: '/admin'
  }
];

const Routes = () => (
  <Switch>
    {routes.map(({path, exact, component}, idx) => (
      <Route path={path} exact={exact} component={component} key={idx} />
    ))}
    <Route component={Search} />
  </Switch>
);

export { Routes, routes }; 