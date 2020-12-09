import React, { Component } from "react"
import { Route, RouteComponentProps, Switch } from "react-router-dom"
import { Search } from "@app/search"
import { SearchBeta } from "@app/searchBeta"
import { Product } from "@app/product"
import { ProductDetails } from "@app/productDetails"
import ProductListing from "@app/productListing"
import { Login } from "@app/login"
import { GitImport } from "./gitImport"
import { ContentDisplay }  from "@app/contentDisplay"
import { IAppState } from "./app"
import { ProductProvider } from "./contexts/ProductContext"

interface IAppRoute {
  label: string
  component: (routeProps) => JSX.Element
  icon: any
  exact?: boolean
  path: string
  requiresLogin: boolean
}

class Routes extends Component<IAppState> {

  public render() {
    const routes: IAppRoute[] = [
      {
        component: (routeProps) => <Search {...this.props} />,
        exact: true,
        icon: null,
        label: "Search",
        path: "/search",
        requiresLogin: false
      },
      {
        component: (routeProps) => <SearchBeta {...this.props} />,
        exact: true,
        icon: null,
        label: "Search Beta",
        path: "/searchbeta",
        requiresLogin: false
      },
      {
        component: (routeProps) => <Product />,
        exact: true,
        icon: null,
        label: "",
        path: "/product",
        requiresLogin: true
      },
      {
        component: (routeProps) => <ProductProvider><ProductListing {...routeProps}/></ProductProvider>,
        exact: true,
        icon: null,
        label: "",
        path: "/products",
        requiresLogin: true
      },
      {
        component: (routeProps) => <ProductProvider><ProductDetails {...routeProps}/></ProductProvider>,
        exact: true,
        icon: null,
        label: "",
        path: "/products/:id",
        requiresLogin: true
      },
      {
        component: (routeProps) => <GitImport />,
        exact: true,
        icon: null,
        label: "",
        path: "/git",
        requiresLogin: true
      },
      {
        component: (routeProps) => <Login />,
        exact: true,
        icon: null,
        label: "", // Empty because we are using the Brand component to render the text.
        path: "/login",
        requiresLogin: false
      },
      {
        component: (routeProps) => <ContentDisplay {...routeProps} />,
        exact: false,
        icon: null,
        label: "", // Empty because we are using the Brand component to render the text.
        path: "/module/:data",
        requiresLogin: true
      },
      {
        component: (routeProps) => <ContentDisplay {...routeProps} />,
        exact: false,
        icon: null,
        label: "", // Empty because we are using the Brand component to render the text.
        path: "/assembly/:data",
        requiresLogin: true
      }
    ]

    return (
      // https://github.com/ReactTraining/react-router/issues/5521#issuecomment-329491083
      <Switch>
        {routes.map(({ path, exact, component, requiresLogin }, idx) => (
          <Route path={path} exact={exact} render={(routeProps) => (this.props.userAuthenticated || !requiresLogin) ? component(routeProps) : <Login />} key={idx} />
        ))}
        <Route render={() => <Search {...this.props} />} />
      </Switch>
    )
  }
}

export { Routes }