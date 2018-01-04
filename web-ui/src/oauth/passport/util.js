'use strict';

const updateSessionUser = (req, user = {}) => {
  req.session.passport.user = Object.assign(
    {},
    req.session.passport.user,
    user
  );
};

module.exports = { updateSessionUser };
