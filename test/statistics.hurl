GET {{restapi_url}}/system/statistics/sessions
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/sessions/clusterSessions" exists
xpath "/sessions/localSessions" exists