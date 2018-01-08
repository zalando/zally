export const ZalandoProfile = { parse };

function parse(body) {
  let json = JSON.parse(body);
  return {
    id: json['uid'],
    name: json['cn'],
    provider: 'zalando',
    _raw: body,
    _json: json,
  };
}
