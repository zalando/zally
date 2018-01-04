'use strict';

module.exports = { simple };

function simple(clientId, clientSecret) {
  return done => {
    done(null, { clientId, clientSecret });
  };
}
