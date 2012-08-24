/** Creating user table **/
CREATE SEQUENCE calcentral_users_seq;

CREATE TABLE calcentral_users (
    id INTEGER DEFAULT NEXTVAL('calcentral_users_seq'),
    uid VARCHAR(255) NOT NULL,
    lastName VARCHAR(255),
    firstName VARCHAR(255),
    activeFlag BOOLEAN,
    PRIMARY KEY (id)
);

CREATE INDEX calcentral_users_index ON calcentral_users USING btree (id);

INSERT INTO calcentral_users(id, uid, lastName, firstName, activeFlag) VALUES (NEXTVAL('calcentral_users_seq'), '192517', 'Lin', 'Yu-Hung', TRUE);
INSERT INTO calcentral_users(id, uid, lastName, firstName, activeFlag) VALUES (NEXTVAL('calcentral_users_seq'), '978966', 'Vuerings', 'Christian', TRUE);
INSERT INTO calcentral_users(id, uid, lastName, firstName, activeFlag) VALUES (NEXTVAL('calcentral_users_seq'), '211159', 'Davis', 'Ray', TRUE);
INSERT INTO calcentral_users(id, uid, lastName, firstName, activeFlag) VALUES (NEXTVAL('calcentral_users_seq'), '2040', 'Heyer', 'Oliver', TRUE);
INSERT INTO calcentral_users(id, uid, lastName, firstName, activeFlag) VALUES (NEXTVAL('calcentral_users_seq'), '322279', 'Cochran', 'Eli', TRUE);
INSERT INTO calcentral_users(id, uid, lastName, firstName, activeFlag) VALUES (NEXTVAL('calcentral_users_seq'), '675750', 'Hollowgrass', 'Rachel', TRUE);
INSERT INTO calcentral_users(id, uid, lastName, firstName, activeFlag) VALUES (NEXTVAL('calcentral_users_seq'), '208861', 'Bloodworth', 'Allison', TRUE);
INSERT INTO calcentral_users(id, uid, lastName, firstName, activeFlag) VALUES (NEXTVAL('calcentral_users_seq'), '238382', 'Geuy', 'Bernadette', TRUE);
INSERT INTO calcentral_users(id, uid, lastName, firstName, activeFlag) VALUES (NEXTVAL('calcentral_users_seq'), '300846', 'Vuerings', 'Evil Christian', TRUE);