
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

The paths of all endpoints below are relative to the root of the Openfire admin console, for example `http://example.org:9090`.

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
| 200 | A list of Openfire users. | `UserEntities` (XML or JSON) |

## Create user

> **POST** /plugins/restapi/v1/users

Add a new user to Openfire.

**Request body** (required): `UserEntity` (XML or JSON) - The definition of the user to create.

**Responses**

| Status | Description | Response body |
|--------|-------------|---------------|
| 201 | The user was created. |  |
| 400 | No user definition, username or password was provided. | `ErrorResponse` |
| 409 | A user with this username already exists. | `ErrorResponse` |

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
| 200 | The Openfire user. | `UserEntity` (XML or JSON) |
| 404 | No user with that username was found. | `ErrorResponse` (XML or JSON) |

## Update user

> **PUT** /plugins/restapi/v1/users/{username}

Update an existing user in Openfire.

**Parameters**

| Name | Located in | Required | Description | Default value |
|------|------------|----------|-------------|---------------|
| username | path | yes | The username of the user to update. |  |

**Request body** (required): `UserEntity` (XML or JSON) - The updated definition of the user.

**Responses**

| Status | Description | Response body |
|--------|-------------|---------------|
| 200 | The user was updated. |  |
| 404 | No user with that username was found. | `ErrorResponse` |
| 409 | The user is to be renamed, but a user with the new username already exists. | `ErrorResponse` |

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
| 404 | No user with that username was found. | `ErrorResponse` |

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
| 200 | The names of the groups that the user is in. | `UserGroupsEntity` (XML or JSON) |
| 404 | No user with that username was found. | `ErrorResponse` (XML or JSON) |

## Add user to groups

> **POST** /plugins/restapi/v1/users/{username}/groups

Add a particular user to a collection of groups. When a group that is provided does not exist, it will be automatically created if possible.

**Parameters**

| Name | Located in | Required | Description | Default value |
|------|------------|----------|-------------|---------------|
| username | path | yes | The username of the user that is to be added to groups. |  |

**Request body** (required): `UserGroupsEntity` (XML or JSON) - A collection of names for groups that the user is to be added to.

**Responses**

| Status | Description | Response body |
|--------|-------------|---------------|
| 201 | The user was added to all groups. |  |
| 400 | The username cannot be parsed into a JID. | `ErrorResponse` |

## Delete user from groups

> **DELETE** /plugins/restapi/v1/users/{username}/groups

Removes a user from a collection of groups.

**Parameters**

| Name | Located in | Required | Description | Default value |
|------|------------|----------|-------------|---------------|
| username | path | yes | The username of the user that is to be removed from groups. |  |

**Request body** (required): `UserGroupsEntity` (XML or JSON) - A collection of names for groups from which the user is to be removed.

**Responses**

| Status | Description | Response body |
|--------|-------------|---------------|
| 200 | The user was taken out of the groups. |  |
| 404 | One or more groups could not be found. | `ErrorResponse` |

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
| 400 | The username cannot be parsed into a JID. | `ErrorResponse` |

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
| 404 | The group could not be found. | `ErrorResponse` |

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
| 200 | All roster entries. | `RosterEntities` (XML or JSON) |
| 404 | No user with this username exists. | `ErrorResponse` (XML or JSON) |

## Create roster entry

> **POST** /plugins/restapi/v1/users/{username}/roster

Add a roster entry to the roster (buddies / contact list) of a particular user.

**Parameters**

| Name | Located in | Required | Description | Default value |
|------|------------|----------|-------------|---------------|
| username | path | yes | The username of the user for which to add a roster entry. |  |

**Request body** (required): `RosterItemEntity` (XML or JSON) - The definition of the roster entry that is to be added.

**Responses**

| Status | Description | Response body |
|--------|-------------|---------------|
| 201 | The entry was added to the roster. |  |
| 400 | A roster entry cannot be added to a 'shared group' (try removing group names from the roster entry and try again). | `ErrorResponse` |
| 404 | No user with this username exists. | `ErrorResponse` |
| 409 | A roster entry already exists for the provided contact JID. | `ErrorResponse` |

## Update roster entry

> **PUT** /plugins/restapi/v1/users/{username}/roster/{rosterJid}

Changes a roster entry on the roster (buddies / contact list) of a particular user.

**Parameters**

