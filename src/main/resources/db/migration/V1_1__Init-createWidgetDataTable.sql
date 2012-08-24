/** Creating widget data table **/
CREATE SEQUENCE calcentral_widgetdata_seq;

CREATE TABLE calcentral_widgetdata (
    id INTEGER DEFAULT NEXTVAL('calcentral_widgetdata_seq'),
    uid INTEGER NOT NULL,
    widgetId VARCHAR(255) NOT NULL,
    data TEXT,
    PRIMARY KEY (id)
);

CREATE INDEX calcentral_widgetdata_index ON calcentral_widgetdata USING btree (id);
