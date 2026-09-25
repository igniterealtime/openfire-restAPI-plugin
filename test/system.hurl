# TEST: Liveness check
GET {{restapi_url}}/system/liveness
Authorization: {{authkey}}
HTTP 200

# TEST: Liveness check for deadlocked threads
GET {{restapi_url}}/system/liveness/deadlock
Authorization: {{authkey}}
HTTP 200

# TEST: Liveness check for system properties
GET {{restapi_url}}/system/liveness/properties
Authorization: {{authkey}}
HTTP 200

# TEST: Readiness check
GET {{restapi_url}}/system/readiness
Authorization: {{authkey}}
HTTP 200

# TEST: Readiness check for the cluster
GET {{restapi_url}}/system/readiness/cluster
Authorization: {{authkey}}
HTTP 200

# TEST: Readiness check for connections
GET {{restapi_url}}/system/readiness/connections
Authorization: {{authkey}}
HTTP 200

# TEST: Readiness check for plugins
GET {{restapi_url}}/system/readiness/plugins
Authorization: {{authkey}}
HTTP 200

# TEST: Readiness check for the server
GET {{restapi_url}}/system/readiness/server
Authorization: {{authkey}}
HTTP 200

# TEST: List the system properties, which excludes the plugin's own properties
GET {{restapi_url}}/system/properties
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/properties/property" count > 150
xpath "/properties/property[@key='admin.authorizedJIDs']" exists
xpath "string(/properties/property[@key='abstractGroupProvider.shared.recursive']/@value)" == "false"
# https://github.com/igniterealtime/openfire-restAPI-plugin/issues/244
# The plugin's own properties (e.g. plugin.restapi.enabled) must not be exposed in the listing.
xpath "/properties/property[starts-with(@key, 'plugin.restapi.')]" not exists

# TEST: Get a property that has a default value, but was never set
# https://github.com/igniterealtime/openfire-restAPI-plugin/issues/242
# abstractGroupProvider.shared.recursive is registered through Openfire's SystemProperty API with a default value.
# It used to 404 when queried individually, as that lookup checked JiveGlobals only.
GET {{restapi_url}}/system/properties/abstractGroupProvider.shared.recursive
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/property" count == 1
xpath "string(/property/@value)" == "false"

# TEST: Get a property that has no default value, and was never set
# https://github.com/igniterealtime/openfire-restAPI-plugin/issues/242
# adminConsole.servlet-request-authenticator is registered through Openfire's SystemProperty API without a default value.
# It used to 404 when queried individually, as that lookup checked JiveGlobals only.
GET {{restapi_url}}/system/properties/adminConsole.servlet-request-authenticator
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/property" count == 1

# TEST: Create a property
POST {{restapi_url}}/system/properties
Authorization: {{authkey}}
Content-Type: application/xml
```
<?xml version="1.0" encoding="UTF-8"?>
<property key="test.key" value="test-value" />
```
HTTP 201

GET {{restapi_url}}/system/properties/test.key
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/property" count == 1
xpath "string(/property/@key)" == "test.key"
xpath "string(/property/@value)" == "test-value"

# TEST: Update a property
PUT {{restapi_url}}/system/properties/test.key
Authorization: {{authkey}}
Content-Type: application/xml
```
<?xml version="1.0" encoding="UTF-8"?>
<property key="test.key" value="new-value" />
```
HTTP 200

GET {{restapi_url}}/system/properties/test.key
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "string(/property/@value)" == "new-value"

# TEST: Updating a property that does not exist fails
PUT {{restapi_url}}/system/properties/wrong.key
Authorization: {{authkey}}
Content-Type: application/xml
```
<?xml version="1.0" encoding="UTF-8"?>
<property key="wrong.key" value="new-value" />
```
HTTP 404

# TEST: Updating a property with a key that does not match the URL is rejected
PUT {{restapi_url}}/system/properties/test.key
Authorization: {{authkey}}
Content-Type: application/xml
```
<?xml version="1.0" encoding="UTF-8"?>
<property key="wrong.key" value="new-value" />
```
HTTP 400

# TEST: Delete a property
DELETE {{restapi_url}}/system/properties/test.key
Authorization: {{authkey}}
HTTP 200

GET {{restapi_url}}/system/properties/test.key
Authorization: {{authkey}}
HTTP 404

