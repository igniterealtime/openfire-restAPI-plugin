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
# path.
GET {{restapi_url}}/chatrooms/ops/visitors
Authorization: {{authkey}}
HTTP 404

# TODO: Replacing (PUT) or adding (POST) a collection of affiliations cannot be done, as the request body is
# deserialized to the abstract AffiliatedEntities class: XML requests are rejected with a 415, and JSON requests with a
# 400. Add tests once that is fixed.

# TODO: Affiliating the members of a group that does not exist returns 201, and affiliates a user with the group's name
# (e.g. nonexistent@example.org) instead. Add a test expecting a 404 once that is fixed.


DELETE {{restapi_url}}/chatrooms/ops
Authorization: {{authkey}}
HTTP 200

DELETE {{restapi_url}}/groups/crew
Authorization: {{authkey}}
HTTP 200
