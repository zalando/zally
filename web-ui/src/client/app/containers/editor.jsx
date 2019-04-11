import React from 'react';
import { Msg } from '../components/msg.jsx';
import { Violations } from './violations.jsx';
import { ViolationsResult } from '../components/violations.jsx';
import { EditorInputForm } from '../components/editor.jsx';
import { Dialog } from '../components/dialog.jsx';
import { id } from 'brace/worker/json';

export const editorErrorToAnnotations = error => {
  if (!error || !error.mark) {
    return [];
  }
  return [
    {
      row: error.mark.line,
      column: error.mark.column,
      type: 'error',
      text: error.reason,
    },
  ];
};

export class Editor extends Violations {
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
    this.state.editorDirty = true;
    this.state.editorValue =
      (!this.state.externalId && this.Storage.getItem('editor-value')) || '';
    this.handleOnInputValueChange = this.handleOnInputValueChange.bind(this);
    this.handleFormSubmit = this.handleFormSubmit.bind(this);
    this.handleHideOverlay = this.handleHideOverlay.bind(this);
  }

  componentDidMount() {
    if (this.state.externalId) {
      this.getApiViolationsByExternalId(this.state.externalId)
        .then(response => {
          this.setState({
            pending: false,
            ajaxComplete: true,
            externalId: response.external_id,
            violations: response.violations,
            violationsCount: response.violations_count,
            editorValue: response.api_definition,
          });
          this.Storage.setItem('editor-value', this.state.editorValue);
          this.updateInputValue(this.state.editorValue);
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
    } else {
      this.updateInputValue(this.state.editorValue);
    }
  }

  handleFormSubmit(event) {
    if (this.state.editorDirty !== false) {
      super.handleFormSubmit(event);
    } else {
      event.preventDefault();
    }
    this.setState({ showOverlay: true, editorDirty: false });
  }

  handleHideOverlay() {
    this.setState({ showOverlay: false });
  }

  updateInputValue(value) {
    this.setState({
      inputValue: value,
      editorDirty: true,
      editorError: null,
      editorAnnotations: [],
    });
  }

  handleOnInputValueChange(value) {
    this.Storage.setItem('editor-value', value);

    this.setState({
      editorValue: value,
    });
    this.updateInputValue(value);
  }

  render() {
    return (
      <div className="dc-row editor-tab">
        <div className="dc-column dc-column--small-12 dc-column--large-7">
          <div className="dc-column__contents">
            <EditorInputForm
              error={this.state.editorError}
              annotations={this.state.editorAnnotations}
              value={this.state.editorValue}
              onSubmit={this.handleFormSubmit}
              onChange={this.handleOnInputValueChange}
              pending={this.state.pending}
              dirty={this.state.editorDirty}
            />
          </div>
        </div>
        <div className="dc-column dc-column--small-12 dc-column--large-5">
          <div className="dc-column__contents">
            {this.state.editorError ? (
              <Msg
                type="error"
                title="ERROR"
                text={this.state.editorError.message}
                closeButton={false}
              />
            ) : null}
            <div className="dc-show-from-large">
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
          </div>
        </div>
        <Dialog show={this.state.showOverlay} onHide={this.handleHideOverlay}>
          <ViolationsResult
            pending={this.state.pending}
            complete={this.state.ajaxComplete}
            errorMsgText={this.state.error}
            externalId={this.state.externalId}
            violations={this.state.violations}
            successMsgTitle={this.state.successMsgTitle}
            successMsgText={this.state.successMsgText}
          />
        </Dialog>
      </div>
    );
  }
}
