import React, { Component } from 'react'
import { Route, RouteComponentProps, Switch } from 'react-router-dom'
import { Search } from '@app/search'
import { Module } from '@app/module'
import { Product } from '@app/product'
import { ProductListing } from '@app/productListing'
import { Login } from '@app/login'
import { GitImport } from './gitImport'
import { ModuleDisplay } from '@app/moduleDisplay'
import { IAppState } from './app'

interface IAppRoute {
  label: string
  component: JSX.Element
  icon: any
  exact?: boolean
  path: string
  requiresLogin: boolean
}

class Routes extends Component<IAppState> {

  public render() {
    const routes: IAppRoute[] = [
      {
        component: <Search {...this.props} />,
        exact: true,
        icon: null,
        label: 'Search',
        path: '/search',
        requiresLogin: false
      },
      {
        component: <Module />,
        exact: true,
        icon: null,
        label: '',
        path: '/module',
        requiresLogin: true
      },
      {
        component: <Product />,
        exact: true,
        icon: null,
        label: '',
        path: '/product',
        requiresLogin: true
      },
      {
        component: <ProductListing />,
        exact: true,
        icon: null,
        label: '',
        path: '/products',
        requiresLogin: true
      },
      {
        component: <GitImport />,
        exact: true,
        icon: null,
        label: '',
        path: '/git',
        requiresLogin: true
      },
      {
        component: <Login />,
        exact: true,
        icon: null,
        label: '', // Empty because we are using the Brand component to render the text.
        path: '/login',
        requiresLogin: false
      },
      {
        component: <ModuleDisplay />,
        exact: false,
        icon: null,
        label: '', // Empty because we are using the Brand component to render the text.
        path: '/:data',
        requiresLogin: true
      }
    ]

    return (
      // https://github.com/ReactTraining/react-router/issues/5521#issuecomment-329491083
      <Switch>
        {routes.map(({path, exact, component, requiresLogin}, idx) => (
          <Route path={path} exact={exact} render={() => this.props.userAuthenticated || !requiresLogin ? component : <Login />} key={idx} />
        ))}
        <Route render={() => <Search {...this.props} />} />
      </Switch>
    )
  }
}

export { Routes } 