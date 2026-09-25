# TEST: List the chat rooms
GET {{restapi_url}}/chatrooms
Authorization: {{authkey}}
HTTP 200

# TEST: A created chat room can be retrieved
POST {{restapi_url}}/chatrooms
Authorization: {{authkey}}
Content-Type: application/xml
```
<?xml version="1.0" encoding="UTF-8"?>
<chatRoom>
	<roomName>lobby</roomName>
	<naturalName>The Lobby</naturalName>
	<description>Where everyone meets</description>
	<subject>Welcome</subject>
	<maxUsers>30</maxUsers>
	<persistent>true</persistent>
	<publicRoom>true</publicRoom>
	<owners>
		<owner>john@example.org</owner>
	</owners>
	<members>
		<member>jane@example.org</member>
	</members>
</chatRoom>
```
HTTP 201

GET {{restapi_url}}/chatrooms/lobby
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "string(/chatRoom/roomName)" == "lobby"
xpath "string(/chatRoom/naturalName)" == "The Lobby"
xpath "string(/chatRoom/description)" == "Where everyone meets"
xpath "string(/chatRoom/subject)" == "Welcome"
xpath "string(/chatRoom/maxUsers)" == "30"
xpath "string(/chatRoom/persistent)" == "true"
xpath "string(/chatRoom/publicRoom)" == "true"
xpath "/chatRoom/owners/owner" count == 1
xpath "string(/chatRoom/owners/owner)" == "john@example.org"
xpath "/chatRoom/members/member" count == 1
xpath "string(/chatRoom/members/member)" == "jane@example.org"
xpath "/chatRoom/creationDate" exists

# TODO: Creating a chat room that already exists returns 201 (the bulk endpoint reports 'Success'), and replaces the
# existing room's configuration and affiliations, where 409 is documented. Add a test once that is fixed.

# TEST: Only public chat rooms are listed, unless all rooms are asked for
POST {{restapi_url}}/chatrooms
Authorization: {{authkey}}
Content-Type: application/xml
```
<?xml version="1.0" encoding="UTF-8"?>
<chatRoom>
	<roomName>backroom</roomName>
	<naturalName>The Back Room</naturalName>
	<description>Not listed</description>
	<publicRoom>false</publicRoom>
</chatRoom>
```
HTTP 201

GET {{restapi_url}}/chatrooms
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/chatRooms/chatRoom[roomName='lobby']" exists
xpath "/chatRooms/chatRoom[roomName='backroom']" not exists

GET {{restapi_url}}/chatrooms?type=public
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/chatRooms/chatRoom[roomName='lobby']" exists
xpath "/chatRooms/chatRoom[roomName='backroom']" not exists

GET {{restapi_url}}/chatrooms?type=all
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/chatRooms/chatRoom[roomName='lobby']" exists
xpath "/chatRooms/chatRoom[roomName='backroom']" exists

# TEST: Search for chat rooms by (part of) their name or natural name
GET {{restapi_url}}/chatrooms?search=lob
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/chatRooms/chatRoom" count == 1
xpath "string(/chatRooms/chatRoom/roomName)" == "lobby"

GET {{restapi_url}}/chatrooms?type=all&search=Back
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/chatRooms/chatRoom" count == 1
xpath "string(/chatRooms/chatRoom/roomName)" == "backroom"

GET {{restapi_url}}/chatrooms?type=all&search=nothing-matches-this
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/chatRooms/chatRoom" count == 0

# TODO: Searching for a term that is not a valid JID node (e.g. 'Back Room', with a space) fails with a 500, as the
# term is nodeprep'ed to match room names, even though natural names can contain such characters. Add a test once
# that is fixed.

# TEST: Listing the chat rooms of a service that does not exist fails
GET {{restapi_url}}/chatrooms?servicename=nonexistent
Authorization: {{authkey}}
HTTP 404
[Asserts]
xpath "string(/error/exception)" == "MUCServiceNotFoundException"
xpath "string(/error/resource)" == "nonexistent"

# TEST: Getting a chat room that does not exist fails
GET {{restapi_url}}/chatrooms/nonexistent
Authorization: {{authkey}}
HTTP 404
[Asserts]
xpath "string(/error/exception)" == "RoomNotFoundException"
xpath "string(/error/resource)" == "nonexistent"

# TEST: Getting a chat room of a service that does not exist fails
GET {{restapi_url}}/chatrooms/lobby?servicename=nonexistent
Authorization: {{authkey}}
HTTP 404
[Asserts]
xpath "string(/error/exception)" == "MUCServiceNotFoundException"

# TEST: Update a chat room
PUT {{restapi_url}}/chatrooms/lobby
Authorization: {{authkey}}
Content-Type: application/xml
```
<?xml version="1.0" encoding="UTF-8"?>
<chatRoom>
	<roomName>lobby</roomName>
	<naturalName>The Grand Lobby</naturalName>
	<description>Where everyone meets, now bigger</description>
	<subject>Welcome</subject>
	<maxUsers>50</maxUsers>
	<persistent>true</persistent>
	<publicRoom>true</publicRoom>
	<owners>
		<owner>john@example.org</owner>
	</owners>
	<members>
		<member>jane@example.org</member>
	</members>
</chatRoom>
```
HTTP 200

