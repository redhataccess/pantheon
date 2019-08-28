import React from 'react';
import { NavLinks }  from './NavLinks';
//import { NavItem } from '@patternfly/react-core';
import "isomorphic-fetch"

import { shallow, mount } from 'enzyme';

import { withRouter } from 'react-router'
import { Link, Route, Router, Switch } from 'react-router-dom'
import { createMemoryHistory } from 'history'
import { render, fireEvent } from '@testing-library/react'
//import { NavList } from '@patternfly/react-core';

const Home = () => <div>You are on the Home page</div>
const Search = () => <div>Search Query</div>
const NoMatch = () => <div>No match</div>

const LocationDisplay = withRouter(({ location }) => (
  <div data-testid="location-display">{location.pathname}</div>
))

function App() {
  return (
    <div>
      <Link to="/" data-testid="home">Home</Link>
      <Link to="/search">Search</Link>
      <Switch>
        <Route exact path="/search" component={Search} />
        <Route path="/pantheon" component={Home} />
        <Route component={NoMatch} />
      </Switch>
      <LocationDisplay />
    </div>
  )
}

// a handy function that can be utilized for any component
// that relies on the router being in context
function renderWithRouter(
  ui,
  {
    route = '/',
    history = createMemoryHistory({ initialEntries: [route] }),
  } = {}
) {
  return {
    ...render(<Router history={history}>{ui}</Router>),
    // adding `history` to the returned utilities to allow us
    // to reference it in our tests (just try to avoid using
    // this to test implementation details).
    history,
  }
}
describe('NavLinks tests', () => {
  test('should render NavLinks component', () => {
    const view = shallow(<NavLinks />);
    expect(view).toMatchSnapshot();
  });

  // it('should render a Data List', () => {
  //   const wrapper = mount(<NavLinks />);
  //   const navList = wrapper.find(NavList);
  //   expect(navList.exists()).toBe(true)
  // });

  test('full app rendering/navigating', () => {
    const { container, getByText, getByTestId } = renderWithRouter(<App />)
    expect(getByTestId('home')).toBeTruthy()
    const leftClick = { button: 0 }
    fireEvent.click(getByText(/search/i), leftClick)
    expect(container.innerHTML).toMatch('Search Query')
  })

  test('landing on a bad page', () => {
    const { container } = renderWithRouter(<App />, {
      route: '/something-that-does-not-match',
    })
    expect(container.innerHTML).toMatch('No match')
  })

  test('rendering a component that uses withRouter', () => {
    const route = '/some-route'
    const { getByTestId } = renderWithRouter(<LocationDisplay />, { route })
    expect(getByTestId('location-display').textContent).toBe(route)
  })
});
