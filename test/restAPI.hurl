### CLUSTERING ###

GET {{host}}/plugins/restapi/v1/clustering/status
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "string(/clustering/status)" == "Disabled" 

GET {{host}}/plugins/restapi/v1/clustering/nodes
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/clusterNodes[not(child::node())]" exists


### USER GROUPS ###

GET http://localhost:9090/plugins/restapi/v1/groups
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/groups[not(child::node())]" exists # groups at the root, with no child nodes

POST http://localhost:9090/plugins/restapi/v1/groups
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

GET http://localhost:9090/plugins/restapi/v1/groups # check if the group was created
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/groups/group[name='group1']" exists

GET http://localhost:9090/plugins/restapi/v1/groups/group1
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/group[name='group1']" exists
xpath "string(/group/description)" == "test-group"

PUT http://localhost:9090/plugins/restapi/v1/groups/group1
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

GET http://localhost:9090/plugins/restapi/v1/groups/group1
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/group[name='group1']" exists
xpath "string(/group/description)" == "test-group-updated"

DELETE http://localhost:9090/plugins/restapi/v1/groups/group1
Authorization: {{authkey}}
HTTP 200


### CHAT ROOMS ###
GET http://localhost:9090/plugins/restapi/v1/chatrooms
Authorization: {{authkey}}
HTTP 200

GET http://localhost:9090/plugins/restapi/v1/sessions
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/sessions[not(child::node())]" count == 1 # sessions at the root, with no child nodes


GET http://localhost:9090/plugins/restapi/v1/system/readiness/server
Authorization: {{authkey}}
HTTP 200