GET {{restapi_url}}/chatrooms/lobby
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "string(/chatRoom/naturalName)" == "The Grand Lobby"
xpath "string(/chatRoom/description)" == "Where everyone meets, now bigger"
xpath "string(/chatRoom/maxUsers)" == "50"

# TODO: Updating a chat room that does not exist returns 200 and creates the room. Add a test once it is decided
# whether that should be a 404 instead.

# TEST: A new chat room has no participants, occupants or history
GET {{restapi_url}}/chatrooms/lobby/participants
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/participants/participant" count == 0

GET {{restapi_url}}/chatrooms/lobby/occupants
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/occupants/occupant" count == 0

GET {{restapi_url}}/chatrooms/lobby/chathistory
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/messages/message" count == 0

# TEST: Getting the participants, occupants or history of a chat room that does not exist fails
GET {{restapi_url}}/chatrooms/nonexistent/participants
Authorization: {{authkey}}
HTTP 404
[Asserts]
xpath "string(/error/exception)" == "RoomNotFoundException"

GET {{restapi_url}}/chatrooms/nonexistent/occupants
Authorization: {{authkey}}
HTTP 404
[Asserts]
xpath "string(/error/exception)" == "RoomNotFoundException"

GET {{restapi_url}}/chatrooms/nonexistent/chathistory
Authorization: {{authkey}}
HTTP 404
[Asserts]
xpath "string(/error/exception)" == "RoomNotFoundException"


# TEST: A client that joins a chat room is listed as a participant and occupant, and what it says is in the history
# Log in anonymously over BOSH (XEP-0124/XEP-0206). The server may send stanzas (like pings) at any time, so responses
# are only checked for the presence of the stanzas that are expected.
POST {{bosh_url}}/http-bind/
Content-Type: text/xml; charset=utf-8
[Options]
variable: nick=visitor-{{newUuid}}
```
<body rid='1000' to='example.org' xml:lang='en' wait='5' hold='1' ver='1.6' xmpp:version='1.0' xmlns:xmpp='urn:xmpp:xbosh' xmlns='http://jabber.org/protocol/httpbind'/>
```
HTTP 200
[Captures]
sid: xpath "string(/_:body/@sid)"

POST {{bosh_url}}/http-bind/
Content-Type: text/xml; charset=utf-8
```
<body rid='1001' sid='{{sid}}' xmlns='http://jabber.org/protocol/httpbind'><auth xmlns='urn:ietf:params:xml:ns:xmpp-sasl' mechanism='ANONYMOUS'/></body>
```
HTTP 200
[Asserts]
xpath "//*[local-name()='success']" exists

POST {{bosh_url}}/http-bind/
Content-Type: text/xml; charset=utf-8
```
<body rid='1002' sid='{{sid}}' to='example.org' xml:lang='en' xmpp:restart='true' xmlns:xmpp='urn:xmpp:xbosh' xmlns='http://jabber.org/protocol/httpbind'/>
```
HTTP 200

POST {{bosh_url}}/http-bind/
Content-Type: text/xml; charset=utf-8
```
<body rid='1003' sid='{{sid}}' xmlns='http://jabber.org/protocol/httpbind'><iq type='set' id='bind' xmlns='jabber:client'><bind xmlns='urn:ietf:params:xml:ns:xmpp-bind'/></iq></body>
```
HTTP 200
[Captures]
bare_jid: xpath "substring-before(//*[local-name()='jid'], '/')"

POST {{bosh_url}}/http-bind/
Content-Type: text/xml; charset=utf-8
```
<body rid='1004' sid='{{sid}}' xmlns='http://jabber.org/protocol/httpbind'><presence xmlns='jabber:client'/></body>
```
HTTP 200

# Join the room.
POST {{bosh_url}}/http-bind/
Content-Type: text/xml; charset=utf-8
```
<body rid='1005' sid='{{sid}}' xmlns='http://jabber.org/protocol/httpbind'><presence to='lobby@conference.example.org/{{nick}}' xmlns='jabber:client'><x xmlns='http://jabber.org/protocol/muc'/></presence></body>
```
HTTP 200
[Asserts]
xpath "//*[local-name()='presence'][@from='lobby@conference.example.org/{{nick}}']" exists

GET {{restapi_url}}/chatrooms/lobby/participants
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/participants/participant" count == 1
xpath "string(/participants/participant/jid)" == "lobby@conference.example.org/{{nick}}"
xpath "string(/participants/participant/role)" == "participant"
xpath "string(/participants/participant/affiliation)" == "none"

GET {{restapi_url}}/chatrooms/lobby/occupants
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/occupants/occupant" count == 1
xpath "string(/occupants/occupant/jid)" == "lobby@conference.example.org/{{nick}}"
xpath "string(/occupants/occupant/userAddress)" startsWith "{{bare_jid}}/"