| Name | Located in | Required | Description | Default value |
|------|------------|----------|-------------|---------------|
| username | path | yes | The username of the user for which to update a roster entry. |  |
| rosterJid | path | yes | The JID of the entry/contact to update. |  |

**Request body** (required): `RosterItemEntity` (XML or JSON) - The updated definition of the roster entry.

**Responses**

| Status | Description | Response body |
|--------|-------------|---------------|
| 200 | The roster entry was updated. |  |
| 400 | A roster entry cannot be added with a 'shared group'. | `ErrorResponse` |
| 404 | No user with this username exists. | `ErrorResponse` |
| 409 | A roster entry already exists for the provided contact JID. | `ErrorResponse` |

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
| 400 | A roster entry cannot be removed from a 'shared group'. | `ErrorResponse` |
| 404 | No user with this username exists, or its roster did not contain this entry. | `ErrorResponse` |

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

**Request body** (required): string (XML) - The updated definition of the vCard.

**Responses**

| Status | Description | Response body |
|--------|-------------|---------------|
| 200 | The vCard was updated/created. |  |
| 400 | Provided data could not be parsed. | `ErrorResponse` |
| 409 | Cannot change vCard, as Openfire is configured to have read-only vCards. | `ErrorResponse` |

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
| 409 | Cannot delete vCard, as Openfire is configured to have read-only vCards. | `ErrorResponse` |

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
| 404 | No user with this username exists. | `ErrorResponse` |

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
| 404 | No user with this username exists. | `ErrorResponse` |

# User Group

Managing Openfire user groups.

## Get groups

> **GET** /plugins/restapi/v1/groups

Get a list of all user groups.

**Responses**

| Status | Description | Response body |
|--------|-------------|---------------|
| 200 | All groups. | `GroupEntities` (XML or JSON) |

## Create group

> **POST** /plugins/restapi/v1/groups

Create a new user group.

**Request body** (required): `GroupEntity` (XML or JSON) - The group that needs to be created.

**Responses**

| Status | Description | Response body |
|--------|-------------|---------------|
| 201 | Group created. |  |
| 400 | Group or group name missing, or invalid syntax for a property. | `ErrorResponse` |
| 409 | Group already exists. | `ErrorResponse` |

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
| 200 | The group. | `GroupEntity` (XML or JSON) |
| 404 | Group with this name not found. | `ErrorResponse` (XML or JSON) |

## Update group

> **PUT** /plugins/restapi/v1/groups/{groupName}

Updates / overwrites an existing user group. Note that the name of the group cannot be changed.

**Parameters**

| Name | Located in | Required | Description | Default value |
|------|------------|----------|-------------|---------------|
| groupName | path | yes | The name of the group that needs to be updated. Example: `Colleagues` |  |

**Request body** (required): `GroupEntity` (XML or JSON) - The new group definition that needs to overwrite the old definition.

**Responses**

| Status | Description | Response body |
|--------|-------------|---------------|
| 200 | Group updated. |  |
| 400 | Group or group name missing, or name does not match existing group, or invalid syntax for a property. | `ErrorResponse` |
| 404 | Group with this name not found. | `ErrorResponse` |

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
| 404 | Group with this name not found. | `ErrorResponse` |

# Chat service

Managing multi-user chat services.

## Get chat services

> **GET** /plugins/restapi/v1/chatservices

Get a list of all multi-user chat services.

**Responses**

| Status | Description | Response body |
|--------|-------------|---------------|
| 200 | All chat services. | `MUCServiceEntities` (XML or JSON) |

## Create chat service

> **POST** /plugins/restapi/v1/chatservices

Create a new multi-user chat service.

**Request body** (required): `MUCServiceEntity` (XML or JSON) - The MUC service that needs to be created.

**Responses**

| Status | Description | Response body |
|--------|-------------|---------------|
| 201 | Service created. |  |
| 403 | Service creation is not permitted. | `ErrorResponse` |
| 409 | Service already exists, or another conflict occurred while creating the service. | `ErrorResponse` |

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
| 200 | All chat rooms. | `MUCRoomEntities` (XML or JSON) |
| 404 | MUC service does not exist or is not accessible. | `ErrorResponse` (XML or JSON) |

## Create chat room

> **POST** /plugins/restapi/v1/chatrooms

Create a new multi-user chat room.

**Parameters**

