import React from 'react';
import yaml from 'js-yaml';
import { Msg } from '../components/msg.jsx';
import { Violations } from './violations.jsx';
import { ViolationsResult } from '../components/violations.jsx';
import { EditorInputForm } from '../components/editor.jsx';
import { Dialog } from '../components/dialog.jsx';

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
    this.state.editorDirty = true;
    this.state.editorValue = this.Storage.getItem('editor-value') || '';
    this.handleOnInputValueChange = this.handleOnInputValueChange.bind(this);
    this.handleFormSubmit = this.handleFormSubmit.bind(this);
    this.handleHideOverlay = this.handleHideOverlay.bind(this);
  }

  componentDidMount() {
    this.updateInputValue(this.state.editorValue);
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
    try {
      const inputValue = yaml.load(value);
      this.setState({
        inputValue: inputValue,
        editorDirty: true,
        editorError: null,
        editorAnnotations: [],
      });
    } catch (e) {
      console.warn(e); // eslint-disable-line no-console
      this.setState({
        inputValue: null,
        editorError: e,
        editorAnnotations: editorErrorToAnnotations(e),
      });
    }
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
            violations={this.state.violations}
            successMsgTitle={this.state.successMsgTitle}
            successMsgText={this.state.successMsgText}
          />
        </Dialog>
      </div>
    );
  }
}
