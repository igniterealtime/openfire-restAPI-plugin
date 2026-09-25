
GET {{restapi_url}}/groups
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/groups[not(child::node())]" exists # groups at the root, with no child nodes

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

GET {{restapi_url}}/groups # check if the group was created
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

DELETE {{restapi_url}}/groups/group1
Authorization: {{authkey}}
HTTP 200

