/* global global */
/* eslint-disable no-console */
const { debug } = require('../debug');

describe('debug', () => {
  console.log = jest.fn();

  describe("don't log", () => {
    test("don't log if window.env.DEBUG is != true", () => {
      debug();
      expect(console.log).not.toHaveBeenCalled();
    });
  });

  describe('log', () => {
    beforeEach(() => {
      global.window.env = { DEBUG: true };
    });

    test('if window.env.DEBUG is == true', () => {
      console.debug = 'not a function';
      debug();
      expect(console.log).toHaveBeenCalled();
    });

    test('using console.debug, if window.env.DEBUG is == true and console.debug is a function', () => {
      console.debug = jest.fn();
      debug();
      expect(console.debug).toHaveBeenCalled();
    });
  });
});
