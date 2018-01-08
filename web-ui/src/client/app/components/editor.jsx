import React from 'react';
import 'brace';
import AceEditor from 'react-ace';

import 'brace/mode/yaml';
import 'brace/mode/json';
import 'brace/theme/chrome';
import 'brace/ext/language_tools';
import 'brace/ext/searchbox';
import 'brace/snippets/yaml';
import 'brace/snippets/json';

export function ValidateButton({ disabled, dirty }) {
  return (
    <button
      type="submit"
      disabled={disabled}
      className={
        'dc-btn dc-btn--primary editor-input-form__button' +
        (disabled ? ' dc-btn--disabled' : '')
      }
    >
      <span className="dc-hide-from-large">
        {dirty ? 'VALIDATE' : 'SEE RESULTS'}
      </span>
      <span className="dc-show-from-large">VALIDATE</span>
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
  dirty,
}) {
  const validateButtonIsDisabled = pending || error || !value.trim();

  return (
    <form onSubmit={onSubmit} className="editor-input-form">
      <div className="dc-row">
        <div className="dc-column">
          <label className="dc-label editor-input-form__label">
            Paste in a Swagger schema and click
          </label>
        </div>
        <div className="dc-column dc-column--shrink">
          <ValidateButton
            disabled={validateButtonIsDisabled}
            dirty={dirty || pending}
          />
        </div>
      </div>
      <Editor annotations={annotations} onChange={onChange} value={value} />
    </form>
  );
}

const jsonRegex = /^[ \r\n\t]*[{[]/;

export function Editor({ annotations, value, onChange }) {
  const mode = jsonRegex.test(value) ? 'json' : 'yaml';
  return (
    <div className="editor">
      <AceEditor
        className="editor__ace-editor"
        mode={mode}
        theme="chrome"
        width="100%"
        height="100%"
        annotations={annotations}
        enableBasicAutocompletion
        enableLiveAutocompletion
        showPrintMargin={false}
        value={value}
        onChange={onChange}
        editorProps={{ $blockScrolling: true, enableSnippets: true }}
      />
    </div>
  );
}
