Oracle integration and emulation

1. Using real Oracle DB

To integrate directly with campus data, put lines like the following in your "dataSource.properties"
configuration:

# Enable Oracle connection.
campusDataSource.targetName=oracleDataSource
oracleDataSource.username=whozit
oracleDataSource.url=jdbc:oracle:thin:@something-somewhere
oracleDataSource.password=whatzit

****

2. Extracting data for import

If we continue on this route, we'll almost certainly have to move to a more programmatic
approach to data loads using Java or a scripting language. For the person-info table, however,
I just used SQL Developer query and export, followed by some regexp replacements. E.g., for email:

Search: ^(.+) values \(([0-9]+),(.+),'[a-z_]+@[a-z.]+.edu',
Replace: \1 values (\2, \3,'\2@example.edu',

Here were the queries:

-- Development users
SELECT * FROM BSPACE_PERSON_INFO_VW WHERE LDAP_UID IN (226144, 238382, 6576, 592722, 863980, 3060, 2040, 322279,
  211159, 192517, 208861, 271592, 95509, 904715, 675750, 177473, 53791, 18437, 313561, 323487, 12005, 266945, 730057,
  741134, 191779, 5698);

-- CalNet test users <https://wikihub.berkeley.edu/display/calnet/Universal+Test+IDs>
SELECT * FROM BSPACE_PERSON_INFO_VW WHERE LDAP_UID IN (212372, 212373, 212374, 212375, 212376, 212377, 212378,
  212379, 212380, 212381, 212382, 212383, 212384, 212385, 212386, 212387, 212388, 232588, 300846, 300847, 300848,
  300849, 300850, 300851, 300852, 300853, 300854, 300855, 300856, 300857, 300858, 300859, 300860, 300861, 300862,
  300863, 300864, 300865, 300866, 300867, 300868, 300869, 300870, 300871, 300872, 300873, 300874, 300875, 300876,
  300877, 300878, 300879, 300880, 300881, 300882, 300883, 300884, 300885, 300886, 300887, 300888, 300889, 300890,
  300891, 300892, 300893, 300894, 300895, 300896, 300897, 300898, 300899, 300900, 300901, 300902, 300903, 300904,
  300905, 300906, 300907, 300908, 300909, 300910, 300911, 300912, 300913, 300914, 300915, 300916, 300917, 300918,
  300919, 300920, 300921, 300922, 300923, 300924, 300925, 300926, 300927, 300928, 300929, 300930, 300931, 300932,
  300933, 300934, 300935, 300936, 300937, 300938, 300939, 300940, 300941, 300942, 300943, 300944, 300945, 321703,
  321765, 322583, 322584, 322585, 322586, 322587, 322588, 322589, 322590, 324731);
