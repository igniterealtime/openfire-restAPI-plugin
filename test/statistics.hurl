GET http://localhost:9090/plugins/restapi/v1/system/statistics/sessions
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/sessions/clusterSessions" exists
xpath "/sessions/localSessions" exists