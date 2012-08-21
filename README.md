#CalCentral

Home of CalCentral.

To run the server, you will first need PostgreSQL running on your machine. 

There's a good setup guide for OSX at <http://russbrooks.com/2010/11/25/install-postgresql-9-on-os-x> and guides for
other OSes at <http://wiki.postgresql.org/wiki/Detailed_installation_guides>. On my Mac, I found it easiest to
use the "brew install postgresql" method from the first page. If you don't have brew, get it here:
<http://mxcl.github.com/homebrew/>

Install postgres:
```
brew update
brew install postgresql
initdb /usr/local/var/postgres
```

Start postgres and create a blank db:
```
pg_ctl -D /usr/local/var/postgres -l /usr/local/var/postgres/server.log start
psql postgres
create database calcentral;
create user calcentral with password 'secret';
grant all privileges on database calcentral to calcentral;
```

To start the CalCentral server:
```
cd web
mvn jetty:run
```

Access the server at <http://localhost:8080/>

To stop the server:
```
ctrl-C
```

You can take all the default properties, or override some (or all) of them by creating your own *.properties
files. There are other optional config settings that can be passed on the mvn command line. A partial reference:
```
mvn jetty:run
		-DcustomPropsDir=/path/to/your/custom/configs
		-Djetty.port=8778
		-Dlog4j.configuration=file:/path/to/my/favorite/log4j.properties
```

To run the server with unit tests plus test coverage reports:
```
mvn clean install cobertura:cobertura jetty:run
```
You can read coverage reports from target/site/cobertura/index.html.

To run the server with integration tests (requires postgres already running):
```
mvn clean verify
```
