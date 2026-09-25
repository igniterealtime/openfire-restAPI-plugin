GET http://localhost:9090/plugins/restapi/v1/users/john/roster
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/roster/rosterItem" count == 1
xpath "string(/roster/rosterItem/jid)" == "jane@example.org"
xpath "string(/roster/rosterItem/nickname)" == "Jane"
xpath "string(/roster/rosterItem/subscriptionType)" == "3"
xpath "/roster/rosterItem/groups" exists

# TODO: Roster add/edit/delete