| Name | Located in | Required | Description | Default value |
|------|------------|----------|-------------|---------------|
| servicename | query | no | The name of the MUC service in which to create a chat room. Example: `conference` | `conference` |
| sendInvitations | query | no | Whether to send invitations to newly affiliated users. Example: `true` | `false` |

**Request body** (required): `MUCRoomEntity` (XML or JSON) - The MUC room that needs to be created.

**Responses**

| Status | Description | Response body |
|--------|-------------|---------------|
| 201 | Room created. |  |
| 403 | Room creation is not permitted. | `ErrorResponse` |
| 404 | MUC service does not exist or is not accessible. | `ErrorResponse` |
| 409 | Room already exists, or another conflict occurred while creating the room. | `ErrorResponse` |

## Create multiple chat rooms

> **POST** /plugins/restapi/v1/chatrooms/bulk

Create a number of new multi-user chat rooms.

**Parameters**

| Name | Located in | Required | Description | Default value |
|------|------------|----------|-------------|---------------|
| servicename | query | no | The name of the MUC service in which to create the chat rooms. Example: `conference` | `conference` |
| sendInvitations | query | no | Whether to send invitations to newly affiliated users. Example: `true` | `false` |

**Request body** (required): `MUCRoomEntities` (XML or JSON) - The MUC rooms that need to be created.

**Responses**

| Status | Description | Response body |
|--------|-------------|---------------|
| 200 | Request has been processed. Results are reported in the response. | `RoomCreationResultEntities` (XML or JSON) |
| 404 | MUC service does not exist or is not accessible. | `ErrorResponse` (XML or JSON) |

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
| 200 | The chat room. | `MUCRoomEntity` (XML or JSON) |
| 404 | The chat room (or its service) can not be found or is not accessible. | `ErrorResponse` (XML or JSON) |

## Update chat room

> **PUT** /plugins/restapi/v1/chatrooms/{roomName}

Updates an existing multi-user chat room.

**Parameters**

| Name | Located in | Required | Description | Default value |
|------|------------|----------|-------------|---------------|
| roomName | path | yes | The name of the chat room that needs to be updated. Example: `lobby` |  |
| servicename | query | no | The name of the MUC service in which to update a chat room. Example: `conference` | `conference` |
| sendInvitations | query | no | Whether to send invitations to newly affiliated users. Example: `true` | `false` |

**Request body** (required): `MUCRoomEntity` (XML or JSON) - The new MUC room definition that needs to overwrite the old definition.

**Responses**

| Status | Description | Response body |
|--------|-------------|---------------|
| 200 | Room updated. |  |
| 403 | Room update/create is not permitted. | `ErrorResponse` |
| 404 | MUC service does not exist or is not accessible. | `ErrorResponse` |
| 409 | This update causes a conflict, possibly with another existing room. | `ErrorResponse` |

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
| 404 | The chat room (or its service) can not be found or is not accessible. | `ErrorResponse` |

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
| 200 | The chat room message history. | `MUCRoomMessageEntities` (XML or JSON) |
| 404 | The chat room (or its service) can not be found or is not accessible. | `ErrorResponse` (XML or JSON) |

## Invite a collection of users and/or groups

> **POST** /plugins/restapi/v1/chatrooms/{roomName}/invite

Invites a collection of users and/or groups to join a specific multi-user chat room. Each entity can be identified by the JID of a user or group, or by the name of a local user or group. When a group is invited, all of its members are invited.

**Parameters**

| Name | Located in | Required | Description | Default value |
|------|------------|----------|-------------|---------------|
| roomName | path | yes | The name of the chat room to which to invite users and/or groups. Example: `lobby` |  |
| servicename | query | no | The name of the chat room's MUC service. Example: `conference` | `conference` |

**Request body** (required): `MUCInvitationsEntity` (XML or JSON) - The invitation message to send and whom to send it to.

**Responses**

| Status | Description | Response body |
|--------|-------------|---------------|
| 200 | Invitation sent. |  |
| 403 | Not allowed to invite a user or group to this room. | `ErrorResponse` |
| 404 | The chat room (or its service) can not be found or is not accessible. | `ErrorResponse` |

## Invite user or group

> **POST** /plugins/restapi/v1/chatrooms/{roomName}/invite/{jid}

Invites a user or group to join a specific multi-user chat room.

**Parameters**

