'use strict';

const chai = require('chai');
const expect = chai.expect;
const sinon = require('sinon');
const sinonChai = require('sinon-chai');
const proxyquire = require('proxyquire').noCallThru();
chai.use(sinonChai);

describe('server.fetch', () => {

  let nodeFetch, fetch, fetchResponse, nodeFetchError, fetchError, fetchErrorResponse;

  before(() => {

    nodeFetch = sinon.spy(() => {
      return Promise.resolve({
        status: 200 ,
        statusText: 'Success'
      });
    });

    fetch = proxyquire('../../../src/server/fetch', {
      'node-fetch': nodeFetch
    });

    fetchResponse = fetch();


  });

  it('should export a function', () => {
    expect(fetch).to.be.a.function;
  });

  describe('when invoking the function', () => {
    it('should respond with the data', (done) => {
      fetchResponse.then((response) => {
        expect(response.status).to.be.equal(200);
        done();
      });
    });
  });

  before(() => {

    nodeFetchError = sinon.spy(() => {
      return Promise.resolve({
        status: 500 ,
        statusText: 'Error Message'
      });
    });

    fetchError = proxyquire('../../../src/server/fetch', {
      'node-fetch': nodeFetchError
    });

    fetchErrorResponse = fetchError();
  });

  describe('when invoking the function and getting an error from fetch', () => {
    it('should respond with error', (done) => {
      fetchErrorResponse.catch((response) => {
        expect(response.status).to.be.equal(500);
        done();
      });
    });
  });

});
