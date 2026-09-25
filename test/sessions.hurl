GET {{restapi_url}}/sessions
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/sessions" exists
xpath "/sessions[not(child::node())]" count == 1 # sessions at the root, with no child nodes

GET {{restapi_url}}/sessions/john
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/sessions" exists
xpath "/sessions/session" count == 0

# Openfire does not check whether the user exists: a user that does not exist simply has no sessions. The API documents
# no other response than 200 for this endpoint.
GET {{restapi_url}}/sessions/nonexistent
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/sessions" exists
xpath "/sessions/session" count == 0


# Create a session for john, by logging in over BOSH (XEP-0124/XEP-0206).
POST {{bosh_url}}/http-bind/
Content-Type: text/xml; charset=utf-8
```
<body rid='1000' to='example.org' xml:lang='en' wait='10' hold='1' ver='1.6' xmpp:version='1.0' xmlns:xmpp='urn:xmpp:xbosh' xmlns='http://jabber.org/protocol/httpbind'/>
```
HTTP 200
[Captures]
sid: xpath "string(/_:body/@sid)"

# SASL PLAIN, with base64("\0john\0secret")
POST {{bosh_url}}/http-bind/
Content-Type: text/xml; charset=utf-8
```
<body rid='1001' sid='{{sid}}' xmlns='http://jabber.org/protocol/httpbind'><auth xmlns='urn:ietf:params:xml:ns:xmpp-sasl' mechanism='PLAIN'>AGpvaG4Ac2VjcmV0</auth></body>
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
[Asserts]
xpath "string(//*[local-name()='jid'])" == "john@example.org/hurl"

# The session is only listed once it has sent initial presence.
POST {{bosh_url}}/http-bind/
Content-Type: text/xml; charset=utf-8
```
<body rid='1004' sid='{{sid}}' xmlns='http://jabber.org/protocol/httpbind'><presence xmlns='jabber:client'/></body>
```
HTTP 200

GET {{restapi_url}}/sessions
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/sessions/session[sessionId='john@example.org/hurl']" exists

GET {{restapi_url}}/sessions/john
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/sessions/session" count == 1
xpath "string(/sessions/session/sessionId)" == "john@example.org/hurl"
xpath "string(/sessions/session/username)" == "john"
xpath "string(/sessions/session/resource)" == "hurl"
xpath "string(/sessions/session/node)" == "Local"
xpath "string(/sessions/session/sessionStatus)" == "Authenticated"
xpath "string(/sessions/session/presenceStatus)" == "Online"
xpath "string(/sessions/session/priority)" == "0"
xpath "/sessions/session/hostAddress" exists
xpath "/sessions/session/creationDate" exists
xpath "/sessions/session/lastActionDate" exists

GET {{restapi_url}}/sessions/jane
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/sessions/session" count == 0

# Kick the session.
DELETE {{restapi_url}}/sessions/john
Authorization: {{authkey}}
HTTP 200

# Sessions are closed asynchronously, so allow for some delay.
GET {{restapi_url}}/sessions/john
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

# Kicking the sessions of a user without sessions is fine.
DELETE {{restapi_url}}/sessions/john
Authorization: {{authkey}}
HTTP 200
