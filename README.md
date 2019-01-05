# REST API Plugin Readme

The REST API Plugin provides the ability to manage Openfire by sending an REST/HTTP request to the server. This plugin’s functionality is useful for applications that need to administer Openfire outside of the Openfire admin console.

## Feature list

*   Get overview over all or specific user and to create, update or delete a user
*   Get overview over all or specific group and to create, update or delete a group
*   Get overview over all user roster entries and to add, update or delete a roster entry
*   Add user to a group and remove a user from a group
*   Lockout, unlock or kick the user (enable / disable)
*   Get overview over all or specific system properties and to create, update or delete system property
*   Get overview over all or specific chat room and to create, update or delete a chat room
*   Get overview over all or specific user sessions
*   Send broadcast message to all online users
*   Get overview of all or specific security audit logs
*   Get chat message history from a Multi User Chatroom

## Available REST API clients

REST API clients are implementations of the REST API in a specific programming language.

### Official

*   JAVA: [https://github.com/igniterealtime/REST-API-Client](https://github.com/igniterealtime/REST-API-Client)

### Third party

*   PHP: [https://github.com/gidkom/php-openfire-restapi](https://github.com/gidkom/php-openfire-restapi) (partly implemented)
*   PHP: [https://github.com/gnello/php-openfire-restapi](https://github.com/gnello/php-openfire-restapi) (partly implemented)
*   GO Lang: [https://github.com/Urethramancer/fireman](https://github.com/Urethramancer/fireman) (partly implemented)
*   Python: [https://github.com/seamus-45/openfire-restapi](https://github.com/seamus-45/openfire-restapi) (partly implemented)

## Installation

Copy restAPI.jar into the plugins directory of your Openfire server. The plugin will be automatically deployed. To upgrade to a newer version, overwrite the restAPI.jar file with the new one.

## Explanation of REST

To provide a standard way of accessing the data the plugin is using REST.

<table>
    <thead>
        <tr>
            <th>HTTP Method</th>
            <th>Usage</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>**GET**</td>
            <td>Receive a read-only data</td>
        </tr>
        <tr>
            <td>**PUT**</td>
            <td>Overwrite an existing resource</td>
        </tr>
        <tr>
            <td>**POST**</td>
            <td>Creates a new resource</td>
        </tr>
        <tr>
            <td>**DELETE**</td>
            <td>Deletes the given resource</td>
        </tr>
    </tbody>
</table>

## Authentication

All REST Endpoint are secured and must be authenticated. There are two ways to authenticate:

*   [Basic HTTP Authentication](http://en.wikipedia.org/wiki/Basic_access_authentication)
*   Shared secret key

The configuration can be done in Openfire Admin console under Server > Server Settings > REST API.

### Basic HTTP Authentication

To access the endpoints is that required to send the Username and Password of a Openfire Admin account in your HTTP header request.

E.g. **Header:** Authorization: Basic YWRtaW46MTIzNDU= (username: admin / password: 12345)

### Shared secret key

To access the endpoints is that required to send the secret key in your header request.  
The secret key can be defined in Openfire Admin console under Server > Server Settings > REST API.

E.g. **Header:** Authorization: s3cretKey

# User related REST Endpoints

## Retrieve users

Endpoint to get all or filtered users

> **GET** /users

**Payload:** none  
**Return value:** Users

### Possible parameters

<table>
    <thead>
        <tr>
            <th>Parameter</th>
            <th>Parameter Type</th>
            <th>Description</th>
            <th>Default value</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>search</td>
            <td>@QueryParam</td>
            <td>Search/Filter by username.  
This act like the wildcard search %String%</td>
            <td></td>
        </tr>
        <tr>
            <td>propertyKey</td>
            <td>@QueryParam</td>
            <td>Filter by user propertyKey.</td>
            <td></td>
        </tr>
        <tr>
            <td>propertyValue</td>
            <td>@QueryParam</td>
            <td>Filter by user propertyKey and propertyValue.  
**Note:** It can only be used within propertyKey parameter</td>
            <td></td>
        </tr>
    </tbody>
</table>

### Examples

f

> **Header**: Authorization: Basic YWRtaW46MTIzNDU=

> **GET** [http://example.org:9090/plugins/restapi/v1/users](http://example.org:9090/plugins/restapi/v1/users)  
> **GET** [http://example.org:9090/plugins/restapi/v1/users?search=testuser](http://example.org:9090/plugins/restapi/v1/users?search=testuser)  
> **GET** [http://example.org:9090/plugins/restapi/v1/users?propertyKey=keyname](http://example.org:9090/plugins/restapi/v1/users?propertyKey=keyname)  
> **GET** [http://example.org:9090/plugins/restapi/v1/users?propertyKey=keyname&propertyValue=keyvalue](http://example.org:9090/plugins/restapi/v1/users?propertyKey=keyname&propertyValue=keyvalue)

If you want to get a JSON format result, please add “**Accept: application/json**” to the **Header**.

## Retrieve a user

Endpoint to get information over a specific user

> **GET** /users/{username}

**Payload:** none  
**Return value:** User

### Possible parameters

<table>
    <thead>
        <tr>
            <th>Parameter</th>
            <th>Parameter Type</th>
            <th>Description</th>
            <th>Default value</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>username</td>
            <td>@Path</td>
            <td>Exact username</td>
            <td></td>
        </tr>
    </tbody>
</table>

### Examples

**Header:** Authorization: Basic YWRtaW46MTIzNDU=

**GET** [http://example.org:9090/plugins/restapi/v1/users/testuser](http://example.org:9090/plugins/restapi/v1/users/testuser)

## Create a user

Endpoint to create a new user

> **POST** /users

**Payload:** User  
**Return value:** HTTP status 201 (Created)

### Examples

#### XML Examples

> **Header:** Authorization: Basic YWRtaW46MTIzNDU=  
> **Header:** Content-Type: application/**xml**

> **POST** [http://example.org:9090/plugins/restapi/v1/users](http://example.org:9090/plugins/restapi/v1/users)

**Payload Example 1 (required parameters):**

    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
    <user>
        <username>test3</username>
        <password>p4ssword</password>
    </user>

**Payload Example 2 (available parameters):**

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

#### JSON Examples

> **Header:** Authorization: Basic YWRtaW46MTIzNDU=  
> **Header:** Content-Type: application/**json**

> **POST** [http://example.org:9090/plugins/restapi/v1/users](http://example.org:9090/plugins/restapi/v1/users)

**Payload Example 1 (required parameters):**

    {
        "username": "admin",
        "password": "p4ssword"
    }

**Payload Example 2 (available parameters):**

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

**REST API Version 1.3.0 and later - Payload Example 2 (available parameters):**

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

## Delete a user

Endpoint to delete a user

> **DELETE** /users/{username}

**Payload:** none  
**Return value:** HTTP status 200 (OK)

### Possible parameters

<table>
    <thead>
        <tr>
            <th>Parameter</th>
            <th>Parameter Type</th>
            <th>Description</th>
            <th>Default value</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>username</td>
            <td>@Path</td>
            <td>Exact username</td>
            <td></td>
        </tr>
    </tbody>
</table>

### Examples

> **Header:** Authorization: Basic YWRtaW46MTIzNDU=  
> **DELETE** [http://example.org:9090/plugins/restapi/v1/users/testuser](http://example.org:9090/plugins/restapi/v1/users/testuser)

## Update a user

Endpoint to update / rename a user

> **PUT** /users/{username}

**Payload:** User  
**Return value:** HTTP status 200 (OK)

### Possible parameters

<table>
    <thead>
        <tr>
            <th>Parameter</th>
            <th>Parameter Type</th>
            <th>Description</th>
            <th>Default value</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>username</td>
            <td>@Path</td>
            <td>Exact username</td>
            <td></td>
        </tr>
    </tbody>
</table>

### Examples

#### XML Example

> **Header:** Authorization: Basic YWRtaW46MTIzNDU=  
> **Header:** Content-Type application/xml

> **PUT** [http://example.org:9090/plugins/restapi/v1/users/testuser](http://example.org:9090/plugins/restapi/v1/users/testuser)

**Payload:**

    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
    <user>
        <username>testuser</username>
        <name>Test User edit</name>
        <email>test@edit.de</email>
        <properties>
            <property key="keyname" value="value"/>
        </properties>
    </user>

#### Rename Example

> **Header:** Authorization: Basic YWRtaW46MTIzNDU=  
> **Header:** Content-Type application/xml

> **PUT** [http://example.org:9090/plugins/restapi/v1/users/oldUsername](http://example.org:9090/plugins/restapi/v1/users/oldUsername)

**Payload:**

    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
    <user>
        <username>newUsername</username>
        <name>Test User edit</name>
        <email>test@edit.de</email>
        <properties>
            <property key="keyname" value="value"/>
        </properties>
    </user>

#### JSON Example

> **Header:** Authorization: Basic YWRtaW46MTIzNDU=  
> **Header:** Content-Type application/json

> **PUT** [http://example.org:9090/plugins/restapi/v1/users/testuser](http://example.org:9090/plugins/restapi/v1/users/testuser)

**Payload:**

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

**REST API Version 1.3.0 and later - Payload Example 2 (available parameters):**

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

## Retrieve all user groups

Endpoint to get group names of a specific user

> **GET** /users/{username}/groups

**Payload:** none  
**Return value:** Groups

### Possible parameters

<table>
    <thead>
        <tr>
            <th>Parameter</th>
            <th>Parameter Type</th>
            <th>Description</th>
            <th>Default value</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>username</td>
            <td>@Path</td>
            <td>Exact username</td>
            <td></td>
        </tr>
    </tbody>
</table>

### Examples

> **Header:** Authorization: Basic YWRtaW46MTIzNDU=

> **GET ** [http://example.org:9090/plugins/restapi/v1/users/testuser/groups](http://example.org:9090/plugins/restapi/v1/users/testuser/groups)

## Add user to groups

Endpoint to add user to a groups

> **POST** /users/{username}/groups

**Payload:** Groups  
**Return value:** HTTP status 201 (Created)

### Possible parameters

<table>
    <thead>
        <tr>
            <th>Parameter</th>
            <th>Parameter Type</th>
            <th>Description</th>
            <th>Default value</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>username</td>
            <td>@Path</td>
            <td>Exact username</td>
            <td></td>
        </tr>
    </tbody>
</table>

### Examples

> **Header:** Authorization: Basic YWRtaW46MTIzNDU=  
> **Header:** Content-Type application/xml

> **POST** [http://example.org:9090/plugins/restapi/v1/users/testuser/groups](http://example.org:9090/plugins/restapi/v1/users/testuser/groups)

**Payload:**

    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
    <groups>
        <groupname>Admins</groupname>
        <groupname>Support</groupname>
    </groups>

## Add user to group

Endpoint to add user to a group

> **POST** /users/{username}/groups/{groupName}

**Payload:** none  
**Return value:** HTTP status 201 (Created)

### Possible parameters

<table>
    <thead>
        <tr>
            <th>Parameter</th>
            <th>Parameter Type</th>
            <th>Description</th>
            <th>Default value</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>username</td>
            <td>@Path</td>
            <td>Exact username</td>
            <td></td>
        </tr>
        <tr>
            <td>groupName</td>
            <td>@Path</td>
            <td>Exact group name</td>
            <td></td>
        </tr>
    </tbody>
</table>

### Examples

> **Header:** Authorization: Basic YWRtaW46MTIzNDU=  
> **Header:** Content-Type application/xml

> **POST** [http://example.org:9090/plugins/restapi/v1/users/testuser/groups/testGroup](http://example.org:9090/plugins/restapi/v1/users/testuser/groups/testGroup)

## Delete a user from a groups

Endpoint to remove a user from a groups

> **DELETE** /users/{username}/groups

**Payload:** Groups  
**Return value:** HTTP status 200 (OK)

### Possible parameters

<table>
    <thead>
        <tr>
            <th>Parameter</th>
            <th>Parameter Type</th>
            <th>Description</th>
            <th>Default value</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>username</td>
            <td>@Path</td>
            <td>Exact username</td>
            <td></td>
        </tr>
    </tbody>
</table>

### Examples

> **Header:** Authorization: Basic YWRtaW46MTIzNDU=  
> **Header:** Content-Type application/xml

> **DELETE** [http://example.org:9090/plugins/restapi/v1/users/testuser/groups](http://example.org:9090/plugins/restapi/v1/users/testuser/groups)

**Payload:**

    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
    <groups>
        <groupname>Admins</groupname>
        <groupname>Support</groupname>
    </groups>

## Delete a user from a group

Endpoint to remove a user from a group

> **DELETE** /users/{username}/groups/{groupName}

**Payload:** none  
**Return value:** HTTP status 200 (OK)

### Possible parameters

<table>
    <thead>
        <tr>
            <th>Parameter</th>
            <th>Parameter Type</th>
            <th>Description</th>
            <th>Default value</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>username</td>
            <td>@Path</td>
            <td>Exact username</td>
            <td></td>
        </tr>
        <tr>
            <td>groupName</td>
            <td>@Path</td>
            <td>Exact group name</td>
            <td></td>
        </tr>
    </tbody>
</table>

### Examples

> **Header:** Authorization: Basic YWRtaW46MTIzNDU=  
> **Header:** Content-Type application/xml

> **DELETE** [http://example.org:9090/plugins/restapi/v1/users/testuser/groups/testGroup](http://example.org:9090/plugins/restapi/v1/users/testuser/groups/testGroup)

## Lockout a user

Endpoint to lockout / ban the user from the chat server. The user will be kicked if the user is online.

> **POST** /lockouts/{username}

**Payload:** none  
**Return value:** HTTP status 201 (Created)

### Possible parameters

<table>
    <thead>
        <tr>
            <th>Parameter</th>
            <th>Parameter Type</th>
            <th>Description</th>
            <th>Default value</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>username</td>
            <td>@Path</td>
            <td>Exact username</td>
            <td></td>
        </tr>
    </tbody>
</table>

### Examples

> **Header:** Authorization: Basic YWRtaW46MTIzNDU=

> **POST** [http://example.org:9090/plugins/restapi/v1/lockouts/testuser](http://example.org:9090/plugins/restapi/v1/lockouts/testuser)

## Unlock a user

Endpoint to unlock / unban the user

> **DELETE** /lockouts/{username}

**Payload:** none  
**Return value:** HTTP status 200 (OK)

### Possible parameters

<table>
    <thead>
        <tr>
            <th>Parameter</th>
            <th>Parameter Type</th>
            <th>Description</th>
            <th>Default value</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>username</td>
            <td>@Path</td>
            <td>Exact username</td>
            <td></td>
        </tr>
    </tbody>
</table>

### Examples

> **Header:** Authorization: Basic YWRtaW46MTIzNDU=

> **DELETE** [http://example.org:9090/plugins/restapi/v1/lockouts/testuser](http://example.org:9090/plugins/restapi/v1/lockouts/testuser)

## Retrieve user roster

Endpoint to get roster entries (buddies) from a specific user

> **GET** /users/{username}/roster

**Payload:** none  
**Return value:** Roster

### Possible parameters

<table>
    <thead>
        <tr>
            <th>Parameter</th>
            <th>Parameter Type</th>
            <th>Description</th>
            <th>Default value</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>username</td>
            <td>@Path</td>
            <td>Exact username</td>
            <td></td>
        </tr>
    </tbody>
</table>

### Examples

> **Header:** Authorization: Basic YWRtaW46MTIzNDU=

> **GET ** [http://example.org:9090/plugins/restapi/v1/users/testuser/roster](http://example.org:9090/plugins/restapi/v1/users/testuser/roster)

## Create a user roster entry

Endpoint to add a new roster entry to a user

> **POST** /users/{username}/roster

**Payload:** RosterItem  
**Return value:** HTTP status 201 (Created)

### Possible parameters

<table>
    <thead>
        <tr>
            <th>Parameter</th>
            <th>Parameter Type</th>
            <th>Description</th>
            <th>Default value</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>username</td>
            <td>@Path</td>
            <td>Exact username</td>
            <td></td>
        </tr>
    </tbody>
</table>

### Examples

> **Header:** Authorization: Basic YWRtaW46MTIzNDU=  
> **Header:** Content-Type application/xml

> **POST** [http://example.org:9090/plugins/restapi/v1/users/testuser/roster](http://example.org:9090/plugins/restapi/v1/users/testuser/roster)

**Payload:**  
Payload Example 1 (required parameters):

    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
    <rosterItem>
    	<jid>peter@pan.de</jid>
    </rosterItem>

Payload Example 2 (available parameters):

    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
    <rosterItem>
    	<jid>peter@pan1.de</jid>
    	<nickname>Peter1</nickname>
    	<subscriptionType>3</subscriptionType>
    	<groups>
    		<group>Friends</group>
    	</groups>
    </rosterItem>

## Delete a user roster entry

Endpoint to remove a roster entry from a user

> **DELETE** /users/{username}/roster/{jid}

**Payload:** none  
**Return value:** HTTP status 200 (OK)

### Possible parameters

<table>
    <thead>
        <tr>
            <th>Parameter</th>
            <th>Parameter Type</th>
            <th>Description</th>
            <th>Default value</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>username</td>
            <td>@Path</td>
            <td>Exact username</td>
            <td></td>
        </tr>
        <tr>
            <td>jid</td>
            <td>@Path</td>
            <td>JID of the roster item</td>
            <td></td>
        </tr>
    </tbody>
</table>

### Examples

> **Header:** Authorization: Basic YWRtaW46MTIzNDU=

> **DELETE** [http://example.org:9090/plugins/restapi/v1/users/testuser/roster/peter@pan.de](http://example.org:9090/plugins/restapi/v1/users/testuser/roster/peter@pan.de)

## Update a user roster entry

Endpoint to update a roster entry

> **PUT** /users/{username}/roster/{jid}

**Payload:** RosterItem  
**Return value:** HTTP status 200 (OK)

### Possible parameters

<table>
    <thead>
        <tr>
            <th>Parameter</th>
            <th>Parameter Type</th>
            <th>Description</th>
            <th>Default value</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>username</td>
            <td>@Path</td>
            <td>Exact username</td>
            <td></td>
        </tr>
        <tr>
            <td>jid</td>
            <td>@Path</td>
            <td>JID of the roster item</td>
            <td></td>
        </tr>
    </tbody>
</table>

### Examples

> **Header:** Authorization: Basic YWRtaW46MTIzNDU=  
> **Header:** Content-Type application/xml

> **PUT** [http://example.org:9090/plugins/restapi/v1/users/testuser/roster/peter@pan.de](http://example.org:9090/plugins/restapi/v1/users/testuser/roster/peter@pan.de)

**Payload:**

    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
    <rosterItem>
    	<jid>peter@pan.de</jid>
    	<nickname>Peter Pan</nickname>
    	<subscriptionType>0</subscriptionType>
    	<groups>
    		<group>Support</group>
    	</groups>
    </rosterItem>

# Chat room related REST Endpoints

## Retrieve all chat rooms

Endpoint to get all chat rooms

> **GET** /chatrooms

**Payload:** none  
**Return value:** Chatrooms

### Possible parameters

<table>
    <thead>
        <tr>
            <th>Parameter</th>
            <th>Parameter Type</th>
            <th>Description</th>
            <th>Default value</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>servicename</td>
            <td>@QueryParam</td>
            <td>The name of the Group Chat Service</td>
            <td>conference</td>
        </tr>
        <tr>
            <td>type</td>
            <td>@QueryParam</td>
            <td>**public:** Only as List Room in Directory set rooms  
**all:** All rooms.</td>
            <td>public</td>
        </tr>
        <tr>
            <td>search</td>
            <td>@QueryParam</td>
            <td>Search/Filter by room name.  
This act like the wildcard search %String%</td>
            <td></td>
        </tr>
    </tbody>
</table>

### Examples

> **Header**: Authorization: Basic YWRtaW46MTIzNDU=

> **GET** [http://example.org:9090/plugins/restapi/v1/chatrooms](http://example.org:9090/plugins/restapi/v1/chatrooms)  
> **GET** [http://example.org:9090/plugins/restapi/v1/chatrooms?type=all](http://example.org:9090/plugins/restapi/v1/chatrooms?type=all)  
> **GET** [http://example.org:9090/plugins/restapi/v1/chatrooms?type=all&servicename=privateconf](http://example.org:9090/plugins/restapi/v1/chatrooms?type=all&servicename=privateconf)  
> **GET** [http://example.org:9090/plugins/restapi/v1/chatrooms?search=test](http://example.org:9090/plugins/restapi/v1/chatrooms?search=test)

## Retrieve a chat room

Endpoint to get information over specific chat room

> **GET** /chatrooms<span>/{roomName}</span>

**Payload:** none  
**Return value:** Chatroom

### Possible parameters

<table>
    <thead>
        <tr>
            <th>Parameter</th>
            <th>Parameter Type</th>
            <th>Description</th>
            <th>Default value</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>roomname</td>
            <td>@Path</td>
            <td>Exact room name</td>
            <td></td>
        </tr>
        <tr>
            <td>servicename</td>
            <td>@QueryParam</td>
            <td>The name of the Group Chat Service</td>
            <td>conference</td>
        </tr>
    </tbody>
</table>

### Examples

> **Header:** Authorization: Basic YWRtaW46MTIzNDU=

> **GET** [http://example.org:9090/plugins/restapi/v1/chatrooms/test](http://example.org:9090/plugins/restapi/v1/chatrooms/test)  
> **GET** [http://example.org:9090/plugins/restapi/v1/chatrooms/test?servicename=privateconf](http://example.org:9090/plugins/restapi/v1/chatrooms/test?servicename=privateconf)

## Retrieve chat room participants

Endpoint to get all participants with a role of specified room.

> **GET** /chatrooms/{roomName}/participants

**Payload:** none  
**Return value:** Participants

### Possible parameters

<table>
    <thead>
        <tr>
            <th>Parameter</th>
            <th>Parameter Type</th>
            <th>Description</th>
            <th>Default value</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>roomname</td>
            <td>@Path</td>
            <td>Exact room name</td>
            <td></td>
        </tr>
        <tr>
            <td>servicename</td>
            <td>@QueryParam</td>
            <td>The name of the Group Chat Service</td>
            <td>conference</td>
        </tr>
    </tbody>
</table>

### Examples

> **Header:** Authorization: Basic YWRtaW46MTIzNDU=

> **GET** [http://example.org:9090/plugins/restapi/v1/chatrooms/room1/participants](http://example.org:9090/plugins/restapi/v1/chatrooms/room1/participants)

## Retrieve chat room occupants

Endpoint to get all occupants (all roles / affiliations) of a specified room.

> **GET** /chatrooms/{roomName}/occupants

**Payload:** none  
**Return value:** Occupants

### Possible parameters

<table>
    <thead>
        <tr>
            <th>Parameter</th>
            <th>Parameter Type</th>
            <th>Description</th>
            <th>Default value</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>roomname</td>
            <td>@Path</td>
            <td>Exact room name</td>
            <td></td>
        </tr>
        <tr>
            <td>servicename</td>
            <td>@QueryParam</td>
            <td>The name of the Group Chat Service</td>
            <td>conference</td>
        </tr>
    </tbody>
</table>

### Examples

> **Header:** Authorization: Basic YWRtaW46MTIzNDU=

> **GET** [http://example.org:9090/plugins/restapi/v1/chatrooms/room1/occupants](http://example.org:9090/plugins/restapi/v1/chatrooms/room1/occupants)

## Retrieve chat room message history

Endpoint to get the chat message history of a specified room.

> **GET** /chatrooms/{roomName}/chathistory

**Payload:** none  
**Return value:** Chat History

### Possible parameters

<table>
    <thead>
        <tr>
            <th>Parameter</th>
            <th>Parameter Type</th>
            <th>Description</th>
            <th>Default value</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>roomname</td>
            <td>@Path</td>
            <td>Exact room name</td>
            <td></td>
        </tr>
        <tr>
            <td>servicename</td>
            <td>@QueryParam</td>
            <td>The name of the Group Chat Service</td>
            <td>conference</td>
        </tr>
    </tbody>
</table>

## Create a chat room

Endpoint to create a new chat room.

> **POST** /chatrooms

**Payload:** Chatroom  
**Return value:** HTTP status 201 (Created)

### Possible parameters

<table>
    <thead>
        <tr>
            <th>Parameter</th>
            <th>Parameter Type</th>
            <th>Description</th>
            <th>Default value</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>servicename</td>
            <td>@QueryParam</td>
            <td>The name of the Group Chat Service</td>
            <td>conference</td>
        </tr>
    </tbody>
</table>

### XML Examples

> **Header:** Authorization: Basic YWRtaW46MTIzNDU=  
> **Header:** Content-Type: application/xml

> **POST** [http://example.org:9090/plugins/restapi/v1/chatrooms](http://example.org:9090/plugins/restapi/v1/chatrooms)

**Payload Example 1 (required parameters):**

    <chatRoom>
        <naturalName>global-1</naturalName>
        <roomName>global</roomName>
        <description>Global Chat Room</description>
    </chatRoom>

**Payload Example 2 (available parameters):**

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

### JSON Examples

> **Header:** Authorization: Basic YWRtaW46MTIzNDU=  
> **Header:** Content-Type: application/json

> **POST** [http://example.org:9090/plugins/restapi/v1/chatrooms](http://example.org:9090/plugins/restapi/v1/chatrooms)

**Payload Example 1 (required parameters):**

    {
    	"roomName": "global",
    	"naturalName": "global-2",
    	"description": "Global chat room"
    }

**Payload Example 2 (available parameters):**

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

**REST API Version 1.3.0 and later - Payload Example 2 (available parameters):**

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

## Delete a chat room

Endpoint to delete a chat room.

> **DELETE** /chatrooms/{roomName}

**Payload:** none  
**Return value:** HTTP status 200 (OK)

### Possible parameters

<table>
    <thead>
        <tr>
            <th>Parameter</th>
            <th>Parameter Type</th>
            <th>Description</th>
            <th>Default value</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>roomname</td>
            <td>@Path</td>
            <td>Exact room name</td>
            <td></td>
        </tr>
        <tr>
            <td>servicename</td>
            <td>@QueryParam</td>
            <td>The name of the Group Chat Service</td>
            <td>conference</td>
        </tr>
    </tbody>
</table>

### Examples

> **Header:** Authorization: Basic YWRtaW46MTIzNDU=

> **DELETE** [http://example.org:9090/plugins/restapi/v1/chatrooms/testroom](http://example.org:9090/plugins/restapi/v1/chatrooms/testroom)  
> **DELETE** [http://example.org:9090/plugins/restapi/v1/chatrooms/testroom?servicename=privateconf](http://example.org:9090/plugins/restapi/v1/chatrooms/testroom?servicename=privateconf)

## Update a chat room

Endpoint to update a chat room.

> **PUT** /chatrooms/{roomName}

**Payload:** Chatroom  
**Return value:** HTTP status 200 (OK)

### Possible parameters

<table>
    <thead>
        <tr>
            <th>Parameter</th>
            <th>Parameter Type</th>
            <th>Description</th>
            <th>Default value</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>roomname</td>
            <td>@Path</td>
            <td>Exact room name</td>
            <td></td>
        </tr>
        <tr>
            <td>servicename</td>
            <td>@QueryParam</td>
            <td>The name of the Group Chat Service</td>
            <td>conference</td>
        </tr>
    </tbody>
</table>

### Examples

> **Header:** Authorization: Basic YWRtaW46MTIzNDU=  
> **Header:** Content-Type application/xml

> **PUT** [http://example.org:9090/plugins/restapi/v1/chatrooms/global](http://example.org:9090/plugins/restapi/v1/chatrooms/global)

**Payload:**

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

## Invite user to a chat Room

Endpoint to invite a user to a room.

> **Header:** Authorization: Basic YWRtaW46MTIzNDU=
> **Header:** Content-Type: application/xml
> **POST** http://localhost:9090/plugins/restapi/v1/chatrooms/{roomName}/invite/{name}

**Payload Example:**

    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
    <mucInvitation>
        <reason>Hello, come to this room, it is nice</reason>
    </mucInvitation>

**Return value:** HTTP status 200 (Ok)

### Possible parameters

<table>
    <thead>
        <tr>
            <th>Parameter</th>
            <th>Parameter Type</th>
            <th>Description</th>
            <th>Default value</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>roomname</td>
            <td>@Path</td>
            <td>Exact room name</td>
            <td></td>
        </tr>
        <tr>
            <td>name</td>
            <td>@Path</td>
            <td>The local username or the user JID</td>
            <td></td>
        </tr>
    </tbody>
</table>


## Add user with role to chat room

Endpoint to add a new user with role to a room.

> **POST** /chatrooms/{roomName}/{roles}/{name}

**Payload:** none  
**Return value:** HTTP status 201 (Created)

### Possible parameters

<table>
    <thead>
        <tr>
            <th>Parameter</th>
            <th>Parameter Type</th>
            <th>Description</th>
            <th>Default value</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>roomname</td>
            <td>@Path</td>
            <td>Exact room name</td>
            <td></td>
        </tr>
        <tr>
            <td>name</td>
            <td>@Path</td>
            <td>The local username or the user JID</td>
            <td></td>
        </tr>
        <tr>
            <td>roles</td>
            <td>@Path</td>
            <td>Available roles:  
**owners**  
**admins**  
**members**  
**outcasts**</td>
            <td></td>
        </tr>
        <tr>
            <td>servicename</td>
            <td>@QueryParam</td>
            <td>The name of the Group Chat Service</td>
            <td>conference</td>
        </tr>
    </tbody>
</table>

### Examples

> **Header:** Authorization: Basic YWRtaW46MTIzNDU=  
> **Header:** Content-Type application/xml

> **POST** [http://example.org:9090/plugins/restapi/v1/chatrooms/global/owners/testUser](http://example.org:9090/plugins/restapi/v1/chatrooms/global/owners/testUser)  
> **POST** [http://example.org:9090/plugins/restapi/v1/chatrooms/global/owners/testUser@openfire.com](http://example.org:9090/plugins/restapi/v1/chatrooms/global/owners/testUser@openfire.com)  
> **POST** [http://example.org:9090/plugins/restapi/v1/chatrooms/global/admins/testUser](http://example.org:9090/plugins/restapi/v1/chatrooms/global/admins/testUser)  
> **POST** [http://example.org:9090/plugins/restapi/v1/chatrooms/global/members/testUser](http://example.org:9090/plugins/restapi/v1/chatrooms/global/members/testUser)  
> **POST** [http://example.org:9090/plugins/restapi/v1/chatrooms/global/outcasts/testUser](http://example.org:9090/plugins/restapi/v1/chatrooms/global/outcasts/testUser)  
> **POST** [http://example.org:9090/plugins/restapi/v1/chatrooms/global/owners/testUser?servicename=privateconf](http://example.org:9090/plugins/restapi/v1/chatrooms/global/owners/testUser?servicename=privateconf)

## Add group with role to chat room

Endpoint to add a new group with role to a room.

> **POST** /chatrooms/{roomName}/{roles}/group/{name}

**Payload:** none  
**Return value:** HTTP status 201 (Created)

### Possible parameters

<table>
    <thead>
        <tr>
            <th>Parameter</th>
            <th>Parameter Type</th>
            <th>Description</th>
            <th>Default value</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>roomname</td>
            <td>@Path</td>
            <td>Exact room name</td>
            <td></td>
        </tr>
        <tr>
            <td>name</td>
            <td>@Path</td>
            <td>The group name</td>
            <td></td>
        </tr>
        <tr>
            <td>roles</td>
            <td>@Path</td>
            <td>Available roles:  
**owners**  
**admins**  
**members**  
**outcasts**</td>
            <td></td>
        </tr>
        <tr>
            <td>servicename</td>
            <td>@QueryParam</td>
            <td>The name of the Group Chat Service</td>
            <td>conference</td>
        </tr>
    </tbody>
</table>

### Examples

> **Header:** Authorization: Basic YWRtaW46MTIzNDU=  
> **Header:** Content-Type application/xml

> **POST** [http://example.org:9090/plugins/restapi/v1/chatrooms/global/owners/group/testGroup](http://example.org:9090/plugins/restapi/v1/chatrooms/global/owners/group/testGroup)  
> **POST** [http://example.org:9090/plugins/restapi/v1/chatrooms/global/admins/group/testGroup](http://example.org:9090/plugins/restapi/v1/chatrooms/global/admins/group/testGroup)  
> **POST** [http://example.org:9090/plugins/restapi/v1/chatrooms/global/members/group/testGroup](http://example.org:9090/plugins/restapi/v1/chatrooms/global/members/group/testGroup)  
> **POST** [http://example.org:9090/plugins/restapi/v1/chatrooms/global/outcasts/group/testGroup](http://example.org:9090/plugins/restapi/v1/chatrooms/global/outcasts/group/testGroup)  
> **POST** [http://example.org:9090/plugins/restapi/v1/chatrooms/global/owners/group/testUser?servicename=privateconf](http://example.org:9090/plugins/restapi/v1/chatrooms/global/owners/group/testUser?servicename=privateconf)

## Delete a user from a chat room

Endpoint to remove a room user role.  
DELETE /chatrooms/{roomName}/{roles}/{name}

**Payload:** none  
**Return value:** HTTP status 200 (OK)

### Possible parameters

<table>
    <thead>
        <tr>
            <th>Parameter</th>
            <th>Parameter Type</th>
            <th>Description</th>
            <th>Default value</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>roomname</td>
            <td>@Path</td>
            <td>Exact room name</td>
            <td></td>
        </tr>
        <tr>
            <td>name</td>
            <td>@Path</td>
            <td>The local username or the user JID</td>
            <td></td>
        </tr>
        <tr>
            <td>roles</td>
            <td>@Path</td>
            <td>Available roles:  
**owners**  
**admins**  
**members**  
**outcasts**</td>
            <td></td>
        </tr>
        <tr>
            <td>servicename</td>
            <td>@QueryParam</td>
            <td>The name of the Group Chat Service</td>
            <td>conference</td>
        </tr>
    </tbody>
</table>

### Examples

> **Header:** Authorization: Basic YWRtaW46MTIzNDU=  
> **Header:** Content-Type application/xml

> **DELETE** [http://example.org:9090/plugins/restapi/v1/chatrooms/global/owners/testUser](http://example.org:9090/plugins/restapi/v1/chatrooms/global/owners/testUser)  
> **DELETE** [http://example.org:9090/plugins/restapi/v1/chatrooms/global/owners/testUser@openfire.com](http://example.org:9090/plugins/restapi/v1/chatrooms/global/owners/testUser@openfire.com)  
> **DELETE** [http://example.org:9090/plugins/restapi/v1/chatrooms/global/admins/testUser](http://example.org:9090/plugins/restapi/v1/chatrooms/global/admins/testUser)  
> **DELETE** [http://example.org:9090/plugins/restapi/v1/chatrooms/global/members/testUser](http://example.org:9090/plugins/restapi/v1/chatrooms/global/members/testUser)  
> **DELETE** [http://example.org:9090/plugins/restapi/v1/chatrooms/global/outcasts/testUser](http://example.org:9090/plugins/restapi/v1/chatrooms/global/outcasts/testUser)  
> **DELETE** [http://example.org:9090/plugins/restapi/v1/chatrooms/global/owners/testUser?servicename=privateconf](http://example.org:9090/plugins/restapi/v1/chatrooms/global/owners/testUser?servicename=privateconf)

# System related REST Endpoints

## Retrieve all system properties

Endpoint to get all system properties

> **GET** /system/properties

**Payload:** none  
**Return value:** System properties

### Examples

> **Header**: Authorization: Basic YWRtaW46MTIzNDU=

> **GET** [http://example.org:9090/plugins/restapi/v1/system/properties](http://example.org:9090/plugins/restapi/v1/system/properties)

## Retrieve system property

Endpoint to get information over specific system property

> **GET** /system/properties/{propertyName}

**Payload:** none  
**Return value:** System property

### Possible parameters

<table>
    <thead>
        <tr>
            <th>Parameter</th>
            <th>Parameter Type</th>
            <th>Description</th>
            <th>Default value</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>propertyName</td>
            <td>@Path</td>
            <td>The name of system property</td>
            <td></td>
        </tr>
    </tbody>
</table>

### Examples

**Header:** Authorization: Basic YWRtaW46MTIzNDU=

**GET** [http://example.org:9090/plugins/restapi/v1/system/properties/xmpp.domain](http://example.org:9090/plugins/restapi/v1/system/properties/xmpp.domain)

## Create a system property

Endpoint to create a system property

> **POST** system/properties

**Payload:** System Property  
**Return value:** HTTP status 201 (Created)

### Examples

> **Header:** Authorization: Basic YWRtaW46MTIzNDU=  
> **Header:** Content-Type: application/xml

> **POST** [http://example.org:9090/plugins/restapi/v1/system/properties](http://example.org:9090/plugins/restapi/v1/system/properties)

**Payload Example:**

    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
    <property key="propertyName" value="propertyValue"/>

## Delete a system property

Endpoint to delete a system property

> **DELETE** /system/properties/{propertyName}

**Payload:** none  
**Return value:** HTTP status 200 (OK)

### Possible parameters

<table>
    <thead>
        <tr>
            <th>Parameter</th>
            <th>Parameter Type</th>
            <th>Description</th>
            <th>Default value</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>propertyName</td>
            <td>@Path</td>
            <td>The name of system property</td>
            <td></td>
        </tr>
    </tbody>
</table>

### Examples

> **Header:** Authorization: Basic YWRtaW46MTIzNDU=

> **DELETE** [http://example.org:9090/plugins/restapi/v1/system/properties/propertyName](http://example.org:9090/plugins/restapi/v1/system/properties/propertyName)

## Update a system property

Endpoint to update / overwrite a system property

> **PUT** /system/properties/{propertyName}

**Payload:** System property  
**Return value:** HTTP status 200 (OK)

### Possible parameters

<table>
    <thead>
        <tr>
            <th>Parameter</th>
            <th>Parameter Type</th>
            <th>Description</th>
            <th>Default value</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>propertyName</td>
            <td>@Path</td>
            <td>The name of system property</td>
            <td></td>
        </tr>
    </tbody>
</table>

### Examples

> **Header:** Authorization: Basic YWRtaW46MTIzNDU=  
> **Header:** Content-Type application/xml

> **PUT** [http://example.org:9090/plugins/restapi/v1/system/properties/propertyName](http://example.org:9090/plugins/restapi/v1/system/properties/propertyName)

**Payload:**

    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
    <property key="propertyName" value="anotherValue"/>

## Retrieve concurrent sessions

Endpoint to get count of concurrent sessions

> **GET** /system/statistics/sessions

**Payload:** none  
**Return value:** Sessions count

### Examples

**Header:** Authorization: Basic YWRtaW46MTIzNDU=

**GET** [http://example.org:9090/plugins/restapi/v1/system/statistics/sessions](http://example.org:9090/plugins/restapi/v1/system/statistics/sessions)

# Group related REST Endpoints

## Retrieve all groups

Endpoint to get all groups

> **GET** /groups

**Payload:** none  
**Return value:** Groups

### Examples

> **Header**: Authorization: Basic YWRtaW46MTIzNDU=

> **GET** [http://example.org:9090/plugins/restapi/v1/groups](http://example.org:9090/plugins/restapi/v1/groups)

## Retrieve a group

Endpoint to get information over specific group

> **GET** /groups/{groupName}

**Payload:** none  
**Return value:** Group

### Possible parameters

<table>
    <thead>
        <tr>
            <th>Parameter</th>
            <th>Parameter Type</th>
            <th>Description</th>
            <th>Default value</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>groupName</td>
            <td>@Path</td>
            <td>The name of the group</td>
            <td></td>
        </tr>
    </tbody>
</table>

### Examples

**Header:** Authorization: Basic YWRtaW46MTIzNDU=

**GET** [http://example.org:9090/plugins/restapi/v1/groups/moderators](http://example.org:9090/plugins/restapi/v1/groups/moderators)

## Create a group

Endpoint to create a new group

> **POST** /groups

**Payload:** Group  
**Return value:** HTTP status 201 (Created)

### Examples

> **Header:** Authorization: Basic YWRtaW46MTIzNDU=  
> **Header:** Content-Type: application/xml

> **POST** [http://example.org:9090/plugins/restapi/v1/groups](http://example.org:9090/plugins/restapi/v1/groups)

**Payload Example:**

    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
    <group>
    	<name>GroupName</name>
    	<description>Some description</description>
    </group>

## Delete a group

Endpoint to delete a group

> **DELETE** /groups/{groupName}

**Payload:** none  
**Return value:** HTTP status 200 (OK)

### Possible parameters

<table>
    <thead>
        <tr>
            <th>Parameter</th>
            <th>Parameter Type</th>
            <th>Description</th>
            <th>Default value</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>groupName</td>
            <td>@Path</td>
            <td>The name of the group</td>
            <td></td>
        </tr>
    </tbody>
</table>

### Examples

> **Header:** Authorization: Basic YWRtaW46MTIzNDU=

> **DELETE** [http://example.org:9090/plugins/restapi/v1/groups/groupToDelete](http://example.org:9090/plugins/restapi/v1/groups/groupToDelete)

## Update a group

Endpoint to update / overwrite a group

> **PUT** /groups/{groupName}

**Payload:** Group  
**Return value:** HTTP status 200 (OK)

### Possible parameters

<table>
    <thead>
        <tr>
            <th>Parameter</th>
            <th>Parameter Type</th>
            <th>Description</th>
            <th>Default value</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>groupName</td>
            <td>@Path</td>
            <td>The name of the group</td>
            <td></td>
        </tr>
    </tbody>
</table>

### Examples

> **Header:** Authorization: Basic YWRtaW46MTIzNDU=  
> **Header:** Content-Type application/xml

> **PUT** [http://example.org:9090/plugins/restapi/v1/groups/groupNameToUpdate](http://example.org:9090/plugins/restapi/v1/groups/groupNameToUpdate)

**Payload:**

    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
    <group>
    	<name>groupNameToUpdate</name>
    	<description>New description</description>
    </group>

# Session related REST Endpoints

## Retrieve all user session

Endpoint to get all user sessions

> **GET** /sessions

**Payload:** none  
**Return value:** Sessions

### Examples

> **Header**: Authorization: Basic YWRtaW46MTIzNDU=

> **GET** [http://example.org:9090/plugins/restapi/v1/sessions](http://example.org:9090/plugins/restapi/v1/sessions)

## Retrieve the user sessions

Endpoint to get sessions from a user

> **GET** /sessions/{username}

**Payload:** none  
**Return value:** Sessions

### Possible parameters

<table>
    <thead>
        <tr>
            <th>Parameter</th>
            <th>Parameter Type</th>
            <th>Description</th>
            <th>Default value</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>username</td>
            <td>@Path</td>
            <td>The username of the user</td>
            <td></td>
        </tr>
    </tbody>
</table>

### Examples

**Header:** Authorization: Basic YWRtaW46MTIzNDU=

**GET** [http://example.org:9090/plugins/restapi/v1/sessions/testuser](http://example.org:9090/plugins/restapi/v1/sessions/testuser)

## Close all user sessions

Endpoint to close/kick sessions from a user

> **DELETE** /sessions/{username}

**Payload:** none  
**Return value:** HTTP status 200 (OK)

### Possible parameters

<table>
    <thead>
        <tr>
            <th>Parameter</th>
            <th>Parameter Type</th>
            <th>Description</th>
            <th>Default value</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>username</td>
            <td>@Path</td>
            <td>The username of the user</td>
            <td></td>
        </tr>
    </tbody>
</table>

### Examples

**Header:** Authorization: Basic YWRtaW46MTIzNDU=

**DELETE** [http://example.org:9090/plugins/restapi/v1/sessions/testuser](http://example.org:9090/plugins/restapi/v1/sessions/testuser)

# Message related REST Endpoints

## Send a broadcast message

Endpoint to send a broadcast/server message to all online users

> **POST** /messages/users

**Payload:** Message  
**Return value:** HTTP status 201 (Created)

### Examples

> **Header**: Authorization: Basic YWRtaW46MTIzNDU=

> **POST** [http://example.org:9090/plugins/restapi/v1/messages/users](http://example.org:9090/plugins/restapi/v1/messages/users)

**Payload:**

    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
    <message>
    	<body>Your message</body>
    </message>

# Security Audit related REST Endpoints

## Retrieve the Security audit logs

Endpoint to get security audit logs

> **GET** /logs/security

**Payload:** none  
**Return value:** Security Audit Logs

### Possible parameters

<table>
    <thead>
        <tr>
            <th>Parameter</th>
            <th>Parameter Type</th>
            <th>Description</th>
            <th>Default value</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>username</td>
            <td>@QueryParam</td>
            <td>Username of user to look up</td>
            <td></td>
        </tr>
        <tr>
            <td>startTime</td>
            <td>@QueryParam</td>
            <td>Oldest timestamp of range of logs to retrieve</td>
            <td></td>
        </tr>
        <tr>
            <td>endTime</td>
            <td>@QueryParam</td>
            <td>Most recent timestamp of range of logs to retrieve</td>
            <td>0 (until now)</td>
        </tr>
        <tr>
            <td>offset</td>
            <td>@QueryParam</td>
            <td>Number of logs to skip</td>
            <td></td>
        </tr>
        <tr>
            <td>limit</td>
            <td>@QueryParam</td>
            <td>Number of logs to retrieve</td>
            <td></td>
        </tr>
    </tbody>
</table>

### Examples

> **Header**: Authorization: Basic YWRtaW46MTIzNDU=

> **GET** [http://example.org:9090/plugins/restapi/v1/logs/security](http://example.org:9090/plugins/restapi/v1/logs/security)

# Data format

Openfire REST API provides XML and JSON as data format. The default data format is XML.  
To get a JSON result, please add “**Accept: application/json**” to the request header.  
If you want to create a resource with JSON data format, please add “**Content-Type: application/json**”.  
Since version RESP API 1.3.2 you can also use GZIP to compress the payload of the request or and the response.  
“**Content-Encoding: gzip**” for the request. “**Accept-Encoding: gzip**” for the response.

## Data types

### User

<table>
    <thead>
        <tr>
            <th>Parameter</th>
            <th>Optional</th>
            <th>Description</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>username</td>
            <td>No</td>
            <td>The username of the user</td>
        </tr>
        <tr>
            <td>name</td>
            <td>Yes</td>
            <td>The name of the user</td>
        </tr>
        <tr>
            <td>email</td>
            <td>Yes</td>
            <td>The email of the user</td>
        </tr>
        <tr>
            <td>password</td>
            <td>No</td>
            <td>The password of the user</td>
        </tr>
        <tr>
            <td>properties</td>
            <td>Yes</td>
            <td>List of properties. Property is a key / value object. The key must to be per user unique</td>
        </tr>
    </tbody>
</table>

### RosterItem

<table>
    <thead>
        <tr>
            <th>Parameter</th>
            <th>Optional</th>
            <th>Description</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>jid</td>
            <td>No</td>
            <td>The JID of the roster item</td>
        </tr>
        <tr>
            <td>nickname</td>
            <td>Yes</td>
            <td>The nickname for the user when used in this roster</td>
        </tr>
        <tr>
            <td>subscriptionType</td>
            <td>Yes</td>
            <td>The subscription type  
Possible numeric values are: -1 (remove), 0 (none), 1 (to), 2 (from), 3 (both)</td>
        </tr>
        <tr>
            <td>groups</td>
            <td>No</td>
            <td>A list of groups to organize roster entries under (e.g. friends, co-workers, etc.)</td>
        </tr>
    </tbody>
</table>

### Chatroom

<table>
    <thead>
        <tr>
            <th>Parameter</th>
            <th>Optional</th>
            <th>Description</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>roomName</td>
            <td>No</td>
            <td>The name/id of the room. Can only contains lowercase and alphanumeric characters.</td>
        </tr>
        <tr>
            <td>naturalName</td>
            <td>No</td>
            <td>Also the name of the room, but can contains non alphanumeric characters. It’s mainly used for users while discovering rooms hosted by the Multi-User Chat service.</td>
        </tr>
        <tr>
            <td>description</td>
            <td>No</td>
            <td>Description text of the room.</td>
        </tr>
        <tr>
            <td>subject</td>
            <td>Yes</td>
            <td>Subject of the room.</td>
        </tr>
        <tr>
            <td>password</td>
            <td>Yes</td>
            <td>The password that the user must provide to enter the room</td>
        </tr>
        <tr>
            <td>creationDate</td>
            <td>Yes</td>
            <td>The date when the room was created. Will be automatically set by creation. Example: 2014-07-10T09:49:12.411+02:00</td>
        </tr>
        <tr>
            <td>modificationDate</td>
            <td>Yes</td>
            <td>The last date when the room’s configuration was modified. If the room’s configuration was never modified then the initial value will be the same as the creation date. Will be automatically set by update. Example: 2014-07-10T09:49:12.411+02:00</td>
        </tr>
        <tr>
            <td>maxUsers</td>
            <td>Yes</td>
            <td>the maximum number of occupants that can be simultaneously in the room. 0 means unlimited number of occupants.</td>
        </tr>
        <tr>
            <td>persistent</td>
            <td>Yes</td>
            <td>Can be “true” or “false”. Persistent rooms are saved to the database to make their configurations persistent together with the affiliation of the users. Otherwise the room will be destroyed if the last occupant leave the room.</td>
        </tr>
        <tr>
            <td>publicRoom</td>
            <td>Yes</td>
            <td>Can be “true” or “false”. True if the room is searchable and visible through service discovery.</td>
        </tr>
        <tr>
            <td>registrationEnabled</td>
            <td>Yes</td>
            <td>Can be “true” or “false”. True if users are allowed to register with the room. By default, room registration is enabled.</td>
        </tr>
        <tr>
            <td>canAnyoneDiscoverJID</td>
            <td>Yes</td>
            <td>Can be “true” or “false”. True if every presence packet will include the JID of every occupant.</td>
        </tr>
        <tr>
            <td>canOccupantsChangeSubject</td>
            <td>Yes</td>
            <td>Can be “true” or “false”. True if participants are allowed to change the room’s subject.</td>
        </tr>
        <tr>
            <td>canOccupantsInvite</td>
            <td>Yes</td>
            <td>Can be “true” or “false”. True if occupants can invite other users to the room. If the room does not require an invitation to enter (i.e. is not members-only) then any occupant can send invitations. On the other hand, if the room is members-only and occupants cannot send invitation then only the room owners and admins are allowed to send invitations.</td>
        </tr>
        <tr>
            <td>canChangeNickname</td>
            <td>Yes</td>
            <td>Can be “true” or “false”. True if room occupants are allowed to change their nicknames in the room. By default, occupants are allowed to change their nicknames.</td>
        </tr>
        <tr>
            <td>logEnabled</td>
            <td>Yes</td>
            <td>Can be “true” or “false”. True if the room’s conversation is being logged. If logging is activated the room conversation will be saved to the database every couple of minutes. The saving frequency is the same for all the rooms and can be configured by changing the property “xmpp.muc.tasks.log.timeout”.</td>
        </tr>
        <tr>
            <td>loginRestrictedToNickname</td>
            <td>Yes</td>
            <td>Can be “true” or “false”. True if registered users can only join the room using their registered nickname. By default, registered users can join the room using any nickname.</td>
        </tr>
        <tr>
            <td>membersOnly</td>
            <td>Yes</td>
            <td>Can be “true” or “false”. True if the room requires an invitation to enter. That is if the room is members-only.</td>
        </tr>
        <tr>
            <td>moderated</td>
            <td>Yes</td>
            <td>Can be “true” or “false”. True if the room in which only those with “voice” may send messages to all occupants.</td>
        </tr>
        <tr>
            <td>broadcastPresenceRoles</td>
            <td>Yes</td>
            <td>The list of roles of which presence will be broadcasted to the rest of the occupants.</td>
        </tr>
        <tr>
            <td>owners</td>
            <td>Yes</td>
            <td>A collection with the current list of owners. The collection contains the bareJID of the users with owner affiliation.</td>
        </tr>
        <tr>
            <td>admins</td>
            <td>Yes</td>
            <td>A collection with the current list of admins. The collection contains the bareJID of the users with admin affiliation.</td>
        </tr>
        <tr>
            <td>members</td>
            <td>Yes</td>
            <td>A collection with the current list of room members. The collection contains the bareJID of the users with member affiliation. If the room is not members-only then the list will contain the users that registered with the room and therefore they may have reserved a nickname.</td>
        </tr>
        <tr>
            <td>outcasts</td>
            <td>Yes</td>
            <td>A collection with the current list of outcast users. An outcast user is not allowed to join the room again. The collection contains the bareJID of the users with outcast affiliation.</td>
        </tr>
        <tr>
            <td>ownerGroups</td>
            <td>Yes</td>
            <td>A collection with the current list of groups with owner affiliation. The collection contains the name only.</td>
        </tr>
        <tr>
            <td>adminGroups</td>
            <td>Yes</td>
            <td>A collection with the current list of groups with admin affiliation. The collection contains the name only.</td>
        </tr>
        <tr>
            <td>memberGroups</td>
            <td>Yes</td>
            <td>A collection with the current list of groups with member affiliation. The collection contains the name only.</td>
        </tr>
        <tr>
            <td>outcastGroups</td>
            <td>Yes</td>
            <td>A collection with the current list of groups with outcast affiliation. The collection contains the name only.</td>
        </tr>
    </tbody>
</table>

### Group

<table>
    <thead>
        <tr>
            <th>Parameter</th>
            <th>Optional</th>
            <th>Description</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>name</td>
            <td>No</td>
            <td>The name of the group</td>
        </tr>
        <tr>
            <td>description</td>
            <td>No</td>
            <td>The description of the group</td>
        </tr>
        <tr>
            <td>admins</td>
            <td>Yes</td>
            <td>A collection with current admins of the group</td>
        </tr>
        <tr>
            <td>members</td>
            <td>Yes</td>
            <td>A collection with current members of the group</td>
        </tr>
    </tbody>
</table>

### System Property

<table>
    <thead>
        <tr>
            <th>Parameter</th>
            <th>Optional</th>
            <th>Description</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>key</td>
            <td>No</td>
            <td>The name of the system property</td>
        </tr>
        <tr>
            <td>value</td>
            <td>No</td>
            <td>The value of the system property</td>
        </tr>
    </tbody>
</table>

### Session

<table>
    <thead>
        <tr>
            <th>Parameter</th>
            <th>Optional</th>
            <th>Description</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>sessionId</td>
            <td>No</td>
            <td>Full JID of a user e.g. ([testUser@testserver.de](mailto:testUser@testserver.de)/SomeRessource)</td>
        </tr>
        <tr>
            <td>username</td>
            <td>No</td>
            <td>The username associated with this session. Can be also “Anonymous”.</td>
        </tr>
        <tr>
            <td>ressource</td>
            <td>Yes</td>
            <td>Ressource name</td>
        </tr>
        <tr>
            <td>node</td>
            <td>No</td>
            <td>Can be “Local” or “Remote”</td>
        </tr>
        <tr>
            <td>sessionStatus</td>
            <td>No</td>
            <td>The current status of this session. Can be “Closed”, “Connected”, “Authenticated” or “Unknown”.</td>
        </tr>
        <tr>
            <td>presenceStatus</td>
            <td>No</td>
            <td>The status of this presence packet, a natural-language description of availability status.</td>
        </tr>
        <tr>
            <td>priority</td>
            <td>No</td>
            <td>The priority of the session. The valid priority range is -128 through 128.</td>
        </tr>
        <tr>
            <td>hostAddress</td>
            <td>No</td>
            <td>Tthe IP address string in textual presentation.</td>
        </tr>
        <tr>
            <td>hostName</td>
            <td>No</td>
            <td>The host name for this IP address.</td>
        </tr>
        <tr>
            <td>creationDate</td>
            <td>No</td>
            <td>The date the session was created.</td>
        </tr>
        <tr>
            <td>lastActionDate</td>
            <td>No</td>
            <td>The time the session last had activity.</td>
        </tr>
        <tr>
            <td>secure</td>
            <td>No</td>
            <td>Is “true” if this connection is secure.</td>
        </tr>
    </tbody>
</table>

### Sessions count

<table>
    <thead>
        <tr>
            <th>Parameter</th>
            <th>Optional</th>
            <th>Description</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>clusterSessions</td>
            <td>No</td>
            <td>Number of client sessions that are authenticated with the server. This includes anonymous and non-anoymous users from the whole cluster.</td>
        </tr>
        <tr>
            <td>localSessions</td>
            <td>No</td>
            <td>Number of client sessions that are authenticated with the server. This includes anonymous and non-anoymous users.</td>
        </tr>
    </tbody>
</table>

### Security Audit Logs

<table>
    <thead>
        <tr>
            <th>Parameter</th>
            <th>Optional</th>
            <th>Description</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>logId</td>
            <td>No</td>
            <td>unique ID of this log</td>
        </tr>
        <tr>
            <td>username</td>
            <td>No</td>
            <td>the username of the user who performed this event</td>
        </tr>
        <tr>
            <td>timestamp</td>
            <td>No</td>
            <td>the time stamp of when this event occurred</td>
        </tr>
        <tr>
            <td>summary</td>
            <td>No</td>
            <td>the summary, or short description of what transpired in the event</td>
        </tr>
        <tr>
            <td>node</td>
            <td>No</td>
            <td>the node that triggered the event, usually a hostname or IP address</td>
        </tr>
        <tr>
            <td>details</td>
            <td>No</td>
            <td>detailed information about what occurred in the event</td>
        </tr>
    </tbody>
</table>

# (Deprecated) User Service Plugin Readme

## Overview

The User Service Plugin provides the ability to add,edit,delete users and manage their rosters by sending an http request to the server. It is intended to be used by applications automating the user administration process. This plugin’s functionality is useful for applications that need to administer users outside of the Openfire admin console. An example of such an application might be a live sports reporting application that uses XMPP as its transport, and creates/deletes users according to the receipt, or non receipt, of a subscription fee.

## Installation

Copy userservice.jar into the plugins directory of your Openfire server. The plugin will then be automatically deployed. To upgrade to a new version, copy the new userservice.jar file over the existing file.

## Configuration

Access to the service is restricted with a “secret” that can be viewed and set from the User Service page in the Openfire admin console. This page is located on the admin console under “Server” and then “Server Settings”. This should really only be considered weak security. The plugin was initially written with the assumption that http access to the Openfire service was only available to trusted machines. In the case of the plugin’s author, a web application running on the same server as Openfire makes the request.

## Using the Plugin

To administer users, submit HTTP requests to the userservice service. The service address is [hostname]plugins/restapi/userservice. For example, if your server name is “[example.com](http://example.com)”, the URL is [http://example.com/plugins/restapi/userservice](http://example.com/plugins/restapi/userservice)

The following parameters can be passed into the request:

<table>
    <thead>
        <tr>
            <th>Name</th>
            <th></th>
            <th>Description</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>type</td>
            <td>Required</td>
            <td>The admin service required. Possible values are ‘add’, ‘delete’, ‘update’, ‘enable’, ‘disable’, ‘add_roster’, ‘update_roster’, ‘delete_roster’, ‘grouplist’, ‘usergrouplist’.</td>
        </tr>
        <tr>
            <td>secret</td>
            <td>Required</td>
            <td>The secret key that allows access to the User Service.</td>
        </tr>
        <tr>
            <td>username</td>
            <td>Required</td>
            <td>The username of the user to ‘add’, ‘delete’, ‘update’, ‘enable’, ‘disable’, ‘add_roster’, ‘update_roster’, ‘delete_roster’. ie the part before the @ symbol.</td>
        </tr>
        <tr>
            <td>password</td>
            <td>Required for ‘add’ operation</td>
            <td>The password of the new user or the user being updated.</td>
        </tr>
        <tr>
            <td>name</td>
            <td>Optional</td>
            <td>The display name of the new user or the user being updated. For ‘add_roster’, ‘update_roster’ operations specifies the nickname of the roster item.</td>
        </tr>
        <tr>
            <td>email</td>
            <td>Optional</td>
            <td>The email address of the new user or the user being updated.</td>
        </tr>
        <tr>
            <td>groups</td>
            <td>Optional</td>
            <td>List of groups where the user is a member. Values are comma delimited. When used with types “add” or “update”, it adds the user to shared groups and auto-creates new groups. When used with ‘add_roster’ and ‘update_roster’, it adds the user to roster groups provided the group name does not clash with an existing shared group.</td>
        </tr>
        <tr>
            <td>item_jid</td>
            <td>Required for ‘add_roster’, ‘update_roster’, ‘delete_roster’ operations.</td>
            <td>The JID of the roster item</td>
        </tr>
        <tr>
            <td>subscription</td>
            <td>Optional</td>
            <td>Type of subscription for ‘add_roster’, ‘update_roster’ operations. Possible numeric values are: -1(remove), 0(none), 1(to), 2(from), 3(both).</td>
        </tr>
    </tbody>
</table>

## Sample HTML

The following example adds a user

[http://example.com:9090/plugins/restapi/userservice?type=add&secret=bigsecret&username=kafka&password=drowssap&name=franz&email=franz@kafka.com](http://example.com:9090/plugins/restapi/userservice?type=add&secret=bigsecret&username=kafka&password=drowssap&name=franz&email=franz@kafka.com)

The following example adds a user, adds two shared groups (if not existing) and adds the user to both groups.

[http://example.com:9090/plugins/restapi/userservice?type=add&secret=bigsecret&username=kafka&password=drowssap&name=franz&email=franz@kafka.com&groups=support,finance](http://example.com:9090/plugins/restapi/userservice?type=add&secret=bigsecret&username=kafka&password=drowssap&name=franz&email=franz@kafka.com&groups=support,finance)

The following example deletes a user and all roster items of the user.

[http://example.com:9090/plugins/restapi/userservice?type=delete&secret=bigsecret&username=kafka](http://example.com:9090/plugins/restapi/userservice?type=delete&secret=bigsecret&username=kafka)

The following example disables a user (lockout)

[http://example.com:9090/plugins/restapi/userservice?type=disable&secret=bigsecret&username=kafka](http://example.com:9090/plugins/restapi/userservice?type=disable&secret=bigsecret&username=kafka)

The following example enables a user (removes lockout)

[http://example.com:9090/plugins/restapi/userservice?type=enable&secret=bigsecret&username=kafka](http://example.com:9090/plugins/restapi/userservice?type=enable&secret=bigsecret&username=kafka)

The following example updates a user

[http://example.com:9090/plugins/restapi/userservice?type=update&secret=bigsecret&username=kafka&password=drowssap&name=franz&email=beetle@kafka.com](http://example.com:9090/plugins/restapi/userservice?type=update&secret=bigsecret&username=kafka&password=drowssap&name=franz&email=beetle@kafka.com)

The following example adds new roster item with subscription ‘both’ for user ‘kafka’

[http://example.com:9090/plugins/restapi/userservice?type=add_roster&secret=bigsecret&username=kafka&item_jid=franz@example.com&name=franz&subscription=3](http://example.com:9090/plugins/restapi/userservice?type=add_roster&secret=bigsecret&username=kafka&item_jid=franz@example.com&name=franz&subscription=3)

The following example adds new roster item with subscription ‘both’ for user ‘kafka’ and adds kafka to roster groups ‘family’ and ‘friends’

[http://example.com:9090/plugins/restapi/userservice?type=add_roster&secret=bigsecret&username=kafka&item_jid=franz@example.com&name=franz&subscription=3&groups=family,friends](http://example.com:9090/plugins/restapi/userservice?type=add_roster&secret=bigsecret&username=kafka&item_jid=franz@example.com&name=franz&subscription=3&groups=family,friends)

The following example updates existing roster item to subscription ‘none’ for user ‘kafka’

[http://example.com:9090/plugins/restapi/userservice?type=update_roster&secret=bigsecret&username=kafka&item_jid=franz@example.com&name=franz&subscription=0](http://example.com:9090/plugins/restapi/userservice?type=update_roster&secret=bigsecret&username=kafka&item_jid=franz@example.com&name=franz&subscription=0)

The following example deletes a specific roster item ‘franz@kafka.com’ for user ‘kafka’

[http://example.com:9090/plugins/restapi/userservice?type=delete_roster&secret=bigsecret&username=kafka&item_jid=franz@example.com](http://example.com:9090/plugins/restapi/userservice?type=delete_roster&secret=bigsecret&username=kafka&item_jid=franz@example.com)

The following example gets all groups

[http://example.com:9090/plugins/restapi/userservice?type=grouplist&secret=bigsecret](http://example.com:9090/plugins/restapi/userservice?type=grouplist&secret=bigsecret)  
Which replies an XML group list formatted like this:

    <result>
        <groupname>group1</groupname>
        <groupname>group2</groupname>
    </result>

The following example gets all groups for a specific user

[http://example.com:9090/plugins/restapi/userservice?type=usergrouplist&secret=bigsecret&username=kafka](http://example.com:9090/plugins/restapi/userservice?type=usergrouplist&secret=bigsecret&username=kafka)  
Which replies an XML group list formatted like this:

    <result>
        <groupname>usergroup1</groupname>
        <groupname>usergroup2</groupname>
    </result>

* When sending double characters (Chinese/Japanese/Korean etc) you should URLEncode the string as utf8.  
In Java this is done like this  
URLEncoder.encode(username, “UTF-8”));  
If the strings are encoded incorrectly, double byte characters will look garbeled in the Admin Console.

## Server Reply

The server will reply to all User Service requests with an XML result page. If the request was processed successfully the return will be a “result” element with a text body of “OK”, or an XML grouplist formatted like in the example for “grouplist” and “usergrouplist” above. If the request was unsuccessful, the return will be an “error” element with a text body of one of the following error strings.

<table>
    <thead>
        <tr>
            <th>Error String</th>
            <th>Description</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>IllegalArgumentException</td>
            <td>One of the parameters passed in to the User Service was bad.</td>
        </tr>
        <tr>
            <td>UserNotFoundException</td>
            <td>No user of the name specified, for a delete or update operation, exists on this server. For ‘update_roster’ operation, roster item to be updated was not found.</td>
        </tr>
        <tr>
            <td>UserAlreadyExistsException</td>
            <td>A user with the same name as the user about to be added, already exists. For ‘add_roster’ operation, roster item with the same JID already exists.</td>
        </tr>
        <tr>
            <td>RequestNotAuthorised</td>
            <td>The supplied secret does not match the secret specified in the Admin Console or the requester is not a valid IP address.</td>
        </tr>
        <tr>
            <td>UserServiceDisabled</td>
            <td>The User Service is currently set to disabled in the Admin Console.</td>
        </tr>
        <tr>
            <td>SharedGroupException</td>
            <td>Roster item can not be added/deleted to/from a shared group for operations with roster.</td>
        </tr>
    </tbody>
</table>
