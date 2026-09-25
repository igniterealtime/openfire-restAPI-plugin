
# TEST: There are no groups initially
GET {{restapi_url}}/groups
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/groups[not(child::node())]" exists # groups at the root, with no child nodes

# TEST: A created group is listed, and can be retrieved
POST {{restapi_url}}/groups
Authorization: {{authkey}}
Content-Type: application/xml
```
<?xml version="1.0" encoding="UTF-8"?>
<group>
	<name>group1</name>
	<description>test-group</description>
	<shared>false</shared>
	<admins>
		<admin>jane</admin>
	</admins>
	<members>
		<member>john</member>
	</members>
</group>
```
HTTP 201

GET {{restapi_url}}/groups
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/groups/group[name='group1']" exists

GET {{restapi_url}}/groups/group1
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/group[name='group1']" exists
xpath "string(/group/description)" == "test-group"

# TEST: Updating a group changes its description
PUT {{restapi_url}}/groups/group1
Authorization: {{authkey}}
Content-Type: application/xml
```
<?xml version="1.0" encoding="UTF-8"?>
<group>
    <name>group1</name>
    <description>test-group-updated</description>
    <shared>false</shared>
    <admins>
        <admin>jane</admin>
    </admins>
    <members>
        <member>john</member>
    </members>
</group>
```
HTTP 200

GET {{restapi_url}}/groups/group1
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/group[name='group1']" exists
xpath "string(/group/description)" == "test-group-updated"

# TEST: Delete a group
DELETE {{restapi_url}}/groups/group1
Authorization: {{authkey}}
HTTP 200

# TEST: A group can be created without an admins element
# Disabled until https://github.com/igniterealtime/openfire-restAPI-plugin/issues/288 is fixed.
POST {{restapi_url}}/groups
Authorization: {{authkey}}
Content-Type: application/xml
[Options]
skip: true
```
<?xml version="1.0" encoding="UTF-8"?>
<group>
	<name>group2</name>
	<description>A group without an admins element</description>
	<shared>false</shared>
	<members>
		<member>john</member>
	</members>
</group>
```
HTTP 201

GET {{restapi_url}}/groups/group2
Authorization: {{authkey}}
[Options]
skip: true
HTTP 200
[Asserts]
xpath "/group/members/member" count == 1
xpath "/group/admins/admin" count == 0

DELETE {{restapi_url}}/groups/group2
Authorization: {{authkey}}
[Options]
skip: true
HTTP 200
