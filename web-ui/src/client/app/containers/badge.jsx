import React from 'react';
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
          const score = response.score * 100; // 0.XX -> XX%
          const color = this.colorForScore(score);
          const shieldsBaseUrl = 'https://img.shields.io/badge/';
          const shieldsUrl = shieldsBaseUrl.concat(
            'API%20Linter%20Score',
            '-',
            score.toString(),
            '%25',
            '-',
            color,
            '.svg'
          );
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

  colorForScore(score) {
    return score < 90 ? (score < 80 ? 'red' : 'orange') : 'green';
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
