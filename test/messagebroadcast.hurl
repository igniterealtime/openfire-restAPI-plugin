POST http://localhost:9090/plugins/restapi/v1/messages/users
Authorization: {{authkey}}
Content-Type: application/xml
```
<?xml version="1.0" encoding="UTF-8"?>
<message>
	<body>test</body>
</message>
```
HTTP 201

# TODO: What could validate behaviour?