# TEST: Getting plugin.restapi.enabled is forbidden
# https://github.com/igniterealtime/openfire-restAPI-plugin/issues/244
# The REST API plugin should not allow users to access properties used to control the plugin's functionality.
GET {{restapi_url}}/system/properties/plugin.restapi.enabled
Authorization: {{authkey}}
HTTP 403

# TEST: Updating plugin.restapi.enabled is forbidden
# https://github.com/igniterealtime/openfire-restAPI-plugin/issues/244
# The REST API plugin should not allow users to access properties used to control the plugin's functionality.
PUT {{restapi_url}}/system/properties/plugin.restapi.enabled
Authorization: {{authkey}}
Content-Type: application/xml
```
<?xml version="1.0" encoding="UTF-8"?>
<property key="plugin.restapi.enabled" value="true" />
```
HTTP 403

# TEST: Deleting plugin.restapi.enabled is forbidden
# https://github.com/igniterealtime/openfire-restAPI-plugin/issues/244
# The REST API plugin should not allow users to access properties used to control the plugin's functionality.
DELETE {{restapi_url}}/system/properties/plugin.restapi.enabled
Authorization: {{authkey}}
HTTP 403


# TEST: Getting plugin.restapi.httpAuth is forbidden
# https://github.com/igniterealtime/openfire-restAPI-plugin/issues/244
# The REST API plugin should not allow users to access properties used to control the plugin's functionality.
GET {{restapi_url}}/system/properties/plugin.restapi.httpAuth
Authorization: {{authkey}}
HTTP 403

# TEST: Updating plugin.restapi.httpAuth is forbidden
# https://github.com/igniterealtime/openfire-restAPI-plugin/issues/244
# The REST API plugin should not allow users to access properties used to control the plugin's functionality.
PUT {{restapi_url}}/system/properties/plugin.restapi.httpAuth
Authorization: {{authkey}}
Content-Type: application/xml
```
<?xml version="1.0" encoding="UTF-8"?>
<property key="plugin.restapi.httpAuth" value="custom" />
```
HTTP 403

# TEST: Deleting plugin.restapi.httpAuth is forbidden
# https://github.com/igniterealtime/openfire-restAPI-plugin/issues/244
# The REST API plugin should not allow users to access properties used to control the plugin's functionality.
DELETE {{restapi_url}}/system/properties/plugin.restapi.httpAuth
Authorization: {{authkey}}
HTTP 403


# TEST: Getting plugin.restapi.customAuthFilter is forbidden
# https://github.com/igniterealtime/openfire-restAPI-plugin/issues/244
# The REST API plugin should not allow users to access properties used to control the plugin's functionality.
GET {{restapi_url}}/system/properties/plugin.restapi.customAuthFilter
Authorization: {{authkey}}
HTTP 403

# TEST: Updating plugin.restapi.customAuthFilter is forbidden
# https://github.com/igniterealtime/openfire-restAPI-plugin/issues/244
# The REST API plugin should not allow users to access properties used to control the plugin's functionality.
PUT {{restapi_url}}/system/properties/plugin.restapi.customAuthFilter
Authorization: {{authkey}}
Content-Type: application/xml
```
<?xml version="1.0" encoding="UTF-8"?>
<property key="plugin.restapi.customAuthFilter" value="test.class" />
```
HTTP 403

# TEST: Deleting plugin.restapi.customAuthFilter is forbidden
# https://github.com/igniterealtime/openfire-restAPI-plugin/issues/244
# The REST API plugin should not allow users to access properties used to control the plugin's functionality.
DELETE {{restapi_url}}/system/properties/plugin.restapi.customAuthFilter
Authorization: {{authkey}}
HTTP 403


# TEST: Getting plugin.restapi.secret is forbidden
# https://github.com/igniterealtime/openfire-restAPI-plugin/issues/244
# The REST API plugin should not allow users to access properties used to control the plugin's functionality.
GET {{restapi_url}}/system/properties/plugin.restapi.secret
Authorization: {{authkey}}
HTTP 403

# TEST: Updating plugin.restapi.secret is forbidden
# https://github.com/igniterealtime/openfire-restAPI-plugin/issues/244
# The REST API plugin should not allow users to access properties used to control the plugin's functionality.
PUT {{restapi_url}}/system/properties/plugin.restapi.secret
Authorization: {{authkey}}
Content-Type: application/xml
```
<?xml version="1.0" encoding="UTF-8"?>
<property key="plugin.restapi.secret" value="test" />
```
HTTP 403

