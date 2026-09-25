GET http://localhost:9090/plugins/restapi/v1/sessions
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/sessions" exists
xpath "/sessions[not(child::node())]" count == 1 # sessions at the root, with no child nodes

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