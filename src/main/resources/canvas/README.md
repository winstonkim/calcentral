# Loading campus data into Canvas

```
calcentral> mvn -e -P load-canvas exec:java
```

This can be combined with other Maven goals:
```
calcentral> mvn -e -P load-canvas clean install flyway:clean flyway:migrate  exec:java
```

# Enabling SIS import

The Canvas REST API by itself does not provide any way to create terms or to distinguish manually-added data from campus-added data.
Those features require SIS import from CSV files.

If running a local open-source instance, you need to run the job-handling server alongside the normal Canvas server:
```
canvas> script/delayed_job run
canvas> script/server
```

And you need to have an access token for administrative access.

And you need to enable "SIS Import" for the account. Go to account settings (e.g., <http://localhost:3000/accounts/2/settings>) and check "Features" / "SIS imports".

# Running SIS import

```
curl -H "Authorization: Bearer $accessToken" \
  'http://localhost:3000/api/v1/accounts/2/sis_imports.json?import_type=instructure_csv' \
  -F attachment=@departments.csv
curl -H "Authorization: Bearer $accessToken" \
  'http://localhost:3000/api/v1/accounts/2/sis_imports.json?import_type=instructure_csv' \
   -F attachment=@terms.csv
curl -H "Authorization: Bearer $acessToken" \
  'http://localhost:3000/api/v1/accounts/2/sis_imports.json?import_type=instructure_csv' \
  -F attachment=@courses.csv
curl -H "Authorization: Bearer $accessToken" \
  'http://localhost:3000/api/v1/accounts/2/sis_imports.json?import_type=instructure_csv' \
  -F attachment=@sections.csv
curl -H "Authorization: Bearer $accessToken" \
  'http://localhost:3000/api/v1/accounts/2/sis_imports.json?import_type=instructure_csv' \
  -F attachment=@users.csv
curl -H "Authorization: Bearer $accessToken" \
  'http://localhost:3000/api/v1/accounts/2/sis_imports.json?import_type=instructure_csv' \
   -F attachment=@enrollments.csv -F batch_mode=1 -F batch_mode_term_id='sis_term_id:2012-D'
```

# Sample Oracle queries

For section data:

```
SELECT TERM_YR || '-' || TERM_CD || '-' || COURSE_CNTL_NUM, instruction_format || ' ' || section_num \
  FROM BSPACE_COURSE_INFO_VW WHERE TERM_YR=2012 AND TERM_CD='D' AND DEPT_NAME='PSYCH' AND CATALOG_ID='101';
```
