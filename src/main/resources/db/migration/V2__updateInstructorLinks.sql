UPDATE calcentral_users
SET link = 'http://math.berkeley.edu/people/faculty/richard-e-borcherds' WHERE uid = '6106';
INSERT INTO calcentral_users ( uid, link )
SELECT '6106', 'http://math.berkeley.edu/people/faculty/richard-e-borcherds'
WHERE NOT EXISTS ( SELECT uid FROM calcentral_users WHERE uid = '6106' );

UPDATE calcentral_users
SET link = 'http://math.berkeley.edu/people/grad/christopher-policastro' WHERE uid = '1002157';
INSERT INTO calcentral_users ( uid, link )
SELECT '1002157', 'http://math.berkeley.edu/people/grad/christopher-policastro'
WHERE NOT EXISTS ( SELECT uid FROM calcentral_users WHERE uid = '1002157' );

UPDATE calcentral_users
SET link = 'http://chem.berkeley.edu/faculty/stacy/index.php' WHERE uid = '4419';
INSERT INTO calcentral_users ( uid, link )
SELECT '4419', 'http://chem.berkeley.edu/faculty/stacy/index.php'
WHERE NOT EXISTS ( SELECT uid FROM calcentral_users WHERE uid = '4419' );

UPDATE calcentral_users
SET link = 'http://www.denero.org/' WHERE uid = '260296';
INSERT INTO calcentral_users ( uid, link )
SELECT '260296', 'http://www.denero.org/'
WHERE NOT EXISTS ( SELECT uid FROM calcentral_users WHERE uid = '260296' );

UPDATE calcentral_users
SET link = 'http://journalism.berkeley.edu/faculty/pollan/', profileimagelink = '//berkeley.box.com/shared/static/d0ttw1j6vdfrfrbs5p39.jpg' WHERE uid = '214421';
INSERT INTO calcentral_users (uid, profileimagelink, link)
SELECT '214421', '//berkeley.box.com/shared/static/d0ttw1j6vdfrfrbs5p39.jpg', 'http://journalism.berkeley.edu/faculty/pollan/'
WHERE NOT EXISTS ( SELECT uid FROM calcentral_users WHERE uid = '214421' );
