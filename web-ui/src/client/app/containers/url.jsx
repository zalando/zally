import React from 'react';
import { Redirect } from 'react-router-dom';
import { Violations } from './violations.jsx';
import { ViolationsResult } from '../components/violations.jsx';
import { URLInputForm } from '../components/url.jsx';

export class URL extends Violations {
  constructor(props) {
    super(props);
    this.state.goToEditor = false;
    this.state.inputValue = this.Storage.getItem('url-value') || '';
    this.handleOnInputValueChange = this.handleOnInputValueChange.bind(this);
    this.handleFormSubmit = this.handleFormSubmit.bind(this);
    this.handleOnEditFile = this.handleOnEditFile.bind(this);
  }

  handleOnInputValueChange(event) {
    this.Storage.setItem('url-value', event.target.value);
    super.handleOnInputValueChange(event);
  }

  handleOnEditFile() {
    this.setState({ pending: true });
    return this.props
      .getFile(this.state.inputValue)
      .then(file => {
        this.Storage.setItem('editor-value', file);
        this.setState({
          pending: false,
          goToEditor: true,
        });
      })
      .catch(error => {
        console.error(error); // eslint-disable-line no-console

        this.setState({
          pending: false,
          ajaxComplete: true,
          error: error || Violations.DEFAULT_ERROR_MESSAGE,
        });
      });
  }

  render() {
    if (this.state.goToEditor) {
      return <Redirect to="/editor" />;
    }

    return (
      <div>
        <URLInputForm
          value={this.state.inputValue}
          onSubmit={this.handleFormSubmit}
          onChange={this.handleOnInputValueChange}
          pending={this.state.pending}
          onEditFile={this.handleOnEditFile}
        />

        <ViolationsResult
          pending={this.state.pending}
          complete={this.state.ajaxComplete}
          errorMsgText={this.state.error}
          externalId={this.state.externalId}
          violations={this.state.violations}
          successMsgTitle={this.state.successMsgTitle}
          successMsgText={this.state.successMsgText}
        />
      </div>
    );
  }
}
