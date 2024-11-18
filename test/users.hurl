GET http://localhost:9090/plugins/restapi/v1/users
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/users/user" count == 3
xpath "/users/user[username='admin']" exists
xpath "/users/user[username='jane']" exists
xpath "/users/user[username='john']" exists
xpath "/users/user[name='John Doe']" exists
xpath "/users/user[email='john.doe@example.com']" exists

GET http://localhost:9090/plugins/restapi/v1/users?search=john
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/users/user" count == 1
xpath "/users/user[username='john']" exists
xpath "/users/user[name='John Doe']" exists
xpath "/users/user[email='john.doe@example.com']" exists

# TODO: Add a user with a property to the demoboot
# How to launch with custom demoboot.xml locally?
#GET http://localhost:9090/plugins/restapi/v1/users?propertyKey=tea&propertyValue=earlgreyhot
#Authorization: {{authkey}}
#HTTP 200
#[Asserts]
#xpath "/users/user" count == 1

POST http://localhost:9090/plugins/restapi/v1/users
Authorization: {{authkey}}
Content-Type: application/xml
```
<?xml version="1.0" encoding="UTF-8"?>
<user>
	<username>jeanluc</username>
	<name>Jean-Luc</name>
	<email>jlp@example.com</email>
	<password>makeitso</password>
	<properties>
		<property key="tea" value="earlgreyhot" />
	</properties>
</user>
```
HTTP 201

GET http://localhost:9090/plugins/restapi/v1/users?propertyKey=tea&propertyValue=earlgreyhot
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/users/user" count == 1
xpath "string(/users/user/username)" == "jeanluc"
xpath "/users/user[name='Jean-Luc']" exists
xpath "/users/user[email='jlp@example.com']" exists
# TODO xpath for attributes
xpath "/users/user/properties/property" exists

PUT http://localhost:9090/plugins/restapi/v1/users/jeanluc
Authorization: {{authkey}}
Content-Type: application/xml
```
<?xml version="1.0" encoding="UTF-8"?>
<user>
	<username>jeanluc</username>
	<name>Jean-Luc</name>
	<email>jeanluc@example.com</email>
	<properties>
		<property key="tea" value="earlgreyhot" />
	</properties>
</user>
```
HTTP 200


GET http://localhost:9090/plugins/restapi/v1/users/jeanluc
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/user" count == 1
xpath "string(/user/username)" == "jeanluc"
xpath "/user[name='Jean-Luc']" exists
xpath "/user[email='jlp@example.com']" not exists
xpath "/user[email='jeanluc@example.com']" exists

DELETE http://localhost:9090/plugins/restapi/v1/users/jeanluc
Authorization: {{authkey}}
HTTP 200

GET http://localhost:9090/plugins/restapi/v1/users/jeanluc
Authorization: {{authkey}}
HTTP 404
[Asserts]
xpath "string(/error/exception)" == "UserNotFoundException"
xpath "string(/error/message)" == "Could not get user"
xpath "string(/error/resource)" == "jeanluc"

GET http://localhost:9090/plugins/restapi/v1/users/john/roster
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/roster/rosterItem" count == 1
xpath "string(/roster/rosterItem/jid)" == "jane@example.org"
xpath "string(/roster/rosterItem/nickname)" == "Jane"
xpath "string(/roster/rosterItem/subscriptionType)" == "3"
xpath "/roster/rosterItem/groups" exists

# TODO: Roster add/edit/delete

# TODO: Group membership

# TODO: User lockouts