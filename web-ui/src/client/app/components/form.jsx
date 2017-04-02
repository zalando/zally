import React from 'react';
import {ViolationsResult} from './violations.jsx'
import {CompleteMsg} from './complete-msg.jsx';

const css = `
    .violations-result__spinner {
        text-align:center;
    }
`;

export default class Form extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      error: null,
      pending: false,
      complete: false,
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
    this.setState({ error: null, complete: false });
  }

  handleFormSubmit(event) {
    event.preventDefault();

    this.setState({ error: null, pending: true, complete: false });

    this.props.RestService
      .getApiViolations(this.state.inputValue)
      .then((response) => {
        this.setState({
          pending: false,
          complete: true,
          violations: response.violations,
          violationsCount: response.violations_count
        })
      })
      .catch((error) => {
        console.error(error);
        this.setState({
          pending: false,
          complete: true,
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
            disabled={this.state.pending}
            className={"dc-btn dc-btn--primary  " + (this.state.pending ? "dc-btn--disabled" : "")}>
            Submit
          </button>
        </form>

        <ViolationsResult
          pending={this.state.pending}
          complete={this.state.complete}
          violations={this.state.violations}
          errorMsgText={this.state.error}
          successMsgTitle="Good Job!"
          successMsgText="No violations found in the analyzed schema."
        />

      </div>
    )
  }
}

