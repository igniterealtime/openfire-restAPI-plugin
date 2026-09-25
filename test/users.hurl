# TEST: List all users
GET {{restapi_url}}/users
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/users/user" count == 3
xpath "/users/user[username='admin']" exists
xpath "/users/user[username='jane']" exists
xpath "/users/user[username='john']" exists
xpath "/users/user[name='John Doe']" exists
xpath "/users/user[email='john.doe@example.com']" exists

# TEST: Search for users by (part of) their username
GET {{restapi_url}}/users?search=john
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/users/user" count == 1
xpath "/users/user[username='john']" exists
xpath "/users/user[name='John Doe']" exists
xpath "/users/user[email='john.doe@example.com']" exists

# TEST: Search for users by a property that no user has
GET {{restapi_url}}/users?propertyKey=tea
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/users/user" count == 0

# TEST: Create a user with a property
POST {{restapi_url}}/users
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

# TEST: Search for users by property key and value
GET {{restapi_url}}/users?propertyKey=tea&propertyValue=earlgreyhot
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/users/user" count == 1
xpath "string(/users/user/username)" == "jeanluc"
xpath "/users/user[name='Jean-Luc']" exists
xpath "/users/user[email='jlp@example.com']" exists
xpath "/users/user/properties/property" count == 1
xpath "string(/users/user/properties/property/@key)" == "tea"
xpath "string(/users/user/properties/property/@value)" == "earlgreyhot"

# TEST: Search for users by property key only
GET {{restapi_url}}/users?propertyKey=tea
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/users/user" count == 1
xpath "string(/users/user/username)" == "jeanluc"

# TEST: Search for users by property key and a non-matching value
GET {{restapi_url}}/users?propertyKey=tea&propertyValue=coffee
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/users/user" count == 0

# TEST: Update a user
PUT {{restapi_url}}/users/jeanluc
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


GET {{restapi_url}}/users/jeanluc
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/user" count == 1
xpath "string(/user/username)" == "jeanluc"
xpath "/user[name='Jean-Luc']" exists
xpath "/user[email='jlp@example.com']" not exists
xpath "/user[email='jeanluc@example.com']" exists

# TEST: Delete a user
DELETE {{restapi_url}}/users/jeanluc
Authorization: {{authkey}}
HTTP 200

GET {{restapi_url}}/users/jeanluc
Authorization: {{authkey}}
HTTP 404
[Asserts]
xpath "string(/error/exception)" == "UserNotFoundException"
xpath "string(/error/message)" == "Could not get user"
xpath "string(/error/resource)" == "jeanluc"
