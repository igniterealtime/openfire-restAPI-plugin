GET http://localhost:9090/plugins/restapi/v1/chatservices
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/chatServices/chatService" count == 1
xpath "string(/chatServices/chatService/serviceName)" == "conference"
xpath "string(/chatServices/chatService/description)" == "Public Chatrooms"
xpath "string(/chatServices/chatService/hidden)" == "false"
