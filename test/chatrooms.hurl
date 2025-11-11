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