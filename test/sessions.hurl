GET http://localhost:9090/plugins/restapi/v1/sessions
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/sessions" exists

GET http://localhost:9090/plugins/restapi/v1/sessions/john
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/sessions" exists

# TODO: This documents behaviour, but is it correct??
GET http://localhost:9090/plugins/restapi/v1/sessions/nonexistent
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/sessions" exists

# TODO: create a session (somehow), then read its props, then kick it with DELETE