const fs = require('fs');

export const CredentialsLoader = { stups, kubernetes };

function stups(clientJsonPath) {
  return done => {
    syncFile(clientJsonPath, (err, data) => {
      if (err) return done(err);
      const json = JSON.parse(data),
        credentials = {
          clientId: json['client_id'],
          clientSecret: json['client_secret'],
        };
      done(null, credentials);
    });
  };
}

function kubernetes(clientIdPath, clientSecretPath) {
  return done => {
    syncFile(
      clientIdPath,
      (err, clientId) => (err ? done(err) : done(null, { clientId }))
    );
    syncFile(
      clientSecretPath,
      (err, clientSecret) => (err ? done(err) : done(null, { clientSecret }))
    );
  };
}

function syncFile(file, done) {
  let fileCallback = (err, data) =>
      err ? done(err) : done(null, data.replace(/\n$/, '')),
    fileListener = () => fs.readFile(file, 'utf8', fileCallback),
    watcher = fs.watchFile(file, fileListener);
  watcher.emit('change');
}
