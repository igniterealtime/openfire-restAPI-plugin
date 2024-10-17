GET http://localhost:9090/plugins/restapi/v1/system/liveness
Authorization: {{authkey}}
HTTP 200

GET http://localhost:9090/plugins/restapi/v1/system/liveness/deadlock
Authorization: {{authkey}}
HTTP 200

GET http://localhost:9090/plugins/restapi/v1/system/liveness/properties
Authorization: {{authkey}}
HTTP 200

GET http://localhost:9090/plugins/restapi/v1/system/readiness
Authorization: {{authkey}}
HTTP 200

GET http://localhost:9090/plugins/restapi/v1/system/readiness/cluster
Authorization: {{authkey}}
HTTP 200

GET http://localhost:9090/plugins/restapi/v1/system/readiness/connections
Authorization: {{authkey}}
HTTP 200

GET http://localhost:9090/plugins/restapi/v1/system/readiness/plugins
Authorization: {{authkey}}
HTTP 200

GET http://localhost:9090/plugins/restapi/v1/system/readiness/server
Authorization: {{authkey}}
HTTP 200

GET http://localhost:9090/plugins/restapi/v1/system/properties
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/properties/property" count > 150
# TODO test that property with attribute key=admin.authorizedJIDs exists
# TODO test that property with attribute key=plugin.restapi.enabled has value=true

POST http://localhost:9090/plugins/restapi/v1/system/properties
Authorization: {{authkey}}
Content-Type: application/xml
```
<?xml version="1.0" encoding="UTF-8"?>
<property key="test.key" value="test-value" />
```
HTTP 201

GET http://localhost:9090/plugins/restapi/v1/system/properties/test.key
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/property" count == 1
# TODO test that property with attribute key=test.key has value=test-value

PUT http://localhost:9090/plugins/restapi/v1/system/properties/test.key
Authorization: {{authkey}}
Content-Type: application/xml
```
<?xml version="1.0" encoding="UTF-8"?>
<property key="test.key" value="new-value" />
```
HTTP 200

PUT http://localhost:9090/plugins/restapi/v1/system/properties/wrong.key
Authorization: {{authkey}}
Content-Type: application/xml
```
<?xml version="1.0" encoding="UTF-8"?>
<property key="wrong.key" value="new-value" />
```
HTTP 404

PUT http://localhost:9090/plugins/restapi/v1/system/properties/test.key
Authorization: {{authkey}}
Content-Type: application/xml
```
<?xml version="1.0" encoding="UTF-8"?>
<property key="wrong.key" value="new-value" />
```
HTTP 400

DELETE http://localhost:9090/plugins/restapi/v1/system/properties/test.key
Authorization: {{authkey}}
HTTP 200