# Broadcasting without any sessions to deliver to is fine.
POST {{restapi_url}}/messages/users
Authorization: {{authkey}}
Content-Type: application/xml
```
<?xml version="1.0" encoding="UTF-8"?>
<message>
	<body>test</body>
</message>
```
HTTP 201

POST {{restapi_url}}/messages/users
Authorization: {{authkey}}
Content-Type: application/xml
```
<?xml version="1.0" encoding="UTF-8"?>
<message>
	<body></body>
</message>
```
HTTP 400
[Asserts]
xpath "string(/error/exception)" == "IllegalArgumentException"

POST {{restapi_url}}/messages/users
Authorization: {{authkey}}
Content-Type: application/xml
```
<?xml version="1.0" encoding="UTF-8"?>
<message/>
```
HTTP 400
[Asserts]
xpath "string(/error/exception)" == "IllegalArgumentException"


# Log in anonymously over BOSH (XEP-0124/XEP-0206), to be able to receive the broadcast.
POST {{bosh_url}}/http-bind/
Content-Type: text/xml; charset=utf-8
```
<body rid='1000' to='example.org' xml:lang='en' wait='10' hold='1' ver='1.6' xmpp:version='1.0' xmlns:xmpp='urn:xmpp:xbosh' xmlns='http://jabber.org/protocol/httpbind'/>
```
HTTP 200
[Captures]
sid: xpath "string(/_:body/@sid)"

POST {{bosh_url}}/http-bind/
Content-Type: text/xml; charset=utf-8
```
<body rid='1001' sid='{{sid}}' xmlns='http://jabber.org/protocol/httpbind'><auth xmlns='urn:ietf:params:xml:ns:xmpp-sasl' mechanism='ANONYMOUS'/></body>
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
<body rid='1003' sid='{{sid}}' xmlns='http://jabber.org/protocol/httpbind'><iq type='set' id='bind' xmlns='jabber:client'><bind xmlns='urn:ietf:params:xml:ns:xmpp-bind'/></iq></body>
```
HTTP 200

POST {{bosh_url}}/http-bind/
Content-Type: text/xml; charset=utf-8
```
<body rid='1004' sid='{{sid}}' xmlns='http://jabber.org/protocol/httpbind'><presence xmlns='jabber:client'/></body>
```
HTTP 200

POST {{restapi_url}}/messages/users
Authorization: {{authkey}}
Content-Type: application/xml
```
<?xml version="1.0" encoding="UTF-8"?>
<message>
	<body>Hello from the REST API</body>
</message>
```
HTTP 201

# Poll for the broadcast, which is delivered as a headline message from the server.
POST {{bosh_url}}/http-bind/
Content-Type: text/xml; charset=utf-8
```
<body rid='1005' sid='{{sid}}' xmlns='http://jabber.org/protocol/httpbind'/>
```
HTTP 200
[Asserts]
xpath "//*[local-name()='message']" count == 1
xpath "string(//*[local-name()='message']/@from)" == "example.org"
xpath "string(//*[local-name()='message']/@type)" == "headline"
xpath "string(//*[local-name()='message']/*[local-name()='body'])" == "Hello from the REST API"

# Log out.
POST {{bosh_url}}/http-bind/
Content-Type: text/xml; charset=utf-8
```
<body rid='1006' sid='{{sid}}' type='terminate' xmlns='http://jabber.org/protocol/httpbind'><presence type='unavailable' xmlns='jabber:client'/></body>
```
HTTP 200
[Asserts]
xpath "string(/_:body/@type)" == "terminate"
