# TEST: Get the roster of a user from the demoboot configuration
GET {{restapi_url}}/users/john/roster
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/roster/rosterItem" count == 1
xpath "string(/roster/rosterItem/jid)" == "jane@example.org"
xpath "string(/roster/rosterItem/nickname)" == "Jane"
xpath "string(/roster/rosterItem/subscriptionType)" == "3"
xpath "/roster/rosterItem/groups" exists


# Roster changes are tested on a dedicated user, which is removed at the end.
POST {{restapi_url}}/users
Authorization: {{authkey}}
Content-Type: application/xml
```
<?xml version="1.0" encoding="UTF-8"?>
<user>
	<username>data</username>
	<password>spot</password>
</user>
```
HTTP 201

# TEST: A new user has an empty roster
GET {{restapi_url}}/users/data/roster
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/roster/rosterItem" count == 0

# TEST: Add a roster entry
POST {{restapi_url}}/users/data/roster
Authorization: {{authkey}}
Content-Type: application/xml
```
<?xml version="1.0" encoding="UTF-8"?>
<rosterItem>
	<jid>john@example.org</jid>
	<nickname>John</nickname>
	<subscriptionType>3</subscriptionType>
	<groups>
		<group>Friends</group>
	</groups>
</rosterItem>
```
HTTP 201

GET {{restapi_url}}/users/data/roster
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/roster/rosterItem" count == 1
xpath "string(/roster/rosterItem/jid)" == "john@example.org"
xpath "string(/roster/rosterItem/nickname)" == "John"
xpath "string(/roster/rosterItem/subscriptionType)" == "3"
xpath "/roster/rosterItem/groups/group" count == 1
xpath "string(/roster/rosterItem/groups/group)" == "Friends"

# TEST: Adding an entry for a contact that is already on the roster is a conflict
POST {{restapi_url}}/users/data/roster
Authorization: {{authkey}}
Content-Type: application/xml
```
<?xml version="1.0" encoding="UTF-8"?>
<rosterItem>
	<jid>john@example.org</jid>
</rosterItem>
```
HTTP 409
[Asserts]
xpath "string(/error/exception)" == "UserAlreadyExistsException"

# TEST: Adding a roster entry without a JID is rejected
POST {{restapi_url}}/users/data/roster
Authorization: {{authkey}}
Content-Type: application/xml
```
<?xml version="1.0" encoding="UTF-8"?>
<rosterItem>
	<nickname>Nobody</nickname>
</rosterItem>
```
HTTP 400
[Asserts]
xpath "string(/error/exception)" == "IllegalArgumentException"

# TEST: Adding a roster entry with an invalid subscription type is rejected, and adds no entry
# Disabled until https://github.com/igniterealtime/openfire-restAPI-plugin/issues/276 is fixed.
POST {{restapi_url}}/users/data/roster
Authorization: {{authkey}}
Content-Type: application/xml
[Options]
skip: true
```
<?xml version="1.0" encoding="UTF-8"?>
<rosterItem>
	<jid>jane@example.org</jid>
	<subscriptionType>9</subscriptionType>
</rosterItem>
```
HTTP 400

GET {{restapi_url}}/users/data/roster
Authorization: {{authkey}}
[Options]
skip: true
HTTP 200
[Asserts]
xpath "/roster/rosterItem[jid='jane@example.org']" not exists

# TEST: A roster entry JID without a domain is resolved against the local domain
# Disabled until https://github.com/igniterealtime/openfire-restAPI-plugin/issues/277 is fixed.
# (The issue also allows for rejecting such a JID with a 400 instead, in which case this test needs to change.)
POST {{restapi_url}}/users/data/roster
Authorization: {{authkey}}
Content-Type: application/xml
[Options]
skip: true
```
<?xml version="1.0" encoding="UTF-8"?>
<rosterItem>
	<jid>jane</jid>
</rosterItem>
```
HTTP 201

GET {{restapi_url}}/users/data/roster
Authorization: {{authkey}}
[Options]
skip: true
HTTP 200
[Asserts]
xpath "/roster/rosterItem[jid='jane@example.org']" exists
xpath "/roster/rosterItem[jid='jane']" not exists

