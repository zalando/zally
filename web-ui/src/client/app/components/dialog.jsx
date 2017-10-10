import React from 'react';

export const Dialog = ({ show, children, onHide }) => {
  return (
    <div
      className={`dc-hide-from-large editor-dialog__backdrop ${show
        ? 'editor-dialog--show'
        : ''}`}
    >
      <div className="dc-overlay" />
      <div className="dc-dialog">
        <div className="dc-dialog__content editor-dialog__container">
          <div className="dc-dialog__body editor-dialog__body">{children}</div>
          <div className="editor-dialog__close" onClick={onHide}>
            <i className="dc-icon dc-icon--close dc-icon--interactive dc-dialog__close__icon" />
          </div>
        </div>
      </div>
    </div>
  );
};
