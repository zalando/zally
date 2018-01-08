import * as child_process from 'child_process';
import * as util from 'util';
import { ZalandoOAuthStrategy } from './';

export function ZTokenOAuthStrategy(options, verify) {
  ZalandoOAuthStrategy.call(this, options, verify);
}

util.inherits(ZTokenOAuthStrategy, ZalandoOAuthStrategy);

ZTokenOAuthStrategy.prototype.authenticate = function(req, options) {
  const self = this;
  const params = self.tokenParams(options);

  child_process.exec('ztoken', (err, stdout, stderr) => {
    if (err) return self.error(err);
    if (!stdout) return self.fail(stderr);

    const accessToken = stdout.trim();

    self._loadUserProfile(accessToken, function(err, profile) {
      if (err) return self.error(err);

      function verified(err, user, info) {
        if (err) return self.error(err);
        if (!user) return self.fail(info);
        self.success(user, info || {});
      }

      try {
        if (self._passReqToCallback) {
          const arity = self._verify.length;
          if (arity === 6) {
            self._verify(req, accessToken, null, params, profile, verified);
          } else {
            // arity == 5
            self._verify(req, accessToken, null, profile, verified);
          }
        } else {
          const arity = self._verify.length;
          if (arity === 5) {
            self._verify(accessToken, null, params, profile, verified);
          } else {
            // arity == 4
            self._verify(accessToken, null, profile, verified);
          }
        }
      } catch (ex) {
        return self.error(ex);
      }
    });
  });
};
