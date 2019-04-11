import React from 'react';
import { URL } from './url.jsx';
import { Editor } from './editor.jsx';
import { Rules } from './rules.jsx';
import { Route, Switch, Redirect } from 'react-router-dom';
import { LinkContainer, IndexLinkContainer } from 'react-router-bootstrap';
import FluidContainer from '../components/fluid-container.jsx';

export function ViolationsTab({
  authenticated,
  getApiViolationsByURL,
  getApiViolationsBySchema,
  getApiViolationsByExternalId,
  getSupportedRules,
  getFile,
  Storage,
}) {
  if (!authenticated) {
    return <Redirect to="/login" />;
  }
  return (
    <div className="dc-container">
      <h4 className="dc-h4">
        Check if your&nbsp;
        <a
          href="http://swagger.io/specification/"
          target="_blank"
          className="dc-link"
        >
          SWAGGER Schema
        </a>{' '}
        conforms to&nbsp;
        <a
          href="http://zalando.github.io/restful-api-guidelines/"
          target="_blank"
          className="dc-link"
        >
          Zalando's REST API Guidelines
        </a>
      </h4>

      <ul className="dc-tab">
        <IndexLinkContainer
          to="/"
          className="dc-tab__element"
          activeClassName="dc-tab__element--active"
        >
          <li>BY URL</li>
        </IndexLinkContainer>
        <LinkContainer
          to="/editor"
          className="dc-tab__element"
          activeClassName="dc-tab__element--active"
        >
          <li>EDITOR</li>
        </LinkContainer>
        <LinkContainer
          to="/rules"
          className="dc-tab__element"
          activeClassName="dc-tab__element--active"
        >
          <li>RULES</li>
        </LinkContainer>
      </ul>
      <FluidContainer>
        <div className="tab-contents">
          <Switch>
            <Route
              exact
              path="/"
              render={props => (
                <URL
                  getApiViolations={getApiViolationsByURL}
                  Storage={Storage}
                  getFile={getFile}
                  {...props}
                />
              )}
            />
            <Route
              exact
              path="/editor"
              render={props => (
                <Editor
                  getApiViolations={getApiViolationsBySchema}
                  getApiViolationsByExternalId={getApiViolationsByExternalId}
                  Storage={Storage}
                  {...props}
                />
              )}
            />
            <Route
              path="/editor/:externalId"
              render={props => (
                <Editor
                  getApiViolations={getApiViolationsBySchema}
                  getApiViolationsByExternalId={getApiViolationsByExternalId}
                  Storage={Storage}
                  {...props}
                />
              )}
            />
            <Route
              exact
              path="/rules"
              render={props => (
                <Rules
                  getSupportedRules={getSupportedRules}
                  Storage={Storage}
                  {...props}
                />
              )}
            />
            <Redirect path="*" to="/" />
          </Switch>
        </div>
      </FluidContainer>
    </div>
  );
}
