import React, { Component } from 'react';
import { Route, RouteComponentProps, Switch } from 'react-router-dom';
import Search from '@app/search';
import { Module } from '@app/module';
import { Product } from '@app/product';
import { ProductListing } from '@app/productListing';
import { Login } from '@app/login';
import { GitImport } from './gitImport';
import { ModuleDisplay } from '@app/moduleDisplay';
import { IAppState } from './app';

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
    component: Product,
    exact: true,
    icon: null,
    label: '',
    path: '/product'
  },
  {
    component: ProductListing,
    exact: true,
    icon: null,
    label: '',
    path: '/products'
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
    component: ModuleDisplay,
    exact: false,
    icon: null,
    label: '', // Empty because we are using the Brand component to render the text.
    path: '/:data'
  }
];

class Routes extends Component<IAppState> {

  public render() {
    console.log('routes username: ' + this.props.username)
    return (

      // https://github.com/ReactTraining/react-router/issues/5521#issuecomment-329491083

      <Switch>
        {routes.map(({path, exact, component}, idx) => (
          <Route path={path} exact={exact} component={component} key={idx} {...this.props} />
        ))}
        <Route render={() => <Search {...this.props} />} />
      </Switch>
    )
  }
}

export { Routes }; 