| Name | Located in | Required | Description | Default value |
|------|------------|----------|-------------|---------------|
| roomName | path | yes | The name of the chat room to which to invite a user or group. Example: `lobby` |  |
| jid | path | yes | The entity to invite into the room: the JID of a user or group, or the name of a local user or group. When a group is invited, all of its members are invited. Example: `john@example.org` |  |
| servicename | query | no | The name of the chat room's MUC service. Example: `conference` | `conference` |

**Request body** (required): `MUCInvitationEntity` (XML or JSON) - The invitation message to send and whom to send it to.

**Responses**

| Status | Description | Response body |
|--------|-------------|---------------|
| 200 | Invitation sent. |  |
| 403 | Not allowed to invite a user to this room. | `ErrorResponse` |
| 404 | The chat room (or its service) can not be found or is not accessible. | `ErrorResponse` |

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
| 200 | The chat room occupants. | `OccupantEntities` (XML or JSON) |
| 404 | The chat room (or its service) can not be found or is not accessible. | `ErrorResponse` (XML or JSON) |

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
| 200 | The chat room participants. | `ParticipantEntities` (XML or JSON) |
| 404 | The chat room (or its service) can not be found or is not accessible. | `ErrorResponse` (XML or JSON) |

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
| 400 | Provided 'affiliations' value is invalid. | `ErrorResponse` (XML or JSON) |
| 404 | The chat room (or its service) can not be found or is not accessible. | `ErrorResponse` (XML or JSON) |

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

**Request body** (required): `AffiliatedEntities` (XML or JSON) - The list of users to affiliate to the room.

**Responses**

| Status | Description | Response body |
|--------|-------------|---------------|
| 201 | Users have been affiliated to the room. |  |
| 400 | Provided values cannot be parsed as JIDs, or provided 'affiliations' value is invalid. | `ErrorResponse` |
| 403 | Not allowed to perform this affiliation change. | `ErrorResponse` |
| 404 | The chat room (or its service) can not be found or is not accessible. | `ErrorResponse` |

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

**Request body** (required): `AffiliatedEntities` (XML or JSON) - The new list of users with this particular affiliation.

**Responses**

| Status | Description | Response body |
|--------|-------------|---------------|
| 201 | Affiliations of the room have been replaced. |  |
| 400 | Provided values cannot be parsed as JIDs, or provided 'affiliations' value is invalid. | `ErrorResponse` |
| 403 | Not allowed to perform this affiliation change. | `ErrorResponse` |
| 404 | The chat room (or its service) can not be found or is not accessible. | `ErrorResponse` |

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
| 400 | Provided 'affiliations' value is invalid. | `ErrorResponse` |
| 403 | Not allowed to perform this affiliation change. | `ErrorResponse` |
| 404 | The chat room (or its service) can not be found or is not accessible. | `ErrorResponse` |

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
| 400 | Provided 'affiliations' value is invalid. | `ErrorResponse` |
| 403 | Not allowed to remove this affiliation. | `ErrorResponse` |
| 404 | The chat room (or its service) can not be found or is not accessible. | `ErrorResponse` |
| 409 | Applying this affiliation change would cause a room conflict. | `ErrorResponse` |

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
| 400 | Provided 'affiliations' value is invalid. | `ErrorResponse` |
| 403 | Not allowed to perform this affiliation change. | `ErrorResponse` |
| 404 | The chat room (or its service) can not be found or is not accessible. | `ErrorResponse` |

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
| 400 | Provided 'affiliations' value is invalid. | `ErrorResponse` |
| 403 | Not allowed to remove this affiliation. | `ErrorResponse` |
| 404 | The chat room (or its service) can not be found or is not accessible. | `ErrorResponse` |
| 409 | Applying this affiliation change would cause a room conflict. | `ErrorResponse` |

# Client Sessions

Managing live client sessions.

## Get all sessions

> **GET** /plugins/restapi/v1/sessions

Retrieve all live client sessions.

**Responses**

| Status | Description | Response body |
|--------|-------------|---------------|
| 200 | The client sessions currently active in Openfire. | `SessionEntities` (XML or JSON) |

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
| 200 | The client sessions for one particular user that are currently active in Openfire. | `SessionEntities` (XML or JSON) |

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

**Request body** (required): `MessageEntity` (XML or JSON) - The message that is to be broadcast.

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
| 200 | A message count. | `MsgArchiveEntity` (XML or JSON) |

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
| 200 | The requested log entries. | `SecurityAuditLogs` (XML or JSON) |
| 403 | The audit log is not readable (configured to be write-only). | `ErrorResponse` (XML or JSON) |

