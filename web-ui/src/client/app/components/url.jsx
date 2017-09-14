import React from 'react';

export function URLInputForm({
  pending,
  inputValue,
  onSubmit,
  onInputValueChange,
  onEditFile,
}) {
  const validateButtonIsDisabled = pending || !inputValue.trim();
  return (
    <form onSubmit={onSubmit} className="url-input-form">
      <label className="dc-label">Enter full path to your swagger file</label>
      <input
        className="dc-input dc-input--block"
        value={inputValue}
        onChange={onInputValueChange}
        type="url"
        name="path"
        placeholder="e.g https://raw.githubusercontent.com/OAI/OpenAPI-Specification/master/examples/v2.0/json/petstore.json"
        required
        pattern="https?://.+"
      />
      <button
        type="submit"
        disabled={validateButtonIsDisabled}
        className={
          'dc-btn dc-btn--primary  ' +
          (validateButtonIsDisabled ? 'dc-btn--disabled' : '')
        }
      >
        VALIDATE
      </button>
      <button
        type="button"
        disabled={validateButtonIsDisabled}
        onClick={onEditFile}
        className={
          'dc-btn dc-btn--secondary  ' +
          (validateButtonIsDisabled ? 'dc-btn--disabled' : '')
        }
      >
        EDIT
      </button>
    </form>
  );
}
