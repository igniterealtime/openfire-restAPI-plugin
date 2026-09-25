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

GET {{restapi_url}}/users/geordi/groups
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/groups/groupname" count == 0

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

DELETE {{restapi_url}}/users/geordi/groups/security
Authorization: {{authkey}}
HTTP 200

GET {{restapi_url}}/users/geordi/groups
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/groups/groupname" count == 2
xpath "/groups[groupname='security']" not exists

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

DELETE {{restapi_url}}/users/geordi/groups/nonexistent
Authorization: {{authkey}}
HTTP 404
[Asserts]
xpath "string(/error/exception)" == "GroupNotFoundException"
xpath "string(/error/resource)" == "nonexistent"

GET {{restapi_url}}/users/nonexistent/groups
Authorization: {{authkey}}
HTTP 404
[Asserts]
xpath "string(/error/exception)" == "UserNotFoundException"

# TODO: Adding a user to a group that does not exist is documented to create that group, but fails with a 500, as
# GroupController#createGroup dereferences the (null) members of the GroupEntity that
# UserServiceController#addUserToGroup(s) creates. (Creating a group through POST /groups without <members/> and
# <admins/> fails in the same way.) Add tests for automatic group creation once that is fixed.

# TODO: Adding a user that does not exist to an (existing) group returns 201 and adds the JID to the group, where
# 404 might be expected. Add a test once the intended behaviour is decided.

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
