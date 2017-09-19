import React from 'react';

export function Login({ user, login }) {
  return (
    <div style={{ textAlign: 'center' }}>
      {!user.authenticated ? (
        <p data-test-id="not-authenticated">
          You should{' '}
          <span className="dc-link" onClick={login}>
            login
          </span>{' '}
          to access Zally API Linter
          <br />
          <br />
          <button
            type="button"
            className="dc-btn dc-btn--primary"
            onClick={login}
          >
            LOGIN
          </button>
        </p>
      ) : (
        <p data-test-id="authenticated">You are logged in!</p>
      )}
    </div>
  );
}
