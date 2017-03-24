'use strict';

const chai = require('chai');
const expect = chai.expect;
const sinonChai = require('sinon-chai');
chai.use(sinonChai);

describe('server.logger', () => {

  /*eslint-disable no-unused-vars*/

  const logger = require('../../../src/server/logger');

  it('should export a logger', () => {
    expect(logger).to.be.a.object;
  });

  it('should have a log level', () => {
    expect(logger.level).to.exist;
  });
});