DELETE {{restapi_url}}/users/data/roster/jane@example.org
Authorization: {{authkey}}
[Options]
skip: true
HTTP 200

# TEST: Adding a roster entry with an empty or invalid JID is rejected
# Disabled until https://github.com/igniterealtime/openfire-restAPI-plugin/issues/278 is fixed.
POST {{restapi_url}}/users/data/roster
Authorization: {{authkey}}
Content-Type: application/xml
[Options]
skip: true
```
<?xml version="1.0" encoding="UTF-8"?>
<rosterItem>
	<jid></jid>
</rosterItem>
```
HTTP 400

POST {{restapi_url}}/users/data/roster
Authorization: {{authkey}}
Content-Type: application/xml
[Options]
skip: true
```
<?xml version="1.0" encoding="UTF-8"?>
<rosterItem>
	<jid>not a@valid@jid</jid>
</rosterItem>
```
HTTP 400

# TEST: Adding a roster entry for a user that does not exist fails
# Disabled until https://github.com/igniterealtime/openfire-restAPI-plugin/issues/274 is fixed.
POST {{restapi_url}}/users/nonexistent/roster
Authorization: {{authkey}}
Content-Type: application/xml
[Options]
skip: true
```
<?xml version="1.0" encoding="UTF-8"?>
<rosterItem>
	<jid>john@example.org</jid>
</rosterItem>
```
HTTP 404

# TEST: Getting the roster of a user that does not exist fails
# Disabled until https://github.com/igniterealtime/openfire-restAPI-plugin/issues/275 is fixed.
GET {{restapi_url}}/users/nonexistent/roster
Authorization: {{authkey}}
[Options]
skip: true
HTTP 404

# TEST: Update a roster entry
PUT {{restapi_url}}/users/data/roster/john@example.org
Authorization: {{authkey}}
Content-Type: application/xml
```
<?xml version="1.0" encoding="UTF-8"?>
<rosterItem>
	<jid>john@example.org</jid>
	<nickname>Johnny</nickname>
	<subscriptionType>1</subscriptionType>
	<groups>
		<group>Crew</group>
	</groups>
</rosterItem>
```
HTTP 200

GET {{restapi_url}}/users/data/roster
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/roster/rosterItem" count == 1
xpath "string(/roster/rosterItem/jid)" == "john@example.org"
xpath "string(/roster/rosterItem/nickname)" == "Johnny"
xpath "string(/roster/rosterItem/subscriptionType)" == "1"
xpath "/roster/rosterItem/groups/group" count == 1
xpath "string(/roster/rosterItem/groups/group)" == "Crew"

# TEST: Updating an entry for a contact that is not on the roster fails
PUT {{restapi_url}}/users/data/roster/jane@example.org
Authorization: {{authkey}}
Content-Type: application/xml
```
<?xml version="1.0" encoding="UTF-8"?>
<rosterItem>
	<jid>jane@example.org</jid>
	<nickname>Jane</nickname>
</rosterItem>
```
HTTP 404
[Asserts]
xpath "string(/error/exception)" == "UserNotFoundException"
xpath "string(/error/resource)" == "jane@example.org"

# TEST: Delete a roster entry
DELETE {{restapi_url}}/users/data/roster/john@example.org
Authorization: {{authkey}}
HTTP 200

GET {{restapi_url}}/users/data/roster
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/roster/rosterItem" count == 0

# TEST: Deleting an entry that is not on the roster fails
DELETE {{restapi_url}}/users/data/roster/john@example.org
Authorization: {{authkey}}
HTTP 404
[Asserts]
xpath "string(/error/exception)" == "RosterItemNotFound"
xpath "string(/error/resource)" == "john@example.org"

# TEST: Deleting a roster entry of a user that does not exist fails
DELETE {{restapi_url}}/users/nonexistent/roster/john@example.org
Authorization: {{authkey}}
HTTP 404
[Asserts]
xpath "string(/error/exception)" == "UserNotFoundException"
xpath "string(/error/resource)" == "nonexistent"

DELETE {{restapi_url}}/users/data
Authorization: {{authkey}}
HTTP 200
