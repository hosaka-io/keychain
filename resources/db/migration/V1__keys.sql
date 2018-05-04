CREATE TABLE keychain.keys (
  kid text NOT NULL,
  key_data text NOT NULL,
  can_add boolean NOT NULL DEFAULT false,
  created_on timestamp without time zone NOT NULL DEFAULT now(),
  PRIMARY KEY (kid)
);
