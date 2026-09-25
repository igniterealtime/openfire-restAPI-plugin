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

GET {{restapi_url}}/users/data/roster
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/roster/rosterItem" count == 0

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

# Adding an entry for a contact that is already on the roster is a conflict.
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

# A roster entry needs a JID.
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

# TODO: Adding a roster entry with an invalid subscriptionType (e.g. 9) returns 409 (UserAlreadyExistsException), and
# the entry is created anyway, as UserServiceController#addRosterItem validates the subscription type only after
# creating the roster item. Add a test expecting a 400 (and no new roster entry) once that is fixed.

# TODO: A JID without an '@' is taken to be a domain JID: adding a roster entry with <jid>john</jid> stores an entry
# for the domain 'john', rather than for john@example.org (unlike group members, which resolve a username against the
# local domain). An empty or unparsable JID causes a 500 rather than a 400. Add tests once the intended behaviour is
# decided.

# TODO: Adding a roster entry for a user that does not exist returns 201 (and stores the roster entry), where 404 is
# documented. Getting the roster of a user that does not exist returns 200 (rather than 404) too. Add tests for those
# once UserServiceController#getUserRoster checks that the user exists.

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

# Updating an entry for a contact that is not on the roster fails.
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

DELETE {{restapi_url}}/users/data/roster/john@example.org
Authorization: {{authkey}}
HTTP 200

GET {{restapi_url}}/users/data/roster
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/roster/rosterItem" count == 0

DELETE {{restapi_url}}/users/data/roster/john@example.org
Authorization: {{authkey}}
HTTP 404
[Asserts]
xpath "string(/error/exception)" == "RosterItemNotFound"
xpath "string(/error/resource)" == "john@example.org"

DELETE {{restapi_url}}/users/nonexistent/roster/john@example.org
Authorization: {{authkey}}
HTTP 404
[Asserts]
xpath "string(/error/exception)" == "UserNotFoundException"
xpath "string(/error/resource)" == "nonexistent"

DELETE {{restapi_url}}/users/data
Authorization: {{authkey}}
HTTP 200
