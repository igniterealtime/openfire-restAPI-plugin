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

# https://github.com/igniterealtime/openfire-restAPI-plugin/issues/242
# abstractGroupProvider.shared.recursive is registered through Openfire's SystemProperty API with a default value.
# It used to 404 when queried individually, as that lookup checked JiveGlobals only.
GET http://localhost:9090/plugins/restapi/v1/system/properties/abstractGroupProvider.shared.recursive
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/property" count == 1
xpath "string(/property/@value)" == "false"

# https://github.com/igniterealtime/openfire-restAPI-plugin/issues/242
# adminConsole.servlet-request-authenticator is registered through Openfire's SystemProperty API without a default value.
# It used to 404 when queried individually, as that lookup checked JiveGlobals only.
GET http://localhost:9090/plugins/restapi/v1/system/properties/adminConsole.servlet-request-authenticator
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/property" count == 1

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

# https://github.com/igniterealtime/openfire-restAPI-plugin/issues/244
# The REST API plugin should not allow users to access properties used to control the plugin's functionality.
GET http://localhost:9090/plugins/restapi/v1/system/properties/plugin.restapi.enabled
Authorization: {{authkey}}
HTTP 403

# https://github.com/igniterealtime/openfire-restAPI-plugin/issues/244
# The REST API plugin should not allow users to access properties used to control the plugin's functionality.
PUT http://localhost:9090/plugins/restapi/v1/system/properties/plugin.restapi.enabled
Authorization: {{authkey}}
Content-Type: application/xml
```
<?xml version="1.0" encoding="UTF-8"?>
<property key="plugin.restapi.enabled" value="true" />
```
HTTP 403

# https://github.com/igniterealtime/openfire-restAPI-plugin/issues/244
# The REST API plugin should not allow users to access properties used to control the plugin's functionality.
DELETE http://localhost:9090/plugins/restapi/v1/system/properties/plugin.restapi.enabled
Authorization: {{authkey}}
HTTP 403


# https://github.com/igniterealtime/openfire-restAPI-plugin/issues/244
# The REST API plugin should not allow users to access properties used to control the plugin's functionality.
GET http://localhost:9090/plugins/restapi/v1/system/properties/plugin.restapi.httpAuth
Authorization: {{authkey}}
HTTP 403

# https://github.com/igniterealtime/openfire-restAPI-plugin/issues/244
# The REST API plugin should not allow users to access properties used to control the plugin's functionality.
PUT http://localhost:9090/plugins/restapi/v1/system/properties/plugin.restapi.httpAuth
Authorization: {{authkey}}
Content-Type: application/xml
```
<?xml version="1.0" encoding="UTF-8"?>
<property key="plugin.restapi.httpAuth" value="custom" />
```
HTTP 403

# https://github.com/igniterealtime/openfire-restAPI-plugin/issues/244
# The REST API plugin should not allow users to access properties used to control the plugin's functionality.
DELETE http://localhost:9090/plugins/restapi/v1/system/properties/plugin.restapi.httpAuth
Authorization: {{authkey}}
HTTP 403


# https://github.com/igniterealtime/openfire-restAPI-plugin/issues/244
# The REST API plugin should not allow users to access properties used to control the plugin's functionality.
GET http://localhost:9090/plugins/restapi/v1/system/properties/plugin.restapi.customAuthFilter
Authorization: {{authkey}}
HTTP 403

# https://github.com/igniterealtime/openfire-restAPI-plugin/issues/244
# The REST API plugin should not allow users to access properties used to control the plugin's functionality.
PUT http://localhost:9090/plugins/restapi/v1/system/properties/plugin.restapi.customAuthFilter
Authorization: {{authkey}}
Content-Type: application/xml
```
<?xml version="1.0" encoding="UTF-8"?>
<property key="plugin.restapi.customAuthFilter" value="test.class" />
```
HTTP 403

# https://github.com/igniterealtime/openfire-restAPI-plugin/issues/244
# The REST API plugin should not allow users to access properties used to control the plugin's functionality.
DELETE http://localhost:9090/plugins/restapi/v1/system/properties/plugin.restapi.customAuthFilter
Authorization: {{authkey}}
HTTP 403


# https://github.com/igniterealtime/openfire-restAPI-plugin/issues/244
# The REST API plugin should not allow users to access properties used to control the plugin's functionality.
GET http://localhost:9090/plugins/restapi/v1/system/properties/plugin.restapi.secret
Authorization: {{authkey}}
HTTP 403

# https://github.com/igniterealtime/openfire-restAPI-plugin/issues/244
# The REST API plugin should not allow users to access properties used to control the plugin's functionality.
PUT http://localhost:9090/plugins/restapi/v1/system/properties/plugin.restapi.secret
Authorization: {{authkey}}
Content-Type: application/xml
```
<?xml version="1.0" encoding="UTF-8"?>
<property key="plugin.restapi.secret" value="test" />
```
HTTP 403

