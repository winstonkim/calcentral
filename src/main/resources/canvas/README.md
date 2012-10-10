# Loading campus data into Canvas

```
calcentral> mvn -e -P load-canvas exec:java
```

This can be combined with other Maven goals:
```
calcentral> mvn -e -P load-canvas clean install exec:java
```

One known gap remains in the load-canvas Java implementation. Our code uses Spring's RestTemplate to communicate with Canvas, but when it mixes file data with text parameters in a multipart form, Canvas/Rails is unable to parse the data correctly. While we sort this out, the only way to replace all official enrollment data for the current term is to resort to Curl:

```
curl -H "Authorization: Bearer $adminAccessToken" \
  'http://localhost:3000/api/v1/accounts/2/sis_imports.json?import_type=instructure_csv' \
   -F attachment=@target/canvasload/enrollments.csv -F batch_mode=1 -F batch_mode_term_id='sis_term_id:2012-D'
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

For OAuth support, you need to have Redis running and configure Canvas to use it. WARNING: The documentation at <https://github.com/instructure/canvas-lms/wiki/Production-Start> implies that you do not need to set up "config/redis.yml" if you include Redis server settings in "config/cache_store.yml". In fact, you need both (at least with the current Canvas code).
