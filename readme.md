
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

# User related REST Endpoints

## Retrieve users
Endpoint to get all or filtered users
> **GET** /users

**Payload:** none

**Return value:** Users

### Possible parameters

| Parameter     | Parameter Type | Description                                                                                                  | Default value |
|---------------|----------------|--------------------------------------------------------------------------------------------------------------|---------------|
| search        | @QueryParam    | Search/Filter by username. <br> This act like the wildcard search %String%                                   |               |
| propertyKey   | @QueryParam    | Filter by user propertyKey.                                                                                  |               |
| propertyValue | @QueryParam    | Filter by user propertyKey and propertyValue. <br>**Note:** It can only be used within propertyKey parameter |               |

### Examples

>**Header**: Authorization: Basic YWRtaW46MTIzNDU=

>**GET** http://example.org:9090/plugins/restapi/v1/users

>**GET** http://example.org:9090/plugins/restapi/v1/users?search=testuser

>**GET** http://example.org:9090/plugins/restapi/v1/users?propertyKey=keyname

>**GET** http://example.org:9090/plugins/restapi/v1/users?propertyKey=keyname&propertyValue=keyvalue

If you want to get a JSON format result, please add "**Accept: application/json**" to the **Header**.

## Retrieve a user 
Endpoint to get information over a specific user
> **GET** /users/{username}

**Payload:** none

**Return value:** User

### Possible parameters

| Parameter | Parameter Type | Description      | Default value |
|-----------|----------------|------------------|---------------|
| username	 | 	@Path         | 	Exact username	 |               |

### Examples

>**Header:** Authorization: Basic YWRtaW46MTIzNDU=
>
>**GET** http://example.org:9090/plugins/restapi/v1/users/testuser

## Create a user
Endpoint to create a new user
> **POST** /users

**Payload:** User
**Return value:** HTTP status 201 (Created)

### Examples
#### XML Examples


