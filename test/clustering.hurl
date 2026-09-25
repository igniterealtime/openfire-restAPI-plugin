GET {{restapi_url}}/clustering/status
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "string(/clustering/status)" == "Disabled" 

GET {{restapi_url}}/clustering/nodes
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/clusterNodes[not(child::node())]" exists
