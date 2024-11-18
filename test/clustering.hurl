GET {{host}}/plugins/restapi/v1/clustering/status
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "string(/clustering/status)" == "Disabled" 

GET {{host}}/plugins/restapi/v1/clustering/nodes
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/clusterNodes[not(child::node())]" exists