POST {{bosh_url}}/http-bind/
Content-Type: text/xml; charset=utf-8
```
<body rid='1006' sid='{{sid}}' xmlns='http://jabber.org/protocol/httpbind'><message to='lobby@conference.example.org' type='groupchat' xmlns='jabber:client'><body>Hello, room</body></message></body>
```
HTTP 200

GET {{restapi_url}}/chatrooms/lobby/chathistory
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/messages/message" count == 1
xpath "string(/messages/message/from)" == "lobby@conference.example.org/{{nick}}"
xpath "string(/messages/message/type)" == "groupchat"
xpath "string(/messages/message/body)" == "Hello, room"


# TEST: Invite a user to a chat room, with a reason
POST {{restapi_url}}/chatrooms/lobby/invite/{{bare_jid}}
Authorization: {{authkey}}
Content-Type: application/xml
```
<?xml version="1.0" encoding="UTF-8"?>
<mucInvitation>
	<reason>Please join us</reason>
</mucInvitation>
```
HTTP 200

# The poll returns everything that is pending for the client, which includes the invitation.
POST {{bosh_url}}/http-bind/
Content-Type: text/xml; charset=utf-8
```
<body rid='1007' sid='{{sid}}' xmlns='http://jabber.org/protocol/httpbind'/>
```
HTTP 200
[Asserts]
xpath "//*[local-name()='invite']" count == 1
xpath "string(//*[local-name()='message'][*[local-name()='x']/*[local-name()='invite']]/@from)" == "lobby@conference.example.org"
xpath "string(//*[local-name()='invite']/*[local-name()='reason'])" == "Please join us"

# TODO: Inviting a user without a request body (the reason) fails with a 500 (a NullPointerException), rather than
# sending an invitation without a reason, or returning a 400. Add a test once that is fixed.

# TEST: Invite a collection of users to a chat room
POST {{restapi_url}}/chatrooms/lobby/invite
Authorization: {{authkey}}
Content-Type: application/xml
```
<?xml version="1.0" encoding="UTF-8"?>
<mucInvitations>
	<reason>Please join us, again</reason>
	<jidsToInvite>
		<jid>{{bare_jid}}</jid>
	</jidsToInvite>
</mucInvitations>
```
HTTP 200

POST {{bosh_url}}/http-bind/
Content-Type: text/xml; charset=utf-8
```
<body rid='1008' sid='{{sid}}' xmlns='http://jabber.org/protocol/httpbind'/>
```
HTTP 200
[Asserts]
xpath "//*[local-name()='invite']" count == 1
xpath "string(//*[local-name()='invite']/*[local-name()='reason'])" == "Please join us, again"

POST {{bosh_url}}/http-bind/
Content-Type: text/xml; charset=utf-8
```
<body rid='1009' sid='{{sid}}' type='terminate' xmlns='http://jabber.org/protocol/httpbind'><presence type='unavailable' xmlns='jabber:client'/></body>
```
HTTP 200


# TEST: Create multiple chat rooms at once
POST {{restapi_url}}/chatrooms/bulk
Authorization: {{authkey}}
Content-Type: application/xml
```
<?xml version="1.0" encoding="UTF-8"?>
<chatRooms>
	<chatRoom>
		<roomName>bulk1</roomName>
		<naturalName>Bulk room 1</naturalName>
		<description>Created in bulk</description>
	</chatRoom>
	<chatRoom>
		<roomName>bulk2</roomName>
		<naturalName>Bulk room 2</naturalName>
		<description>Created in bulk</description>
	</chatRoom>
</chatRooms>
```
HTTP 200
[Asserts]
xpath "/results/success/result" count == 2
xpath "/results/success/result[roomName='bulk1'][resultType='Success']" exists
xpath "/results/success/result[roomName='bulk2'][resultType='Success']" exists
xpath "/results/failure/result" count == 0

GET {{restapi_url}}/chatrooms/bulk1
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "string(/chatRoom/naturalName)" == "Bulk room 1"

GET {{restapi_url}}/chatrooms/bulk2
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "string(/chatRoom/naturalName)" == "Bulk room 2"

# TEST: Delete a chat room
DELETE {{restapi_url}}/chatrooms/lobby
Authorization: {{authkey}}
HTTP 200

GET {{restapi_url}}/chatrooms/lobby
Authorization: {{authkey}}
HTTP 404

# TEST: Deleting a chat room that does not exist fails
DELETE {{restapi_url}}/chatrooms/nonexistent
Authorization: {{authkey}}
HTTP 404
[Asserts]
xpath "string(/error/exception)" == "RoomNotFoundException"


DELETE {{restapi_url}}/chatrooms/backroom
Authorization: {{authkey}}
HTTP 200

DELETE {{restapi_url}}/chatrooms/bulk1
Authorization: {{authkey}}
HTTP 200

DELETE {{restapi_url}}/chatrooms/bulk2
Authorization: {{authkey}}
HTTP 200
