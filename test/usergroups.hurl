# Group membership is tested on a dedicated user, which is removed at the end.
POST {{restapi_url}}/users
Authorization: {{authkey}}
Content-Type: application/xml
```
<?xml version="1.0" encoding="UTF-8"?>
<user>
	<username>geordi</username>
	<password>visor</password>
</user>
```
HTTP 201

POST {{restapi_url}}/groups
Authorization: {{authkey}}
Content-Type: application/xml
```
<?xml version="1.0" encoding="UTF-8"?>
<group>
	<name>security</name>
	<description>Security</description>
	<shared>false</shared>
	<admins/>
	<members/>
</group>
```
HTTP 201

POST {{restapi_url}}/groups
Authorization: {{authkey}}
Content-Type: application/xml
```
<?xml version="1.0" encoding="UTF-8"?>
<group>
	<name>klingons</name>
	<description>Klingons</description>
	<shared>false</shared>
	<admins/>
	<members/>
</group>
```
HTTP 201

POST {{restapi_url}}/groups
Authorization: {{authkey}}
Content-Type: application/xml
```
<?xml version="1.0" encoding="UTF-8"?>
<group>
	<name>bridge</name>
	<description>Bridge crew</description>
	<shared>false</shared>
	<admins/>
	<members/>
</group>
```
HTTP 201

# TEST: A new user is in no groups
GET {{restapi_url}}/users/geordi/groups
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/groups/groupname" count == 0

# TEST: Add a user to a single group, and to multiple groups
POST {{restapi_url}}/users/geordi/groups/security
Authorization: {{authkey}}
HTTP 201

POST {{restapi_url}}/users/geordi/groups
Authorization: {{authkey}}
Content-Type: application/xml
```
<?xml version="1.0" encoding="UTF-8"?>
<groups>
	<groupname>klingons</groupname>
	<groupname>bridge</groupname>
</groups>
```
HTTP 201

GET {{restapi_url}}/users/geordi/groups
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/groups/groupname" count == 3
xpath "/groups[groupname='security']" exists
xpath "/groups[groupname='klingons']" exists
xpath "/groups[groupname='bridge']" exists

GET {{restapi_url}}/groups/klingons
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/group/members/member" count == 1
xpath "string(/group/members/member)" == "geordi@example.org"

# TEST: Remove a user from a single group
DELETE {{restapi_url}}/users/geordi/groups/security
Authorization: {{authkey}}
HTTP 200

GET {{restapi_url}}/users/geordi/groups
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/groups/groupname" count == 2
xpath "/groups[groupname='security']" not exists

# TEST: Remove a user from multiple groups
DELETE {{restapi_url}}/users/geordi/groups
Authorization: {{authkey}}
Content-Type: application/xml
```
<?xml version="1.0" encoding="UTF-8"?>
<groups>
	<groupname>klingons</groupname>
	<groupname>bridge</groupname>
</groups>
```
HTTP 200

GET {{restapi_url}}/users/geordi/groups
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/groups/groupname" count == 0

# TEST: Removing a user from a group that does not exist fails
DELETE {{restapi_url}}/users/geordi/groups/nonexistent
Authorization: {{authkey}}
HTTP 404
[Asserts]
xpath "string(/error/exception)" == "GroupNotFoundException"
xpath "string(/error/resource)" == "nonexistent"

# TEST: Getting the groups of a user that does not exist fails
GET {{restapi_url}}/users/nonexistent/groups
Authorization: {{authkey}}
HTTP 404
[Asserts]
xpath "string(/error/exception)" == "UserNotFoundException"

# TEST: Adding a user to a group that does not exist creates that group
# Disabled until https://github.com/igniterealtime/openfire-restAPI-plugin/issues/279 is fixed.
POST {{restapi_url}}/users/geordi/groups/engineering
Authorization: {{authkey}}
[Options]
skip: true
HTTP 201

GET {{restapi_url}}/groups/engineering
Authorization: {{authkey}}
[Options]
skip: true
HTTP 200
[Asserts]
xpath "/group/members[member='geordi@example.org']" exists

POST {{restapi_url}}/users/geordi/groups
Authorization: {{authkey}}
Content-Type: application/xml
[Options]
skip: true
```
<?xml version="1.0" encoding="UTF-8"?>
<groups>
	<groupname>sickbay</groupname>
</groups>
```
HTTP 201

GET {{restapi_url}}/groups/sickbay
Authorization: {{authkey}}
[Options]
skip: true
HTTP 200
[Asserts]
xpath "/group/members[member='geordi@example.org']" exists

DELETE {{restapi_url}}/groups/engineering
Authorization: {{authkey}}
[Options]
skip: true
HTTP 200

DELETE {{restapi_url}}/groups/sickbay
Authorization: {{authkey}}
[Options]
skip: true
HTTP 200

# TEST: Adding a user that does not exist to a group fails
# Disabled until https://github.com/igniterealtime/openfire-restAPI-plugin/issues/280 is fixed.
POST {{restapi_url}}/users/nonexistent/groups/security
Authorization: {{authkey}}
[Options]
skip: true
HTTP 404

POST {{restapi_url}}/users/nonexistent/groups
Authorization: {{authkey}}
Content-Type: application/xml
[Options]
skip: true
```
<?xml version="1.0" encoding="UTF-8"?>
<groups>
	<groupname>security</groupname>
</groups>
```
HTTP 404

GET {{restapi_url}}/groups/security
Authorization: {{authkey}}
[Options]
skip: true
HTTP 200
[Asserts]
xpath "/group/members[member='nonexistent@example.org']" not exists

DELETE {{restapi_url}}/groups/security
Authorization: {{authkey}}
HTTP 200

DELETE {{restapi_url}}/groups/klingons
Authorization: {{authkey}}
HTTP 200

DELETE {{restapi_url}}/groups/bridge
Authorization: {{authkey}}
HTTP 200

DELETE {{restapi_url}}/users/geordi
Authorization: {{authkey}}
HTTP 200