# Statistics

Inspecting Openfire statistics.

## Get client session counts

> **GET** /plugins/restapi/v1/system/statistics/sessions

Retrieve statistics on the number of client sessions.

**Responses**

| Status | Description | Response body |
|--------|-------------|---------------|
| 200 | The requested statistics. | `SessionsCount` (XML or JSON) |

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
| 200 | The system properties. | `SystemProperties` (XML or JSON) |

## Create system property

> **POST** /plugins/restapi/v1/system/properties

Create a new Openfire system property. Will overwrite a pre-existing system property that uses the same name.

**Request body** (required): `SystemProperty` (XML or JSON) - The system property to create.

**Responses**

| Status | Description | Response body |
|--------|-------------|---------------|
| 201 | The system property is created. |  |
| 400 | No system property was provided, the system property has no value, or its name is not valid. The name must consist of one or more dot-separated parts, each consisting of ASCII letters, digits, underscores, apostrophes and hyphens. | `ErrorResponse` |
| 403 | Prohibited to create this system property. | `ErrorResponse` |
| 409 | The name of the system property differs only in case from the name of an existing system property. | `ErrorResponse` |

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
| 200 | The requested system property. | `SystemProperty` (XML or JSON) |
| 403 | Reading this system property is prohibited. | `ErrorResponse` (XML or JSON) |
| 404 | The system property could not be found. | `ErrorResponse` (XML or JSON) |

## Update system property

> **PUT** /plugins/restapi/v1/system/properties/{propertyKey}

Updates an existing Openfire system property.

**Parameters**

| Name | Located in | Required | Description | Default value |
|------|------------|----------|-------------|---------------|
| propertyKey | path | yes | The name of the system property to update. Example: `foo.bar.xyz` |  |

**Request body** (required): `SystemProperty` (XML or JSON) - The new system property definition that replaces an existing definition.

**Responses**

| Status | Description | Response body |
|--------|-------------|---------------|
| 200 | The system property is updated. |  |
| 400 | No system property was provided, the system property has no value, or it does not match the name in the URL. | `ErrorResponse` |
| 403 | Prohibited to update this system property. | `ErrorResponse` |
| 404 | The system property could not be found. | `ErrorResponse` |
| 409 | The name of the system property differs only in case from the name of another existing system property. | `ErrorResponse` |

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
| 400 | The name of the system property is not valid. It must consist of one or more dot-separated parts, each consisting of ASCII letters, digits, underscores, apostrophes and hyphens. | `ErrorResponse` |
| 403 | Prohibited to delete this system property, or one of its child properties. | `ErrorResponse` |
| 404 | The system property could not be found. | `ErrorResponse` |
| 409 | Deleting this system property could also delete unintended properties (other than this property and its child properties). This can happen, for example, when its name contains an underscore, which can match any character. | `ErrorResponse` |

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
| 200 | All cluster nodes. | `ClusterNodeEntities` (XML or JSON) |

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
| 200 | The cluster node. | `ClusterNodeEntity` (XML or JSON) |
| 404 | The provided NodeID does not identify an existing cluster node. | `ErrorResponse` (XML or JSON) |

## Get clustering status

> **GET** /plugins/restapi/v1/clustering/status

Describes the point-in-time state of Openfire's clustering with other servers. The status is one of: 'SENIOR AND ONLY MEMBER', 'Senior member', 'Junior member', 'Starting up' or 'Disabled'.

**Responses**

| Status | Description | Response body |
|--------|-------------|---------------|
| 200 | Status returned. | `ClusteringEntity` (XML or JSON) |

<!-- END GENERATED ENDPOINTS -->

# Data format
Openfire REST API provides XML and JSON as data format. The default data format is XML.
To get a JSON result, please add "**Accept: application/json**" to the request header.
If you want to create a resource with JSON data format, please add "**Content-Type: application/json**".

## Data types

### ClusterNode

| Parameter    | Optional | Description                                                                         |
|--------------|----------|-------------------------------------------------------------------------------------|
| hostName     | No       | The hostname and IP address of the server on which this cluster node is running.    |
| nodeID       | No       | A unique identifier of this cluster node.                                           |
| joinedTime   | No       | Timestamp when the node joined the cluster.                                         |
| seniorMember | No       | Boolean value indicating if the node is currently the senior member of the cluster. |

### User

