GET http://localhost:9090/plugins/restapi/v1/logs/security
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/logs/log" count >= 1 # Minimum of 'plugin loaded'?
xpath "/logs/log/details" exists
xpath "/logs/log/logId" exists
xpath "/logs/log/node" exists
xpath "/logs/log/summary" exists
xpath "/logs/log/timestamp" exists
xpath "/logs/log/username" exists