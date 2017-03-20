'use strict';

const chai = require('chai');
const expect = chai.expect;
const sinon = require('sinon');
const sinonChai = require('sinon-chai');
const proxyquire = require('proxyquire');
chai.use(sinonChai);

describe('server.tokeninfo-handler', () => {

  let env, request, logger, tokeninfoHandler, req, res;

  before(() => {

    env = {
      'OAUTH_TOKENINFO_URL': 'tokenInfoUrl'
    };

    request = sinon.spy();

    logger = {
      debug: () => {}
    };

    req = {
      pipe: () => {
        return req;
      }
    };

    res = {};

    sinon.spy(logger, 'debug');
    sinon.spy(req, 'pipe');

    tokeninfoHandler = proxyquire('../../../src/server/tokeninfo-handler',{
      './env': env,
      'request': request,
      './logger': logger
    });

    tokeninfoHandler(req, res);


  });

  it('should export a function', () => {
    expect(tokeninfoHandler).to.be.a.function;
  });

  describe('when invoking the function', () => {

    it('should log the debug message', () => {
      expect(logger.debug).to.have.been.calledOnce;
    });

    it('should read the URL from env.OAUTH_TOKENINFO_URL', () => {
      expect(request).to.have.been.calledOnce;
      expect(request).to.have.been.calledWith('tokenInfoUrl');
    });

    it('should proxy the request', () => {
      expect(req.pipe).to.have.been.calledTwice;
    });
  });
});
