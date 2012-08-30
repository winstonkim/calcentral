/** Creating user table **/
CREATE SEQUENCE calcentral_users_seq;

CREATE TABLE calcentral_users (
    id INTEGER DEFAULT NEXTVAL('calcentral_users_seq'),
    uid VARCHAR(255) NOT NULL,
    preferredName VARCHAR(255),
    link VARCHAR(255),
    firstLogin TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE INDEX calcentral_users_index ON calcentral_users USING btree (id);

/** Creating widget data table **/
CREATE SEQUENCE calcentral_widgetdata_seq;

CREATE TABLE calcentral_widgetdata (
    id INTEGER DEFAULT NEXTVAL('calcentral_widgetdata_seq'),
    uid VARCHAR(255) NOT NULL,
    widgetId VARCHAR(255) NOT NULL,
    data TEXT,
    PRIMARY KEY (id)
);

CREATE INDEX calcentral_widgetdata_index ON calcentral_widgetdata USING btree (id);
