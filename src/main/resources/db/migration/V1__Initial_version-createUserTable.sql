/** Creating user table **/
CREATE SEQUENCE calcentral_users_seq;

CREATE TABLE calcentral_users (
    id INTEGER DEFAULT NEXTVAL('calcentral_users_seq'),
    uid VARCHAR(255) NOT NULL,
    preferredName VARCHAR(255),
    link VARCHAR(255),
    profileImageLink TEXT,
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

/** Creating user OAuth2 tokens table **/
CREATE SEQUENCE calcentral_oauth2_seq;

CREATE TABLE calcentral_oauth2 (
    id INTEGER DEFAULT NEXTVAL('calcentral_oauth2_seq'),
    uid VARCHAR(255) NOT NULL,
    appId VARCHAR(255) NOT NULL,
    token TEXT,
    refreshToken TEXT,
    expirationTime BIGINT,
    PRIMARY KEY (id)
);

CREATE INDEX calcentral_oauth2_index ON calcentral_oauth2 USING btree (id);
CREATE UNIQUE INDEX calcentral_oauth2_user_app_index ON calcentral_oauth2 USING btree (uid, appId);

/** Create and populate colleges metadata table **/

CREATE TABLE calcentral_classtree_colleges (
    slug character varying(128) NOT NULL,
    title_prefix character varying(128),
    title character varying(128),
    cssclass character varying(128),
    id integer NOT NULL
);

INSERT INTO calcentral_classtree_colleges VALUES ('collegeoflettersscienceartshumanities', 'College of Letters & Science', 'Arts & Humanities', 'letters-and-science', 1);
INSERT INTO calcentral_classtree_colleges VALUES ('collegeofletterssciencebiologicalsciences', 'College of Letters & Science', 'Biological Sciences', 'letters-and-science', 2);
INSERT INTO calcentral_classtree_colleges VALUES ('haasschoolofbusiness', 'Haas School of', 'Business', NULL, 3);
INSERT INTO calcentral_classtree_colleges VALUES ('collegeofchemistry', 'College of', 'Chemistry', NULL, 4);
INSERT INTO calcentral_classtree_colleges VALUES ('graduateschoolofeducation', 'Graduate School of', 'Education', 'graduate', 5);
INSERT INTO calcentral_classtree_colleges VALUES ('collegeofengineering', 'College of', 'Engineering', NULL, 6);
INSERT INTO calcentral_classtree_colleges VALUES ('collegeofenvironmentaldesign', 'College of', 'Environmental Design', NULL, 7);
INSERT INTO calcentral_classtree_colleges VALUES ('schoolofinformation', 'School of', 'Information', NULL, 8);
INSERT INTO calcentral_classtree_colleges VALUES ('interdepartmentalgraduateprograms', 'Interdepartmental', 'Graduate Programs', 'graduate', 9);
INSERT INTO calcentral_classtree_colleges VALUES ('graduateschoolofjournalism', 'Graduate School of', 'Journalism', NULL, 10);
INSERT INTO calcentral_classtree_colleges VALUES ('schooloflaw', 'School of', 'Law', NULL, 11);
INSERT INTO calcentral_classtree_colleges VALUES ('collegeofletterssciencemathematicalphysicalsciences', 'College of Letters & Science', 'Mathematical & Physical Sciences', 'letters-and-science', 12);
INSERT INTO calcentral_classtree_colleges VALUES ('collegeofnaturalresources', 'College of', 'Natural Resources', NULL, 13);
INSERT INTO calcentral_classtree_colleges VALUES ('schoolofoptometry', 'School of', 'Optometry', NULL, 14);
INSERT INTO calcentral_classtree_colleges VALUES ('schoolofpublichealth', 'School of', 'Public Health', NULL, 15);
INSERT INTO calcentral_classtree_colleges VALUES ('goldmanschoolofpublicpolicy', 'Goldman School of', 'Public Policy', NULL, 16);
INSERT INTO calcentral_classtree_colleges VALUES ('collegeofletterssciencesocialsciences', 'College of Letters & Science', 'Social Sciences', 'letters-and-science', 17);
INSERT INTO calcentral_classtree_colleges VALUES ('schoolofsocialwelfare', 'School of', 'Social Welfare', NULL, 18);
INSERT INTO calcentral_classtree_colleges VALUES ('undergraduateinterdisciplinarystudies', 'College of Letters & Science', 'Undergraduate & Interdisciplinary Studies', 'letters-and-science', 19);

ALTER TABLE ONLY calcentral_classtree_colleges
    ADD CONSTRAINT calcentral_classtree_colleges_pkey PRIMARY KEY (id);

CREATE UNIQUE INDEX calcentral_classtree_colleges_id ON calcentral_classtree_colleges USING btree (id);

/** Create and populate departments metadata table **/

CREATE TABLE calcentral_classtree_departments (
    dept_key character varying(12) NOT NULL,
    title character varying(48),
    college_id integer NOT NULL,
    id integer NOT NULL
);

INSERT INTO calcentral_classtree_departments VALUES ('AHMA', 'Ancient History & Mediterranean Archaeology', 1, 1);
INSERT INTO calcentral_classtree_departments VALUES ('ARABIC', 'Arabic', 1, 2);
INSERT INTO calcentral_classtree_departments VALUES ('ASIANST', 'Asian Studies', 1, 3);
INSERT INTO calcentral_classtree_departments VALUES ('CATALAN', 'Catalan', 1, 4);
INSERT INTO calcentral_classtree_departments VALUES ('CELTIC', 'Celtic Studies', 1, 5);
INSERT INTO calcentral_classtree_departments VALUES ('CHINESE', 'Chinese', 1, 6);
INSERT INTO calcentral_classtree_departments VALUES ('CLASSIC', 'Classics', 1, 7);
INSERT INTO calcentral_classtree_departments VALUES ('COM LIT', 'Comparative Literature', 1, 8);
INSERT INTO calcentral_classtree_departments VALUES ('CUNEIF', 'Cuneiform', 1, 9);
INSERT INTO calcentral_classtree_departments VALUES ('DUTCH', 'Dutch', 1, 10);
INSERT INTO calcentral_classtree_departments VALUES ('EA LANG', 'East Asian Languages and Cultures', 1, 11);
INSERT INTO calcentral_classtree_departments VALUES ('EAEURST', 'East European Studies', 1, 12);
INSERT INTO calcentral_classtree_departments VALUES ('EGYPT', 'Egyptian', 1, 13);
INSERT INTO calcentral_classtree_departments VALUES ('ENGLISH', 'English', 1, 14);
INSERT INTO calcentral_classtree_departments VALUES ('FILIPN', 'Filipino', 1, 15);
INSERT INTO calcentral_classtree_departments VALUES ('FILM', 'Film and Media', 1, 16);
INSERT INTO calcentral_classtree_departments VALUES ('FRENCH', 'French', 1, 17);
INSERT INTO calcentral_classtree_departments VALUES ('GERMAN', 'German', 1, 18);
INSERT INTO calcentral_classtree_departments VALUES ('GREEK', 'Greek', 1, 19);
INSERT INTO calcentral_classtree_departments VALUES ('HEBREW', 'Hebrew', 1, 20);
INSERT INTO calcentral_classtree_departments VALUES ('HIN-URD', 'Hindi-Urdu', 1, 21);
INSERT INTO calcentral_classtree_departments VALUES ('HISTART', 'History of Art', 1, 22);
INSERT INTO calcentral_classtree_departments VALUES ('ITALIAN', 'Italian', 1, 23);
INSERT INTO calcentral_classtree_departments VALUES ('JAPAN', 'Japanese', 1, 24);
INSERT INTO calcentral_classtree_departments VALUES ('JEWISH', 'Jewish Studies', 1, 25);
INSERT INTO calcentral_classtree_departments VALUES ('KHMER', 'Khmer', 1, 26);
INSERT INTO calcentral_classtree_departments VALUES ('KOREAN', 'Korean', 1, 27);
INSERT INTO calcentral_classtree_departments VALUES ('LATIN', 'Latin', 1, 28);
INSERT INTO calcentral_classtree_departments VALUES ('MALAY/I', 'Malay/Indonesian', 1, 29);
INSERT INTO calcentral_classtree_departments VALUES ('MED ST', 'Medieval Studies', 1, 30);
INSERT INTO calcentral_classtree_departments VALUES ('MUSIC', 'Music', 1, 31);
INSERT INTO calcentral_classtree_departments VALUES ('NE STUD', 'Near Eastern Studies', 1, 32);
INSERT INTO calcentral_classtree_departments VALUES ('PERSIAN', 'Persian', 1, 33);
INSERT INTO calcentral_classtree_departments VALUES ('PHILOS', 'Philosophy', 1, 34);
INSERT INTO calcentral_classtree_departments VALUES ('ART', 'Practice of Art', 1, 35);
INSERT INTO calcentral_classtree_departments VALUES ('PUNJABI', 'Punjabi', 1, 37);
INSERT INTO calcentral_classtree_departments VALUES ('RHETOR', 'Rhetoric', 1, 38);
INSERT INTO calcentral_classtree_departments VALUES ('SANSKR', 'Sanskrit', 1, 39);
INSERT INTO calcentral_classtree_departments VALUES ('SCANDIN', 'Scandinavian', 1, 40);
INSERT INTO calcentral_classtree_departments VALUES ('SLAVIC', 'Slavic Languages and Literatures', 1, 41);
INSERT INTO calcentral_classtree_departments VALUES ('S,SEASN', 'South and Southeast Asian Studies', 1, 42);
INSERT INTO calcentral_classtree_departments VALUES ('SPANISH', 'Spanish', 1, 43);
INSERT INTO calcentral_classtree_departments VALUES ('TAMIL', 'Tamil', 1, 45);
INSERT INTO calcentral_classtree_departments VALUES ('TELUGU', 'Telugu', 1, 46);
INSERT INTO calcentral_classtree_departments VALUES ('THAI', 'Thai', 1, 47);
INSERT INTO calcentral_classtree_departments VALUES ('THEATER', 'Theater, Dance, and Performance Studies', 1, 48);
INSERT INTO calcentral_classtree_departments VALUES ('TIBETAN', 'Tibetan', 1, 49);
INSERT INTO calcentral_classtree_departments VALUES ('TURKISH', 'Turkish', 1, 50);
INSERT INTO calcentral_classtree_departments VALUES ('VIETNMS', 'Vietnamese', 1, 51);
INSERT INTO calcentral_classtree_departments VALUES ('YIDDISH', 'Yiddish', 1, 52);
INSERT INTO calcentral_classtree_departments VALUES ('BIOLOGY', 'Biology', 2, 53);
INSERT INTO calcentral_classtree_departments VALUES ('BIOPHY', 'Biophysics', 2, 54);
INSERT INTO calcentral_classtree_departments VALUES ('INTEGBI', 'Integrative Biology', 2, 55);
INSERT INTO calcentral_classtree_departments VALUES ('MCELLBI', 'Molecular and Cell Biology', 2, 56);
INSERT INTO calcentral_classtree_departments VALUES ('PHYS ED', 'Physical Education', 2, 57);
INSERT INTO calcentral_classtree_departments VALUES ('EWMBA', 'Eve/Wknd Masters in Bus. Adm.', 3, 58);
INSERT INTO calcentral_classtree_departments VALUES ('XMBA', 'Executive Masters in Bus. Adm.', 3, 59);
INSERT INTO calcentral_classtree_departments VALUES ('MBA', 'Masters in Business Administration', 3, 60);
INSERT INTO calcentral_classtree_departments VALUES ('MFE', 'Masters in Financial Engineering', 3, 61);
INSERT INTO calcentral_classtree_departments VALUES ('PHDBA', 'Ph.D. in Business Administration', 3, 62);
INSERT INTO calcentral_classtree_departments VALUES ('UGBA', 'Undergrad. Business Administration', 3, 63);
INSERT INTO calcentral_classtree_departments VALUES ('CHM ENG', 'Chemical & Biomolecular Engineering', 4, 64);
INSERT INTO calcentral_classtree_departments VALUES ('CHEM', 'Chemistry', 4, 65);
INSERT INTO calcentral_classtree_departments VALUES ('MAT SCI', 'Materials Science & Engineering', 4, 66);
INSERT INTO calcentral_classtree_departments VALUES ('NUC ENG', 'Nuclear Engineering', 4, 67);
INSERT INTO calcentral_classtree_departments VALUES ('EDUC', 'Education', 5, 68);
INSERT INTO calcentral_classtree_departments VALUES ('SCMATHE', 'Science & Mathematics Education', 5, 69);
INSERT INTO calcentral_classtree_departments VALUES ('AST', 'Applied Science & Technology', 6, 70);
INSERT INTO calcentral_classtree_departments VALUES ('BIO ENG', 'Bioengineering', 6, 71);
INSERT INTO calcentral_classtree_departments VALUES ('CIV ENG', 'Civil & Environmental Engineering', 6, 72);
INSERT INTO calcentral_classtree_departments VALUES ('COMPSCI', 'Computer Science', 6, 73);
INSERT INTO calcentral_classtree_departments VALUES ('EL ENG', 'Electrical Engineering', 6, 74);
INSERT INTO calcentral_classtree_departments VALUES ('ENGIN', 'Engineering', 6, 75);
INSERT INTO calcentral_classtree_departments VALUES ('IND ENG', 'Industrial Engineering & Operations Research', 6, 76);
INSERT INTO calcentral_classtree_departments VALUES ('MEC ENG', 'Mechanical Engineering', 6, 77);
INSERT INTO calcentral_classtree_departments VALUES ('NSE', 'Nanoscale Science & Engineering', 6, 78);
INSERT INTO calcentral_classtree_departments VALUES ('ARCH', 'Architecture', 7, 79);
INSERT INTO calcentral_classtree_departments VALUES ('CY PLAN', 'City & Regional Planning', 7, 80);
INSERT INTO calcentral_classtree_departments VALUES ('ENV DES', 'Environmental Design', 7, 81);
INSERT INTO calcentral_classtree_departments VALUES ('LD ARCH', 'Landscape Architecture', 7, 82);
INSERT INTO calcentral_classtree_departments VALUES ('VIS STD', 'Visual Studies', 7, 83);
INSERT INTO calcentral_classtree_departments VALUES ('INFO', 'Information', 8, 84);
INSERT INTO calcentral_classtree_departments VALUES ('COMPBIO', 'Comparative Biochemistry', 9, 85);
INSERT INTO calcentral_classtree_departments VALUES ('ENE,RES', 'Energy & Resources Group', 9, 86);
INSERT INTO calcentral_classtree_departments VALUES ('JOURN', 'Journalism', 10, 87);
INSERT INTO calcentral_classtree_departments VALUES ('LAW', 'Law', 11, 88);
INSERT INTO calcentral_classtree_departments VALUES ('LEGALST', 'Legal Studies', 11, 89);
INSERT INTO calcentral_classtree_departments VALUES ('ASTRON', 'Astronomy', 12, 90);
INSERT INTO calcentral_classtree_departments VALUES ('EPS', 'Earth & Planetary Science', 12, 91);
INSERT INTO calcentral_classtree_departments VALUES ('PHYSICS', 'Physics', 12, 92);
INSERT INTO calcentral_classtree_departments VALUES ('STAT', 'Statistics', 12, 93);
INSERT INTO calcentral_classtree_departments VALUES ('A,RESEC', 'Agricultural & Resource Economics', 13, 94);
INSERT INTO calcentral_classtree_departments VALUES ('ESPM', 'Environmental Science Policy & Management', 13, 95);
INSERT INTO calcentral_classtree_departments VALUES ('ENVECON', 'Environmental Economics & Policy', 13, 96);
INSERT INTO calcentral_classtree_departments VALUES ('NAT RES', 'Natural Resources', 13, 97);
INSERT INTO calcentral_classtree_departments VALUES ('NUSCTX', 'Nutritional Sciences & Toxicology', 13, 98);
INSERT INTO calcentral_classtree_departments VALUES ('PLANTBI', 'Plant & Microbial Biology', 13, 99);
INSERT INTO calcentral_classtree_departments VALUES ('OPTOM', 'Optometry', 14, 100);
INSERT INTO calcentral_classtree_departments VALUES ('VIS SCI', 'Vision Science', 14, 101);
INSERT INTO calcentral_classtree_departments VALUES ('HMEDSCI', 'Health & Medical Sciences', 15, 102);
INSERT INTO calcentral_classtree_departments VALUES ('PB HLTH', 'Public Health', 15, 103);
INSERT INTO calcentral_classtree_departments VALUES ('PUB POL', 'Public Policy', 16, 104);
INSERT INTO calcentral_classtree_departments VALUES ('AFRICAM', 'African American Studies', 17, 105);
INSERT INTO calcentral_classtree_departments VALUES ('ANTHRO', 'Anthropology', 17, 106);
INSERT INTO calcentral_classtree_departments VALUES ('DEMOG', 'Demography', 17, 107);
INSERT INTO calcentral_classtree_departments VALUES ('ECON', 'Economics', 17, 108);
INSERT INTO calcentral_classtree_departments VALUES ('ETH STD', 'Ethnic Studies', 17, 109);
INSERT INTO calcentral_classtree_departments VALUES ('ETH GRP', 'Ethnic Studies Graduate Group', 17, 110);
INSERT INTO calcentral_classtree_departments VALUES ('GWS', 'Gender and Women''s Studies', 17, 111);
INSERT INTO calcentral_classtree_departments VALUES ('GEOG', 'Geography', 17, 112);
INSERT INTO calcentral_classtree_departments VALUES ('HISTORY', 'History', 17, 113);
INSERT INTO calcentral_classtree_departments VALUES ('LINGUIS', 'Linguistics', 17, 114);
INSERT INTO calcentral_classtree_departments VALUES ('POL SCI', 'Political Science', 17, 115);
INSERT INTO calcentral_classtree_departments VALUES ('PORTUG', 'Portuguese', 17, 116);
INSERT INTO calcentral_classtree_departments VALUES ('PSYCH', 'Psychology', 17, 117);
INSERT INTO calcentral_classtree_departments VALUES ('SOCIOL', 'Sociology', 17, 118);
INSERT INTO calcentral_classtree_departments VALUES ('SOC WEL', 'Social Welfare', 18, 119);
INSERT INTO calcentral_classtree_departments VALUES ('AEROSPC', 'Aerospace Studies', 19, 120);
INSERT INTO calcentral_classtree_departments VALUES ('AMERSTD', 'American Studies', 19, 121);
INSERT INTO calcentral_classtree_departments VALUES ('ASAMST', 'Asian American Studies', 19, 122);
INSERT INTO calcentral_classtree_departments VALUES ('BANGLA', 'Bengali', 19, 123);
INSERT INTO calcentral_classtree_departments VALUES ('CHICANO', 'Chicano Studies', 19, 124);
INSERT INTO calcentral_classtree_departments VALUES ('COG SCI', 'Cognitive Science', 19, 125);
INSERT INTO calcentral_classtree_departments VALUES ('COLWRIT', 'College Writing Program', 19, 126);
INSERT INTO calcentral_classtree_departments VALUES ('DEV STD', 'Development Studies', 19, 127);
INSERT INTO calcentral_classtree_departments VALUES ('ENV SCI', 'Environmental Sciences', 19, 128);
INSERT INTO calcentral_classtree_departments VALUES ('ISF', 'Interdisciplinary Studies', 19, 129);
INSERT INTO calcentral_classtree_departments VALUES ('MEDIAST', 'Media Studies', 19, 130);
INSERT INTO calcentral_classtree_departments VALUES ('M E STU', 'Middle Eastern Studies', 19, 131);
INSERT INTO calcentral_classtree_departments VALUES ('MIL AFF', 'Military Affairs', 19, 132);
INSERT INTO calcentral_classtree_departments VALUES ('MIL SCI', 'Military Science', 19, 133);
INSERT INTO calcentral_classtree_departments VALUES ('NATAMST', 'Native American Studies', 19, 134);
INSERT INTO calcentral_classtree_departments VALUES ('NAV SCI', 'Naval Science', 19, 135);
INSERT INTO calcentral_classtree_departments VALUES ('NWMEDIA', 'New Media', 19, 136);
INSERT INTO calcentral_classtree_departments VALUES ('PACS', 'Peace & Conflict Studies', 19, 137);
INSERT INTO calcentral_classtree_departments VALUES ('POLECON', 'Political Economy of Industrial Societies', 19, 138);
INSERT INTO calcentral_classtree_departments VALUES ('RELIGST', 'Religious Studies', 19, 140);
INSERT INTO calcentral_classtree_departments VALUES ('MATH', 'Mathematics', 12, 141);

ALTER TABLE ONLY calcentral_classtree_departments
    ADD CONSTRAINT calcentral_classtree_departments_pkey PRIMARY KEY (id);

CREATE INDEX calcentral_classtree_departments_college_id_index ON calcentral_classtree_departments USING btree (college_id);

CREATE UNIQUE INDEX calcentral_classtree_departments_id_index ON calcentral_classtree_departments USING btree (id);

-- Add profile URLs for selected instructors.
INSERT INTO calcentral_users (uid, link) VALUES ('191779', 'http://birdhouse.org/blog/');
INSERT INTO calcentral_users (uid, profileimagelink) values ('300883', 'https://ucberkeley.instructure.com/images/thumbnails/21446477/rrV3CW9MapIJBpYMhRrWFth6tqW5xX2NG1EMatL5');

-- Add classpages lookup table and initial data

CREATE SEQUENCE calcentral_classpages_localdata_seq;

CREATE TABLE calcentral_classpages_localdata (
	classPageId VARCHAR(20) NOT NULL,
	webcastId VARCHAR(255) DEFAULT NULL,
	canvasCourseId VARCHAR(255) DEFAULT NULL, 
	id INTEGER DEFAULT NEXTVAL('calcentral_classpages_localdata_seq'),
	PRIMARY KEY (id)
);

CREATE UNIQUE INDEX calcentral_classpages_localdata_index ON calcentral_classpages_localdata USING btree(classPageId);

INSERT INTO calcentral_classpages_localdata VALUES ('2012D07058','PL-XXv-cvA_iBiOpo1VZM0IdEzZbMPkdze');
INSERT INTO calcentral_classpages_localdata VALUES ('2012D7303','EC4B95C1CB493BCC18');
INSERT INTO calcentral_classpages_localdata VALUES ('2012D7402','EC97B4BB33E4D16505');
INSERT INTO calcentral_classpages_localdata VALUES ('2012D7495','ECFBA8ADB24BCABACF');
INSERT INTO calcentral_classpages_localdata VALUES ('2012D11003','EC22261BB8885D7B73');
INSERT INTO calcentral_classpages_localdata VALUES ('2012D11306','ECB6993AFF1CEA7BF9');
INSERT INTO calcentral_classpages_localdata VALUES ('2012D26127','ECF15FD0984DACED4F');
INSERT INTO calcentral_classpages_localdata VALUES ('2012D26547','ECB3A2DB612736B98A');
INSERT INTO calcentral_classpages_localdata VALUES ('2012D26580','EC-XXv-cvA_iDTKE56ZRv92RJNnLmy2aZh');
INSERT INTO calcentral_classpages_localdata VALUES ('2012D26622','PL-XXv-cvA_iBifi0GQVF1R9M_QBWw3xgG');
INSERT INTO calcentral_classpages_localdata VALUES ('2012D26190','EC87898FD0A141069E');
INSERT INTO calcentral_classpages_localdata VALUES ('2012D26289','EC352C71372073F900');
INSERT INTO calcentral_classpages_localdata VALUES ('2012D26427','EC1A2EBAC4283FE3EA');
INSERT INTO calcentral_classpages_localdata VALUES ('2012D19030','ECE9D3A91B1948E289');
INSERT INTO calcentral_classpages_localdata VALUES ('2012D22378','EC7C1A46DC5C45F130');
INSERT INTO calcentral_classpages_localdata VALUES ('2012D23701','ECB713CC75E480C2A3');
INSERT INTO calcentral_classpages_localdata VALUES ('2012D25195','ECC9EE10FC67D122E1');
INSERT INTO calcentral_classpages_localdata VALUES ('2012D25303','ECA1050AE87B04B73A');
INSERT INTO calcentral_classpages_localdata VALUES ('2012D25099','EC24B1924B1524F340');
INSERT INTO calcentral_classpages_localdata VALUES ('2012D25705','EC8A24132C07C45F61');
INSERT INTO calcentral_classpages_localdata VALUES ('2012D01290','PL77B377278B1C86B6');
INSERT INTO calcentral_classpages_localdata VALUES ('2012D39546','EC-XXv-cvA_iCUZW065POKTT55Tjrx07Nq');
INSERT INTO calcentral_classpages_localdata VALUES ('2012D51560','PL-XXv-cvA_iCoBT2PF2n968Q6VjSb5EDt');
INSERT INTO calcentral_classpages_localdata VALUES ('2012D51956','EC8BA4F60A6065A8D4');
INSERT INTO calcentral_classpages_localdata VALUES ('2012D54247','EC5151EDC091F3F5F1');
INSERT INTO calcentral_classpages_localdata VALUES ('2012D56260','ECBE6EC9188D1E03A4');
INSERT INTO calcentral_classpages_localdata VALUES ('2012D64009','EC81A61335380A116C');
INSERT INTO calcentral_classpages_localdata VALUES ('2012D67298','EC-XXv-cvA_iBlaNwUBZmY286yPVQQtY6z');
INSERT INTO calcentral_classpages_localdata VALUES ('2012D67127','ECF728FFDC99C630B1');
INSERT INTO calcentral_classpages_localdata VALUES ('2012D69432','EC8BA4F60A6065A8D4');
INSERT INTO calcentral_classpages_localdata VALUES ('2012D69069','EC5A65D468FE52559C');
INSERT INTO calcentral_classpages_localdata VALUES ('2012D76205','EC8F3828AE57C83ACD');
INSERT INTO calcentral_classpages_localdata VALUES ('2012D76223','EC4BE1B382D122D539');
INSERT INTO calcentral_classpages_localdata VALUES ('2012D76304','EC8B42B94BE8BC93C6');
INSERT INTO calcentral_classpages_localdata VALUES ('2012D81603','EC2C26C949DB5332DB');
INSERT INTO calcentral_classpages_localdata VALUES ('2012D22639','EC962640B1DD22DDCE');
INSERT INTO calcentral_classpages_localdata VALUES ('2012D48037','EC28B5EDF74E734607');
INSERT INTO calcentral_classpages_localdata VALUES ('2012D51608','EC490D965CBF2D596A');
INSERT INTO calcentral_classpages_localdata VALUES ('2012D46448','EC4013AECFC90AFC13');