| Parameter  | Optional | Description                                                                              |
|------------|----------|------------------------------------------------------------------------------------------|
| username   | No       | The username of the user                                                                 |
| name       | Yes      | The name of the user                                                                     |
| email      | Yes      | The email of the user                                                                    |
| password   | No       | The password of the user                                                                 |
| properties | Yes      | List of properties. Property is a key / value object. The key must to be per user unique |

### RosterItem
| Parameter        | Optional | Description                                                                                               |
|------------------|----------|-----------------------------------------------------------------------------------------------------------|
| jid              | No       | The JID of the roster item                                                                                |
| nickname         | Yes      | The nickname for the user when used in this roster                                                        |
| subscriptionType | Yes      | The subscription type <br> Possible numeric values are: -1 (remove), 0 (none), 1 (to), 2 (from), 3 (both) |
| groups           | No       | A list of groups to organize roster entries under (e.g. friends, co-workers, etc.)                        |

### Chatroom

| Parameter                 | Optional | Description                                                                                                                                                                                                                                                                                                                                                      |
|---------------------------|----------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| roomName                  | No       | The name/id of the room. Can only contains lowercase and alphanumeric characters.                                                                                                                                                                                                                                                                                |
| naturalName               | No       | Also the name of the room, but can contains non alphanumeric characters. It's mainly used for users while discovering rooms hosted by the Multi-User Chat service.                                                                                                                                                                                               |
| description               | No       | Description text of the room.                                                                                                                                                                                                                                                                                                                                    |
| subject                   | Yes      | Subject of the room.                                                                                                                                                                                                                                                                                                                                             |
| password                  | Yes      | The password that the user must provide to enter the room                                                                                                                                                                                                                                                                                                        |
| creationDate              | Yes      | The date when the room was created. Will be automatically set by creation. Example: 2014-07-10T09:49:12.411+02:00                                                                                                                                                                                                                                                |
| modificationDate          | Yes      | The last date when the room's configuration was modified. If the room's configuration  was never modified then the initial value will be the same as the creation date. Will be automatically set by update. Example: 2014-07-10T09:49:12.411+02:00                                                                                                              |
| maxUsers                  | Yes      | the maximum number of occupants that can be simultaneously in the room. 0 means unlimited number of occupants.                                                                                                                                                                                                                                                   |
| persistent                | Yes      | Can be "true" or "false". Persistent rooms are saved to the database to make their configurations persistent together with the affiliation of the users. Otherwise the room will be destroyed if the last occupant leave the room.                                                                                                                               |
| publicRoom                | Yes      | Can be "true" or "false". True if the room is searchable and visible through service discovery.                                                                                                                                                                                                                                                                  |
| registrationEnabled       | Yes      | Can be "true" or "false". True if users are allowed to register with the room. By default, room registration is enabled.                                                                                                                                                                                                                                         |
| canAnyoneDiscoverJID      | Yes      | Can be "true" or "false". True if every presence packet will include the JID of every occupant.                                                                                                                                                                                                                                                                  |
| canOccupantsChangeSubject | Yes      | Can be "true" or "false". True if participants are allowed to change the room's subject.                                                                                                                                                                                                                                                                         |
| canOccupantsInvite        | Yes      | Can be "true" or "false". True if occupants can invite other users to the room. If the room does not require an invitation to enter (i.e. is not members-only) then any occupant can send invitations. On the other hand, if the room is members-only and occupants cannot send invitation then only the room owners and admins are allowed to send invitations. |
| canChangeNickname         | Yes      | Can be "true" or "false". True if room occupants are allowed to change their nicknames in the room. By default, occupants are allowed to change their nicknames.                                                                                                                                                                                                 |
| logEnabled                | Yes      | Can be "true" or "false". True if the room's conversation is being logged. If logging is activated the room conversation will be saved to the database every couple of minutes. The saving frequency is the same for all the rooms and can be configured by changing the property "xmpp.muc.tasks.log.timeout".                                                  |
| loginRestrictedToNickname | Yes      | Can be "true" or "false". True if registered users can only join the room using their registered nickname. By default, registered users can join the room using any nickname.                                                                                                                                                                                    |
| membersOnly               | Yes      | Can be "true" or "false". True if the room requires an invitation to enter. That is if the room is members-only.                                                                                                                                                                                                                                                 |
| moderated                 | Yes      | Can be "true" or "false". True if the room in which only those with "voice" may send messages to all occupants.                                                                                                                                                                                                                                                  |
| allowPM                   | Yes      | One of "anyone", "participants", "moderators" or "none". Controls who is allowed to send private messages to other occupants in the room.                                                                                                                                                                                                                        |
| broadcastPresenceRoles    | Yes      | The list of roles of which presence will be broadcasted to the rest of the occupants.                                                                                                                                                                                                                                                                            |
| owners                    | Yes      | A collection with the current list of owners. The collection contains the bareJID of the users with owner affiliation.                                                                                                                                                                                                                                           |
| admins                    | Yes      | A collection with the current list of admins. The collection contains the bareJID of the users with admin affiliation.                                                                                                                                                                                                                                           |
| members                   | Yes      | A collection with the current list of room members. The collection contains the bareJID of the users with member affiliation. If the room is not members-only then the list  will contain the users that registered with the room and therefore they may have reserved a nickname.                                                                               |
| outcasts                  | Yes      | A collection with the current list of outcast users. An outcast user is not allowed to join the room again. The collection contains the bareJID of the users with outcast affiliation.                                                                                                                                                                           |
| ownerGroups               | Yes      | A collection with the current list of groups with owner affiliation. The collection contains the name only.                                                                                                                                                                                                                                                      |
| adminGroups               | Yes      | A collection with the current list of groups with admin affiliation. The collection contains the name only.                                                                                                                                                                                                                                                      |
| memberGroups              | Yes      | A collection with the current list of groups with member affiliation. The collection contains the name only.                                                                                                                                                                                                                                                     |
| outcastGroups             | Yes      | A collection with the current list of groups with outcast affiliation. The collection contains the name only.                                                                                                                                                                                                                                                    |