# TEST: Deleting plugin.restapi.secret is forbidden
# https://github.com/igniterealtime/openfire-restAPI-plugin/issues/244
# The REST API plugin should not allow users to access properties used to control the plugin's functionality.
DELETE {{restapi_url}}/system/properties/plugin.restapi.secret
Authorization: {{authkey}}
HTTP 403


# TEST: Getting plugin.restapi.serviceLoggingEnabled is forbidden
# https://github.com/igniterealtime/openfire-restAPI-plugin/issues/244
# The REST API plugin should not allow users to access properties used to control the plugin's functionality.
GET {{restapi_url}}/system/properties/plugin.restapi.serviceLoggingEnabled
Authorization: {{authkey}}
HTTP 403

# TEST: Updating plugin.restapi.serviceLoggingEnabled is forbidden
# https://github.com/igniterealtime/openfire-restAPI-plugin/issues/244
# The REST API plugin should not allow users to access properties used to control the plugin's functionality.
PUT {{restapi_url}}/system/properties/plugin.restapi.serviceLoggingEnabled
Authorization: {{authkey}}
Content-Type: application/xml
```
<?xml version="1.0" encoding="UTF-8"?>
<property key="plugin.restapi.serviceLoggingEnabled" value="true" />
```
HTTP 403

# TEST: Deleting plugin.restapi.serviceLoggingEnabled is forbidden
# https://github.com/igniterealtime/openfire-restAPI-plugin/issues/244
# The REST API plugin should not allow users to access properties used to control the plugin's functionality.
DELETE {{restapi_url}}/system/properties/plugin.restapi.serviceLoggingEnabled
Authorization: {{authkey}}
HTTP 403


# TEST: Getting plugin.restapi.allowedIPs is forbidden
# https://github.com/igniterealtime/openfire-restAPI-plugin/issues/244
# The REST API plugin should not allow users to access properties used to control the plugin's functionality.
GET {{restapi_url}}/system/properties/plugin.restapi.allowedIPs
Authorization: {{authkey}}
HTTP 403

# TEST: Updating plugin.restapi.allowedIPs is forbidden
# https://github.com/igniterealtime/openfire-restAPI-plugin/issues/244
# The REST API plugin should not allow users to access properties used to control the plugin's functionality.
PUT {{restapi_url}}/system/properties/plugin.restapi.allowedIPs
Authorization: {{authkey}}
Content-Type: application/xml
```
<?xml version="1.0" encoding="UTF-8"?>
<property key="plugin.restapi.allowedIPs" value="true" />
```
HTTP 403

# TEST: Deleting plugin.restapi.allowedIPs is forbidden
# https://github.com/igniterealtime/openfire-restAPI-plugin/issues/244
# The REST API plugin should not allow users to access properties used to control the plugin's functionality.
DELETE {{restapi_url}}/system/properties/plugin.restapi.allowedIPs
Authorization: {{authkey}}
HTTP 403


# TEST: Deleting the parent of the plugin's own properties is forbidden
# https://github.com/igniterealtime/openfire-restAPI-plugin/issues/244
# Openfire deletes a property together with all of its child properties. Deleting the parent of the plugin's own
# properties (which exist in this setup) should therefore not be allowed either.
DELETE {{restapi_url}}/system/properties/plugin.restapi
Authorization: {{authkey}}
HTTP 403


# TEST: Deleting a property also deletes its child properties
POST {{restapi_url}}/system/properties
Authorization: {{authkey}}
Content-Type: application/xml
```
<?xml version="1.0" encoding="UTF-8"?>
<property key="test.parent" value="parent-value" />
```
HTTP 201

POST {{restapi_url}}/system/properties
Authorization: {{authkey}}
Content-Type: application/xml
```
<?xml version="1.0" encoding="UTF-8"?>
<property key="test.parent.child" value="child-value" />
```
HTTP 201

DELETE {{restapi_url}}/system/properties/test.parent
Authorization: {{authkey}}
HTTP 200

GET {{restapi_url}}/system/properties/test.parent.child
Authorization: {{authkey}}
HTTP 404


# TEST: Deleting a property is refused when the underscore in its key would match other properties
# https://github.com/igniterealtime/openfire-restAPI-plugin/issues/259
# Openfire deletes a property using SQL LIKE, in which an underscore matches any character. Deleting a property should
# be refused when that would also delete a property other than the property itself and its child properties.
POST {{restapi_url}}/system/properties
Authorization: {{authkey}}
Content-Type: application/xml
```
<?xml version="1.0" encoding="UTF-8"?>
<property key="test.foo_bar" value="value" />
```
HTTP 201

