'use strict';

const passport = require('passport');
const env = require('../../server/env');
const { AuthError } = require('./errors');

const requireLogin = () => {
  return (req, res, next) => {
    if (!env.OAUTH_ENABLED) {
      next();
      return;
    }

    if (!req.user) {
      next(new AuthError('Authentication Failed'));
      return;
    }

    if (!req.user.accessToken) {
      next(
        new AuthError(
          'Authentication Failed - access token not present in the current user session'
        )
      );
      return;
    }

    req.headers['Authorization'] = `Bearer ${req.user.accessToken}`;

    next();
  };
};

const authenticate = options => {
  return passport.authenticate('zalando', options);
};

module.exports = {
  requireLogin,
  authenticate,
};
