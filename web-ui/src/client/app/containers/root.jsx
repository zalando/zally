import React from 'react';
import { BrowserRouter } from 'react-router-dom';
import { App } from './app.jsx';

export function Root(props) {
  return (
    <BrowserRouter basename={props.env.MOUNTPATH}>
      <App
        login={props.login}
        logout={props.logout}
        env={props.env}
        getSupportedRules={props.RestService.getSupportedRules.bind(
          props.RestService
        )}
        getApiViolationsByURL={props.RestService.getApiViolationsByURL.bind(
          props.RestService
        )}
        getApiViolationsBySchema={props.RestService.getApiViolationsBySchema.bind(
          props.RestService
        )}
        getApiViolationsByExternalId={props.RestService.getApiViolationsByExternalId.bind(
          props.RestService
        )}
        getFile={props.RestService.getFile.bind(props.RestService)}
        Storage={props.Storage}
        user={props.user}
      />
    </BrowserRouter>
  );
}
