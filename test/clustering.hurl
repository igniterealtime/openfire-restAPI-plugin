# TEST: Clustering is reported as disabled on a standalone server
GET {{restapi_url}}/clustering/status
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "string(/clustering/status)" == "Disabled" 

# TEST: A standalone server has no cluster nodes
GET {{restapi_url}}/clustering/nodes
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/clusterNodes[not(child::node())]" exists
