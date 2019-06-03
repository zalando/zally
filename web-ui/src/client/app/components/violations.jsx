import React from 'react';
import { If } from './util.jsx';
import { Msg } from './msg.jsx';
import { RuleType } from './rules.jsx';
import FluidContainer from './fluid-container.jsx';
import { Link } from 'react-router-dom';

export function Violations(props) {
  return (
    <div>
      <div className="dc-row">
        <div className="dc-column">
          <h3>
            VIOLATIONS
            <span style={{ float: 'right' }}>
              <Link to={'/editor/' + props.externalId} className="dc-link">
                <i className="dc-icon dc-icon--interactive dc-icon--link" />
              </Link>
            </span>
          </h3>
        </div>
      </div>
      <div className="violations-container">
        <FluidContainer>
          <div className="dc-row">
            <div className="dc-column">
              <ul className="violations-content">
                {props.violations.map((violation, index) => {
                  return <Violation key={index} violation={violation} />;
                })}
              </ul>
            </div>
          </div>
        </FluidContainer>
      </div>
    </div>
  );
}

export function Violation(props) {
  const { violation } = props;
  // OpenAPI 3 violations have a `pointer` and Swagger violations have `paths`.
  const paths = violation.pointer ? [violation.pointer] : violation.paths || [];
  return (
    <li
      style={{
        marginBottom: '32px',
        paddingBottom: '32px',
        borderBottom: '1px solid #ccc',
      }}
    >
      <h4 className="dc-h4">
        <RuleType type={violation.violation_type} />
        {violation.violation_type} {'\u2013'} {violation.title}
      </h4>

      <p>{violation.description}</p>

      <If
        test={() => !!violation.rule_link}
        dataTestId="if-violation-rule-link"
      >
        <ViolationRuleLink ruleLink={violation.rule_link} />
      </If>

      <If test={() => !!violation.pointer} dataTestId="if-violation-pointer">
        <ViolationPointer pointer={violation.pointer} />
      </If>

      <If
        test={() => !!paths.length && !violation.pointer}
        dataTestId="if-violation-paths"
      >
        <ViolationPaths paths={paths} />
      </If>
    </li>
  );
}

export function ViolationRuleLink(props) {
  return (
    <p>
      Rule:{' '}
      <a href={props.ruleLink} className="dc-link" target="_blank">
        {props.ruleLink}
      </a>
    </p>
  );
}

export function ViolationPointer(props) {
  const display = props.pointer
    .replace(/^\//g, '')
    .replace(/\//g, ' > ')
    .replace(/~1/g, '/')
    .replace(/~0/g, '~');
  return <p>Location: {display}</p>;
}

export function ViolationPaths(props) {
  return (
    <span>
      <p>Paths:</p>
      <ul>
        {props.paths.map((path, i) => {
          return <li key={i}>{path}</li>;
        })}
      </ul>
    </span>
  );
}

export function ViolationsResult(props) {
  return (
    <div className="violations-result">
      <If test={() => props.pending} dataTestId="if-loading">
        <div className="violations-result__spinner">
          <div className="dc-spinner dc-spinner--small" />
        </div>
      </If>
      <If
        test={() =>
          !props.pending &&
          props.complete &&
          !props.errorMsgText &&
          props.violations.length === 0
        }
        dataTestId="if-success"
      >
        <Msg
          type="success"
          title={props.successMsgTitle}
          text={props.successMsgText}
        />
      </If>
      <If
        test={() => !props.pending && props.complete && props.errorMsgText}
        dataTestId="if-error"
      >
        <Msg type="error" title="ERROR" text={props.errorMsgText} />
      </If>
      <If
        test={() => !props.pending && props.complete && props.violations.length}
        dataTestId="if-violations"
      >
        <Violations
          externalId={props.externalId}
          violations={props.violations}
          violationsCount={props.violationsCount}
        />
      </If>
    </div>
  );
}