POST {{restapi_url}}/system/properties
Authorization: {{authkey}}
Content-Type: application/xml
```
<?xml version="1.0" encoding="UTF-8"?>
<property key="test.fooXbar.child" value="value" />
```
HTTP 201

DELETE {{restapi_url}}/system/properties/test.foo_bar
Authorization: {{authkey}}
HTTP 409

GET {{restapi_url}}/system/properties/test.fooXbar.child
Authorization: {{authkey}}
HTTP 200

DELETE {{restapi_url}}/system/properties/test.fooXbar.child
Authorization: {{authkey}}
HTTP 200

DELETE {{restapi_url}}/system/properties/test.foo_bar
Authorization: {{authkey}}
HTTP 200


# TEST: Get one of Openfire's own properties with apostrophes in its key
# https://github.com/igniterealtime/openfire-restAPI-plugin/issues/259
# Openfire itself uses apostrophes in property keys (e.g. for the caches of MUC services), which should be usable in
# the URL path.
GET {{restapi_url}}/system/properties/cache.MUCService'conference'Rooms.size
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "string(/property/@key)" == "cache.MUCService'conference'Rooms.size"

# TEST: A property with apostrophes in its key can be created, retrieved and deleted
POST {{restapi_url}}/system/properties
Authorization: {{authkey}}
Content-Type: application/xml
```
<?xml version="1.0" encoding="UTF-8"?>
<property key="test.MUCService'room'Rooms.size" value="42" />
```
HTTP 201

GET {{restapi_url}}/system/properties/test.MUCService'room'Rooms.size
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "string(/property/@value)" == "42"

DELETE {{restapi_url}}/system/properties/test.MUCService'room'Rooms.size
Authorization: {{authkey}}
HTTP 200

GET {{restapi_url}}/system/properties/test.MUCService'room'Rooms.size
Authorization: {{authkey}}
HTTP 404


# TEST: Creating or updating a property without a value is rejected, and deletes nothing
# Openfire treats setting a property to a null value as deleting it, together with all of its child properties.
# Creating or updating a property without a value should therefore be rejected, as it would otherwise bypass the
# checks that prevent (forbidden) child properties from being deleted.
POST {{restapi_url}}/system/properties
Authorization: {{authkey}}
Content-Type: application/xml
```
<?xml version="1.0" encoding="UTF-8"?>
<property key="test.nullparent" value="parent-value" />
```
HTTP 201

POST {{restapi_url}}/system/properties
Authorization: {{authkey}}
Content-Type: application/xml
```
<?xml version="1.0" encoding="UTF-8"?>
<property key="test.nullparent.child" value="child-value" />
```
HTTP 201

POST {{restapi_url}}/system/properties
Authorization: {{authkey}}
Content-Type: application/xml
```
<?xml version="1.0" encoding="UTF-8"?>
<property key="test.nullparent" />
```
HTTP 400

PUT {{restapi_url}}/system/properties/test.nullparent
Authorization: {{authkey}}
Content-Type: application/xml
```
<?xml version="1.0" encoding="UTF-8"?>
<property key="test.nullparent" />
```
HTTP 400

GET {{restapi_url}}/system/properties/test.nullparent
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "string(/property/@value)" == "parent-value"

GET {{restapi_url}}/system/properties/test.nullparent.child
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "string(/property/@value)" == "child-value"

DELETE {{restapi_url}}/system/properties/test.nullparent
Authorization: {{authkey}}
HTTP 200

# TEST: Creating the parent of the plugin's own properties without a value is rejected
# The parent of the plugin's own properties may not be 'created' without a value (as that would delete the plugin's
# configuration) either.
POST {{restapi_url}}/system/properties
Authorization: {{authkey}}
Content-Type: application/xml
```
<?xml version="1.0" encoding="UTF-8"?>
<property key="plugin" />
```
HTTP 400

# TEST: A property can have an empty value
# An empty value is a value (rather than a deletion), and should be stored as such.
POST {{restapi_url}}/system/properties
Authorization: {{authkey}}
Content-Type: application/xml
```
<?xml version="1.0" encoding="UTF-8"?>
<property key="test.emptyvalue" value="" />
```
HTTP 201

GET {{restapi_url}}/system/properties/test.emptyvalue
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "string(/property/@value)" == ""

DELETE {{restapi_url}}/system/properties/test.emptyvalue
Authorization: {{authkey}}
HTTP 200