### Group

| Parameter   | Optional | Description                                    |
|-------------|----------|------------------------------------------------|
| name        | No       | The name of the group                          |
| description | No       | The description of the group                   |
| admins      | Yes      | A collection with current admins of the group  |
| members     | Yes      | A collection with current members of the group |

### System Property

| Parameter | Optional | Description                      |
|-----------|----------|----------------------------------|
| key       | No       | The name of the system property  |
| value     | No       | The value of the system property |

### Session
| Parameter      | Optional | Description                                                                                     |
|----------------|----------|-------------------------------------------------------------------------------------------------|
| sessionId      | No       | Full JID of a user e.g. (testUser@testserver.de/SomeRessource)                                  |
| username       | No       | The username associated with this session. Can be also "Anonymous".                             |
| resource       | Yes      | Resource name                                                                                   |
| node           | No       | Can be "Local" or "Remote"                                                                      |
| sessionStatus  | No       | The current status of this session. Can be "Closed", "Connected", "Authenticated" or "Unknown". |
| presenceStatus | No       | The status of this presence packet, a natural-language description of availability status.      |
| priority       | No       | The priority of the session. The valid priority range is -128 through 128.                      |
| hostAddress    | No       | The IP address string in textual presentation.                                                  |
| hostName       | No       | The host name for this IP address.                                                              |
| creationDate   | No       | The date the session was created.                                                               |
| lastActionDate | No       | The time the session last had activity.                                                         |
| secure         | No       | Is "true" if this connection is secure.                                                         |

### Sessions count
| Parameter       | Optional | Description                                                                                                                              |
|-----------------|----------|------------------------------------------------------------------------------------------------------------------------------------------|
| clusterSessions | No       | Number of client sessions that are authenticated with the server. This includes anonymous and non-anoymous users from the whole cluster. |
| localSessions   | No       | Number of client sessions that are authenticated with the server. This includes anonymous and non-anoymous users.                        |

### Security Audit Logs
| Parameter | Optional | Description                                                         |
|-----------|----------|---------------------------------------------------------------------|
| logId     | No       | Unique ID of this log                                               |
| username  | No       | The username of the user who performed this event                   |
| timestamp | No       | The time stamp of when this event occurred                          |
| summary   | No       | The summary, or short description of what transpired in the event   |
| node      | No       | The node that triggered the event, usually a hostname or IP address |
| details   | No       | Detailed information about what occurred in the event               |

### Occupants
| Parameter   | Optional | Description             |
|-------------|----------|-------------------------|
| jid         | No       | The JID of the MUC room |
| userAddress | No       | The JID of the user     |
| role        | No       | Role of the user        |
| affiliation | No       | Affiliation of the user |
