# Affiliations are tested on a dedicated chat room, and a dedicated group, which are removed at the end.
POST {{restapi_url}}/chatrooms
Authorization: {{authkey}}
Content-Type: application/xml
```
<?xml version="1.0" encoding="UTF-8"?>
<chatRoom>
	<roomName>ops</roomName>
	<naturalName>Operations</naturalName>
	<description>Affiliations test room</description>
	<owners>
		<owner>john@example.org</owner>
	</owners>
</chatRoom>
```
HTTP 201

POST {{restapi_url}}/groups
Authorization: {{authkey}}
Content-Type: application/xml
```
<?xml version="1.0" encoding="UTF-8"?>
<group>
	<name>crew</name>
	<description>Crew</description>
	<shared>false</shared>
	<admins/>
	<members>
		<member>john</member>
		<member>jane</member>
	</members>
</group>
```
HTTP 201


# TEST: The owners that a chat room was created with are listed
GET {{restapi_url}}/chatrooms/ops/owners
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/owners/owner" count == 1
xpath "string(/owners/owner)" == "john@example.org"

# TEST: A new chat room has no admins, members or outcasts
GET {{restapi_url}}/chatrooms/ops/admins
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/admins/admin" count == 0

GET {{restapi_url}}/chatrooms/ops/members
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/members/member" count == 0

GET {{restapi_url}}/chatrooms/ops/outcasts
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/outcasts/outcast" count == 0

# TEST: Add an affiliation by JID
POST {{restapi_url}}/chatrooms/ops/members/jane@example.org
Authorization: {{authkey}}
HTTP 201

GET {{restapi_url}}/chatrooms/ops/members
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/members/member" count == 1
xpath "string(/members/member)" == "jane@example.org"

# TEST: A user has only one affiliation, so adding another replaces the previous one
POST {{restapi_url}}/chatrooms/ops/admins/jane@example.org
Authorization: {{authkey}}
HTTP 201

GET {{restapi_url}}/chatrooms/ops/admins
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/admins/admin" count == 1
xpath "string(/admins/admin)" == "jane@example.org"

GET {{restapi_url}}/chatrooms/ops/members
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/members/member" count == 0

# TEST: Add an affiliation by username, which is resolved against the local domain
POST {{restapi_url}}/chatrooms/ops/outcasts/mallory
Authorization: {{authkey}}
HTTP 201

GET {{restapi_url}}/chatrooms/ops/outcasts
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/outcasts/outcast" count == 1
xpath "string(/outcasts/outcast)" == "mallory@example.org"

# TEST: Remove an affiliation
DELETE {{restapi_url}}/chatrooms/ops/admins/jane@example.org
Authorization: {{authkey}}
HTTP 200

GET {{restapi_url}}/chatrooms/ops/admins
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/admins/admin" count == 0

DELETE {{restapi_url}}/chatrooms/ops/outcasts/mallory@example.org
Authorization: {{authkey}}
HTTP 200

GET {{restapi_url}}/chatrooms/ops/outcasts
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/outcasts/outcast" count == 0

# TEST: Removing the last owner of a chat room is a conflict
DELETE {{restapi_url}}/chatrooms/ops/owners/john@example.org
Authorization: {{authkey}}
HTTP 409
[Asserts]
xpath "string(/error/exception)" == "NotAllowedException"

GET {{restapi_url}}/chatrooms/ops/owners
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "string(/owners/owner)" == "john@example.org"

# TEST: Affiliate the members of a group, which the room lists as a group (or its members, when expanded)
POST {{restapi_url}}/chatrooms/ops/members/group/crew
Authorization: {{authkey}}
HTTP 201

GET {{restapi_url}}/chatrooms/ops
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/chatRoom/memberGroups/memberGroup" count == 1
xpath "string(/chatRoom/memberGroups/memberGroup)" == "crew"
xpath "/chatRoom/members/member" count == 0

GET {{restapi_url}}/chatrooms/ops?expandGroups=true
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/chatRoom/members[member='john@example.org']" exists
xpath "/chatRoom/members[member='jane@example.org']" exists

