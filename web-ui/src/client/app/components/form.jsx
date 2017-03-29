import React from 'react';
import {Violations} from './violations.jsx'
import {CompleteMsg} from './complete-msg.jsx';

const css = `
    .violations-container__spinner {
        text-align:center;
    }
`;

export default class Form extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      error: null,
      loading: false,
      ajaxComplete: false,
      inputValue: '',
      violations: [],
      violationsCount: {
        could: 0,
        hint: 0,
        must: 0,
        should: 0
      }
    };
  }

  clearError() {
    this.setState({ error: null, ajaxComplete: false });
  }

  resetAjaxComplete() {
    this.setState({ ajaxComplete: false });
  }

  handleFormSubmit(event) {
    event.preventDefault();

    this.setState({ error: null, loading: true, ajaxComplete: false });

    this.props.RestService
      .getApiViolations(this.state.inputValue)
      .then((response) => {
        this.setState({
          loading: false,
          ajaxComplete: true,
          violations: response.violations,
          violationsCount: response.violations_count
        })
      })
      .catch((error) => {
        console.error(error);
        this.setState({
          loading: false,
          ajaxComplete: true,
          error: 'Ooops something went wrong!',
          violations: [],
          violationsCount: {
            could: 0,
            hint: 0,
            must: 0,
            should: 0
          }
        })
      })
  }

  handleOnInputValueChange(event) {
    this.setState({inputValue: event.target.value});
  }

  render () {
    return (
      <div className="violations-container">
        <style>{css}</style>
        <form onSubmit={this.handleFormSubmit.bind(this)}>
          <label className="dc-label">Enter full path to your swagger file from repo </label>
          <input className="dc-input dc-input--block"
                 value={this.state.inputValue}
                 onChange={this.handleOnInputValueChange.bind(this)}
                 type="url"
                 name="path"
                 placeholder="e.g https://github.com/OAI/OpenAPI-Specification/blob/master/examples/v2.0/json/petstore.json"
                 required pattern="https?://.+" />
          <button
            type="submit"
            disabled={this.state.loading}
            className={"dc-btn dc-btn--primary  " + (this.state.loading ? "dc-btn--disabled" : "")}>
            Submit
          </button>
        </form>

        <CompleteMsg error={this.state.error} violations={this.state.violations} ajaxComplete={this.state.ajaxComplete} onCloseError={this.clearError.bind(this)} onCloseSuccess={this.resetAjaxComplete.bind(this)}  />

        { this.state.loading ? <div className="violations-container__spinner"><div className="dc-spinner dc-spinner--small"></div></div>
          : <Violations violations={this.state.violations} violationsCount={this.state.violationsCount}/> }

      </div>
    )
  }
}

