'use strict';

function ProblemError (error) {
  Error.captureStackTrace(this, this.constructor);
  this.type = error.type || 'about:blank';
  this.title = error.title;
  this.status = error.status;
  this.detail = error.detail;
  this.instance = error.instance;
}
require('util').inherits(ProblemError, Error);


module.exports = {
  ProblemError
};
