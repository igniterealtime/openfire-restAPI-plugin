
# REST API Plugin Readme

The REST API Plugin provides the ability to manage Openfire by sending an REST/HTTP request to the server. This plugin's functionality is useful for applications that need to administer Openfire outside of the Openfire admin console.

## CI Build Status

[![Build Status](https://github.com/igniterealtime/openfire-restAPI-plugin/workflows/Java%20CI/badge.svg)](https://github.com/igniterealtime/openfire-restAPI-plugin/actions)

## Reporting Issues

Issues may be reported to the [forums](https://discourse.igniterealtime.org) or via this repo's [Github Issues](https://github.com/igniterealtime/openfire-restAPI-plugin).

## Feature list
* Get overview over all or specific user and to create, update or delete a user
* Get overview over all or specific group and to create, update or delete a group
* Get overview over all user roster entries and to add, update or delete a roster entry
* Add user to a group and remove a user from a group
* Lockout, unlock or kick the user (enable / disable)
* Get overview over all or specific system properties and to create, update or delete system property
* Get overview over all or specific chat room and to create, update or delete a chat room
* Get overview over all or specific user sessions
* Send broadcast message to all online users
* Get overview of all or specific security audit logs
* Get chat message history from a multi user chat room
* Get clustering status of Openfire
* Get overview of 'readiness' and 'liveness' state of Openfire

## Available REST API clients
REST API clients are implementations of the REST API in a specific programming language.

### Official
* JAVA: https://github.com/igniterealtime/REST-API-Client

### Third party
* PHP: https://github.com/gidkom/php-openfire-restapi (partly implemented)
* PHP: https://github.com/gnello/php-openfire-restapi (partly implemented)
* GO Lang: https://github.com/Urethramancer/fireman (partly implemented)
* Python: https://github.com/seamus-45/openfire-restapi (partly implemented)

## Installation

Copy restAPI.jar into the plugins directory of your Openfire server. The plugin will be automatically deployed. To upgrade to a newer version, overwrite the restAPI.jar file with the new one.

*Important Step:* To enable the plugin make sure to set the system property `adminConsole.access.allow-wildcards-in-excludes` to `true`

Without the above step the REST API plugin always [redirects to login](https://discourse.igniterealtime.org/t/when-i-upload-to-4-7-5-the-restapi-always-redirect/92892).
This was done in response to a [security issue](https://discourse.igniterealtime.org/t/cve-2023-32315-openfire-administration-console-authentication-bypass/92869).

## Explanation of REST

To provide a standard way of accessing the data the plugin is using REST.

| HTTP Method | Usage                          |
|-------------|--------------------------------|
| **GET**     | Receive a read-only data       |
| **PUT**     | Overwrite an existing resource |
| **POST**    | Creates a new resource         |
| **DELETE**  | Deletes the given resource     |

## Authentication
All REST Endpoint are secured and must be authenticated. There are two ways to authenticate: 

 - [Basic HTTP Authentication](http://en.wikipedia.org/wiki/Basic_access_authentication)
 - Shared secret key

The configuration can be done in Openfire Admin console under Server > Server Settings > REST API.

### Basic HTTP Authentication
To access the endpoints is that required to send the Username and Password of a Openfire Admin account in your HTTP header request.

E.g., for username: admin and password: 12345:
>**Header:** Authorization: Basic YWRtaW46MTIzNDU=

### Shared secret key

To access the endpoints is that required to send the secret key in your header request. 
The secret key can be defined in Openfire Admin console under Server > Server Settings > REST API.

E.g.
>**Header:** Authorization: s3cretKey

### Custom authentication filter

In addition to Basic HTTP Authentication and the shared secret key, the REST API plugin can delegate authentication
to a custom implementation, for deployments that need to integrate with an external identity provider, a
different credential store, or additional checks beyond what the built-in mechanisms provide.

This is configured in the Openfire Admin console under Server > Server Settings > REST API, by setting the
authentication type to "custom" and providing the fully qualified class name of your implementation. The class
must already be present on Openfire's classpath (e.g. provided as a JAR file in Openfire's LIB folder) before it can be
selected.

#### Requirements for a custom implementation

A class used as a custom authentication filter must:

1. Implement `javax.ws.rs.container.ContainerRequestFilter`.
2. Be annotated with `@javax.annotation.Priority(javax.ws.rs.Priorities.AUTHENTICATION)`.
3. Reject any request that does not carry valid credentials (for example by throwing a `WebApplicationException` with an
   appropriate `Response.Status`, or by calling `requestContext.abortWith(...)` with an appropriate `4xx` response)
   before returning from `filter()`.

The first two requirements are enforced by the plugin: a class that does not satisfy both will be rejected when
you attempt to save the configuration. The REST API will continue using its default authentication filter.

The third requirement cannot be verified automatically and is the implementer's responsibility. A filter that
implements the interface and carries the annotation, but returns without calling `abortWith(...)` on an
unauthenticated request, will be loaded successfully and will silently grant unauthenticated access.

### Restricting access by IP address

Access to the REST API can additionally be limited to a list of allowed IP addresses. This is configured in the
Openfire Admin console under Server > Server Settings > REST API (backed by the `plugin.restapi.allowedIPs` system
property). When the list is empty, requests from any IP address are accepted.

The plugin checks the IP address of the peer that is directly connected to Openfire. It does not itself inspect
headers like `X-Forwarded-For`, as these can be set to arbitrary values by any client. When the REST API is accessed
through a reverse proxy, configure Openfire's admin console to use the forwarded client address instead: enable
`adminConsole.forwarded.enabled`, and list the IP addresses (or ranges) of your proxies in
`adminConsole.forwarded.trusted.proxies`. Both can be set on the Admin Console Access page (Server > Server
Manager > Admin Console Access). Openfire then uses forwarded headers only on requests that come from one of those
trusted proxies.

Be aware of the following:

- When `adminConsole.forwarded.enabled` is `true` but no trusted proxies are configured, Openfire uses forwarded
  headers from _any_ peer. Any client can then bypass the IP address check by sending a forged header. The REST API
  shows a warning on its admin console page when it detects this configuration.
- Make sure that your reverse proxy _replaces_ any `Forwarded` or `X-Forwarded-For` header that it receives from the
  client, instead of appending to it. Otherwise, a value provided by the client can still end up being used as the
  client's address.
- Changes to the `adminConsole.forwarded.*` properties take effect only after the admin console has been restarted.

<!-- BEGIN GENERATED ENDPOINTS: do not edit this section by hand. It is generated from the OpenAPI annotations in the source code by the Maven build. -->

# REST Endpoints

The paths of all endpoints below are relative to the root of the Openfire admin console, for example `http://example.org:9090`. The data types that are used by these endpoints are described in [Data types](#data-types).

In addition to the responses that are documented for each endpoint, every endpoint can respond with:

- `401`: Web service authentication failed.
- `500`: Unexpected, generic error condition.

Interactive documentation of these endpoints is available in the Openfire admin console, via the link on the REST API settings page (Server > Server Settings > REST API).

# Users

Managing Openfire users.

## Get users

> **GET** /plugins/restapi/v1/users

Retrieve all users defined in Openfire (with optional filtering).

**Parameters**

| Name | Located in | Required | Description | Default value |
|------|------------|----------|-------------|---------------|
| search | query | no | Search/Filter by username. This acts like the wildcard search %String%. |  |
| propertyKey | query | no | Filter by a user property name. |  |
| propertyValue | query | no | Filter by user property value. Note: This can only be used in combination with a property name parameter. |  |

**Responses**

| Status | Description | Response body |
|--------|-------------|---------------|
| 200 | A list of Openfire users. | [UserEntities](#userentities) (XML or JSON) |

## Create user

> **POST** /plugins/restapi/v1/users

Add a new user to Openfire.

**Request body** (required): [UserEntity](#userentity) (XML or JSON) - The definition of the user to create.

<details>
<summary>Example request bodies</summary>

XML (`Content-Type: application/xml`):

```xml
<user>
    <username>john</username>
    <name>John Doe</name>
    <email>john@example.org</email>
    <password>s3cr3t</password>
    <properties>
        <property key="department" value="Sales"/>
    </properties>
</user>
```

JSON (`Content-Type: application/json`):

```json
{
  "username" : "john",
  "name" : "John Doe",
  "email" : "john@example.org",
  "password" : "s3cr3t",
  "properties" : [ {
    "key" : "department",
    "value" : "Sales"
  } ]
}
```

</details>

**Responses**

| Status | Description | Response body |
|--------|-------------|---------------|
| 201 | The user was created. |  |
| 400 | No user definition, username or password was provided. | [ErrorResponse](#errorresponse) |
| 409 | A user with this username already exists. | [ErrorResponse](#errorresponse) |

## Get user

> **GET** /plugins/restapi/v1/users/{username}

Retrieve a user that is defined in Openfire.

**Parameters**

| Name | Located in | Required | Description | Default value |
|------|------------|----------|-------------|---------------|
| username | path | yes | The username of the user to return. |  |

**Responses**

| Status | Description | Response body |
|--------|-------------|---------------|
| 200 | The Openfire user. | [UserEntity](#userentity) (XML or JSON) |
| 404 | No user with that username was found. | [ErrorResponse](#errorresponse) (XML or JSON) |

## Update user

> **PUT** /plugins/restapi/v1/users/{username}

Update an existing user in Openfire.

**Parameters**

| Name | Located in | Required | Description | Default value |
|------|------------|----------|-------------|---------------|
| username | path | yes | The username of the user to update. |  |

**Request body** (required): [UserEntity](#userentity) (XML or JSON) - The updated definition of the user.

<details>
<summary>Example request bodies</summary>

XML (`Content-Type: application/xml`):

```xml
<user>
    <username>john</username>
    <name>John Doe</name>
    <email>john@example.org</email>
    <password>s3cr3t</password>
    <properties>
        <property key="department" value="Sales"/>
    </properties>
</user>
```

JSON (`Content-Type: application/json`):

```json
{
  "username" : "john",
  "name" : "John Doe",
  "email" : "john@example.org",
  "password" : "s3cr3t",
  "properties" : [ {
    "key" : "department",
    "value" : "Sales"
  } ]
}
```

</details>

**Responses**

| Status | Description | Response body |
|--------|-------------|---------------|
| 200 | The user was updated. |  |
| 404 | No user with that username was found. | [ErrorResponse](#errorresponse) |
| 409 | The user is to be renamed, but a user with the new username already exists. | [ErrorResponse](#errorresponse) |

## Delete user

> **DELETE** /plugins/restapi/v1/users/{username}

Remove an existing user from Openfire.

**Parameters**

| Name | Located in | Required | Description | Default value |
|------|------------|----------|-------------|---------------|
| username | path | yes | The username of the user to remove. |  |

**Responses**

| Status | Description | Response body |
|--------|-------------|---------------|
| 200 | The user was removed. |  |
| 404 | No user with that username was found. | [ErrorResponse](#errorresponse) |

## Get user's groups

> **GET** /plugins/restapi/v1/users/{username}/groups

Retrieve names of all groups that a particular user is in.

**Parameters**

| Name | Located in | Required | Description | Default value |
|------|------------|----------|-------------|---------------|
| username | path | yes | The username of the user for which to return group names. |  |

**Responses**

| Status | Description | Response body |
|--------|-------------|---------------|
| 200 | The names of the groups that the user is in. | [UserGroupsEntity](#usergroupsentity) (XML or JSON) |
| 404 | No user with that username was found. | [ErrorResponse](#errorresponse) (XML or JSON) |

## Add user to groups

> **POST** /plugins/restapi/v1/users/{username}/groups

Add a particular user to a collection of groups. When a group that is provided does not exist, it will be automatically created if possible.

**Parameters**

| Name | Located in | Required | Description | Default value |
|------|------------|----------|-------------|---------------|
| username | path | yes | The username of the user that is to be added to groups. |  |

**Request body** (required): [UserGroupsEntity](#usergroupsentity) (XML or JSON) - A collection of names for groups that the user is to be added to.

<details>
<summary>Example request bodies</summary>

XML (`Content-Type: application/xml`):

```xml
<groups>
    <groupname>Sales</groupname>
</groups>
```

JSON (`Content-Type: application/json`):

```json
{
  "groupnames" : [ "Sales" ]
}
```

</details>

**Responses**

| Status | Description | Response body |
|--------|-------------|---------------|
| 201 | The user was added to all groups. |  |
| 400 | The username cannot be parsed into a JID. | [ErrorResponse](#errorresponse) |

## Delete user from groups

> **DELETE** /plugins/restapi/v1/users/{username}/groups

Removes a user from a collection of groups.

**Parameters**

| Name | Located in | Required | Description | Default value |
|------|------------|----------|-------------|---------------|
| username | path | yes | The username of the user that is to be removed from groups. |  |

**Request body** (required): [UserGroupsEntity](#usergroupsentity) (XML or JSON) - A collection of names for groups from which the user is to be removed.

<details>
<summary>Example request bodies</summary>

XML (`Content-Type: application/xml`):

```xml
<groups>
    <groupname>Sales</groupname>
</groups>
```

JSON (`Content-Type: application/json`):

```json
{
  "groupnames" : [ "Sales" ]
}
```

</details>

**Responses**

| Status | Description | Response body |
|--------|-------------|---------------|
| 200 | The user was taken out of the groups. |  |
| 404 | One or more groups could not be found. | [ErrorResponse](#errorresponse) |

## Add user to group

> **POST** /plugins/restapi/v1/users/{username}/groups/{groupName}

Add a particular user to a particular group. When the group does not exist, it will be automatically created if possible.

**Parameters**

| Name | Located in | Required | Description | Default value |
|------|------------|----------|-------------|---------------|
| username | path | yes | The username of the user that is to be added to a group. |  |
| groupName | path | yes | The name of the group that the user is to be added to. |  |

**Responses**

| Status | Description | Response body |
|--------|-------------|---------------|
| 201 | The user was added to the group. |  |
| 400 | The username cannot be parsed into a JID. | [ErrorResponse](#errorresponse) |

## Delete user from group

> **DELETE** /plugins/restapi/v1/users/{username}/groups/{groupName}

Removes a user from a group.

**Parameters**

| Name | Located in | Required | Description | Default value |
|------|------------|----------|-------------|---------------|
| username | path | yes | The username of the user that is to be removed from a group. |  |
| groupName | path | yes | The name of the group that the user is to be removed from. |  |

**Responses**

| Status | Description | Response body |
|--------|-------------|---------------|
| 200 | The user was taken out of the group. |  |
| 404 | The group could not be found. | [ErrorResponse](#errorresponse) |

## Retrieve user roster

> **GET** /plugins/restapi/v1/users/{username}/roster

Get a list of all roster entries (buddies / contact list) of a particular user.

**Parameters**

| Name | Located in | Required | Description | Default value |
|------|------------|----------|-------------|---------------|
| username | path | yes | The username of the user for which to retrieve the roster entries. |  |

**Responses**

| Status | Description | Response body |
|--------|-------------|---------------|
| 200 | All roster entries. | [RosterEntities](#rosterentities) (XML or JSON) |
| 404 | No user with this username exists. | [ErrorResponse](#errorresponse) (XML or JSON) |

## Create roster entry

> **POST** /plugins/restapi/v1/users/{username}/roster

Add a roster entry to the roster (buddies / contact list) of a particular user.

**Parameters**

| Name | Located in | Required | Description | Default value |
|------|------------|----------|-------------|---------------|
| username | path | yes | The username of the user for which to add a roster entry. |  |

**Request body** (required): [RosterItemEntity](#rosteritementity) (XML or JSON) - The definition of the roster entry that is to be added.

<details>
<summary>Example request bodies</summary>

XML (`Content-Type: application/xml`):

```xml
<rosterItem>
    <jid>jane@example.org</jid>
    <nickname>Jane</nickname>
    <subscriptionType>3</subscriptionType>
    <groups>
        <group>Friends</group>
    </groups>
</rosterItem>
```

JSON (`Content-Type: application/json`):

```json
{
  "jid" : "jane@example.org",
  "nickname" : "Jane",
  "subscriptionType" : 3,
  "groups" : [ "Friends" ]
}
```

</details>

**Responses**

| Status | Description | Response body |
|--------|-------------|---------------|
| 201 | The entry was added to the roster. |  |
| 400 | A roster entry cannot be added to a 'shared group' (try removing group names from the roster entry and try again). | [ErrorResponse](#errorresponse) |
| 404 | No user with this username exists. | [ErrorResponse](#errorresponse) |
| 409 | A roster entry already exists for the provided contact JID. | [ErrorResponse](#errorresponse) |

## Update roster entry

> **PUT** /plugins/restapi/v1/users/{username}/roster/{rosterJid}

Changes a roster entry on the roster (buddies / contact list) of a particular user.

**Parameters**

| Name | Located in | Required | Description | Default value |
|------|------------|----------|-------------|---------------|
| username | path | yes | The username of the user for which to update a roster entry. |  |
| rosterJid | path | yes | The JID of the entry/contact to update. |  |

**Request body** (required): [RosterItemEntity](#rosteritementity) (XML or JSON) - The updated definition of the roster entry.

<details>
<summary>Example request bodies</summary>

XML (`Content-Type: application/xml`):

```xml
<rosterItem>
    <jid>jane@example.org</jid>
    <nickname>Jane</nickname>
    <subscriptionType>3</subscriptionType>
    <groups>
        <group>Friends</group>
    </groups>
</rosterItem>
```

JSON (`Content-Type: application/json`):

```json
{
  "jid" : "jane@example.org",
  "nickname" : "Jane",
  "subscriptionType" : 3,
  "groups" : [ "Friends" ]
}
```

</details>

**Responses**

| Status | Description | Response body |
|--------|-------------|---------------|
| 200 | The roster entry was updated. |  |
| 400 | A roster entry cannot be added with a 'shared group'. | [ErrorResponse](#errorresponse) |
| 404 | No user with this username exists. | [ErrorResponse](#errorresponse) |
| 409 | A roster entry already exists for the provided contact JID. | [ErrorResponse](#errorresponse) |

## Remove roster entry

> **DELETE** /plugins/restapi/v1/users/{username}/roster/{rosterJid}

Removes one of the roster entries (contacts) of a particular user.

**Parameters**

| Name | Located in | Required | Description | Default value |
|------|------------|----------|-------------|---------------|
| username | path | yes | The username of the user for which to remove a roster entry. |  |
| rosterJid | path | yes | The JID of the entry/contact to remove. |  |

**Responses**

| Status | Description | Response body |
|--------|-------------|---------------|
| 200 | The entry was removed from the roster. |  |
| 400 | A roster entry cannot be removed from a 'shared group'. | [ErrorResponse](#errorresponse) |
| 404 | No user with this username exists, or its roster did not contain this entry. | [ErrorResponse](#errorresponse) |

## Get user's vCard

> **GET** /plugins/restapi/v1/users/{username}/vcard

Retrieves the vCard for a particular user.

**Parameters**

| Name | Located in | Required | Description | Default value |
|------|------------|----------|-------------|---------------|
| username | path | yes | The username of the user for which to return the vCard. |  |

**Responses**

| Status | Description | Response body |
|--------|-------------|---------------|
| 200 | The vCard of the user. |  |
| 204 | No vCard found. |  |

## Update vCard

> **PUT** /plugins/restapi/v1/users/{username}/vcard

Creates or changes a vCard of a particular user.

**Parameters**

| Name | Located in | Required | Description | Default value |
|------|------------|----------|-------------|---------------|
| username | path | yes | The username of the user for which to update the vCard. |  |

**Request body** (required): string (XML) - The updated definition of the vCard, in the vcard-temp format of XEP-0054.

<details>
<summary>Example request body</summary>

`Content-Type: application/xml`:

```xml
<vCard xmlns="vcard-temp">
    <FN>Janice Francis Doe</FN>
    <N>
        <FAMILY>Doe</FAMILY>
        <GIVEN>Janice</GIVEN>
        <MIDDLE>Francis</MIDDLE>
    </N>
    <NICKNAME>Jane</NICKNAME>
    <EMAIL>
        <INTERNET/>
        <PREF/>
        <USERID>j.doe@example.org</USERID>
    </EMAIL>
</vCard>
```

</details>

**Responses**

| Status | Description | Response body |
|--------|-------------|---------------|
| 200 | The vCard was updated/created. |  |
| 400 | Provided data could not be parsed. | [ErrorResponse](#errorresponse) |
| 409 | Cannot change vCard, as Openfire is configured to have read-only vCards. | [ErrorResponse](#errorresponse) |

## Delete vCard

> **DELETE** /plugins/restapi/v1/users/{username}/vcard

Removes a vCard of a particular user.

**Parameters**

| Name | Located in | Required | Description | Default value |
|------|------------|----------|-------------|---------------|
| username | path | yes | The username of the user for which to delete the vCard. |  |

**Responses**

| Status | Description | Response body |
|--------|-------------|---------------|
| 200 | The vCard was deleted. |  |
| 409 | Cannot delete vCard, as Openfire is configured to have read-only vCards. | [ErrorResponse](#errorresponse) |

## Lock user out

> **POST** /plugins/restapi/v1/lockouts/{username}

Lockout / ban the user from the chat server. The user will be kicked if the user is online.

**Parameters**

| Name | Located in | Required | Description | Default value |
|------|------------|----------|-------------|---------------|
| username | path | yes | The username of the user that is to be locked out. |  |

**Responses**

| Status | Description | Response body |
|--------|-------------|---------------|
| 201 | The user was locked out. |  |
| 404 | No user with this username exists. | [ErrorResponse](#errorresponse) |

## Unlock user

> **DELETE** /plugins/restapi/v1/lockouts/{username}

Removes a previously applied lockout / ban of a user.

**Parameters**

| Name | Located in | Required | Description | Default value |
|------|------------|----------|-------------|---------------|
| username | path | yes | The username of the user for which the lockout is to be undone. |  |

**Responses**

| Status | Description | Response body |
|--------|-------------|---------------|
| 200 | User is unlocked. |  |
| 404 | No user with this username exists. | [ErrorResponse](#errorresponse) |

# User Group

Managing Openfire user groups.

## Get groups

> **GET** /plugins/restapi/v1/groups

Get a list of all user groups.

**Responses**

| Status | Description | Response body |
|--------|-------------|---------------|
| 200 | All groups. | [GroupEntities](#groupentities) (XML or JSON) |

## Create group

> **POST** /plugins/restapi/v1/groups

Create a new user group.

**Request body** (required): [GroupEntity](#groupentity) (XML or JSON) - The group that needs to be created.

<details>
<summary>Example request bodies</summary>

XML (`Content-Type: application/xml`):

```xml
<group>
    <name>UserGroup1</name>
    <description>My group of users</description>
    <admins>
        <admin>jane.smith</admin>
    </admins>
    <members>
        <member>john.jones</member>
    </members>
    <shared>false</shared>
</group>
```

JSON (`Content-Type: application/json`):

```json
{
  "name" : "UserGroup1",
  "description" : "My group of users",
  "admins" : [ "jane.smith" ],
  "members" : [ "john.jones" ],
  "shared" : false
}
```

</details>

**Responses**

| Status | Description | Response body |
|--------|-------------|---------------|
| 201 | Group created. |  |
| 400 | Group or group name missing, or invalid syntax for a property. | [ErrorResponse](#errorresponse) |
| 409 | Group already exists. | [ErrorResponse](#errorresponse) |

## Get group

> **GET** /plugins/restapi/v1/groups/{groupName}

Get one specific user group by name.

**Parameters**

| Name | Located in | Required | Description | Default value |
|------|------------|----------|-------------|---------------|
| groupName | path | yes | The name of the group that needs to be fetched. Example: `Colleagues` |  |

**Responses**

| Status | Description | Response body |
|--------|-------------|---------------|
| 200 | The group. | [GroupEntity](#groupentity) (XML or JSON) |
| 404 | Group with this name not found. | [ErrorResponse](#errorresponse) (XML or JSON) |

## Update group

> **PUT** /plugins/restapi/v1/groups/{groupName}

Updates / overwrites an existing user group. Note that the name of the group cannot be changed.

**Parameters**

| Name | Located in | Required | Description | Default value |
|------|------------|----------|-------------|---------------|
| groupName | path | yes | The name of the group that needs to be updated. Example: `Colleagues` |  |

**Request body** (required): [GroupEntity](#groupentity) (XML or JSON) - The new group definition that needs to overwrite the old definition.

<details>
<summary>Example request bodies</summary>

XML (`Content-Type: application/xml`):

```xml
<group>
    <name>UserGroup1</name>
    <description>My group of users</description>
    <admins>
        <admin>jane.smith</admin>
    </admins>
    <members>
        <member>john.jones</member>
    </members>
    <shared>false</shared>
</group>
```

JSON (`Content-Type: application/json`):

```json
{
  "name" : "UserGroup1",
  "description" : "My group of users",
  "admins" : [ "jane.smith" ],
  "members" : [ "john.jones" ],
  "shared" : false
}
```

</details>

**Responses**

| Status | Description | Response body |
|--------|-------------|---------------|
| 200 | Group updated. |  |
| 400 | Group or group name missing, or name does not match existing group, or invalid syntax for a property. | [ErrorResponse](#errorresponse) |
| 404 | Group with this name not found. | [ErrorResponse](#errorresponse) |

## Delete group

> **DELETE** /plugins/restapi/v1/groups/{groupName}

Removes an existing user group.

**Parameters**

| Name | Located in | Required | Description | Default value |
|------|------------|----------|-------------|---------------|
| groupName | path | yes | The name of the group that needs to be removed. Example: `Colleagues` |  |

**Responses**

| Status | Description | Response body |
|--------|-------------|---------------|
| 200 | Group deleted. |  |
| 404 | Group with this name not found. | [ErrorResponse](#errorresponse) |

# Chat service

Managing multi-user chat services.

## Get chat services

> **GET** /plugins/restapi/v1/chatservices

Get a list of all multi-user chat services.

**Responses**

| Status | Description | Response body |
|--------|-------------|---------------|
| 200 | All chat services. | [MUCServiceEntities](#mucserviceentities) (XML or JSON) |

## Create chat service

> **POST** /plugins/restapi/v1/chatservices

Create a new multi-user chat service.

**Request body** (required): [MUCServiceEntity](#mucserviceentity) (XML or JSON) - The MUC service that needs to be created.

<details>
<summary>Example request bodies</summary>

XML (`Content-Type: application/xml`):

```xml
<chatService>
    <serviceName>conference</serviceName>
    <description>A public service</description>
    <hidden>false</hidden>
</chatService>
```

JSON (`Content-Type: application/json`):

```json
{
  "serviceName" : "conference",
  "description" : "A public service",
  "hidden" : false
}
```

</details>

**Responses**

| Status | Description | Response body |
|--------|-------------|---------------|
| 201 | Service created. |  |
| 403 | Service creation is not permitted. | [ErrorResponse](#errorresponse) |
| 409 | Service already exists, or another conflict occurred while creating the service. | [ErrorResponse](#errorresponse) |

# Chat room

Managing multi-user chat rooms.

## Get chat rooms

> **GET** /plugins/restapi/v1/chatrooms

Get a list of all multi-user chat rooms of a particular chat room service.

**Parameters**

| Name | Located in | Required | Description | Default value |
|------|------------|----------|-------------|---------------|
| servicename | query | no | The name of the MUC service for which to return all chat rooms. Example: `conference` | `conference` |
| type | query | no | Room type-based filter: 'all' or 'public'. | `public` |
| search | query | no | Search/Filter by room name.<br>This acts like the wildcard search %String% Example: `conference` |  |
| expandGroups | query | no | For all groups defined in owners, admins, members and outcasts, list individual members instead of the group name. | `false` |

**Responses**

| Status | Description | Response body |
|--------|-------------|---------------|
| 200 | All chat rooms. | [MUCRoomEntities](#mucroomentities) (XML or JSON) |
| 404 | MUC service does not exist or is not accessible. | [ErrorResponse](#errorresponse) (XML or JSON) |

## Create chat room

> **POST** /plugins/restapi/v1/chatrooms

Create a new multi-user chat room.

**Parameters**

| Name | Located in | Required | Description | Default value |
|------|------------|----------|-------------|---------------|
| servicename | query | no | The name of the MUC service in which to create a chat room. Example: `conference` | `conference` |
| sendInvitations | query | no | Whether to send invitations to newly affiliated users. Example: `true` | `false` |

**Request body** (required): [MUCRoomEntity](#mucroomentity) (XML or JSON) - The MUC room that needs to be created.

<details>
<summary>Example request bodies</summary>

XML (`Content-Type: application/xml`):

```xml
<chatRoom>
    <roomName>global</roomName>
    <naturalName>Global Chat</naturalName>
    <description>A room for everyone</description>
    <password>s3cr3t</password>
    <subject>Welcome!</subject>
    <creationDate>2026-01-31T12:34:56.789Z</creationDate>
    <modificationDate>2026-01-31T12:34:56.789Z</modificationDate>
    <maxUsers>30</maxUsers>
    <persistent>true</persistent>
    <publicRoom>true</publicRoom>
    <registrationEnabled>false</registrationEnabled>
    <canAnyoneDiscoverJID>false</canAnyoneDiscoverJID>
    <canOccupantsChangeSubject>false</canOccupantsChangeSubject>
    <canOccupantsInvite>false</canOccupantsInvite>
    <canChangeNickname>true</canChangeNickname>
    <logEnabled>true</logEnabled>
    <loginRestrictedToNickname>false</loginRestrictedToNickname>
    <membersOnly>false</membersOnly>
    <moderated>false</moderated>
    <broadcastPresenceRoles>
        <broadcastPresenceRole>moderator</broadcastPresenceRole>
    </broadcastPresenceRoles>
    <owners>
        <owner>admin@example.org</owner>
    </owners>
    <admins>
        <admin>jane@example.org</admin>
    </admins>
    <members>
        <member>john@example.org</member>
    </members>
    <outcasts>
        <outcast>spammer@example.org</outcast>
    </outcasts>
    <ownerGroups>
        <ownerGroup>Management</ownerGroup>
    </ownerGroups>
    <adminGroups>
        <adminGroup>Moderators</adminGroup>
    </adminGroups>
    <memberGroups>
        <memberGroup>Sales</memberGroup>
    </memberGroups>
    <outcastGroups>
        <outcastGroup>Banned</outcastGroup>
    </outcastGroups>
    <allowPM>anyone</allowPM>
</chatRoom>
```

JSON (`Content-Type: application/json`):

```json
{
  "roomName" : "global",
  "naturalName" : "Global Chat",
  "description" : "A room for everyone",
  "password" : "s3cr3t",
  "subject" : "Welcome!",
  "creationDate" : 1769862896789,
  "modificationDate" : 1769862896789,
  "maxUsers" : 30,
  "persistent" : true,
  "publicRoom" : true,
  "registrationEnabled" : false,
  "canAnyoneDiscoverJID" : false,
  "canOccupantsChangeSubject" : false,
  "canOccupantsInvite" : false,
  "canChangeNickname" : true,
  "logEnabled" : true,
  "loginRestrictedToNickname" : false,
  "membersOnly" : false,
  "moderated" : false,
  "broadcastPresenceRoles" : [ "moderator" ],
  "owners" : [ "admin@example.org" ],
  "admins" : [ "jane@example.org" ],
  "members" : [ "john@example.org" ],
  "outcasts" : [ "spammer@example.org" ],
  "ownerGroups" : [ "Management" ],
  "adminGroups" : [ "Moderators" ],
  "memberGroups" : [ "Sales" ],
  "outcastGroups" : [ "Banned" ],
  "allowPM" : "anyone"
}
```

</details>

**Responses**

| Status | Description | Response body |
|--------|-------------|---------------|
| 201 | Room created. |  |
| 403 | Room creation is not permitted. | [ErrorResponse](#errorresponse) |
| 404 | MUC service does not exist or is not accessible. | [ErrorResponse](#errorresponse) |
| 409 | Room already exists, or another conflict occurred while creating the room. | [ErrorResponse](#errorresponse) |

## Create multiple chat rooms

> **POST** /plugins/restapi/v1/chatrooms/bulk

Create a number of new multi-user chat rooms.

**Parameters**

| Name | Located in | Required | Description | Default value |
|------|------------|----------|-------------|---------------|
| servicename | query | no | The name of the MUC service in which to create the chat rooms. Example: `conference` | `conference` |
| sendInvitations | query | no | Whether to send invitations to newly affiliated users. Example: `true` | `false` |

**Request body** (required): [MUCRoomEntities](#mucroomentities) (XML or JSON) - The MUC rooms that need to be created.

<details>
<summary>Example request bodies</summary>

XML (`Content-Type: application/xml`):

```xml
<chatRooms>
    <chatRoom>
        <roomName>global</roomName>
        <naturalName>Global Chat</naturalName>
        <description>A room for everyone</description>
        <password>s3cr3t</password>
        <subject>Welcome!</subject>
        <creationDate>2026-01-31T12:34:56.789Z</creationDate>
        <modificationDate>2026-01-31T12:34:56.789Z</modificationDate>
        <maxUsers>30</maxUsers>
        <persistent>true</persistent>
        <publicRoom>true</publicRoom>
        <registrationEnabled>false</registrationEnabled>
        <canAnyoneDiscoverJID>false</canAnyoneDiscoverJID>
        <canOccupantsChangeSubject>false</canOccupantsChangeSubject>
        <canOccupantsInvite>false</canOccupantsInvite>
        <canChangeNickname>true</canChangeNickname>
        <logEnabled>true</logEnabled>
        <loginRestrictedToNickname>false</loginRestrictedToNickname>
        <membersOnly>false</membersOnly>
        <moderated>false</moderated>
        <broadcastPresenceRoles>
            <broadcastPresenceRole>moderator</broadcastPresenceRole>
        </broadcastPresenceRoles>
        <owners>
            <owner>admin@example.org</owner>
        </owners>
        <admins>
            <admin>jane@example.org</admin>
        </admins>
        <members>
            <member>john@example.org</member>
        </members>
        <outcasts>
            <outcast>spammer@example.org</outcast>
        </outcasts>
        <ownerGroups>
            <ownerGroup>Management</ownerGroup>
        </ownerGroups>
        <adminGroups>
            <adminGroup>Moderators</adminGroup>
        </adminGroups>
        <memberGroups>
            <memberGroup>Sales</memberGroup>
        </memberGroups>
        <outcastGroups>
            <outcastGroup>Banned</outcastGroup>
        </outcastGroups>
        <allowPM>anyone</allowPM>
    </chatRoom>
</chatRooms>
```

JSON (`Content-Type: application/json`):

```json
{
  "chatRooms" : [ {
    "roomName" : "global",
    "naturalName" : "Global Chat",
    "description" : "A room for everyone",
    "password" : "s3cr3t",
    "subject" : "Welcome!",
    "creationDate" : 1769862896789,
    "modificationDate" : 1769862896789,
    "maxUsers" : 30,
    "persistent" : true,
    "publicRoom" : true,
    "registrationEnabled" : false,
    "canAnyoneDiscoverJID" : false,
    "canOccupantsChangeSubject" : false,
    "canOccupantsInvite" : false,
    "canChangeNickname" : true,
    "logEnabled" : true,
    "loginRestrictedToNickname" : false,
    "membersOnly" : false,
    "moderated" : false,
    "broadcastPresenceRoles" : [ "moderator" ],
    "owners" : [ "admin@example.org" ],
    "admins" : [ "jane@example.org" ],
    "members" : [ "john@example.org" ],
    "outcasts" : [ "spammer@example.org" ],
    "ownerGroups" : [ "Management" ],
    "adminGroups" : [ "Moderators" ],
    "memberGroups" : [ "Sales" ],
    "outcastGroups" : [ "Banned" ],
    "allowPM" : "anyone"
  } ]
}
```

</details>

**Responses**

| Status | Description | Response body |
|--------|-------------|---------------|
| 200 | Request has been processed. Results are reported in the response. | [RoomCreationResultEntities](#roomcreationresultentities) (XML or JSON) |
| 404 | MUC service does not exist or is not accessible. | [ErrorResponse](#errorresponse) (XML or JSON) |

## Get chat room

> **GET** /plugins/restapi/v1/chatrooms/{roomName}

Get information of a specific multi-user chat room.

**Parameters**

| Name | Located in | Required | Description | Default value |
|------|------------|----------|-------------|---------------|
| roomName | path | yes | The name of the MUC room to return. Example: `lobby` |  |
| servicename | query | no | The name of the MUC service for which to return a chat room. Example: `conference` | `conference` |
| expandGroups | query | no | For all groups defined in owners, admins, members and outcasts, list individual members instead of the group name. | `false` |

**Responses**

| Status | Description | Response body |
|--------|-------------|---------------|
| 200 | The chat room. | [MUCRoomEntity](#mucroomentity) (XML or JSON) |
| 404 | The chat room (or its service) can not be found or is not accessible. | [ErrorResponse](#errorresponse) (XML or JSON) |

## Update chat room

> **PUT** /plugins/restapi/v1/chatrooms/{roomName}

Updates an existing multi-user chat room.

**Parameters**

| Name | Located in | Required | Description | Default value |
|------|------------|----------|-------------|---------------|
| roomName | path | yes | The name of the chat room that needs to be updated. Example: `lobby` |  |
| servicename | query | no | The name of the MUC service in which to update a chat room. Example: `conference` | `conference` |
| sendInvitations | query | no | Whether to send invitations to newly affiliated users. Example: `true` | `false` |

**Request body** (required): [MUCRoomEntity](#mucroomentity) (XML or JSON) - The new MUC room definition that needs to overwrite the old definition.

<details>
<summary>Example request bodies</summary>

XML (`Content-Type: application/xml`):

```xml
<chatRoom>
    <roomName>global</roomName>
    <naturalName>Global Chat</naturalName>
    <description>A room for everyone</description>
    <password>s3cr3t</password>
    <subject>Welcome!</subject>
    <creationDate>2026-01-31T12:34:56.789Z</creationDate>
    <modificationDate>2026-01-31T12:34:56.789Z</modificationDate>
    <maxUsers>30</maxUsers>
    <persistent>true</persistent>
    <publicRoom>true</publicRoom>
    <registrationEnabled>false</registrationEnabled>
    <canAnyoneDiscoverJID>false</canAnyoneDiscoverJID>
    <canOccupantsChangeSubject>false</canOccupantsChangeSubject>
    <canOccupantsInvite>false</canOccupantsInvite>
    <canChangeNickname>true</canChangeNickname>
    <logEnabled>true</logEnabled>
    <loginRestrictedToNickname>false</loginRestrictedToNickname>
    <membersOnly>false</membersOnly>
    <moderated>false</moderated>
    <broadcastPresenceRoles>
        <broadcastPresenceRole>moderator</broadcastPresenceRole>
    </broadcastPresenceRoles>
    <owners>
        <owner>admin@example.org</owner>
    </owners>
    <admins>
        <admin>jane@example.org</admin>
    </admins>
    <members>
        <member>john@example.org</member>
    </members>
    <outcasts>
        <outcast>spammer@example.org</outcast>
    </outcasts>
    <ownerGroups>
        <ownerGroup>Management</ownerGroup>
    </ownerGroups>
    <adminGroups>
        <adminGroup>Moderators</adminGroup>
    </adminGroups>
    <memberGroups>
        <memberGroup>Sales</memberGroup>
    </memberGroups>
    <outcastGroups>
        <outcastGroup>Banned</outcastGroup>
    </outcastGroups>
    <allowPM>anyone</allowPM>
</chatRoom>
```

JSON (`Content-Type: application/json`):

```json
{
  "roomName" : "global",
  "naturalName" : "Global Chat",
  "description" : "A room for everyone",
  "password" : "s3cr3t",
  "subject" : "Welcome!",
  "creationDate" : 1769862896789,
  "modificationDate" : 1769862896789,
  "maxUsers" : 30,
  "persistent" : true,
  "publicRoom" : true,
  "registrationEnabled" : false,
  "canAnyoneDiscoverJID" : false,
  "canOccupantsChangeSubject" : false,
  "canOccupantsInvite" : false,
  "canChangeNickname" : true,
  "logEnabled" : true,
  "loginRestrictedToNickname" : false,
  "membersOnly" : false,
  "moderated" : false,
  "broadcastPresenceRoles" : [ "moderator" ],
  "owners" : [ "admin@example.org" ],
  "admins" : [ "jane@example.org" ],
  "members" : [ "john@example.org" ],
  "outcasts" : [ "spammer@example.org" ],
  "ownerGroups" : [ "Management" ],
  "adminGroups" : [ "Moderators" ],
  "memberGroups" : [ "Sales" ],
  "outcastGroups" : [ "Banned" ],
  "allowPM" : "anyone"
}
```

</details>

**Responses**

| Status | Description | Response body |
|--------|-------------|---------------|
| 200 | Room updated. |  |
| 403 | Room update/create is not permitted. | [ErrorResponse](#errorresponse) |
| 404 | MUC service does not exist or is not accessible. | [ErrorResponse](#errorresponse) |
| 409 | This update causes a conflict, possibly with another existing room. | [ErrorResponse](#errorresponse) |

## Delete chat room

> **DELETE** /plugins/restapi/v1/chatrooms/{roomName}

Removes an existing multi-user chat room.

**Parameters**

| Name | Located in | Required | Description | Default value |
|------|------------|----------|-------------|---------------|
| roomName | path | yes | The name of the MUC room to delete. Example: `lobby` |  |
| servicename | query | no | The name of the MUC service from which to delete a chat room. Example: `conference` | `conference` |

**Responses**

| Status | Description | Response body |
|--------|-------------|---------------|
| 200 | Room deleted. |  |
| 404 | The chat room (or its service) can not be found or is not accessible. | [ErrorResponse](#errorresponse) |

## Get room history

> **GET** /plugins/restapi/v1/chatrooms/{roomName}/chathistory

Get messages that have been exchanged in a specific multi-user chat room.

**Parameters**

| Name | Located in | Required | Description | Default value |
|------|------------|----------|-------------|---------------|
| roomName | path | yes | The name of the chat room for which to return message history. Example: `lobby` |  |
| servicename | query | no | The name of the chat room's MUC service. Example: `conference` | `conference` |

**Responses**

| Status | Description | Response body |
|--------|-------------|---------------|
| 200 | The chat room message history. | [MUCRoomMessageEntities](#mucroommessageentities) (XML or JSON) |
| 404 | The chat room (or its service) can not be found or is not accessible. | [ErrorResponse](#errorresponse) (XML or JSON) |

## Invite a collection of users and/or groups

> **POST** /plugins/restapi/v1/chatrooms/{roomName}/invite

Invites a collection of users and/or groups to join a specific multi-user chat room. Each entity can be identified by the JID of a user or group, or by the name of a local user or group. When a group is invited, all of its members are invited.

**Parameters**

| Name | Located in | Required | Description | Default value |
|------|------------|----------|-------------|---------------|
| roomName | path | yes | The name of the chat room to which to invite users and/or groups. Example: `lobby` |  |
| servicename | query | no | The name of the chat room's MUC service. Example: `conference` | `conference` |

**Request body** (required): [MUCInvitationsEntity](#mucinvitationsentity) (XML or JSON) - The invitation message to send and whom to send it to.

<details>
<summary>Example request bodies</summary>

XML (`Content-Type: application/xml`):

```xml
<mucInvitations>
    <reason>Come join this cool room please!</reason>
    <jidsToInvite>
        <jid>john@example.org</jid>
    </jidsToInvite>
</mucInvitations>
```

JSON (`Content-Type: application/json`):

```json
{
  "reason" : "Come join this cool room please!",
  "jidsToInvite" : [ "john@example.org" ]
}
```

</details>

**Responses**

| Status | Description | Response body |
|--------|-------------|---------------|
| 200 | Invitation sent. |  |
| 403 | Not allowed to invite a user or group to this room. | [ErrorResponse](#errorresponse) |
| 404 | The chat room (or its service) can not be found or is not accessible. | [ErrorResponse](#errorresponse) |

## Invite user or group

> **POST** /plugins/restapi/v1/chatrooms/{roomName}/invite/{jid}

Invites a user or group to join a specific multi-user chat room.

**Parameters**

| Name | Located in | Required | Description | Default value |
|------|------------|----------|-------------|---------------|
| roomName | path | yes | The name of the chat room to which to invite a user or group. Example: `lobby` |  |
| jid | path | yes | The entity to invite into the room: the JID of a user or group, or the name of a local user or group. When a group is invited, all of its members are invited. Example: `john@example.org` |  |
| servicename | query | no | The name of the chat room's MUC service. Example: `conference` | `conference` |

**Request body** (required): [MUCInvitationEntity](#mucinvitationentity) (XML or JSON) - The invitation message to send and whom to send it to.

<details>
<summary>Example request bodies</summary>

XML (`Content-Type: application/xml`):

```xml
<mucInvitation>
    <reason>Come join this cool room please!</reason>
</mucInvitation>
```

JSON (`Content-Type: application/json`):

```json
{
  "reason" : "Come join this cool room please!"
}
```

</details>

**Responses**

| Status | Description | Response body |
|--------|-------------|---------------|
| 200 | Invitation sent. |  |
| 403 | Not allowed to invite a user to this room. | [ErrorResponse](#errorresponse) |
| 404 | The chat room (or its service) can not be found or is not accessible. | [ErrorResponse](#errorresponse) |

## Get room occupants

> **GET** /plugins/restapi/v1/chatrooms/{roomName}/occupants

Get all occupants of a specific multi-user chat room.

**Parameters**

| Name | Located in | Required | Description | Default value |
|------|------------|----------|-------------|---------------|
| roomName | path | yes | The name of the chat room for which to return occupants. Example: `lobby` |  |
| servicename | query | no | The name of the chat room's MUC service. Example: `conference` | `conference` |

**Responses**

| Status | Description | Response body |
|--------|-------------|---------------|
| 200 | The chat room occupants. | [OccupantEntities](#occupantentities) (XML or JSON) |
| 404 | The chat room (or its service) can not be found or is not accessible. | [ErrorResponse](#errorresponse) (XML or JSON) |

## Get room participants

> **GET** /plugins/restapi/v1/chatrooms/{roomName}/participants

Get all participants of a specific multi-user chat room.

**Parameters**

| Name | Located in | Required | Description | Default value |
|------|------------|----------|-------------|---------------|
| roomName | path | yes | The name of the chat room for which to return participants. Example: `lobby` |  |
| servicename | query | no | The name of the chat room's MUC service. Example: `conference` | `conference` |

**Responses**

| Status | Description | Response body |
|--------|-------------|---------------|
| 200 | The chat room participants. | [ParticipantEntities](#participantentities) (XML or JSON) |
| 404 | The chat room (or its service) can not be found or is not accessible. | [ErrorResponse](#errorresponse) (XML or JSON) |

## Get room affiliations

> **GET** /plugins/restapi/v1/chatrooms/{roomName}/{affiliation}

Retrieves a list of JIDs for all users that have a particular affiliation with a multi-user chat room.

**Parameters**

| Name | Located in | Required | Description | Default value |
|------|------------|----------|-------------|---------------|
| servicename | query | no | The name of the MUC service that the MUC room is part of. Example: `conference` | `conference` |
| roomName | path | yes | The name of the MUC room for which to return affiliations. Example: `lobby` |  |
| affiliation | path | yes | The type of affiliation. One of: 'admins', 'members', 'outcasts', 'owners'. Example: `members` |  |

**Responses**

| Status | Description | Response body |
|--------|-------------|---------------|
| 200 | Affiliated user list retrieved. | unspecified (XML or JSON) |
| 400 | Provided 'affiliations' value is invalid. | [ErrorResponse](#errorresponse) (XML or JSON) |
| 404 | The chat room (or its service) can not be found or is not accessible. | [ErrorResponse](#errorresponse) (XML or JSON) |

## Add room affiliations

> **POST** /plugins/restapi/v1/chatrooms/{roomName}/{affiliation}

Affiliates multiple users to a particular multi-user chat room (without removing existing affiliated users of that type). Note that a user can only have one type of affiliation with a room. By affiliating a user to a room, any other pre-existing affiliation for that user is removed.

**Parameters**

| Name | Located in | Required | Description | Default value |
|------|------------|----------|-------------|---------------|
| servicename | query | no | The name of the MUC service that the MUC room is part of. Example: `conference` | `conference` |
| roomName | path | yes | The name of the MUC room to which users are to be affiliated. Example: `lobby` |  |
| affiliation | path | yes | The type of affiliation. One of: 'admins', 'members', 'outcasts', 'owners'. Example: `members` |  |
| sendInvitations | query | no | Whether to send invitations to newly affiliated users. Example: `true` | `false` |

**Request body** (required): [AffiliatedEntities](#affiliatedentities) (XML or JSON) - The list of users to affiliate to the room.

**Responses**

| Status | Description | Response body |
|--------|-------------|---------------|
| 201 | Users have been affiliated to the room. |  |
| 400 | Provided values cannot be parsed as JIDs, or provided 'affiliations' value is invalid. | [ErrorResponse](#errorresponse) |
| 403 | Not allowed to perform this affiliation change. | [ErrorResponse](#errorresponse) |
| 404 | The chat room (or its service) can not be found or is not accessible. | [ErrorResponse](#errorresponse) |

## Replace room affiliations

> **PUT** /plugins/restapi/v1/chatrooms/{roomName}/{affiliation}

Replaces the list of users in a multi-user chat room with a specific affiliation with a new list of users. Note that a user can only have one type of affiliation with a room. By affiliating a user to a room, any other pre-existing affiliation for that user is removed.

**Parameters**

| Name | Located in | Required | Description | Default value |
|------|------------|----------|-------------|---------------|
| servicename | query | no | The name of the MUC service that the MUC room is part of. Example: `conference` | `conference` |
| roomName | path | yes | The name of the MUC room of which affiliations are to be replaced. Example: `lobby` |  |
| affiliation | path | yes | The type of affiliation. One of: 'admins', 'members', 'outcasts', 'owners'. Example: `members` |  |
| sendInvitations | query | no | Whether to send invitations to newly affiliated users. Example: `true` | `false` |

**Request body** (required): [AffiliatedEntities](#affiliatedentities) (XML or JSON) - The new list of users with this particular affiliation.

**Responses**

| Status | Description | Response body |
|--------|-------------|---------------|
| 201 | Affiliations of the room have been replaced. |  |
| 400 | Provided values cannot be parsed as JIDs, or provided 'affiliations' value is invalid. | [ErrorResponse](#errorresponse) |
| 403 | Not allowed to perform this affiliation change. | [ErrorResponse](#errorresponse) |
| 404 | The chat room (or its service) can not be found or is not accessible. | [ErrorResponse](#errorresponse) |

## Add group room affiliations

> **POST** /plugins/restapi/v1/chatrooms/{roomName}/{affiliation}/group/{groupname}

Affiliate all members of an Openfire user group to a multi-user chat room. Note that a user can only have one type of affiliation with a room. By affiliating a user to a room, any other pre-existing affiliation for that user is removed.

**Parameters**

| Name | Located in | Required | Description | Default value |
|------|------------|----------|-------------|---------------|
| servicename | query | no | The name of the MUC service that the MUC room is part of. Example: `conference` | `conference` |
| groupname | path | yes | The name of the user group from which all members will be affiliated to the room. Example: `Operators` |  |
| affiliation | path | yes | The type of affiliation. One of: 'admins', 'members', 'outcasts', 'owners'. Example: `members` |  |
| roomName | path | yes | The name of the MUC room to which affiliations are to be added. Example: `lobby` |  |
| sendInvitations | query | no | Whether to send invitations to newly affiliated users. Example: `true` | `false` |

**Responses**

| Status | Description | Response body |
|--------|-------------|---------------|
| 201 | Affiliations added to the room. |  |
| 400 | Provided 'affiliations' value is invalid. | [ErrorResponse](#errorresponse) |
| 403 | Not allowed to perform this affiliation change. | [ErrorResponse](#errorresponse) |
| 404 | The chat room (or its service) can not be found or is not accessible. | [ErrorResponse](#errorresponse) |

## Remove group room affiliations

> **DELETE** /plugins/restapi/v1/chatrooms/{roomName}/{affiliation}/group/{groupname}

Removes affiliation for all members of an Openfire user group from a multi-user chat room.

**Parameters**

| Name | Located in | Required | Description | Default value |
|------|------------|----------|-------------|---------------|
| groupname | path | yes | The name of the user group from which all members will get their room affiliation removed. Example: `Operators` |  |
| servicename | query | no | The name of the MUC service that the MUC room is part of. Example: `conference` | `conference` |
| affiliation | path | yes | The type of affiliation. One of: 'admins', 'members', 'outcasts', 'owners'. Example: `members` |  |
| roomName | path | yes | The name of the MUC room from which affiliations are to be removed. Example: `lobby` |  |

**Responses**

| Status | Description | Response body |
|--------|-------------|---------------|
| 200 | Affiliations removed from the room. |  |
| 400 | Provided 'affiliations' value is invalid. | [ErrorResponse](#errorresponse) |
| 403 | Not allowed to remove this affiliation. | [ErrorResponse](#errorresponse) |
| 404 | The chat room (or its service) can not be found or is not accessible. | [ErrorResponse](#errorresponse) |
| 409 | Applying this affiliation change would cause a room conflict. | [ErrorResponse](#errorresponse) |

## Add room affiliation

> **POST** /plugins/restapi/v1/chatrooms/{roomName}/{affiliation}/{jid}

Affiliates a single user to a multi-user chat room. Note that a user can only have one type of affiliation with a room. By affiliating a user to a room, any other pre-existing affiliation for that user is removed.

**Parameters**

| Name | Located in | Required | Description | Default value |
|------|------------|----------|-------------|---------------|
| servicename | query | no | The name of the MUC service that the MUC room is part of. Example: `conference` | `conference` |
| jid | path | yes | The entity that is to be affiliated: a (bare) JID, or the name of a local user. Example: `john@example.org` |  |
| affiliation | path | yes | The type of affiliation. One of: 'admins', 'members', 'outcasts', 'owners'. Example: `members` |  |
| roomName | path | yes | The name of the MUC room to which an affiliation is to be added. Example: `lobby` |  |
| sendInvitations | query | no | Whether to send invitations to newly affiliated users. Example: `true` | `false` |

**Responses**

| Status | Description | Response body |
|--------|-------------|---------------|
| 201 | User has been affiliated to the room. |  |
| 400 | Provided 'affiliations' value is invalid. | [ErrorResponse](#errorresponse) |
| 403 | Not allowed to perform this affiliation change. | [ErrorResponse](#errorresponse) |
| 404 | The chat room (or its service) can not be found or is not accessible. | [ErrorResponse](#errorresponse) |

## Remove room affiliation

> **DELETE** /plugins/restapi/v1/chatrooms/{roomName}/{affiliation}/{jid}

Removes an affiliation of a user to a multi-user chat room.

**Parameters**

| Name | Located in | Required | Description | Default value |
|------|------------|----------|-------------|---------------|
| jid | path | yes | The entity for which the room affiliation is to be removed: a (bare) JID, or the name of a local user. Example: `john@example.org` |  |
| servicename | query | no | The name of the MUC service that the MUC room is part of. Example: `conference` | `conference` |
| affiliation | path | yes | The type of affiliation. One of: 'admins', 'members', 'outcasts', 'owners'. Example: `members` |  |
| roomName | path | yes | The name of the MUC room from which an affiliation is to be removed. Example: `lobby` |  |

**Responses**

| Status | Description | Response body |
|--------|-------------|---------------|
| 200 | Affiliation removed from the room. |  |
| 400 | Provided 'affiliations' value is invalid. | [ErrorResponse](#errorresponse) |
| 403 | Not allowed to remove this affiliation. | [ErrorResponse](#errorresponse) |
| 404 | The chat room (or its service) can not be found or is not accessible. | [ErrorResponse](#errorresponse) |
| 409 | Applying this affiliation change would cause a room conflict. | [ErrorResponse](#errorresponse) |

# Client Sessions

Managing live client sessions.

## Get all sessions

> **GET** /plugins/restapi/v1/sessions

Retrieve all live client sessions.

**Responses**

| Status | Description | Response body |
|--------|-------------|---------------|
| 200 | The client sessions currently active in Openfire. | [SessionEntities](#sessionentities) (XML or JSON) |

## Get user sessions

> **GET** /plugins/restapi/v1/sessions/{username}

Retrieve all live client sessions for a particular user.

**Parameters**

| Name | Located in | Required | Description | Default value |
|------|------------|----------|-------------|---------------|
| username | path | yes | The name of a user for which to return client sessions. Example: `johndoe` |  |

**Responses**

| Status | Description | Response body |
|--------|-------------|---------------|
| 200 | The client sessions for one particular user that are currently active in Openfire. | [SessionEntities](#sessionentities) (XML or JSON) |

## Kick user sessions

> **DELETE** /plugins/restapi/v1/sessions/{username}

Close/disconnect all live client sessions for a particular user.

**Parameters**

| Name | Located in | Required | Description | Default value |
|------|------------|----------|-------------|---------------|
| username | path | yes | The name of a user for which to drop all client sessions. Example: `johndoe` |  |

**Responses**

| Status | Description | Response body |
|--------|-------------|---------------|
| 200 | The client sessions for one particular user have been closed. |  |

# Message

Sending (chat) messages to users.

## Broadcast

> **POST** /plugins/restapi/v1/messages/users

Sends a message to all users that are currently online.

**Request body** (required): [MessageEntity](#messageentity) (XML or JSON) - The message that is to be broadcast.

<details>
<summary>Example request bodies</summary>

XML (`Content-Type: application/xml`):

```xml
<message>
    <body>The server will be restarted in 5 minutes.</body>
</message>
```

JSON (`Content-Type: application/json`):

```json
{
  "body" : "The server will be restarted in 5 minutes."
}
```

</details>

**Responses**

| Status | Description | Response body |
|--------|-------------|---------------|
| 201 | Message is sent. |  |
| 400 | The message content is empty or missing. |  |

# Message Archive

Server-sided storage of chat messages.

## Unread message count

> **GET** /plugins/restapi/v1/archive/messages/unread/{jid}

Gets a count of messages that haven't been delivered to the user yet.

**Parameters**

| Name | Located in | Required | Description | Default value |
|------|------------|----------|-------------|---------------|
| jid | path | yes | The (bare) JID of the user for which the unread message count needs to be fetched. Example: `john@example.org` |  |

**Responses**

| Status | Description | Response body |
|--------|-------------|---------------|
| 200 | A message count. | [MsgArchiveEntity](#msgarchiveentity) (XML or JSON) |

# Security Audit Log

Inspecting the security audit log.

## Get log entries

> **GET** /plugins/restapi/v1/logs/security

Retrieve entries from the security audit log.

**Parameters**

| Name | Located in | Required | Description | Default value |
|------|------------|----------|-------------|---------------|
| username | query | no | The name of a user for which to filter events. Example: `admin` |  |
| offset | query | no | Number of log entries to skip. Example: `0` |  |
| limit | query | no | Number of log entries to retrieve. Example: `100` | `100` |
| startTime | query | no | Oldest timestamp of range of logs to retrieve. 0 for 'forever'. |  |
| endTime | query | no | Most recent timestamp of range of logs to retrieve. 0 for 'now'. |  |

**Responses**

| Status | Description | Response body |
|--------|-------------|---------------|
| 200 | The requested log entries. | [SecurityAuditLogs](#securityauditlogs) (XML or JSON) |
| 403 | The audit log is not readable (configured to be write-only). | [ErrorResponse](#errorresponse) (XML or JSON) |

# Statistics

Inspecting Openfire statistics.

## Get client session counts

> **GET** /plugins/restapi/v1/system/statistics/sessions

Retrieve statistics on the number of client sessions.

**Responses**

| Status | Description | Response body |
|--------|-------------|---------------|
| 200 | The requested statistics. | [SessionsCount](#sessionscount) (XML or JSON) |

# System

Managing Openfire system configuration.

## Perform all liveness checks

> **GET** /plugins/restapi/v1/system/liveness

Detects if Openfire has reached a state that it cannot recover from, except for with a restart, based on every liveness check that it has implemented.

**Responses**

| Status | Description | Response body |
|--------|-------------|---------------|
| 200 | The system is live. |  |
| 503 | At least one liveness check failed: the system is determined to not be alive. |  |

## Perform 'deadlock' liveness check

> **GET** /plugins/restapi/v1/system/liveness/deadlock

Detects if Openfire has reached a state that it cannot recover from because of a deadlock.

**Responses**

| Status | Description | Response body |
|--------|-------------|---------------|
| 200 | The system is live. |  |
| 503 | A deadlock is detected. |  |

## Perform 'properties' liveness check

> **GET** /plugins/restapi/v1/system/liveness/properties

Detects if Openfire has reached a state that it cannot recover from because a system property change requires a restart to take effect.

**Responses**

| Status | Description | Response body |
|--------|-------------|---------------|
| 200 | The system is live. |  |
| 503 | One or more system property changes that require a server restart have been detected. |  |

## Get system properties

> **GET** /plugins/restapi/v1/system/properties

Get all Openfire system properties.

**Responses**

| Status | Description | Response body |
|--------|-------------|---------------|
| 200 | The system properties. | [SystemProperties](#systemproperties) (XML or JSON) |

## Create system property

> **POST** /plugins/restapi/v1/system/properties

Create a new Openfire system property. Will overwrite a pre-existing system property that uses the same name.

**Request body** (required): [SystemProperty](#systemproperty) (XML or JSON) - The system property to create.

<details>
<summary>Example request bodies</summary>

XML (`Content-Type: application/xml`):

```xml
<property key="xmpp.domain" value="example.org"/>
```

JSON (`Content-Type: application/json`):

```json
{
  "key" : "xmpp.domain",
  "value" : "example.org"
}
```

</details>

**Responses**

| Status | Description | Response body |
|--------|-------------|---------------|
| 201 | The system property is created. |  |
| 400 | No system property was provided, the system property has no value, or its name is not valid. The name must consist of one or more dot-separated parts, each consisting of ASCII letters, digits, underscores, apostrophes and hyphens. | [ErrorResponse](#errorresponse) |
| 403 | Prohibited to create this system property. | [ErrorResponse](#errorresponse) |
| 409 | The name of the system property differs only in case from the name of an existing system property. | [ErrorResponse](#errorresponse) |

## Get system property

> **GET** /plugins/restapi/v1/system/properties/{propertyKey}

Get a specific Openfire system property.

**Parameters**

| Name | Located in | Required | Description | Default value |
|------|------------|----------|-------------|---------------|
| propertyKey | path | yes | The name of the system property to return. Example: `foo.bar.xyz` |  |

**Responses**

| Status | Description | Response body |
|--------|-------------|---------------|
| 200 | The requested system property. | [SystemProperty](#systemproperty) (XML or JSON) |
| 403 | Reading this system property is prohibited. | [ErrorResponse](#errorresponse) (XML or JSON) |
| 404 | The system property could not be found. | [ErrorResponse](#errorresponse) (XML or JSON) |

## Update system property

> **PUT** /plugins/restapi/v1/system/properties/{propertyKey}

Updates an existing Openfire system property.

**Parameters**

| Name | Located in | Required | Description | Default value |
|------|------------|----------|-------------|---------------|
| propertyKey | path | yes | The name of the system property to update. Example: `foo.bar.xyz` |  |

**Request body** (required): [SystemProperty](#systemproperty) (XML or JSON) - The new system property definition that replaces an existing definition.

<details>
<summary>Example request bodies</summary>

XML (`Content-Type: application/xml`):

```xml
<property key="xmpp.domain" value="example.org"/>
```

JSON (`Content-Type: application/json`):

```json
{
  "key" : "xmpp.domain",
  "value" : "example.org"
}
```

</details>

**Responses**

| Status | Description | Response body |
|--------|-------------|---------------|
| 200 | The system property is updated. |  |
| 400 | No system property was provided, the system property has no value, or it does not match the name in the URL. | [ErrorResponse](#errorresponse) |
| 403 | Prohibited to update this system property. | [ErrorResponse](#errorresponse) |
| 404 | The system property could not be found. | [ErrorResponse](#errorresponse) |
| 409 | The name of the system property differs only in case from the name of another existing system property. | [ErrorResponse](#errorresponse) |

## Remove system property

> **DELETE** /plugins/restapi/v1/system/properties/{propertyKey}

Removes an existing Openfire system property, together with all of its child properties (properties of which the name starts with the name of this property, followed by a dot).

**Parameters**

| Name | Located in | Required | Description | Default value |
|------|------------|----------|-------------|---------------|
| propertyKey | path | yes | The name of the system property to delete. Example: `foo.bar.xyz` |  |

**Responses**

| Status | Description | Response body |
|--------|-------------|---------------|
| 200 | The system property and its child properties are deleted. |  |
| 400 | The name of the system property is not valid. It must consist of one or more dot-separated parts, each consisting of ASCII letters, digits, underscores, apostrophes and hyphens. | [ErrorResponse](#errorresponse) |
| 403 | Prohibited to delete this system property, or one of its child properties. | [ErrorResponse](#errorresponse) |
| 404 | The system property could not be found. | [ErrorResponse](#errorresponse) |
| 409 | Deleting this system property could also delete unintended properties (other than this property and its child properties). This can happen, for example, when its name contains an underscore, which can match any character. | [ErrorResponse](#errorresponse) |

## Perform all readiness checks

> **GET** /plugins/restapi/v1/system/readiness

Detects if Openfire is in a state where it is ready to process traffic, based on every readiness check that it has implemented.

**Responses**

| Status | Description | Response body |
|--------|-------------|---------------|
| 200 | The system is ready. |  |
| 503 | At least one readiness check failed: the system is determined to not be able to process traffic. |  |

## Perform 'cluster' readiness check

> **GET** /plugins/restapi/v1/system/readiness/cluster

Detects if the cluster functionality has finished starting (or is disabled).

**Responses**

| Status | Description | Response body |
|--------|-------------|---------------|
| 200 | The system is ready. |  |
| 503 | Clustering functionality is enabled, but has not finished starting up yet. |  |

## Perform 'connections' readiness check

> **GET** /plugins/restapi/v1/system/readiness/connections

Detects if Openfire is ready to accept connection requests.

**Parameters**

| Name | Located in | Required | Description | Default value |
|------|------------|----------|-------------|---------------|
| connectionType | query | no | Optional. Use to limit the check to one particular connection type. One of: SOCKET_S2S, SOCKET_C2S, BOSH_C2S, WEBADMIN, COMPONENT, CONNECTION_MANAGER. Example: `SOCKET_C2S` |  |
| encrypted | query | no | Check the encrypted (true) or unencrypted (false) variant of the connection type. Only used in combination with 'connectionType', as without it, all types and both encrypted and unencrypted are checked. |  |

**Responses**

| Status | Description | Response body |
|--------|-------------|---------------|
| 200 | The system is ready. |  |
| 400 | The provided connectionType value is invalid. |  |
| 503 | Openfire currently does not accept (all) connections. |  |

## Perform 'plugins' readiness check

> **GET** /plugins/restapi/v1/system/readiness/plugins

Detects if Openfire has finished starting its plugins.

**Responses**

| Status | Description | Response body |
|--------|-------------|---------------|
| 200 | The system is ready. |  |
| 503 | Plugins have not all been started yet. |  |

## Perform 'server started' readiness check

> **GET** /plugins/restapi/v1/system/readiness/server

Detects if Openfire's core service has been started.

**Responses**

| Status | Description | Response body |
|--------|-------------|---------------|
| 200 | The system is ready. |  |
| 503 | The Openfire service has not finished starting up yet. |  |

# Clustering

Reporting the status of Openfire clustering.

## Get all cluster nodes

> **GET** /plugins/restapi/v1/clustering/nodes

Get a list of all nodes of the cluster. Note that this endpoint can only return data for remote nodes when the instance of Openfire that processes this query has successfully joined the cluster.

**Responses**

| Status | Description | Response body |
|--------|-------------|---------------|
| 200 | All cluster nodes. | [ClusterNodeEntities](#clusternodeentities) (XML or JSON) |

## Get a specific cluster node

> **GET** /plugins/restapi/v1/clustering/nodes/{nodeId}

Get a specific node of the cluster. Note that this endpoint can only return data for remote nodes when the instance of Openfire that processes this query has successfully joined the cluster.

**Parameters**

| Name | Located in | Required | Description | Default value |
|------|------------|----------|-------------|---------------|
| nodeId | path | yes | The nodeID value for a particular node. Example: `52a89928-66f7-45fd-9bb8-096de07400ac` |  |

**Responses**

| Status | Description | Response body |
|--------|-------------|---------------|
| 200 | The cluster node. | [ClusterNodeEntity](#clusternodeentity) (XML or JSON) |
| 404 | The provided NodeID does not identify an existing cluster node. | [ErrorResponse](#errorresponse) (XML or JSON) |

## Get clustering status

> **GET** /plugins/restapi/v1/clustering/status

Describes the point-in-time state of Openfire's clustering with other servers. The status is one of: 'SENIOR AND ONLY MEMBER', 'Senior member', 'Junior member', 'Starting up' or 'Disabled'.

**Responses**

| Status | Description | Response body |
|--------|-------------|---------------|
| 200 | Status returned. | [ClusteringEntity](#clusteringentity) (XML or JSON) |

<!-- END GENERATED ENDPOINTS -->

# Data format
Openfire REST API provides XML and JSON as data format. The default data format is XML.
To get a JSON result, please add "**Accept: application/json**" to the request header.
If you want to create a resource with JSON data format, please add "**Content-Type: application/json**".

<!-- BEGIN GENERATED DATA TYPES: do not edit this section by hand. It is generated from the OpenAPI annotations in the source code by the Maven build. -->

## Data types

These are the data types that are used in the request and response bodies of the endpoints. The name of a field is the name that is used in JSON. When XML uses a different name, it is mentioned in the description of the field.

Date/time values are represented as an ISO-8601 formatted text in XML (for example: `2026-01-31T12:34:56.789Z`), and as the number of milliseconds since the Unix epoch in JSON (for example: `1769862896789`). In JSON request bodies, the ISO-8601 format can also be used.

### AdminEntities

A list of entities that have an admin affiliation with a multi-user chat room.

XML root element: `<admins>`

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| admins | array of string | no | The JIDs (or names of local users) of the entities. In XML, items are represented as `<admin>` elements. Example: `jane@example.org` |

### AffiliatedEntities

A list of entities that have a particular affiliation with a multi-user chat room. Depending on the affiliation, this is an AdminEntities, MemberEntities, OutcastEntities or OwnerEntities value.

### ClusterNodeEntities

A list of the nodes in an Openfire cluster.

XML root element: `<clusterNodes>`

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| clusterNodes | array of [ClusterNodeEntity](#clusternodeentity) | no | The nodes of the cluster. In XML, items are represented as `<clusterNode>` elements. |

### ClusterNodeEntity

A node in an Openfire cluster.

XML root element: `<clusterNode>`

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| hostName | string | no | The host name and IP address of the server on which this cluster node is running. Example: `xmpp1.example.org (192.168.0.10)` |
| nodeID | string | no | The unique identifier of this cluster node. Example: `a3f1c2d4-5e6f-4a7b-8c9d-0e1f2a3b4c5d` |
| joinedTime | date-time | no | The moment at which this node joined the cluster. |
| seniorMember | boolean | no | Whether this node currently is the senior member of the cluster. Example: `true` |

### ClusteringEntity

The clustering status of an Openfire instance.

XML root element: `<clustering>`

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| status | string | no | The clustering status of this Openfire instance. Allowed values: `SENIOR AND ONLY MEMBER`, `Senior member`, `Junior member`, `Starting up`, `Disabled`. Example: `Senior member` |

### ErrorResponse

A description of an error that occurred while processing a request.

XML root element: `<error>`

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| resource | string | no | The resource (for example, a username or room name) that the error relates to. Example: `john` |
| message | string | no | A description of the error. Example: `Could not get user` |
| exception | string | no | The type of the error. Example: `UserNotFoundException` |

### GroupEntities

A list of Openfire user groups.

XML root element: `<groups>`

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| groups | array of [GroupEntity](#groupentity) | no | The groups. In XML, items are represented as `<group>` elements. |

### GroupEntity

An Openfire user group.

XML root element: `<group>`

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| name | string | yes | The name of the group. When updating a group, this must be equal to the group name in the path of the request. Example: `UserGroup1` |
| description | string | no | The description of the group. Example: `My group of users` |
| shared | boolean | no | Whether the group is shared: whether it automatically appears in the rosters of its members. Example: `false` |
| admins | array of string | no | The admins of the group. When creating or updating a group, each admin can be identified by a username or a JID. Responses contain (bare) JIDs. In XML, items are represented as `<admin>` elements, wrapped in the `<admins>` element. Example: `jane.smith` |
| members | array of string | no | The members of the group. When creating or updating a group, each member can be identified by a username or a JID. Responses contain (bare) JIDs. In XML, items are represented as `<member>` elements, wrapped in the `<members>` element. Example: `john.jones` |

### MUCInvitationEntity

An invitation to join a multi-user chat room.

XML root element: `<mucInvitation>`

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| reason | string | no | The reason that is included in the invitation message(s). Example: `Come join this cool room please!` |

### MUCInvitationsEntity

An invitation for a collection of users and/or groups to join a multi-user chat room.

XML root element: `<mucInvitations>`

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| reason | string | no | The reason that is included in the invitation message(s). Example: `Come join this cool room please!` |
| jidsToInvite | array of string | no | The users and/or groups to invite into the room, each identified by the JID of a user or group, or by the name of a local user or group. In XML, items are represented as `<jid>` elements, wrapped in the `<jidsToInvite>` element. Example: `john@example.org` |

### MUCRoomEntities

A list of multi-user chat rooms.

XML root element: `<chatRooms>`

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| chatRooms | array of [MUCRoomEntity](#mucroomentity) | no | The chat rooms. In XML, items are represented as `<chatRoom>` elements. |

### MUCRoomEntity

A multi-user chat room. When a room is created or updated, boolean values that are not provided are treated as 'false'.

XML root element: `<chatRoom>`

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| roomName | string | yes | The name of the room, which is used as the local part of the room's JID. It is converted to lowercase. When updating a room, this must be equal to the room name in the path of the request. Example: `global` |
| description | string | no | The description of the room. Example: `A room for everyone` |
| password | string | no | The password that users must provide to enter the room. Example: `s3cr3t` |
| subject | string | no | The subject (topic) of the room. Example: `Welcome!` |
| naturalName | string | no | The human-readable name of the room, as shown to users that discover rooms on the chat service. Example: `Global Chat` |
| maxUsers | integer | no | The maximum number of occupants that can be in the room at the same time. 0 means unlimited. Example: `30` |
| creationDate | date-time | no | The moment at which the room was created. When creating a room without this value, the current time is used. |
| modificationDate | date-time | no | The moment at which the configuration of the room was last modified. When creating or updating a room without this value, the current time is used. |
| persistent | boolean | no | Whether the room is persistent. Persistent rooms are saved to the database, and are not destroyed when the last occupant leaves. Example: `true` |
| publicRoom | boolean | no | Whether the room is public: searchable and visible through service discovery. Example: `true` |
| registrationEnabled | boolean | no | Whether users are allowed to register with the room. Example: `false` |
| canAnyoneDiscoverJID | boolean | no | Whether the real JID of every occupant is visible to every other occupant (a non-anonymous room). Example: `false` |
| canOccupantsChangeSubject | boolean | no | Whether participants are allowed to change the subject of the room. Example: `false` |
| canOccupantsInvite | boolean | no | Whether occupants can invite other users to the room. When the room is not members-only, anyone can send invitations regardless of this value. When the room is members-only and this is 'false', only owners and admins can send invitations. Example: `false` |
| canChangeNickname | boolean | no | Whether occupants are allowed to change their nickname in the room. Example: `true` |
| logEnabled | boolean | no | Whether the conversation in the room is logged (saved to the database). Example: `true` |
| loginRestrictedToNickname | boolean | no | Whether registered users can only join the room using their registered nickname. Example: `false` |
| membersOnly | boolean | no | Whether the room is members-only: users need to be a member (or be invited) to enter. Example: `false` |
| moderated | boolean | no | Whether the room is moderated: only occupants with 'voice' can send messages to all occupants. Example: `false` |
| allowPM | string | no | Defines who is allowed to send private messages to other occupants. Must be one of "anyone", "participants", "moderators" or "none". Example: `anyone` |
| broadcastPresenceRoles | array of string | no | The roles of occupants of which presence is broadcast to the other occupants. Each is one of: 'moderator', 'participant', 'visitor'. In XML, items are represented as `<broadcastPresenceRole>` elements, wrapped in the `<broadcastPresenceRoles>` element. Example: `moderator` |
| owners | array of string | no | The (bare) JIDs of the users that have an owner affiliation with the room. When creating a room without owners, the 'admin' user is made owner. In XML, items are represented as `<owner>` elements, wrapped in the `<owners>` element. Example: `admin@example.org` |
| ownerGroups | array of string | no | The names of the user groups that have an owner affiliation with the room. In XML, items are represented as `<ownerGroup>` elements, wrapped in the `<ownerGroups>` element. Example: `Management` |
| admins | array of string | no | The (bare) JIDs of the users that have an admin affiliation with the room. In XML, items are represented as `<admin>` elements, wrapped in the `<admins>` element. Example: `jane@example.org` |
| adminGroups | array of string | no | The names of the user groups that have an admin affiliation with the room. In XML, items are represented as `<adminGroup>` elements, wrapped in the `<adminGroups>` element. Example: `Moderators` |
| members | array of string | no | The (bare) JIDs of the users that have a member affiliation with the room. In XML, items are represented as `<member>` elements, wrapped in the `<members>` element. Example: `john@example.org` |
| memberGroups | array of string | no | The names of the user groups that have a member affiliation with the room. In XML, items are represented as `<memberGroup>` elements, wrapped in the `<memberGroups>` element. Example: `Sales` |
| outcasts | array of string | no | The (bare) JIDs of the users that have an outcast affiliation with the room: users that are banned from the room. In XML, items are represented as `<outcast>` elements, wrapped in the `<outcasts>` element. Example: `spammer@example.org` |
| outcastGroups | array of string | no | The names of the user groups that have an outcast affiliation with the room. In XML, items are represented as `<outcastGroup>` elements, wrapped in the `<outcastGroups>` element. Example: `Banned` |

### MUCRoomMessageEntities

A list of messages from the history of a multi-user chat room.

XML root element: `<messages>`

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| message | array of [MUCRoomMessageEntity](#mucroommessageentity) | no | The messages. In XML, items are represented as `<message>` elements. |

### MUCRoomMessageEntity

A message from the history of a multi-user chat room.

XML root element: `<message>`

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| to | string | no | The JID of the addressee of the message. Example: `global@conference.example.org` |
| from | string | no | The JID of the sender of the message: the room JID, followed by the nickname of the occupant. Example: `global@conference.example.org/john` |
| type | string | no | The XMPP message type. Example: `groupchat` |
| body | string | no | The text of the message. Example: `Hello, everyone!` |
| delay_stamp | string | no | The moment at which the message was originally sent (XEP-0203 delayed delivery timestamp). Example: `2026-01-31T12:34:56.789Z` |
| delay_from | string | no | The JID of the entity that delayed the delivery of the message (XEP-0203). Example: `global@conference.example.org` |

### MUCServiceEntities

A list of multi-user chat services.

XML root element: `<chatServices>`

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| chatService | array of [MUCServiceEntity](#mucserviceentity) | no | The chat services. In XML, items are represented as `<chatService>` elements. |

### MUCServiceEntity

A multi-user chat service.

XML root element: `<chatService>`

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| serviceName | string | yes | The name of the chat service, which is used as the subdomain of the service. Example: `conference` |
| description | string | no | The description of the chat service. Example: `A public service` |
| hidden | boolean | no | Whether the service is hidden from service discovery. Example: `false` |

### MemberEntities

A list of entities that have a member affiliation with a multi-user chat room.

XML root element: `<members>`

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| members | array of string | no | The JIDs (or names of local users) of the entities. In XML, items are represented as `<member>` elements. Example: `john@example.org` |

### MessageEntity

A message.

XML root element: `<message>`

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| body | string | yes | The text of the message. Example: `The server will be restarted in 5 minutes.` |

### MsgArchiveEntity

The number of unread messages of a user.

XML root element: `<archive>`

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| jid | string | no | The JID of the user. Example: `john@example.org` |
| count | integer | no | The number of unread messages. Example: `3` |

### OccupantEntities

A list of occupants of a multi-user chat room.

XML root element: `<occupants>`

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| occupants | array of [OccupantEntity](#occupantentity) | no | The occupants. In XML, items are represented as `<occupant>` elements. |

### OccupantEntity

An occupant of a multi-user chat room.

XML root element: `<occupant>`

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| jid | string | no | The occupant JID: the room JID, followed by the nickname of the occupant. Example: `global@conference.example.org/john` |
| userAddress | string | no | The real (full) JID of the user. Example: `john@example.org/laptop` |
| role | string | no | The role of the occupant in the room. One of: 'moderator', 'participant', 'visitor', 'none'. Example: `participant` |
| affiliation | string | no | The affiliation of the occupant with the room. One of: 'owner', 'admin', 'member', 'outcast', 'none'. Example: `member` |

### OutcastEntities

A list of entities that have an outcast affiliation with a multi-user chat room: entities that are banned from the room.

XML root element: `<outcasts>`

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| outcasts | array of string | no | The JIDs (or names of local users) of the entities. In XML, items are represented as `<outcast>` elements. Example: `spammer@example.org` |

### OwnerEntities

A list of entities that have an owner affiliation with a multi-user chat room.

XML root element: `<owners>`

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| owners | array of string | no | The JIDs (or names of local users) of the entities. In XML, items are represented as `<owner>` elements. Example: `admin@example.org` |

### ParticipantEntities

A list of occupants of a multi-user chat room.

XML root element: `<participants>`

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| participants | array of [ParticipantEntity](#participantentity) | no | The occupants. In XML, items are represented as `<participant>` elements. |

### ParticipantEntity

An occupant of a multi-user chat room.

XML root element: `<participant>`

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| jid | string | no | The occupant JID: the room JID, followed by the nickname of the occupant. Example: `global@conference.example.org/john` |
| role | string | no | The role of the occupant in the room. One of: 'moderator', 'participant', 'visitor', 'none'. Example: `participant` |
| affiliation | string | no | The affiliation of the occupant with the room. One of: 'owner', 'admin', 'member', 'outcast', 'none'. Example: `member` |

### RoomCreationResultEntities

The results of the creation of multiple multi-user chat rooms, grouped by result type.

XML root element: `<results>`

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| success | array of [RoomCreationResultEntity](#roomcreationresultentity) | no | The results of the rooms that were created successfully. In XML, the items are wrapped in the `<success>` element. |
| failure | array of [RoomCreationResultEntity](#roomcreationresultentity) | no | The results of the rooms that could not be created. In XML, the items are wrapped in the `<failure>` element. |
| other | array of [RoomCreationResultEntity](#roomcreationresultentity) | no | The results of a type other than success or failure. In XML, the items are wrapped in the `<other>` element. |

### RoomCreationResultEntity

The result of the creation of one multi-user chat room.

XML root element: `<result>`

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| roomName | string | no | The name of the room that was to be created. Example: `open_chat` |
| resultType | string | no | The result of creating the room. Allowed values: `Success`, `Failure`. Example: `Failure` |
| message | string | no | A message that describes the result. Example: `Room already existed and therefore not created again` |

### RosterEntities

The roster (contact list) of a user.

XML root element: `<roster>`

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| rosterItem | array of [RosterItemEntity](#rosteritementity) | no | The entries of the roster. In XML, items are represented as `<rosterItem>` elements. |

### RosterItemEntity

An entry in the roster (contact list) of a user.

XML root element: `<rosterItem>`

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| jid | string | yes | The JID of the contact. Example: `jane@example.org` |
| nickname | string | no | The name of the contact, as shown in this roster. Example: `Jane` |
| subscriptionType | integer | no | The presence subscription state of the contact. One of: -1 (remove), 0 (none), 1 (to: the user receives presence updates of the contact), 2 (from: the contact receives presence updates of the user), 3 (both). Example: `3` |
| groups | array of string | no | The roster groups (for example 'Friends' or 'Co-workers') that this contact is organized under. In XML, items are represented as `<group>` elements, wrapped in the `<groups>` element. Example: `Friends` |

### SecurityAuditLog

An entry of the security audit log.

XML root element: `<log>`

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| logId | integer | no | The unique identifier of the log entry. Example: `42` |
| username | string | no | The username of the user that performed the audited action. Example: `admin` |
| timestamp | integer | no | The moment at which the audited action occurred, in seconds since the Unix epoch. Example: `1769862896` |
| summary | string | no | A short description of the audited action. Example: `Created new user john` |
| node | string | no | The node that triggered the audited action, usually a host name or IP address. Example: `xmpp1.example.org` |
| details | string | no | Detailed information about the audited action. Example: `name = John Doe, email = john@example.org` |

### SecurityAuditLogs

A list of entries of the security audit log.

XML root element: `<logs>`

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| logs | array of [SecurityAuditLog](#securityauditlog) | no | The log entries. In XML, items are represented as `<log>` elements. |

### SessionEntities

A list of client sessions.

XML root element: `<sessions>`

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| sessions | array of [SessionEntity](#sessionentity) | no | The sessions. In XML, items are represented as `<session>` elements. |

### SessionEntity

A client session.

XML root element: `<session>`

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| sessionId | string | no | The (full) JID of the session. Example: `john@example.org/laptop` |
| username | string | no | The username of the user of the session, or 'Anonymous' for anonymous sessions. Example: `john` |
| resource | string | no | The resource part of the JID of the session. Example: `laptop` |
| node | string | no | Whether the session is connected to the cluster node that processes the request ('Local'), or to another cluster node ('Remote'). Example: `Local` |
| sessionStatus | string | no | The status of the session. One of: 'Closed', 'Connected', 'Authenticated', 'Unknown'. Example: `Authenticated` |
| presenceStatus | string | no | The availability of the user of the session. One of: 'Online', 'Away', 'Available to Chat', 'Do Not Disturb', 'Extended Away', 'Unknown/Not Recognized'. Example: `Online` |
| presenceMessage | string | no | The (optional) natural-language description of the availability of the user of the session. Example: `In a meeting` |
| priority | integer | no | The presence priority of the session, from -128 to 127. Example: `0` |
| hostAddress | string | no | The IP address of the client. Example: `192.168.0.20` |
| hostName | string | no | The host name of the client. Example: `laptop.example.org` |
| creationDate | date-time | no | The moment at which the session was created. |
| lastActionDate | date-time | no | The moment at which the session last had activity. |
| secure | boolean | no | Whether the connection of the session is encrypted. Example: `true` |

### SessionsCount

The number of client sessions.

XML root element: `<sessions>`

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| localSessions | integer | no | The number of authenticated client sessions (of both anonymous and non-anonymous users) on the cluster node that processes the request. Example: `12` |
| clusterSessions | integer | no | The number of authenticated client sessions (of both anonymous and non-anonymous users) in the entire cluster. Example: `30` |

### SystemProperties

A list of Openfire system properties.

XML root element: `<properties>`

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| property | array of [SystemProperty](#systemproperty) | no | The system properties. In XML, items are represented as `<property>` elements. |

### SystemProperty

An Openfire system property.

XML root element: `<property>`

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| key | string | yes | The name of the system property. In XML, this is the `key` attribute. Example: `xmpp.domain` |
| value | string | yes | The value of the system property. In XML, this is the `value` attribute. Example: `example.org` |

### UserEntities

A list of Openfire users.

XML root element: `<users>`

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| users | array of [UserEntity](#userentity) | no | The users. In XML, items are represented as `<user>` elements. |

### UserEntity

An Openfire user.

XML root element: `<user>`

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| username | string | no | The username of the user. Required when creating a user. When updating a user, providing a different username renames the user. Example: `john` |
| name | string | no | The name of the user. Example: `John Doe` |
| email | string | no | The email address of the user. Example: `john@example.org` |
| password | string | no | The password of the user. Required when creating a user. Never included in responses. Example: `s3cr3t` |
| properties | array of [UserProperty](#userproperty) | no | Custom properties of the user. Property keys are unique per user. When updating a user, all existing properties of the user are replaced by the provided properties: omitting this removes all properties of the user. In XML, the items are wrapped in the `<properties>` element. |

### UserGroupsEntity

A list of names of Openfire user groups.

XML root element: `<groups>`

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| groupnames | array of string | no | The names of the groups. In XML, items are represented as `<groupname>` elements. Example: `Sales` |

### UserProperty

A custom property of an Openfire user.

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| key | string | yes | The key (name) of the property. Unique per user. In XML, this is the `key` attribute. Example: `department` |
| value | string | yes | The value of the property. In XML, this is the `value` attribute. Example: `Sales` |

<!-- END GENERATED DATA TYPES -->
