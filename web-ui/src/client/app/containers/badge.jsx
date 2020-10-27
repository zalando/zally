import React, { Component } from 'react';
import { Redirect } from 'react-router-dom';
import { Violations } from './violations.jsx';

export function ShieldsBadge(props) {
  if ('MUST' === props.type)
    return <span className="dc-status dc-status--error" />;
  else if ('SHOULD' === props.type)
    return <span className="dc-status dc-status--new" />;
  else return <span className="dc-status dc-status--inactive" />;
}

export class Badge extends Violations {
  constructor(props) {
    super(props);
    if (
      props &&
      props.match &&
      props.match.params &&
      props.match.params.externalId
    ) {
      this.state.externalId = props.match.params.externalId;
    } else {
      this.state.externalId = null;
    }
  }

  componentDidMount() {
    if (this.state.externalId) {
      this.getApiViolationsByExternalId(this.state.externalId)
        .then(response => {
          const score = this.calculateScore(response.violations_count);
          const color = score < 80 ? (score < 50 ? 'red' : 'orange') : 'green';
          const shieldsBaseUrl =
            'https://img.shields.io/badge/API%20Linter%20Score-';
          const shieldsUrl = shieldsBaseUrl.concat(score, '%25-', color);
          const urlToResult = window.location.protocol.concat(
            '//',
            window.location.host,
            '/editor/',
            this.state.externalId
          );
          this.setState({
            pending: false,
            ajaxComplete: true,
            externalId: response.external_id,
            violations: response.violations,
            violationsCount: response.violations_count,
            shieldsUrl: shieldsUrl,
            urlToResult: urlToResult,
          });
        })
        .catch(error => {
          console.error(error); // eslint-disable-line no-console
          this.setState({
            pending: false,
            ajaxComplete: true,
            error: error.detail || Violations.DEFAULT_ERROR_MESSAGE,
            violations: [],
            violationsCount: {
              could: 0,
              hint: 0,
              must: 0,
              should: 0,
            },
          });
          return Promise.reject(error);
        });
    }
  }

  calculateScore(violationsCount) {
    var score = 1.0;
    score = score - Math.min(0.8, violationsCount.must * 0.2);
    score = score - Math.min(0.15, violationsCount.should * 0.05);
    score = score - Math.min(0.05, violationsCount.may * 0.01);
    return Math.round(score * 100);
  }

  render() {
    return (
      <div>
        <a href={this.state.urlToResult}>
          <img src={this.state.shieldsUrl} />
        </a>
      </div>
    );
  }
}
