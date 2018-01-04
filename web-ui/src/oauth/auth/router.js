'use strict';

const { requireLogin, authenticate } = require('./middlewares');
const env = require('../../server/env');
const router = require('express')();
const bodyParser = require('body-parser');

router.get('/callback', authenticate({ successRedirect: '/' }));

router.get('/login', authenticate());

router.get('/logout', (req, res) => {
  req.logout();
  req.session = null;
  res.redirect('/');
});

router.use('/me', require('./me-handler'));

router.post(
  '/refresh-token',
  requireLogin(),
  bodyParser.json(),
  require('./refresh-token-handler')
);

module.exports = router;
