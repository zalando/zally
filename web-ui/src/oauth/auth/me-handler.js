'use strict';

const env = require('../../server/env');
const logger = require('../../server/logger');
const fetch = require('../fetch');

module.exports = function(req, res) {
  const url = env.OAUTH_TOKENINFO_URL;

  logger.debug(`request token info to: ${url}`);

  return fetch(url, {
    method: 'POST',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
      Authorization: `Bearer ${req.user ? req.user.accessToken : ''}`,
    },
  })
    .then(() => {
      return Promise.resolve({
        username: req.user.username,
        authenticated: true,
      });
    })
    .then(me => {
      res.json(me);
      return Promise.resolve(me);
    })
    .catch(error => {
      logger.warn('token info request failed', error);
      res.status(error.status || 401);
      res.json({
        title: error.message,
        detail: 'token info request failed',
      });
      return Promise.resolve(error);
    });
};