# TEST: Remove the affiliation of the members of a group
DELETE {{restapi_url}}/chatrooms/ops/members/group/crew
Authorization: {{authkey}}
HTTP 200

GET {{restapi_url}}/chatrooms/ops
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/chatRoom/memberGroups/memberGroup" count == 0

# TEST: Getting the affiliations of a chat room that does not exist fails
GET {{restapi_url}}/chatrooms/nonexistent/members
Authorization: {{authkey}}
HTTP 404
[Asserts]
xpath "string(/error/exception)" == "RoomNotFoundException"
xpath "string(/error/resource)" == "nonexistent"

# TEST: An affiliation type that does not exist is not found
# The API documents a 400 for this, but the URL does not match any endpoint, as the affiliation type is part of the
# path. See https://github.com/igniterealtime/openfire-restAPI-plugin/issues/267
GET {{restapi_url}}/chatrooms/ops/visitors
Authorization: {{authkey}}
HTTP 404

# TEST: Replace the affiliations of a type with a collection
# Disabled until https://github.com/igniterealtime/openfire-restAPI-plugin/issues/163 is fixed.
PUT {{restapi_url}}/chatrooms/ops/members
Authorization: {{authkey}}
Content-Type: application/xml
[Options]
skip: true
```
<?xml version="1.0" encoding="UTF-8"?>
<members>
	<member>jane@example.org</member>
	<member>mallory@example.org</member>
</members>
```
HTTP 201

GET {{restapi_url}}/chatrooms/ops/members
Authorization: {{authkey}}
[Options]
skip: true
HTTP 200
[Asserts]
xpath "/members/member" count == 2

PUT {{restapi_url}}/chatrooms/ops/members
Authorization: {{authkey}}
Content-Type: application/xml
[Options]
skip: true
```
<?xml version="1.0" encoding="UTF-8"?>
<members>
	<member>jane@example.org</member>
</members>
```
HTTP 201

GET {{restapi_url}}/chatrooms/ops/members
Authorization: {{authkey}}
[Options]
skip: true
HTTP 200
[Asserts]
xpath "/members/member" count == 1
xpath "string(/members/member)" == "jane@example.org"

DELETE {{restapi_url}}/chatrooms/ops/members/jane@example.org
Authorization: {{authkey}}
[Options]
skip: true
HTTP 200

# TEST: Add a collection of affiliations
# Disabled until https://github.com/igniterealtime/openfire-restAPI-plugin/issues/163 is fixed.
POST {{restapi_url}}/chatrooms/ops/outcasts
Authorization: {{authkey}}
Content-Type: application/xml
[Options]
skip: true
```
<?xml version="1.0" encoding="UTF-8"?>
<outcasts>
	<outcast>eve@example.org</outcast>
	<outcast>mallory@example.org</outcast>
</outcasts>
```
HTTP 201

GET {{restapi_url}}/chatrooms/ops/outcasts
Authorization: {{authkey}}
[Options]
skip: true
HTTP 200
[Asserts]
xpath "/outcasts/outcast" count == 2

DELETE {{restapi_url}}/chatrooms/ops/outcasts/eve@example.org
Authorization: {{authkey}}
[Options]
skip: true
HTTP 200

DELETE {{restapi_url}}/chatrooms/ops/outcasts/mallory@example.org
Authorization: {{authkey}}
[Options]
skip: true
HTTP 200

# TEST: Affiliating the members of a group that does not exist fails
# Disabled until https://github.com/igniterealtime/openfire-restAPI-plugin/issues/286 is fixed.
POST {{restapi_url}}/chatrooms/ops/members/group/nonexistent
Authorization: {{authkey}}
[Options]
skip: true
HTTP 404

GET {{restapi_url}}/chatrooms/ops/members
Authorization: {{authkey}}
[Options]
skip: true
HTTP 200
[Asserts]
xpath "/members[member='nonexistent@example.org']" not exists


DELETE {{restapi_url}}/chatrooms/ops
Authorization: {{authkey}}
HTTP 200

DELETE {{restapi_url}}/groups/crew
Authorization: {{authkey}}
HTTP 200
