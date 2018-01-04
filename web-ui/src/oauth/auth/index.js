'use strict';

const addPassport = require('./add-passport');
const router = require('./router');
const { requireLogin, authenticate } = require('./middlewares');
const { AuthError } = require('./errors');

const addAuth = app => {
  addPassport(app);
  app.use('/auth', router);
};

module.exports = {
  addAuth,
  requireLogin,
  authenticate,
  AuthError,
};
