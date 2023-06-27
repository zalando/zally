import React from 'react';
import { Link, Route, Switch } from 'react-router-dom';
import UserInfo from '../components/user-info.jsx';

import { Login } from './login.jsx';
import { ViolationsTab } from './violations-tab.jsx';
import { Badge } from './badge.jsx';

export function App(props) {
  const {
    user,
    logout,
    login,
    env,
    Storage,
    getApiViolationsByURL,
    getApiViolationsBySchema,
    getApiViolationsByExternalId,
    getSupportedRules,
    getFile,
  } = props;
  const { OAUTH_ENABLED } = env;
  const MOUNTPATH = env.MOUNTPATH || '/';

  return (
    <Switch>
      <Route
        exact
        path="/badges/:externalId"
        render={props => (
          <Badge
            getApiViolationsByExternalId={getApiViolationsByExternalId}
            {...props}
          />
        )}
      />
      <Route
        path="/"
        render={props => (
          <div>
            <div className="main-navigation-bar">
              <h1 className="dc-h1 main-navigation-bar__title">
                <Link to="/" className="main-navigation-bar__link">
                  <img
                    className="main-navigation-bar__logo"
                    src={MOUNTPATH + 'assets/logo.png'}
                  />
                  SBB's API Linter
                </Link>{' '}
                <Link
                  to="https://www.sbb.ch"
                  className="main-navigation-bar__link"
                >
                  <img
                    className="sbb-logo"
                    src={MOUNTPATH + 'assets/SBB.svg'}
                  />
                </Link>{' '}
              </h1>
              {OAUTH_ENABLED === true ? (
                <UserInfo
                  username={user.username}
                  authenticated={user.authenticated}
                  onLogin={login}
                  onLogout={logout}
                />
              ) : null}
            </div>
            <div className="dc-page page-container">
              <Switch>
                <Route
                  path="/login"
                  render={props => (
                    <Login user={user} login={login} {...props} />
                  )}
                />
                <Route
                  render={props => (
                    <ViolationsTab
                      authenticated={user.authenticated || !OAUTH_ENABLED}
                      getSupportedRules={getSupportedRules}
                      getApiViolationsByURL={getApiViolationsByURL}
                      getApiViolationsBySchema={getApiViolationsBySchema}
                      getApiViolationsByExternalId={
                        getApiViolationsByExternalId
                      }
                      getFile={getFile}
                      Storage={Storage}
                      {...props}
                    />
                  )}
                />
              </Switch>
            </div>
            <footer>
              <a
                className="dc-link"
                href="https://github.com/schweizerischebundesbahnen/zally"
                target="_blank"
              >
                Github Project
              </a>{' '}
              - An SBB customised fork of Zaland's awesome{' '}
              <a
                className="dc-link"
                href="https://github.com/zalando/zally"
                target="_blank"
              >
                Zally
              </a>
              {', '}
              which is open sourced under{' '}
              <a
                className="dc-link"
                href="https://github.com/zalando/zally/blob/master/LICENSE"
                target="_blank"
              >
                ZALANDO's License
              </a>{' '}
            </footer>
          </div>
        )}
      />
      <redirect path="*" to="/" />
    </Switch>
  );
}
