# Lockouts are tested on a dedicated user, which is removed at the end.
POST {{restapi_url}}/users
Authorization: {{authkey}}
Content-Type: application/xml
```
<?xml version="1.0" encoding="UTF-8"?>
<user>
	<username>worf</username>
	<password>qapla</password>
</user>
```
HTTP 201

# TEST: Locking out a user closes its sessions
# Log in as worf over BOSH (XEP-0124/XEP-0206), so that there is a session for the lockout to close.
POST {{bosh_url}}/http-bind/
Content-Type: text/xml; charset=utf-8
```
<body rid='1000' to='example.org' xml:lang='en' wait='10' hold='1' ver='1.6' xmpp:version='1.0' xmlns:xmpp='urn:xmpp:xbosh' xmlns='http://jabber.org/protocol/httpbind'/>
```
HTTP 200
[Captures]
sid: xpath "string(/_:body/@sid)"

# SASL PLAIN, with base64("\0worf\0qapla")
POST {{bosh_url}}/http-bind/
Content-Type: text/xml; charset=utf-8
```
<body rid='1001' sid='{{sid}}' xmlns='http://jabber.org/protocol/httpbind'><auth xmlns='urn:ietf:params:xml:ns:xmpp-sasl' mechanism='PLAIN'>AHdvcmYAcWFwbGE=</auth></body>
```
HTTP 200
[Asserts]
xpath "//*[local-name()='success']" exists

POST {{bosh_url}}/http-bind/
Content-Type: text/xml; charset=utf-8
```
<body rid='1002' sid='{{sid}}' to='example.org' xml:lang='en' xmpp:restart='true' xmlns:xmpp='urn:xmpp:xbosh' xmlns='http://jabber.org/protocol/httpbind'/>
```
HTTP 200

POST {{bosh_url}}/http-bind/
Content-Type: text/xml; charset=utf-8
```
<body rid='1003' sid='{{sid}}' xmlns='http://jabber.org/protocol/httpbind'><iq type='set' id='bind' xmlns='jabber:client'><bind xmlns='urn:ietf:params:xml:ns:xmpp-bind'><resource>hurl</resource></bind></iq></body>
```
HTTP 200

POST {{bosh_url}}/http-bind/
Content-Type: text/xml; charset=utf-8
```
<body rid='1004' sid='{{sid}}' xmlns='http://jabber.org/protocol/httpbind'><presence xmlns='jabber:client'/></body>
```
HTTP 200

GET {{restapi_url}}/sessions/worf
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/sessions/session" count == 1

POST {{restapi_url}}/lockouts/worf
Authorization: {{authkey}}
HTTP 201

# Sessions are closed asynchronously, so allow for some delay.
GET {{restapi_url}}/sessions/worf
Authorization: {{authkey}}
[Options]
retry: 10
retry-interval: 100ms
HTTP 200
[Asserts]
xpath "/sessions/session" count == 0

# The BOSH session no longer exists either. (A poll that is made while the session is being closed is still answered.)
POST {{bosh_url}}/http-bind/
Content-Type: text/xml; charset=utf-8
[Options]
retry: 10
retry-interval: 100ms
```
<body rid='1005' sid='{{sid}}' xmlns='http://jabber.org/protocol/httpbind'/>
```
HTTP 404

# TEST: Locking out a user that is already locked out is fine
POST {{restapi_url}}/lockouts/worf
Authorization: {{authkey}}
HTTP 201

# TEST: A user that is locked out cannot log in
POST {{bosh_url}}/http-bind/
Content-Type: text/xml; charset=utf-8
```
<body rid='2000' to='example.org' xml:lang='en' wait='10' hold='1' ver='1.6' xmpp:version='1.0' xmlns:xmpp='urn:xmpp:xbosh' xmlns='http://jabber.org/protocol/httpbind'/>
```
HTTP 200
[Captures]
sid: xpath "string(/_:body/@sid)"