>**Header:** Authorization: Basic YWRtaW46MTIzNDU=
>
>**Header:** Content-Type: application/**xml**
>
>**POST** http://example.org:9090/plugins/restapi/v1/users

**Payload Example 1 (required parameters):**

```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<user>
    <username>test3</username>
    <password>p4ssword</password>
</user>
```

**Payload Example 2 (available parameters):**
```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<user>
    <username>testuser</username>
    <password>p4ssword</password>
    <name>Test User</name>
    <email>test@localhost.de</email>
    <properties>
        <property key="keyname" value="value"/>
        <property key="anotherkey" value="value"/>
    </properties>
</user>
```
#### JSON Examples
>**Header:** Authorization: Basic YWRtaW46MTIzNDU=
> 
>**Header:** Content-Type: application/**json**
> 
>**POST** http://example.org:9090/plugins/restapi/v1/users

**Payload Example 1 (required parameters):**
```json
{
    "username": "admin",
    "password": "p4ssword"
}
```

**Payload Example 2 (available parameters):**
```json
{
    "username": "admin",
    "password": "p4ssword",
    "name": "Administrator",
    "email": "admin@example.com",
    "properties": {
        "property": [
            {
                "@key": "console.rows_per_page",
                "@value": "user-summary=8"
            },
            {
                "@key": "console.order",
                "@value": "session-summary=1"
            }
        ]
    }
}
```

**REST API Version 1.3.0 and later - Payload Example 3 (available parameters):**
```json
{
    "users": [
        {
            "username": "admin",
            "name": "Administrator",
            "email": "admin@example.com",
            "password": "p4ssword",
            "properties": [
                {
                    "key": "console.order",
                    "value": "session-summary=0"
                }
            ]
        },
        {
            "username": "test",
            "name": "Test",
            "password": "p4ssword"
        }
    ]
}
```

## Delete a user
Endpoint to delete a user
> **DELETE** /users/{username}

**Payload:** none

**Return value:** HTTP status 200 (OK)

### Possible parameters

| Parameter | 	Parameter Type | Description    | Default value |
|-----------|-----------------|----------------|---------------|
| username  | @Path 	         | Exact username |               |

### Examples

>**Header:** Authorization: Basic YWRtaW46MTIzNDU=
> 
>**DELETE** http://example.org:9090/plugins/restapi/v1/users/testuser

## Update a user
Endpoint to update / rename a user
> **PUT** /users/{username}

**Payload:** User

**Return value:** HTTP status 200 (OK)

### Possible parameters

| Parameter | 	Parameter Type | Description    | Default value |
|-----------|-----------------|----------------|---------------|
| username  | 	@Path	         | Exact username |               |

### Examples
#### XML Example
>**Header:** Authorization: Basic YWRtaW46MTIzNDU=
> 
>**Header:** Content-Type application/xml
> 
>**PUT** http://example.org:9090/plugins/restapi/v1/users/testuser

**Payload:**
```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<user>
    <username>testuser</username>
    <name>Test User edit</name>
    <email>test@edit.de</email>
    <properties>
        <property key="keyname" value="value"/>
    </properties>
</user>
```
#### Rename Example

>**Header:** Authorization: Basic YWRtaW46MTIzNDU=
> 
>**Header:** Content-Type application/xml
> 
>**PUT** http://example.org:9090/plugins/restapi/v1/users/oldUsername

**Payload:**
```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<user>
    <username>newUsername</username>
    <name>Test User edit</name>
    <email>test@edit.de</email>
    <properties>
        <property key="keyname" value="value"/>
    </properties>
</user>
```

#### JSON Example
>**Header:** Authorization: Basic YWRtaW46MTIzNDU=
> 
>**Header:** Content-Type application/json
> 
>**PUT** http://example.org:9090/plugins/restapi/v1/users/testuser

**Payload:**
```json
{
    "username": "testuser",
    "name": "Test User edit",
    "email": "test@edit.de",
    "properties": {
        "property": {
            "@key": "keyname",
            "@value": "value"
        }
    }
}
```

**REST API Version 1.3.0 and later - Payload Example 2 (available parameters):**
```json
{
    "username": "testuser",
    "name": "Test User edit",
    "email": "test@edit.de",
    "properties": [
        {
            "key": "keyname",
            "value": "value"
        }
    ]
}
```

## Retrieve all user groups 
Endpoint to get group names of a specific user
> **GET** /users/{username}/groups

**Payload:** none

**Return value:** Groups

### Possible parameters

| Parameter | 	Parameter Type | Description    | Default value |
|-----------|-----------------|----------------|---------------|
| username  | 	@Path	         | Exact username |               |

### Examples
>**Header:** Authorization: Basic YWRtaW46MTIzNDU=
> 
>**GET** http://example.org:9090/plugins/restapi/v1/users/testuser/groups

## Add user to groups
Endpoint to add user to a groups
> **POST** /users/{username}/groups

**Payload:** Groups

**Return value:** HTTP status 201 (Created)

### Possible parameters


| Parameter | 	Parameter Type | Description    | Default value |
|-----------|-----------------|----------------|---------------|
| username  | 	@Path	         | Exact username |               |

### Examples
>**Header:** Authorization: Basic YWRtaW46MTIzNDU=
> 
>**Header:** Content-Type application/xml
> 
>**POST** http://example.org:9090/plugins/restapi/v1/users/testuser/groups

**Payload:**
```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<groups>
    <groupname>Admins</groupname>
    <groupname>Support</groupname>
</groups>
```

## Add user to group
Endpoint to add user to a group
> **POST** /users/{username}/groups/{groupName}

**Payload:** none

**Return value:** HTTP status 201 (Created)

### Possible parameters

| Parameter | 	Parameter Type | Description      | Default value |
|-----------|-----------------|------------------|---------------|
| username  | 	@Path	         | Exact username   |               |
| groupName | 	@Path	         | Exact group name |               |

### Examples
>**Header:** Authorization: Basic YWRtaW46MTIzNDU=
> 
>**Header:** Content-Type application/xml
> 
>**POST** http://example.org:9090/plugins/restapi/v1/users/testuser/groups/testGroup

## Delete a user from a groups
Endpoint to remove a user from a groups
>**DELETE** /users/{username}/groups

**Payload:** Groups

**Return value:** HTTP status 200 (OK)

### Possible parameters

| Parameter | 	Parameter Type | Description    | Default value |
|-----------|-----------------|----------------|---------------|
| username  | 	@Path	         | Exact username |               |

### Examples
>**Header:** Authorization: Basic YWRtaW46MTIzNDU=
> 
>**Header:** Content-Type application/xml
> 
>**DELETE** http://example.org:9090/plugins/restapi/v1/users/testuser/groups

**Payload:**
```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<groups>
    <groupname>Admins</groupname>
    <groupname>Support</groupname>
</groups>
```

## Delete a user from a group
Endpoint to remove a user from a group
>**DELETE** /users/{username}/groups/{groupName}

**Payload:** none

**Return value:** HTTP status 200 (OK)

### Possible parameters


| Parameter | 	Parameter Type | Description      | Default value |
|-----------|-----------------|------------------|---------------|
| username  | 	@Path	         | Exact username   |               |
| groupName | 	@Path	         | Exact group name |               |

### Examples
>**Header:** Authorization: Basic YWRtaW46MTIzNDU=
> 
>**Header:** Content-Type application/xml
> 
>**DELETE** http://example.org:9090/plugins/restapi/v1/users/testuser/groups/testGroup

## Lockout a user
Endpoint to lockout / ban the user from the chat server. The user will be kicked if the user is online.
>**POST** /lockouts/{username}

**Payload:** none

**Return value:** HTTP status 201 (Created)

### Possible parameters

| Parameter | 	Parameter Type | Description    | Default value |
|-----------|-----------------|----------------|---------------|
| username  | 	@Path	         | Exact username |               |

### Examples
>**Header:** Authorization: Basic YWRtaW46MTIzNDU=
> 
>**POST** http://example.org:9090/plugins/restapi/v1/lockouts/testuser

## Unlock a user 
Endpoint to unlock / unban the user
>**DELETE** /lockouts/{username}

**Payload:** none

**Return value:** HTTP status 200 (OK)

### Possible parameters

| Parameter | 	Parameter Type | Description    | Default value |
|-----------|-----------------|----------------|---------------|
| username  | 	@Path	         | Exact username |               |

### Examples
>**Header:** Authorization: Basic YWRtaW46MTIzNDU=
> 
>**DELETE** http://example.org:9090/plugins/restapi/v1/lockouts/testuser

## Retrieve user roster 
Endpoint to get roster entries (buddies) from a specific user
>**GET** /users/{username}/roster

**Payload:** none

**Return value:** Roster

### Possible parameters

| Parameter | 	Parameter Type | Description    | Default value |
|-----------|-----------------|----------------|---------------|
| username  | 	@Path	         | Exact username |               |

### Examples
>**Header:** Authorization: Basic YWRtaW46MTIzNDU=
> 
>**GET** http://example.org:9090/plugins/restapi/v1/users/testuser/roster

## Create a user roster entry 
Endpoint to add a new roster entry to a user
>**POST** /users/{username}/roster

**Payload:** RosterItem

**Return value:** HTTP status 201 (Created)

### Possible parameters

| Parameter | 	Parameter Type | Description    | Default value |
|-----------|-----------------|----------------|---------------|
| username  | 	@Path	         | Exact username |               |

### Examples
>**Header:** Authorization: Basic YWRtaW46MTIzNDU=
> 
>**Header:** Content-Type application/xml
> 
>**POST** http://example.org:9090/plugins/restapi/v1/users/testuser/roster

**Payload:**
Payload Example 1 (required parameters):
```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<rosterItem>
	<jid>peter@pan.de</jid>
</rosterItem>
```
Payload Example 2 (available parameters):
```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<rosterItem>
	<jid>peter@pan1.de</jid>
	<nickname>Peter1</nickname>
	<subscriptionType>3</subscriptionType>
	<groups>
		<group>Friends</group>
	</groups>
</rosterItem>
```

## Delete a user roster entry 
Endpoint to remove a roster entry from a user
>**DELETE** /users/{username}/roster/{jid}

**Payload:** none

**Return value:** HTTP status 200 (OK)

### Possible parameters

| Parameter | 	Parameter Type | Description            | Default value |
|-----------|-----------------|------------------------|---------------|
| username  | 	@Path	         | Exact username         |               |
| jid       | 	@Path	         | JID of the roster item |               |

### Examples
>**Header:** Authorization: Basic YWRtaW46MTIzNDU=
> 
>**DELETE** http://example.org:9090/plugins/restapi/v1/users/testuser/roster/peter@pan.de

## Update a user roster entry  
Endpoint to update a roster entry
>**PUT** /users/{username}/roster/{jid}

**Payload:** RosterItem

**Return value:** HTTP status 200 (OK)

### Possible parameters

| Parameter | 	Parameter Type | Description            | Default value |
|-----------|-----------------|------------------------|---------------|
| username  | 	@Path	         | Exact username         |               |
| jid       | 	@Path	         | JID of the roster item |               |

### Examples
>**Header:** Authorization: Basic YWRtaW46MTIzNDU=
> 
>**Header:** Content-Type application/xml
> 
>**PUT** http://example.org:9090/plugins/restapi/v1/users/testuser/roster/peter@pan.de

**Payload:**
```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<rosterItem>
	<jid>peter@pan.de</jid>
	<nickname>Peter Pan</nickname>
	<subscriptionType>0</subscriptionType>
	<groups>
		<group>Support</group>
	</groups>
</rosterItem>
```

## Retrieve user's vcard
Endpoint to get the vCard of a particular user
> **GET** /users/{username}/vcard

**Payload:** none

**Return value:** vCard XML data

### Possible parameters

| Parameter | 	Parameter Type | Description    | Default value |
|-----------|-----------------|----------------|---------------|
| username  | 	@Path	         | Exact username |               |

### Examples
>**Header:** Authorization: Basic YWRtaW46MTIzNDU=
>
>**GET** http://example.org:9090/plugins/restapi/v1/users/testuser/vcard

## Add or update user's vCard
Endpoint to add or replace a vCard of a particular user.
> **PUT** /users/{username}/vcard

**Payload:** vCard XML data

**Return value:** HTTP status 200 (Created)

### Possible parameters


| Parameter | 	Parameter Type | Description    | Default value |
|-----------|-----------------|----------------|---------------|
| username  | 	@Path	         | Exact username |               |

### Examples
>**Header:** Authorization: Basic YWRtaW46MTIzNDU=
>
>**Header:** Content-Type application/xml
>
>**POST** http://example.org:9090/plugins/restapi/v1/users/testuser/vcard

**Payload:**
```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<vCard xmlns="vcard-temp">
    <N>
        <FAMILY>Doe</FAMILY>
        <GIVEN>Janice</GIVEN>
        <MIDDLE>Francis</MIDDLE>
    </N>
    <ORG>
        <ORGNAME/>
        <ORGUNIT/>
    </ORG>
    <NICKNAME>Jane</NICKNAME>
    <FN>Janice Francis Doe</FN>
    <TITLE/>
    <URL/>
    <EMAIL>
        <HOME/>
        <INTERNET/>
        <PREF/>
        <USERID>j.doe@example.org</USERID>
    </EMAIL>
    <TEL>
        <WORK/>
        <VOICE/>
        <NUMBER/>
    </TEL>
    <TEL>
        <WORK/>
        <PAGER/>
        <NUMBER/>
    </TEL>
    <TEL>
        <WORK/>
        <FAX/>
        <NUMBER/>
    </TEL>
    <TEL>
        <WORK/>
        <CELL/>
        <NUMBER/>
    </TEL>
    <TEL>
        <HOME/>
        <VOICE/>
        <NUMBER/>
    </TEL>
    <TEL>
        <HOME/>
        <PAGER/>
        <NUMBER/>
    </TEL>
    <TEL>
        <HOME/>
        <FAX/>
        <NUMBER/>
    </TEL>
    <TEL>
        <HOME/>
        <CELL/>
        <NUMBER/>
    </TEL>
    <ADR>
        <WORK/>
        <LOCALITY/>
        <CTRY/>
        <STREET/>
        <PCODE/>
        <REGION/>
    </ADR>
    <ADR>
        <HOME/>
        <LOCALITY/>
        <CTRY/>
        <STREET/>
        <PCODE/>
        <REGION/>
    </ADR>
</vCard>
```

## Delete user's vcard
Endpoint to remove the vCard of a particular user
> **DELETE** /users/{username}/vcard

**Payload:** none

**Return value:** none

### Possible parameters

| Parameter | 	Parameter Type | Description    | Default value |
|-----------|-----------------|----------------|---------------|
| username  | 	@Path	         | Exact username |               |

### Examples
>**Header:** Authorization: Basic YWRtaW46MTIzNDU=
>
>**DELETE** http://example.org:9090/plugins/restapi/v1/users/testuser/vcard

# Chat room related REST Endpoints

## Retrieve all chat services

Endpoint to get all chat services
>**GET** /chatservices

**Payload:** none

**Return value:** Chat services

**Possible parameters:** none

### Examples

>**Header**: Authorization: Basic YWRtaW46MTIzNDU=
>
>**GET** http://example.org:9090/plugins/restapi/v1/chatservices

## Create a chat service
Endpoint to create a new chat service.
>**POST** /chatservices

**Payload:** Chatservice

**Return value:** HTTP status 201 (Created)

**Possible parameters:** none

### XML Examples

>**Header:** Authorization: Basic YWRtaW46MTIzNDU=
>
>**Header:** Content-Type: application/xml
>
>**POST** http://example.org:9090/plugins/restapi/v1/chatservices

**Payload Example (available parameters):**
```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<chatService>
    <serviceName>new-chat-service-name</serviceName>
    <description>A mightily fine service</description>
    <hidden>false</hidden>
</chatService>
```

### JSON Examples

>**Header:** Authorization: Basic YWRtaW46MTIzNDU=
>
>**Header:** Content-Type: application/json
>
>**POST** http://example.org:9090/plugins/restapi/v1/chatservices

**Payload Example (available parameters):**
```json
{
	"serviceName": "new-chat-service-name",
	"description": "A mightily fine service",
	"hidden": false
}
```

## Retrieve all chat rooms 
Endpoint to get all chat rooms
>**GET** /chatrooms

**Payload:** none

**Return value:** Chatrooms

### Possible parameters

| Parameter    | Parameter Type | Description                                                                   | Default value |
|--------------|----------------|-------------------------------------------------------------------------------|---------------|
| servicename	 | @QueryParam	   | The name of the Group Chat Service                                            | conference    |
| type         | @QueryParam    | **public:** Only as List Room in Directory set rooms <br> **all:** All rooms. | public        |
| search       | @QueryParam    | Search/Filter by room name. <br> This act like the wildcard search %String%   |               |

### Examples

>**Header**: Authorization: Basic YWRtaW46MTIzNDU=
> 
>**GET** http://example.org:9090/plugins/restapi/v1/chatrooms
> 
>**GET** http://example.org:9090/plugins/restapi/v1/chatrooms?type=all
> 
>**GET** http://example.org:9090/plugins/restapi/v1/chatrooms?type=all&servicename=privateconf
> 
>**GET** http://example.org:9090/plugins/restapi/v1/chatrooms?search=test

## Retrieve a chat room
Endpoint to get information over specific chat room
>**GET** /chatrooms<span>/{roomName}

**Payload:** none

**Return value:** Chatroom

### Possible parameters

| Parameter   | Parameter Type | Description                        | Default value |
|-------------|----------------|------------------------------------|---------------|
| roomname    | 	@Path         | 	Exact room name	                  |               |
| servicename | 	@QueryParam	  | The name of the Group Chat Service | 	conference   |

### Examples

>**Header:** Authorization: Basic YWRtaW46MTIzNDU=
> 
>**GET** http://example.org:9090/plugins/restapi/v1/chatrooms/test
> 
>**GET** http://example.org:9090/plugins/restapi/v1/chatrooms/test?servicename=privateconf

## Retrieve chat room participants 
Endpoint to get all participants with a role of specified room.
>**GET** /chatrooms/{roomName}/participants

**Payload:** none

**Return value:** Participants

### Possible parameters

| Parameter   | Parameter Type	 | Description	                       | Default value |
|-------------|-----------------|------------------------------------|---------------|
| roomname    | @Path           | Exact room name                    |               |
| servicename | @QueryParam     | The name of the Group Chat Service | conference    |

### Examples

>**Header:** Authorization: Basic YWRtaW46MTIzNDU=
> 
>**GET** http://example.org:9090/plugins/restapi/v1/chatrooms/room1/participants

## Retrieve chat room occupants
Endpoint to get all occupants (all roles / affiliations) of a specified room.
>**GET** /chatrooms/{roomName}/occupants

**Payload:** none

**Return value:** Occupants

### Possible parameters

| Parameter   | Parameter Type	 | Description	                       | Default value |
|-------------|-----------------|------------------------------------|---------------|
| roomname    | @Path           | Exact room name                    |               |
| servicename | @QueryParam     | The name of the Group Chat Service | conference    |

### Examples

>**Header:** Authorization: Basic YWRtaW46MTIzNDU=
> 
>**GET** http://example.org:9090/plugins/restapi/v1/chatrooms/room1/occupants

## Retrieve chat room message history
Endpoint to get the chat message history of a specified room.

>**GET** /chatrooms/{roomName}/chathistory

**Payload:** none  

**Return value:** Chat History

### Possible parameters

| Parameter   | Parameter Type	 | Description	                       | Default value |
|-------------|-----------------|------------------------------------|---------------|
| roomname    | @Path           | Exact room name                    |               |
| servicename | @QueryParam     | The name of the Group Chat Service | conference    |

## Create a chat room
Endpoint to create a new chat room.
>**POST** /chatrooms

**Payload:** Chatroom

**Return value:** HTTP status 201 (Created)

### Possible parameters

| Parameter       | Parameter Type	 | Description                                     | Default value |
|-----------------|-----------------|-------------------------------------------------|---------------|
| servicename     | @QueryParam     | 	The name of the Group Chat Service             | conference    |
| sendInvitations | @QueryParam     | Whether to send invitations to affiliated users | false         |

### XML Examples

>**Header:** Authorization: Basic YWRtaW46MTIzNDU=
> 
>**Header:** Content-Type: application/xml
> 
>**POST** http://example.org:9090/plugins/restapi/v1/chatrooms

**Payload Example 1 (required parameters):**
```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<chatRoom>
    <naturalName>global-1</naturalName>
    <roomName>global</roomName>
    <description>Global Chat Room</description>
</chatRoom>
```

**Payload Example 2 (available parameters):**
```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<chatRoom>
    <roomName>global</roomName>
    <naturalName>global-2</naturalName>
    <description>Global Chat Room</description>
    <subject>global-2 Subject</subject>
    <creationDate>2014-02-12T15:52:37.592+01:00</creationDate>
    <modificationDate>2014-09-12T15:35:54.702+02:00</modificationDate>
    <maxUsers>0</maxUsers>
    <persistent>true</persistent>
    <publicRoom>true</publicRoom>
    <registrationEnabled>false</registrationEnabled>
    <canAnyoneDiscoverJID>false</canAnyoneDiscoverJID>
    <canOccupantsChangeSubject>false</canOccupantsChangeSubject>
    <canOccupantsInvite>false</canOccupantsInvite>
    <canChangeNickname>false</canChangeNickname>
    <logEnabled>true</logEnabled>
    <loginRestrictedToNickname>false</loginRestrictedToNickname>
    <membersOnly>false</membersOnly>
    <moderated>false</moderated>
    <allowPM>anyone</allowPM>
    <broadcastPresenceRoles>
        <broadcastPresenceRole>moderator</broadcastPresenceRole>
        <broadcastPresenceRole>participant</broadcastPresenceRole>
        <broadcastPresenceRole>visitor</broadcastPresenceRole>
    </broadcastPresenceRoles>
    <owners>
        <owner>owner@localhost</owner>
    </owners>
    <admins>
        <admin>admin@localhost</admin>
    </admins>
    <members>
        <member>member2@localhost</member>
        <member>member1@localhost</member>
    </members>
    <outcasts>
        <outcast>outcast1@localhost</outcast>
    </outcasts>
</chatRoom>
```

### JSON Examples

>**Header:** Authorization: Basic YWRtaW46MTIzNDU=
> 
>**Header:** Content-Type: application/json
> 
>**POST** http://example.org:9090/plugins/restapi/v1/chatrooms

**Payload Example 1 (required parameters):**
```json
{
	"roomName": "global",
	"naturalName": "global-2",
	"description": "Global chat room"
}
```

**Payload Example 2 (available parameters):**
```json
{
    "roomName": "global-1",
    "naturalName": "global-1_test_hello",
    "description": "Global chat room",
    "subject": "Global chat room subject",
    "creationDate": "2012-10-18T16:55:12.803+02:00",
    "modificationDate": "2014-07-10T09:49:12.411+02:00",
    "maxUsers": "0",
    "persistent": "true",
    "publicRoom": "true",
    "registrationEnabled": "false",
    "canAnyoneDiscoverJID": "true",
    "canOccupantsChangeSubject": "false",
    "canOccupantsInvite": "false",
    "canChangeNickname": "false",
    "logEnabled": "true",
    "loginRestrictedToNickname": "true",
    "membersOnly": "false",
    "moderated": "false",
    "allowPM": "anyone",
    "broadcastPresenceRoles": {
        "broadcastPresenceRole": [
            "moderator",
            "participant",
            "visitor"
        ]
    },
    "owners": {
        "owner": "owner@localhost"
    },
    "admins": {
        "admin": [
            "admin@localhost",
            "admin2@localhost"
        ]
    },
    "members": {
        "member": [
            "member@localhost",
            "member2@localhost"
        ]
    },
    "outcasts": {
        "outcast": [
            "outcast@localhost",
            "outcast2@localhost"
        ]
    }
}
```

**REST API Version 1.3.0 and later - Payload Example 2 (available parameters):**
```json
{
    "roomName": "global-1",
    "naturalName": "global-1_test_hello",
    "description": "Global chat room",
    "subject": "Global chat room subject",
    "creationDate": "2012-10-18T16:55:12.803+02:00",
    "modificationDate": "2014-07-10T09:49:12.411+02:00",
    "maxUsers": "0",
    "persistent": "true",
    "publicRoom": "true",
    "registrationEnabled": "false",
    "canAnyoneDiscoverJID": "true",
    "canOccupantsChangeSubject": "false",
    "canOccupantsInvite": "false",
    "canChangeNickname": "false",
    "logEnabled": "true",
    "loginRestrictedToNickname": "true",
    "membersOnly": "false",
    "moderated": "false",
    "allowPM": "anyone",
    "broadcastPresenceRoles": [
        "moderator",
        "participant",
        "visitor"
    ],
    "owners": [
       "owner@localhost"
    ],
    "admins": [
       "admin@localhost"
    ],
    "members": [
        "member@localhost"
    ],
    "outcasts": [
        "outcast@localhost"
    ]
}
```







## Create multiple chat room
Endpoint to create multiple new chat rooms at once.
>**POST** /chatrooms/bulk

**Payload:** Chatrooms

**Return value:** Result list, ordered by successes and failures
```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<results>
    <success>
        <result>
            <roomName>room1</roomName>
            <resultType>Success</resultType>
            <message>Room was successfully created</message>
        </result>
        <result>
            <roomName>room2</roomName>
            <resultType>Success</resultType>
            <message>Room was successfully created</message>
        </result>
    </success>
    <failure/>
    <other/>
</results>
```

```json
{
    "success": [
        {
            "roomName": "room1",
            "resultType": "Success",
            "message": "Room was successfully created"
        },
        {
            "roomName": "room2",
            "resultType": "Success",
            "message": "Room was successfully created"
        }
    ],
    "failure": [],
    "other": []
}
```
### Possible parameters

| Parameter       | Parameter Type	 | Description                                           | Default value |
|-----------------|-----------------|-------------------------------------------------------|---------------|
| servicename     | @QueryParam     | 	The name of the Group Chat Service                   | conference    |
| sendInvitations | @QueryParam     | Whether to send invitations to newly affiliated users | false         |

### XML Examples

>**Header:** Authorization: Basic YWRtaW46MTIzNDU=
>
>**Header:** Content-Type: application/xml
>
>**POST** http://example.org:9090/plugins/restapi/v1/chatrooms/bulk

**Payload Example:**
```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<chatRooms>
    <chatRoom>
        <roomName>room1</roomName>
        <description>description1</description>
    </chatRoom>
    <chatRoom>
        <roomName>room2</roomName>
        <description>description1</description>
    </chatRoom>
</chatRooms>
```

For more examples, with more parameters, see the [create a chat room](#create-a-chat-room) endpoint.

### JSON Examples

>**Header:** Authorization: Basic YWRtaW46MTIzNDU=
>
>**Header:** Content-Type: application/json
>
>**POST** http://example.org:9090/plugins/restapi/v1/chatrooms

**Payload Example 1 (required parameters):**
```json
{
    "chatRooms": [
        { "roomName": "room1", "description": "description1" },
        { "roomName": "room2", "description": "description2" }
    ]
}
```

For more examples, with more parameters, see the [create a chat room](#create-a-chat-room) endpoint.









## Delete a chat room 
Endpoint to delete a chat room.
>**DELETE** /chatrooms/{roomName}

**Payload:** none

**Return value:** HTTP status 200 (OK)

### Possible parameters

| Parameter   | 	Parameter Type | Description                        | Default value |
|-------------|-----------------|------------------------------------|---------------|
| roomname    | @Path 	         | Exact room name                    |               |
| servicename | @QueryParam     | The name of the Group Chat Service | conference    |

### Examples

>**Header:** Authorization: Basic YWRtaW46MTIzNDU=
> 
>**DELETE** http://example.org:9090/plugins/restapi/v1/chatrooms/testroom
> 
>**DELETE** http://example.org:9090/plugins/restapi/v1/chatrooms/testroom?servicename=privateconf

## Update a chat room 
Endpoint to update a chat room.
>**PUT** /chatrooms/{roomName}

**Payload:** Chatroom

**Return value:** HTTP status 200 (OK)

### Possible parameters

| Parameter       | Parameter Type | Description                                           | Default value |
|-----------------|----------------|-------------------------------------------------------|---------------|
| roomname        | @Path          | Exact room name                                       |               |
| servicename     | @QueryParam    | The name of the Group Chat Service                    | conference    |
| sendInvitations | @QueryParam    | Whether to send invitations to newly affiliated users | false         |

### Examples
>**Header:** Authorization: Basic YWRtaW46MTIzNDU=
> 
>**Header:** Content-Type application/xml
> 
>**PUT** http://example.org:9090/plugins/restapi/v1/chatrooms/global

**Payload:**
```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<chatRoom>
    <roomName>global</roomName>
    <naturalName>global-2</naturalName>
    <description>Global Chat Room edit</description>
    <subject>New subject</subject>
    <password>test</password>
    <creationDate>2014-02-12T15:52:37.592+01:00</creationDate>
    <modificationDate>2014-09-12T14:20:56.286+02:00</modificationDate>
    <maxUsers>0</maxUsers>
    <persistent>true</persistent>
    <publicRoom>true</publicRoom>
    <registrationEnabled>false</registrationEnabled>
    <canAnyoneDiscoverJID>false</canAnyoneDiscoverJID>
    <canOccupantsChangeSubject>false</canOccupantsChangeSubject>
    <canOccupantsInvite>false</canOccupantsInvite>
    <canChangeNickname>false</canChangeNickname>
    <logEnabled>true</logEnabled>
    <loginRestrictedToNickname>false</loginRestrictedToNickname>
    <membersOnly>false</membersOnly>
    <moderated>false</moderated>
    <allowPM>anyone</allowPM>
    <broadcastPresenceRoles/>
    <owners>
        <owner>owner@localhost</owner>
    </owners>
    <admins>
        <admin>admin@localhost</admin>
    </admins>
    <members>
        <member>member2@localhost</member>
        <member>member1@localhost</member>
    </members>
    <outcasts>
        <outcast>outcast1@localhost</outcast>
    </outcasts>
</chatRoom>
```

## Invite user or user group to a chat Room

Endpoint to invite a user or a user group to a room.
> **Header:** Authorization: Basic YWRtaW46MTIzNDU=
> 
> **Header:** Content-Type: application/xml
> 
> **POST** http://localhost:9090/plugins/restapi/v1/chatrooms/{roomName}/invite/{name}

**Payload Example:**

```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<mucInvitation>
    <reason>Hello, come to this room, it is nice</reason>
</mucInvitation>
```
**Return value:** HTTP status 200 (OK)

### Possible parameters
| Parameter | 	Parameter Type | Description                                                   | Default value |
|-----------|-----------------|---------------------------------------------------------------|---------------|
| roomname  | 	@Path	         | Exact room name                                               |               |
| name      | @Path	          | The local username or group name or the user JID or group JID |               |

## Invite multiple users and/or user groups to a chat Room

Endpoint to invite multiple users and/or user groups to a room. Works both with JIDs and (user/group) names.
> **Header:** Authorization: Basic YWRtaW46MTIzNDU=
>
> **Header:** Content-Type: application/xml
>
> **POST** http://localhost:9090/plugins/restapi/v1/chatrooms/{roomName}/invite

**Payload Example:**

```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<mucInvitation>
    <reason>Hello, come to this room, it is nice</reason>
    <jidsToInvite>
        <jid>jane@example.org</jid>
        <jid>ADNMQP8=@example.org/695c6ae413c00446733d926ccadefd8b</jid>
        <jid>john</jid>
        <jid>SomeGroupName</jid>
    </jidsToInvite>
</mucInvitation>
```
**Return value:** HTTP status 200 (OK)

### Possible parameters
| Parameter | 	Parameter Type | Description                                                   | Default value |
|-----------|-----------------|---------------------------------------------------------------|---------------|
| roomname  | 	@Path	         | Exact room name                                               |               |

##  Get all users with a particular affiliation in a chat room
Retrieves a list of JIDs for all users with the specified affiliation in a multi-user chat room.

>**GET** /chatrooms/{roomName}/{affiliation}

**Payload:** none

**Return value:** HTTP status 200 (OK)

### Possible parameters

| Parameter   | 	Parameter Type | Description                                                                                | Default value |
|-------------|-----------------|--------------------------------------------------------------------------------------------|---------------|
| roomname    | 	@Path	         | Exact room name                                                                            |               |
| affiliation | 	@Path	         | Available affiliations: <br>**owners**  <br> **admins** <br> **members** <br> **outcasts** |               |
| servicename | 	@QueryParam	   | The name of the Group Chat Service                                                         | conference    |

### Examples
>**Header:** Authorization: Basic YWRtaW46MTIzNDU=
>
>**Header:** Content-Type application/xml
>
>**GET** http://example.org:9090/plugins/restapi/v1/chatrooms/global/member

**Return payload:**
```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<members>
    <member>member2@localhost</member>
    <member>member1@localhost</member>
</members>
```

##  Add user with affiliation to chat room
Endpoint to add a new user with affiliation to a room.
>**POST** /chatrooms/{roomName}/{affiliation}/{name}

**Payload:** none

**Return value:** HTTP status 201 (Created)

### Possible parameters

| Parameter       | 	Parameter Type | Description                                                                                | Default value |
|-----------------|-----------------|--------------------------------------------------------------------------------------------|---------------|
| roomname        | 	@Path	         | Exact room name                                                                            |               |
| name            | 	@Path	         | The local username or the user JID                                                         |               |
| affiliation     | 	@Path	         | Available affiliations: <br>**owners**  <br> **admins** <br> **members** <br> **outcasts** |               |
| servicename     | 	@QueryParam	   | The name of the Group Chat Service                                                         | conference    |
| sendInvitations | @QueryParam     | Whether to send invitation to the newly affiliated user                                    | false         |

### Examples
>**Header:** Authorization: Basic YWRtaW46MTIzNDU=
> 
>**Header:** Content-Type application/xml
> 
>**POST** http://example.org:9090/plugins/restapi/v1/chatrooms/global/owners/testUser
> 
>**POST** http://example.org:9090/plugins/restapi/v1/chatrooms/global/owners/testUser@openfire.com
> 
>**POST** http://example.org:9090/plugins/restapi/v1/chatrooms/global/admins/testUser
> 
>**POST** http://example.org:9090/plugins/restapi/v1/chatrooms/global/members/testUser?sendInvitations=true
> 
>**POST** http://example.org:9090/plugins/restapi/v1/chatrooms/global/outcasts/testUser
> 
>**POST** http://example.org:9090/plugins/restapi/v1/chatrooms/global/owners/testUser?servicename=privateconf

##  Replace all users with a affiliation in a chat room
Endpoint to replace all users with a particular affiliation in a multi-user chat room. Note that a user can only have one type of affiliation with a room. By adding a user using a particular affiliation, any other pre-existing affiliation is removed.
>**PUT** /chatrooms/{roomName}/{affiliation}

**Payload:** list of affiliations

**Return value:** HTTP status 201 (Created)

### Possible parameters

| Parameter       | 	Parameter Type | Description                                                                                | Default value |
|-----------------|-----------------|--------------------------------------------------------------------------------------------|---------------|
| roomname        | 	@Path	         | Exact room name                                                                            |               |
| affiliation     | 	@Path	         | Available affiliations: <br>**owners**  <br> **admins** <br> **members** <br> **outcasts** |               |
| servicename     | 	@QueryParam	   | The name of the Group Chat Service                                                         | conference    |
| sendInvitations | @QueryParam     | Whether to send invitation to newly affiliated users                                       | false         |

### Examples
>**Header:** Authorization: Basic YWRtaW46MTIzNDU=
>
>**Header:** Content-Type application/xml
>
>**PUT** http://example.org:9090/plugins/restapi/v1/chatrooms/global/members
> 
**Request Payload:**
```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<members>
    <member>member2@localhost</member>
    <member>member1@localhost</member>
</members>
```

##  Add multiple users with a affiliation to a chat room
Endpoint to add multiple users with an affiliation to a multi-user chat room. Note that a user can only have one type of affiliation with a room. By adding a user using a particular affiliation, any other pre-existing affiliation is removed.
>**PUT** /chatrooms/{roomName}/{affiliation}

**Payload:** list of affiliations

**Return value:** HTTP status 201 (Created)

### Possible parameters

| Parameter       | 	Parameter Type | Description                                                                               | Default value |
|-----------------|-----------------|-------------------------------------------------------------------------------------------|---------------|
| roomname        | 	@Path	         | Exact room name                                                                           |               |
| affiliation     | 	@Path	         | Available affiliation: <br>**owners**  <br> **admins** <br> **members** <br> **outcasts** |               |
| servicename     | 	@QueryParam	   | The name of the Group Chat Service                                                        | conference    |
| sendInvitations | @QueryParam     | Whether to send invitations to newly affiliated users                                     | false         |

### Examples
>**Header:** Authorization: Basic YWRtaW46MTIzNDU=
>
>**Header:** Content-Type application/xml
>
>**POST** http://example.org:9090/plugins/restapi/v1/chatrooms/global/members
>
**Request Payload:**
```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<members>
    <member>member2@localhost</member>
    <member>member1@localhost</member>
</members>
```

##  Add group with affiliation to chat room
Endpoint to add a new group with affiliation to a room.
>**POST** /chatrooms/{roomName}/{affiliation}/group/{name}

**Payload:** none

**Return value:** HTTP status 201 (Created)

### Possible parameters

| Parameter       | Parameter Type | Description                                                                                | Default value |
|-----------------|----------------|--------------------------------------------------------------------------------------------|---------------|
| roomname        | @Path          | Exact room name                                                                            |               |
| name            | @Path          | The group name                                                                             |               |
| affiliation     | @Path          | Available affiliations: <br>**owners**  <br> **admins** <br> **members** <br> **outcasts** |               |
| servicename     | @QueryParam    | The name of the Group Chat Service                                                         | conference    |
| sendInvitations | @QueryParam    | Whether to send invitations to the users in the newly affiliated groups                    | false         |

### Examples
>**Header:** Authorization: Basic YWRtaW46MTIzNDU=
> 
>**Header:** Content-Type application/xml
>
>**POST** http://example.org:9090/plugins/restapi/v1/chatrooms/global/owners/group/testGroup
> 
>**POST** http://example.org:9090/plugins/restapi/v1/chatrooms/global/admins/group/testGroup
> 
>**POST** http://example.org:9090/plugins/restapi/v1/chatrooms/global/members/group/testGroup
> 
>**POST** http://example.org:9090/plugins/restapi/v1/chatrooms/global/outcasts/group/testGroup
> 
>**POST** http://example.org:9090/plugins/restapi/v1/chatrooms/global/owners/group/testUser?servicename=privateconf


## Delete a user from a chat room 
Endpoint to remove a room user affiliation.
>**DELETE** /chatrooms/{roomName}/{affiliations}/{name}

**Payload:** none

**Return value:** HTTP status 200 (OK)

### Possible parameters

| Parameter    | 	Parameter Type | Description                                                                                | Default value |
|--------------|-----------------|--------------------------------------------------------------------------------------------|---------------|
| roomname     | 	@Path	         | Exact room name                                                                            |               |
| name         | 	@Path	         | The local username or the user JID                                                         |               |
| affiliations | 	@Path	         | Available affiliations: <br>**owners**  <br> **admins** <br> **members** <br> **outcasts** |               |
| servicename  | 	@QueryParam	   | The name of the Group Chat Service                                                         | conference    |

### Examples
>**Header:** Authorization: Basic YWRtaW46MTIzNDU=
> 
>**Header:** Content-Type application/xml
> 
>**DELETE** http://example.org:9090/plugins/restapi/v1/chatrooms/global/owners/testUser
> 
>**DELETE** http://example.org:9090/plugins/restapi/v1/chatrooms/global/owners/testUser@openfire.com
> 
>**DELETE** http://example.org:9090/plugins/restapi/v1/chatrooms/global/admins/testUser
> 
>**DELETE** http://example.org:9090/plugins/restapi/v1/chatrooms/global/members/testUser
> 
>**DELETE** http://example.org:9090/plugins/restapi/v1/chatrooms/global/outcasts/testUser
> 
>**DELETE** http://example.org:9090/plugins/restapi/v1/chatrooms/global/owners/testUser?servicename=privateconf

# System related REST Endpoints

## Retrieve all system properties 
Endpoint to get all system properties
>**GET** /system/properties

**Payload:** none

**Return value:** System properties
 
### Examples

>**Header**: Authorization: Basic YWRtaW46MTIzNDU=
> 
>**GET** http://example.org:9090/plugins/restapi/v1/system/properties

## Retrieve system property 
Endpoint to get information over specific system property
>**GET** /system/properties/{propertyName}

**Payload:** none

**Return value:** System property

### Possible parameters

| Parameter    | 	Parameter Type | Description                 | Default value |
|--------------|-----------------|-----------------------------|---------------|
| propertyName | @Path 	         | The name of system property |               |

### Examples

>**Header:** Authorization: Basic YWRtaW46MTIzNDU=
>
>**GET** http://example.org:9090/plugins/restapi/v1/system/properties/xmpp.domain

## Create a system property 
Endpoint to create a system property
>**POST** system/properties

**Payload:** System Property

**Return value:** HTTP status 201 (Created)

### Examples

>**Header:** Authorization: Basic YWRtaW46MTIzNDU=
> 
>**Header:** Content-Type: application/xml
> 
>**POST** http://example.org:9090/plugins/restapi/v1/system/properties

**Payload Example:**
```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<property key="propertyName" value="propertyValue"/>
```

## Delete a system property
Endpoint to delete a system property
>**DELETE** /system/properties/{propertyName}

**Payload:** none

**Return value:** HTTP status 200 (OK)

### Possible parameters

| Parameter    | 	Parameter Type | Description                 | Default value |
|--------------|-----------------|-----------------------------|---------------|
| propertyName | @Path 	         | The name of system property |               |

### Examples

>**Header:** Authorization: Basic YWRtaW46MTIzNDU=
> 
>**DELETE** http://example.org:9090/plugins/restapi/v1/system/properties/propertyName

## Update a system property
Endpoint to update / overwrite a system property
>**PUT** /system/properties/{propertyName}

**Payload:** System property

**Return value:** HTTP status 200 (OK)

### Possible parameters

| Parameter    | 	Parameter Type | Description                 | Default value |
|--------------|-----------------|-----------------------------|---------------|
| propertyName | @Path 	         | The name of system property |               |

### Examples
>**Header:** Authorization: Basic YWRtaW46MTIzNDU=
> 
>**Header:** Content-Type application/xml
> 
>**PUT** http://example.org:9090/plugins/restapi/v1/system/properties/propertyName

**Payload:**
```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<property key="propertyName" value="anotherValue"/>
```

## Retrieve concurrent sessions
Endpoint to get count of concurrent sessions
>**GET** /system/statistics/sessions

**Payload:** none

**Return value:** Sessions count

### Examples

>**Header:** Authorization: Basic YWRtaW46MTIzNDU=
>
>**GET** http://example.org:9090/plugins/restapi/v1/system/statistics/sessions

## Check the 'liveness' state (using all checks)
Detects if Openfire has reached a state that it cannot recover from, except for with a restart, based on every liveness check that it has implemented.

>**GET** /system/liveness

**Payload:** none

**Return value**: HTTP status 200 (OK). Any HTTP status outside the range 200-399 indicates failure.

## Perform 'deadlock' liveness check
Detects if Openfire has reached a state that it cannot recover from because of a deadlock.
>**GET** /system/liveness/deadlock

**Payload:** none

**Return value**: HTTP status 200 (OK). Any HTTP status outside the range 200-399 indicates failure.

## Perform 'properties' liveness check
Detects if Openfire has reached a state that it cannot recover from because a system property change requires a restart to take effect.
>**GET** /system/liveness/properties

**Payload:** none

**Return value**: HTTP status 200 (OK). Any HTTP status outside the range 200-399 indicates failure.

## Check the 'readiness' state (using all checks)
Detects if Openfire is in a state where it is ready to process traffic, based on every readiness check that it has implemented.
>**GET** /system/readiness

**Payload:** none

**Return value**: HTTP status 200 (OK). Any HTTP status outside the range 200-399 indicates failure.

## Perform 'server' readiness check
Detects if Openfire's core service has been started.
>**GET** /system/readiness/server

**Payload:** none

**Return value**: HTTP status 200 (OK). Any HTTP status outside the range 200-399 indicates failure.

## Perform 'cluster' readiness check
Detects if the cluster functionality has finished starting (or is disabled).
>**GET** /system/readiness/cluster

**Payload:** none

**Return value**: HTTP status 200 (OK). Any HTTP status outside the range 200-399 indicates failure.

## Perform 'plugins' readiness check
Detects if Openfire has finished starting its plugins.
>**GET** /system/readiness/plugins

**Payload:** none

**Return value**: HTTP status 200 (OK). Any HTTP status outside the range 200-399 indicates failure.

## Perform 'connections' readiness check
Detects if Openfire is ready to accept connection requests.
>**GET** /system/readiness/connections

**Payload:** none

**Return value**: HTTP status 200 (OK). Any HTTP status outside the range 200-399 indicates failure.

### Possible parameters

| Parameter      | Parameter Type | Description                                                                                                                                                                                                | Default value |
|----------------|----------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------|
| connectionType | @Path          | Optional. Use to limit the check to one particular connection type. One of: SOCKET_S2S, SOCKET_C2S, BOSH_C2S, WEBADMIN, COMPONENT, CONNECTION_MANAGER                                                      |               |
| encypted       | @Path          | Check the encrypted (true) or unencrypted (false) variant of the connection type. Only used in combination with 'connectionType', as without it, all types and both encrypted and unencrypted are checked. |               |

# Group related REST Endpoints

## Retrieve all groups 
Endpoint to get all groups
>**GET** /groups

**Payload:** none

**Return value:** Groups
 
### Examples

>**Header**: Authorization: Basic YWRtaW46MTIzNDU=
> 
>**GET** http://example.org:9090/plugins/restapi/v1/groups

## Retrieve a group 
Endpoint to get information over specific group
>**GET** /groups/{groupName}

**Payload:** none

**Return value:** Group

### Possible parameters

| Parameter | 	Parameter Type | Description           | Default value |
|-----------|-----------------|-----------------------|---------------|
| groupName | @Path 	         | The name of the group |               |

### Examples

>**Header:** Authorization: Basic YWRtaW46MTIzNDU=
>
>**GET** http://example.org:9090/plugins/restapi/v1/groups/moderators

## Create a group 
Endpoint to create a new group
>**POST** /groups

**Payload:** Group

**Return value:** HTTP status 201 (Created)

### Examples

>**Header:** Authorization: Basic YWRtaW46MTIzNDU=
> 
>**Header:** Content-Type: application/xml
> 
>**POST** http://example.org:9090/plugins/restapi/v1/groups

**Payload Example:**
```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<group>
	<name>GroupName</name>
	<description>Some description</description>
	<isshared>false</isshared>
</group>
```

## Delete a group
Endpoint to delete a group
>**DELETE** /groups/{groupName}

**Payload:** none

**Return value:** HTTP status 200 (OK)

### Possible parameters

| Parameter | 	Parameter Type | Description           | Default value |
|-----------|-----------------|-----------------------|---------------|
| groupName | @Path 	         | The name of the group |               |

### Examples

>**Header:** Authorization: Basic YWRtaW46MTIzNDU=
> 
>**DELETE** http://example.org:9090/plugins/restapi/v1/groups/groupToDelete

## Update a group
Endpoint to update / overwrite a group
>**PUT** /groups/{groupName}

**Payload:** Group

**Return value:** HTTP status 200 (OK)

### Possible parameters

| Parameter | 	Parameter Type | Description           | Default value |
|-----------|-----------------|-----------------------|---------------|
| groupName | @Path 	         | The name of the group |               |

### Examples
>**Header:** Authorization: Basic YWRtaW46MTIzNDU=
> 
>**Header:** Content-Type application/xml
> 
>**PUT** http://example.org:9090/plugins/restapi/v1/groups/groupNameToUpdate

**Payload:**
```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<group>
	<name>groupNameToUpdate</name>
	<description>New description</description>
    <isshared>false</isshared>
</group>
```

# Session related REST Endpoints

## Retrieve all user session
Endpoint to get all user sessions
>**GET** /sessions

**Payload:** none

**Return value:** Sessions
 
### Examples

>**Header**: Authorization: Basic YWRtaW46MTIzNDU=
> 
>**GET** http://example.org:9090/plugins/restapi/v1/sessions

## Retrieve the user sessions
Endpoint to get sessions from a user
>**GET** /sessions/{username}

**Payload:** none

**Return value:** Sessions

### Possible parameters

| Parameter | 	Parameter Type | Description              | Default value |
|-----------|-----------------|--------------------------|---------------|
| username  | @Path 	         | The username of the user |               |

### Examples

>**Header:** Authorization: Basic YWRtaW46MTIzNDU=
>
>**GET** http://example.org:9090/plugins/restapi/v1/sessions/testuser

## Close all user sessions
Endpoint to close/kick sessions from a user
>**DELETE** /sessions/{username}

**Payload:** none

**Return value:** HTTP status 200 (OK)

### Possible parameters

| Parameter | 	Parameter Type | Description              | Default value |
|-----------|-----------------|--------------------------|---------------|
| username  | @Path 	         | The username of the user |               |

### Examples

>**Header:** Authorization: Basic YWRtaW46MTIzNDU=
>
>**DELETE** http://example.org:9090/plugins/restapi/v1/sessions/testuser

# Message related REST Endpoints

## Send a broadcast message
Endpoint to send a broadcast/server message to all online users
>**POST** /messages/users

**Payload:** Message

**Return value:** HTTP status 201 (Created)
 
### Examples

>**Header**: Authorization: Basic YWRtaW46MTIzNDU=
> 
>**POST** http://example.org:9090/plugins/restapi/v1/messages/users

**Payload:**
```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<message>
	<body>Your message</body>
</message>
```
# Security Audit related REST Endpoints

## Retrieve the Security audit logs
Endpoint to get security audit logs
>**GET** /logs/security

**Payload:** none

**Return value:** Security Audit Logs

### Possible parameters

| Parameter | Parameter Type | Description                                        | Default value |
|-----------|----------------|----------------------------------------------------|---------------|
| username  | @QueryParam    | Username of user to look up                        |               |
| startTime | @QueryParam    | Oldest timestamp of range of logs to retrieve      |               |
| endTime   | @QueryParam    | Most recent timestamp of range of logs to retrieve | 0 (until now) |
| offset    | @QueryParam    | Number of logs to skip                             |               |
| limit     | @QueryParam    | Number of logs to retrieve                         |               |

### Examples

>**Header**: Authorization: Basic YWRtaW46MTIzNDU=
> 
>**GET** http://example.org:9090/plugins/restapi/v1/logs/security

# Clustering related REST Endpoints

## Retrieve information for all cluster nodes.
Endpoint to get information for all nodes in the cluster. Note that this endpoint can only return data for remote nodes
when the instance of Openfire that processes this query has successfully joined the cluster.

>**GET** http://example.org:9090/plugins/restapi/v1/clustering/nodes

**Payload:** none

**Return value:** ClusterNodes

### Examples
>**Header:** Authorization: Basic YWRtaW46MTIzNDU=
>
>**GET** http://example.org:9090/plugins/restapi/v1/clustering/nodes
>

## Retrieve information for a specific cluster node.
Endpoint to get information for a specific cluster node. Note that this endpoint can only return data for remote nodes
when the instance of Openfire that processes this query has successfully joined the cluster.

>**GET** http://example.org:9090/plugins/restapi/v1/clustering/nodes/{nodeId}

**Payload:** none

**Return value:** ClusterNode

### Possible parameters

| Parameter | 	Parameter Type | Description  | Default value |
|-----------|-----------------|--------------|---------------|
| nodeId    | 	@Path	         | Exact NodeID |               |

### Examples
>**Header:** Authorization: Basic YWRtaW46MTIzNDU=
>
>**GET** http://example.org:9090/plugins/restapi/v1/clustering/nodes/52a89928-66f7-45fd-9bb8-096de07400ac
> 

## Retrieve the Clustering status
Endpoint to get description of clustering status
>**GET** /clustering/status

**Payload:** none

**Return value:** String describing the clustering status of this Openfire instance

### Examples
>**Header**: Authorization: Basic YWRtaW46MTIzNDU=
>
>**GET** http://example.org:9090/plugins/restapi/v1/clustering/status

### Possible Responses

* SENIOR AND ONLY MEMBER
* Senior member
* Junior member
* Starting up
* Disabled

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

# (Deprecated) User Service Plugin Readme

## Overview

The User Service Plugin provides the ability to add,edit,delete users and manage their rosters by sending an http request to the server. It is intended to be used by applications automating the user administration process. This plugin's functionality is useful for applications that need to administer users outside of the Openfire admin console. An example of such an application might be a live sports reporting application that uses XMPP as its transport, and creates/deletes users according to the receipt, or non receipt, of a subscription fee.

## Installation

Copy userservice.jar into the plugins directory of your Openfire server. The plugin will then be automatically deployed. To upgrade to a new version, copy the new userservice.jar file over the existing file.

## Configuration

Access to the service is restricted with a "secret" that can be viewed and set from the User Service page in the Openfire admin console. This page is located on the admin console under "Server" and then "Server Settings". This should really only be considered weak security. The plugin was initially written with the assumption that http access to the Openfire service was only available to trusted machines. In the case of the plugin's author, a web application running on the same server as Openfire makes the request.

## Using the Plugin

To administer users, submit HTTP requests to the userservice service. The service address is [hostname]plugins/restapi/userservice. For example, if your server name is "example.com", the URL is http://example.com/plugins/restapi/userservice

The following parameters can be passed into the request:

| Name         | 	               			                                                      | Description                                                                                                                                                                                                                                                                                                                            | 
|--------------|--------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| type         | 	Required	                                                               | The admin service required. Possible values are 'add', 'delete', 'update', 'enable', 'disable', 'add_roster', 'update_roster', 'delete_roster', 'grouplist', 'usergrouplist'.                                                                                                                                                          |
| secret       | 	Required	                                                               | The secret key that allows access to the User Service.                                                                                                                                                                                                                                                                                 |
| username     | 	Required	                                                               | The username of the user to 'add', 'delete', 'update', 'enable', 'disable', 'add_roster', 'update_roster', 'delete_roster'. ie the part before the @ symbol.                                                                                                                                                                           |
| password     | 	Required for 'add' operation                                            | The password of the new user or the user being updated.                                                                                                                                                                                                                                                                                |
| name         | 	Optional                                                                | The display name of the new user or the user being updated. For 'add_roster', 'update_roster' operations specifies the nickname of the roster item.                                                                                                                                                                                    |
| email        | 	Optional                                                                | The email address of the new user or the user being updated.                                                                                                                                                                                                                                                                           |
| groups       | 	Optional                                                                | List of groups where the user is a member. Values are comma delimited. When used with types "add" or "update", it adds the user to shared groups and auto-creates new groups. When used with 'add_roster' and 'update_roster', it adds the user to roster groups provided the group name does not clash with an existing shared group. |
| item_jid     | 	Required for 'add_roster', 'update_roster', 'delete_roster' operations. | The JID of the roster item                                                                                                                                                                                                                                                                                                             |
| subscription | Optional                                                                 | Type of subscription for 'add_roster', 'update_roster' operations. Possible numeric values are: -1(remove), 0(none), 1(to), 2(from), 3(both).                                                                                                                                                                                          |

## Sample HTML
The following example adds a user

http://example.com:9090/plugins/restapi/userservice?type=add&secret=bigsecret&username=kafka&password=drowssap&name=franz&email=franz@kafka.com

The following example adds a user, adds two shared groups (if not existing) and adds the user to both groups.

http://example.com:9090/plugins/restapi/userservice?type=add&secret=bigsecret&username=kafka&password=drowssap&name=franz&email=franz@kafka.com&groups=support,finance

The following example deletes a user and all roster items of the user.

http://example.com:9090/plugins/restapi/userservice?type=delete&secret=bigsecret&username=kafka

The following example disables a user (lockout)

http://example.com:9090/plugins/restapi/userservice?type=disable&secret=bigsecret&username=kafka

The following example enables a user (removes lockout)

http://example.com:9090/plugins/restapi/userservice?type=enable&secret=bigsecret&username=kafka

The following example updates a user

http://example.com:9090/plugins/restapi/userservice?type=update&secret=bigsecret&username=kafka&password=drowssap&name=franz&email=beetle@kafka.com

The following example adds new roster item with subscription 'both' for user 'kafka'

http://example.com:9090/plugins/restapi/userservice?type=add_roster&secret=bigsecret&username=kafka&item_jid=franz@example.com&name=franz&subscription=3

The following example adds new roster item with subscription 'both' for user 'kafka' and adds kafka to roster groups 'family' and 'friends'

http://example.com:9090/plugins/restapi/userservice?type=add_roster&secret=bigsecret&username=kafka&item_jid=franz@example.com&name=franz&subscription=3&groups=family,friends

The following example updates existing roster item to subscription 'none' for user 'kafka'

http://example.com:9090/plugins/restapi/userservice?type=update_roster&secret=bigsecret&username=kafka&item_jid=franz@example.com&name=franz&subscription=0

The following example deletes a specific roster item 'franz@kafka.com' for user 'kafka'

http://example.com:9090/plugins/restapi/userservice?type=delete_roster&secret=bigsecret&username=kafka&item_jid=franz@example.com

The following example gets all groups

http://example.com:9090/plugins/restapi/userservice?type=grouplist&secret=bigsecret
Which replies an XML group list formatted like this:
```xml
<result>
    <groupname>group1</groupname>
    <groupname>group2</groupname>
</result>
```

The following example gets all groups for a specific user

http://example.com:9090/plugins/restapi/userservice?type=usergrouplist&secret=bigsecret&username=kafka
Which replies an XML group list formatted like this:
```xml
<result>
    <groupname>usergroup1</groupname>
    <groupname>usergroup2</groupname>
</result>
```

When sending double characters (Chinese/Japanese/Korean etc.) you should URLEncode the string as utf8.
In Java this is done like this

> URLEncoder.encode(username, "UTF-8"));

If the strings are encoded incorrectly, double byte characters will look garbeled in the Admin Console.

## Server Reply
The server will reply to all User Service requests with an XML result page. If the request was processed successfully the return will be a "result" element with a text body of "OK", or an XML grouplist formatted like in the example for "grouplist" and "usergrouplist" above. If the request was unsuccessful, the return will be an "error" element with a text body of one of the following error strings.

| Error String               | 	Description                                                                                                                                                     |
|----------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| IllegalArgumentException   | 	One of the parameters passed in to the User Service was bad.                                                                                                    |
| UserNotFoundException      | 	No user of the name specified, for a delete or update operation, exists on this server. For 'update_roster' operation, roster item to be updated was not found. |
| UserAlreadyExistsException | 	A user with the same name as the user about to be added, already exists. For 'add_roster' operation, roster item with the same JID already exists.              |
| RequestNotAuthorised       | 	The supplied secret does not match the secret specified in the Admin Console or the requester is not a valid IP address.                                        |
| UserServiceDisabled        | 	The User Service is currently set to disabled in the Admin Console.                                                                                             |
| SharedGroupException       | 	Roster item can not be added/deleted to/from a shared group for operations with roster.                                                                         |

