# TEST: The default 'conference' chat service is listed
GET {{restapi_url}}/chatservices
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/chatServices/chatService[serviceName='conference']" count == 1
xpath "string(/chatServices/chatService[serviceName='conference']/description)" == "Public Chatrooms"
xpath "string(/chatServices/chatService[serviceName='conference']/hidden)" == "false"

# The API cannot delete a chat service, so each run creates a new one, with a unique name.

# TEST: A created chat service is listed, and can have chat rooms
POST {{restapi_url}}/chatservices
Authorization: {{authkey}}
Content-Type: application/xml
[Options]
variable: service=test-{{newUuid}}
```
<?xml version="1.0" encoding="UTF-8"?>
<chatService>
	<serviceName>{{service}}</serviceName>
	<description>Test service</description>
	<hidden>true</hidden>
</chatService>
```
HTTP 201

GET {{restapi_url}}/chatservices
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/chatServices/chatService[serviceName='{{service}}']" count == 1
xpath "string(/chatServices/chatService[serviceName='{{service}}']/description)" == "Test service"
xpath "string(/chatServices/chatService[serviceName='{{service}}']/hidden)" == "true"

POST {{restapi_url}}/chatrooms?servicename={{service}}
Authorization: {{authkey}}
Content-Type: application/xml
```
<?xml version="1.0" encoding="UTF-8"?>
<chatRoom>
	<roomName>room</roomName>
	<naturalName>A room</naturalName>
	<description>A room in the test service</description>
</chatRoom>
```
HTTP 201

GET {{restapi_url}}/chatrooms?servicename={{service}}&type=all
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/chatRooms/chatRoom" count == 1
xpath "string(/chatRooms/chatRoom/roomName)" == "room"

DELETE {{restapi_url}}/chatrooms/room?servicename={{service}}
Authorization: {{authkey}}
HTTP 200

# TEST: Creating a chat service that already exists is a conflict
POST {{restapi_url}}/chatservices
Authorization: {{authkey}}
Content-Type: application/xml
```
<?xml version="1.0" encoding="UTF-8"?>
<chatService>
	<serviceName>{{service}}</serviceName>
	<description>Another test service</description>
	<hidden>false</hidden>
</chatService>
```
HTTP 409
[Asserts]
xpath "string(/error/exception)" == "AlreadyExistsException"
xpath "string(/error/resource)" == "{{service}}"

# TEST: Creating a chat service without a name is rejected
# Disabled until https://github.com/igniterealtime/openfire-restAPI-plugin/issues/287 is fixed.
POST {{restapi_url}}/chatservices
Authorization: {{authkey}}
Content-Type: application/xml
[Options]
skip: true
```
<?xml version="1.0" encoding="UTF-8"?>
<chatService>
	<description>No name</description>
	<hidden>false</hidden>
</chatService>
```
HTTP 400

POST {{restapi_url}}/chatservices
Authorization: {{authkey}}
Content-Type: application/xml
[Options]
skip: true
```
<?xml version="1.0" encoding="UTF-8"?>
<chatService>
	<serviceName></serviceName>
	<description>Empty name</description>
	<hidden>false</hidden>
</chatService>
```
HTTP 400
