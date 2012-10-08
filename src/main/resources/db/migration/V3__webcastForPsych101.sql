INSERT INTO calcentral_classpages_localdata ( classPageId )
SELECT '2012D73974'
WHERE NOT EXISTS ( SELECT classPageId FROM calcentral_classpages_localdata WHERE classPageId = '2012D73974' );

UPDATE calcentral_classpages_localdata SET webcastId = 'PLA07B0BAB1D82C53C' WHERE classPageId = '2012D73974';
