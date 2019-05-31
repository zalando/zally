describe('oauth-firewall', () => {
  let mockMe, mockCreateAnonymousUser, firewall;

  beforeEach(() => {
    jest.resetModules();
    window.env = {};
    mockMe = jest.fn();
    mockCreateAnonymousUser = jest.fn();

    mockCreateAnonymousUser.mockReturnValue({
      username: 'anonymous',
    });

    jest.mock('../oauth-util', () => ({
      me: mockMe,
      createAnonymousUser: mockCreateAnonymousUser,
    }));
    firewall = require('../oauth-firewall').default;
  });

  afterEach(() => {
    delete window.env;
  });

  test('resolve immediately with a resolved promise if oauth is disabled', () => {
    window.env.OAUTH_ENABLED = false;
    return firewall().then(({ user }) => {
      expect(mockMe).not.toHaveBeenCalled();
      expect(mockCreateAnonymousUser).toHaveBeenCalled();
      expect(user).toBeDefined();
    });
  });

  test('resolve with error and anonymous user if "me" rejects', () => {
    window.env.OAUTH_ENABLED = true;
    const mockError = new Error('test "me" reject');

    mockMe.mockReturnValueOnce(Promise.reject(mockError));

    return firewall().then(({ user, error }) => {
      expect(mockMe).toHaveBeenCalled();
      expect(mockCreateAnonymousUser).toHaveBeenCalled();
      expect(user).toBeDefined();
      expect(error).toEqual(mockError);
    });
  });

  test('resolve with authenticated user', () => {
    window.env.OAUTH_ENABLED = true;
    const mockMeResponseBody = {
      username: 'foo',
      authenticated: true,
    };

    mockMe.mockReturnValueOnce(Promise.resolve(mockMeResponseBody));

    return firewall().then(({ user }) => {
      expect(mockMe).toHaveBeenCalled();
      expect(user).toEqual(mockMeResponseBody);
    });
  });
});
