import { Component } from 'react';

export class Violations extends Component {
  constructor(props) {
    super(props);

    this.Storage = this.props.Storage;
    this.getApiViolations = this.props.getApiViolations;
    this.getApiViolationsByExternalId = this.props.getApiViolationsByExternalId;

    this.state = {
      error: null,
      pending: false,
      ajaxComplete: false,
      inputValue: '',
      externalId: null,
      violations: [],
      violationsCount: {
        could: 0,
        hint: 0,
        must: 0,
        should: 0,
      },
      successMsgTitle: 'Good Job!',
      successMsgText: 'No violations found in the analyzed schema.',
    };
  }

  handleFormSubmit(event) {
    event.preventDefault();

    this.setState({ error: null, pending: true, ajaxComplete: false });

    return this.getApiViolations(this.state.inputValue)
      .then(response => {
        this.setState({
          pending: false,
          ajaxComplete: true,
          externalId: response.external_id,
          violations: response.violations,
          violationsCount: response.violations_count,
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

  handleOnInputValueChange(event) {
    this.setState({ inputValue: event.target.value });
  }

  render() {
    return null;
  }
}

Violations.DEFAULT_ERROR_MESSAGE = 'Ooops something went wrong!';
