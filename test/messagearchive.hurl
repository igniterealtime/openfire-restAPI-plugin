GET http://localhost:9090/plugins/restapi/v1/archive/messages/unread/john@example.org
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "string(/archive/jid)" == "john@example.org"
xpath "string(/archive/count)" == "0"

# TODO: How to get this >0 ?