# SASL PLAIN, with base64("\0worf\0qapla")
POST {{bosh_url}}/http-bind/
Content-Type: text/xml; charset=utf-8
```
<body rid='2001' sid='{{sid}}' xmlns='http://jabber.org/protocol/httpbind'><auth xmlns='urn:ietf:params:xml:ns:xmpp-sasl' mechanism='PLAIN'>AHdvcmYAcWFwbGE=</auth></body>
```
HTTP 200
[Asserts]
xpath "//*[local-name()='failure']/*[local-name()='not-authorized']" exists

POST {{bosh_url}}/http-bind/
Content-Type: text/xml; charset=utf-8
```
<body rid='2002' sid='{{sid}}' type='terminate' xmlns='http://jabber.org/protocol/httpbind'/>
```
HTTP 200

# TEST: Unlock a user
DELETE {{restapi_url}}/lockouts/worf
Authorization: {{authkey}}
HTTP 200

# TEST: Unlocking a user that is not locked out is fine
DELETE {{restapi_url}}/lockouts/worf
Authorization: {{authkey}}
HTTP 200

# TEST: A user that is no longer locked out can log in again
POST {{bosh_url}}/http-bind/
Content-Type: text/xml; charset=utf-8
```
<body rid='3000' to='example.org' xml:lang='en' wait='10' hold='1' ver='1.6' xmpp:version='1.0' xmlns:xmpp='urn:xmpp:xbosh' xmlns='http://jabber.org/protocol/httpbind'/>
```
HTTP 200
[Captures]
sid: xpath "string(/_:body/@sid)"

# SASL PLAIN, with base64("\0worf\0qapla")
POST {{bosh_url}}/http-bind/
Content-Type: text/xml; charset=utf-8
```
<body rid='3001' sid='{{sid}}' xmlns='http://jabber.org/protocol/httpbind'><auth xmlns='urn:ietf:params:xml:ns:xmpp-sasl' mechanism='PLAIN'>AHdvcmYAcWFwbGE=</auth></body>
```
HTTP 200
[Asserts]
xpath "//*[local-name()='success']" exists

POST {{bosh_url}}/http-bind/
Content-Type: text/xml; charset=utf-8
```
<body rid='3002' sid='{{sid}}' to='example.org' xml:lang='en' xmpp:restart='true' xmlns:xmpp='urn:xmpp:xbosh' xmlns='http://jabber.org/protocol/httpbind'/>
```
HTTP 200

POST {{bosh_url}}/http-bind/
Content-Type: text/xml; charset=utf-8
```
<body rid='3003' sid='{{sid}}' xmlns='http://jabber.org/protocol/httpbind'><iq type='set' id='bind' xmlns='jabber:client'><bind xmlns='urn:ietf:params:xml:ns:xmpp-bind'><resource>hurl</resource></bind></iq></body>
```
HTTP 200

POST {{bosh_url}}/http-bind/
Content-Type: text/xml; charset=utf-8
```
<body rid='3004' sid='{{sid}}' xmlns='http://jabber.org/protocol/httpbind'><presence xmlns='jabber:client'/></body>
```
HTTP 200

POST {{bosh_url}}/http-bind/
Content-Type: text/xml; charset=utf-8
```
<body rid='3005' sid='{{sid}}' type='terminate' xmlns='http://jabber.org/protocol/httpbind'/>
```
HTTP 200

# TEST: Locking out a user that does not exist fails
POST {{restapi_url}}/lockouts/nonexistent
Authorization: {{authkey}}
HTTP 404
[Asserts]
xpath "string(/error/exception)" == "UserNotFoundException"
xpath "string(/error/resource)" == "nonexistent"

# TEST: Unlocking a user that does not exist fails
DELETE {{restapi_url}}/lockouts/nonexistent
Authorization: {{authkey}}
HTTP 404
[Asserts]
xpath "string(/error/exception)" == "UserNotFoundException"
xpath "string(/error/resource)" == "nonexistent"


DELETE {{restapi_url}}/users/worf
Authorization: {{authkey}}
HTTP 200
