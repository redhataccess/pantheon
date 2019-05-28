import React from 'react';
import { BrowserRouter, Route, RouteComponentProps, Switch } from 'react-router-dom';
import Search from '@app/search';
import Module from '@app/module';
import Login from '@app/login';

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
    label: 'Module',
    path: '/module'
  },
  {
    component: Login,
    exact: true,
    icon: null,
    label: '', // Empty because we are using the Brand component to render the text.
    path: '/login'
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