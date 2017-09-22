import React from 'react';
import 'brace';
import AceEditor from 'react-ace';

import 'brace/ext/searchbox';
import 'brace/mode/yaml';
import 'brace/mode/json';
import 'brace/theme/github';

export function ValidateButton({ disabled }) {
  return (
    <button
      type="submit"
      disabled={disabled}
      className={
        'dc-btn dc-btn--primary editor-input-form__button' +
        (disabled ? ' dc-btn--disabled' : '')
      }
    >
      VALIDATE
    </button>
  );
}

export function EditorInputForm({
  pending,
  error,
  onSubmit,
  value,
  annotations,
  onChange,
}) {
  const validateButtonIsDisabled = pending || error || !value.trim();

  return (
    <form onSubmit={onSubmit} className="editor-input-form">
      <label className="dc-label editor-input-form__label">
        Paste in a Swagger schema and click
      </label>
      <ValidateButton disabled={validateButtonIsDisabled} />
      <Editor annotations={annotations} onChange={onChange} value={value} />
      <div className="editor-input-form__bottom-button">
        <ValidateButton disabled={validateButtonIsDisabled} />
      </div>
    </form>
  );
}

export function Editor({ annotations, value, onChange }) {
  return (
    <div className="editor">
      <AceEditor
        className="editor__ace-editor"
        mode="yaml"
        theme="github"
        width="100%"
        annotations={annotations}
        showPrintMargin={false}
        value={value}
        onChange={onChange}
        editorProps={{ $blockScrolling: true }}
      />
    </div>
  );
}
