/** Creating user table **/
CREATE SEQUENCE calcentral_users_seq;

CREATE TABLE calcentral_users (
    id INTEGER DEFAULT NEXTVAL('calcentral_users_seq'),
    uid INTEGER NOT NULL,
    lastName VARCHAR(255),
    firstName VARCHAR(255),
    activeFlag BOOLEAN,
    PRIMARY KEY (id)
);

CREATE INDEX calcentral_users_index ON calcentral_users USING btree (id);

INSERT INTO calcentral_users(id, uid, lastName, firstName, activeFlag) VALUES (NEXTVAL('calcentral_users_seq'), 192517, 'Lin', 'Yu-Hung', TRUE);
INSERT INTO calcentral_users(id, uid, lastName, firstName, activeFlag) VALUES (NEXTVAL('calcentral_users_seq'), 978966, 'Vuerings', 'Christian', TRUE);
INSERT INTO calcentral_users(id, uid, lastName, firstName, activeFlag) VALUES (NEXTVAL('calcentral_users_seq'), 211159, 'Davis', 'Ray', TRUE);