# https://github.com/igniterealtime/openfire-restAPI-plugin/issues/244
# The REST API plugin should not allow users to access properties used to control the plugin's functionality.
DELETE http://localhost:9090/plugins/restapi/v1/system/properties/plugin.restapi.secret
Authorization: {{authkey}}
HTTP 403


# https://github.com/igniterealtime/openfire-restAPI-plugin/issues/244
# The REST API plugin should not allow users to access properties used to control the plugin's functionality.
GET http://localhost:9090/plugins/restapi/v1/system/properties/plugin.restapi.serviceLoggingEnabled
Authorization: {{authkey}}
HTTP 403

# https://github.com/igniterealtime/openfire-restAPI-plugin/issues/244
# The REST API plugin should not allow users to access properties used to control the plugin's functionality.
PUT http://localhost:9090/plugins/restapi/v1/system/properties/plugin.restapi.serviceLoggingEnabled
Authorization: {{authkey}}
Content-Type: application/xml
```
<?xml version="1.0" encoding="UTF-8"?>
<property key="plugin.restapi.serviceLoggingEnabled" value="true" />
```
HTTP 403

# https://github.com/igniterealtime/openfire-restAPI-plugin/issues/244
# The REST API plugin should not allow users to access properties used to control the plugin's functionality.
DELETE http://localhost:9090/plugins/restapi/v1/system/properties/plugin.restapi.serviceLoggingEnabled
Authorization: {{authkey}}
HTTP 403


# https://github.com/igniterealtime/openfire-restAPI-plugin/issues/244
# The REST API plugin should not allow users to access properties used to control the plugin's functionality.
GET http://localhost:9090/plugins/restapi/v1/system/properties/plugin.restapi.allowedIPs
Authorization: {{authkey}}
HTTP 403

# https://github.com/igniterealtime/openfire-restAPI-plugin/issues/244
# The REST API plugin should not allow users to access properties used to control the plugin's functionality.
PUT http://localhost:9090/plugins/restapi/v1/system/properties/plugin.restapi.allowedIPs
Authorization: {{authkey}}
Content-Type: application/xml
```
<?xml version="1.0" encoding="UTF-8"?>
<property key="plugin.restapi.allowedIPs" value="true" />
```
HTTP 403

# https://github.com/igniterealtime/openfire-restAPI-plugin/issues/244
# The REST API plugin should not allow users to access properties used to control the plugin's functionality.
DELETE http://localhost:9090/plugins/restapi/v1/system/properties/plugin.restapi.allowedIPs
Authorization: {{authkey}}
HTTP 403


# https://github.com/igniterealtime/openfire-restAPI-plugin/issues/244
# Openfire deletes a property together with all of its child properties. Deleting the parent of the plugin's own
# properties (which exist in this setup) should therefore not be allowed either.
DELETE http://localhost:9090/plugins/restapi/v1/system/properties/plugin.restapi
Authorization: {{authkey}}
HTTP 403


# Deleting a property also deletes its child properties.
POST http://localhost:9090/plugins/restapi/v1/system/properties
Authorization: {{authkey}}
Content-Type: application/xml
```
<?xml version="1.0" encoding="UTF-8"?>
<property key="test.parent" value="parent-value" />
```
HTTP 201

POST http://localhost:9090/plugins/restapi/v1/system/properties
Authorization: {{authkey}}
Content-Type: application/xml
```
<?xml version="1.0" encoding="UTF-8"?>
<property key="test.parent.child" value="child-value" />
```
HTTP 201

DELETE http://localhost:9090/plugins/restapi/v1/system/properties/test.parent
Authorization: {{authkey}}
HTTP 200

GET http://localhost:9090/plugins/restapi/v1/system/properties/test.parent.child
Authorization: {{authkey}}
HTTP 404


# https://github.com/igniterealtime/openfire-restAPI-plugin/issues/259
# Openfire itself uses apostrophes in property keys (e.g. for the caches of MUC services), which should be usable in
# the URL path.
GET http://localhost:9090/plugins/restapi/v1/system/properties/cache.MUCService'conference'Rooms.size
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "string(/property/@key)" == "cache.MUCService'conference'Rooms.size"

POST http://localhost:9090/plugins/restapi/v1/system/properties
Authorization: {{authkey}}
Content-Type: application/xml
```
<?xml version="1.0" encoding="UTF-8"?>
<property key="test.MUCService'room'Rooms.size" value="42" />
```
HTTP 201

GET http://localhost:9090/plugins/restapi/v1/system/properties/test.MUCService'room'Rooms.size
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "string(/property/@value)" == "42"

DELETE http://localhost:9090/plugins/restapi/v1/system/properties/test.MUCService'room'Rooms.size
Authorization: {{authkey}}
HTTP 200

GET http://localhost:9090/plugins/restapi/v1/system/properties/test.MUCService'room'Rooms.size
Authorization: {{authkey}}
HTTP 404
