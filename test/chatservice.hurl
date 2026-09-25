# TEST: The default 'conference' chat service is listed
GET {{restapi_url}}/chatservices
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/chatServices/chatService" count == 1
xpath "string(/chatServices/chatService/serviceName)" == "conference"
xpath "string(/chatServices/chatService/description)" == "Public Chatrooms"
xpath "string(/chatServices/chatService/hidden)" == "false"
