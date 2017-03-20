'use strict';

const chai = require('chai');
const expect = chai.expect;
const sinonChai = require('sinon-chai');
chai.use(sinonChai);

describe('server.env', () => {

  /*eslint-disable no-unused-vars*/

  const env = require('../../../src/server/env');

  it('should be defined', function (){
    expect(env).to.exist;
  });

  it('should expose a public function', function (){
    expect(env.public).to.be.a.function;
  });

  describe('when invoking the public function', function (){
    it('should expose object containing public keys', function (){
      expect(env.public()).to.be.a.object;
    });
  });

});
