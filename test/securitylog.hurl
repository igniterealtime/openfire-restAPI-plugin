GET http://localhost:9090/plugins/restapi/v1/logs/security
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/logs" exists


# Openfire records a security audit event for every login to the admin console, so log in to create one.
GET http://localhost:9090/login.jsp
HTTP 200
[Captures]
csrf: cookie "csrf"

POST http://localhost:9090/login.jsp
[Form]
login: true
csrf: {{csrf}}
username: admin
password: admin
HTTP 302
[Asserts]
header "Location" == "/index.jsp"


GET http://localhost:9090/plugins/restapi/v1/logs/security
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/logs/log" count >= 1
xpath "/logs/log[summary='Successful admin console login attempt'][username='admin']" exists
xpath "/logs/log/details" exists
xpath "/logs/log/logId" exists
xpath "/logs/log/node" exists
xpath "/logs/log/summary" exists
xpath "/logs/log/timestamp" exists
xpath "/logs/log/username" exists

GET http://localhost:9090/plugins/restapi/v1/logs/security?username=admin
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/logs/log" count >= 1
xpath "/logs/log[username!='admin']" not exists

GET http://localhost:9090/plugins/restapi/v1/logs/security?username=nonexistent
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/logs/log" count == 0

GET http://localhost:9090/plugins/restapi/v1/logs/security?limit=1
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/logs/log" count == 1

GET http://localhost:9090/plugins/restapi/v1/logs/security?offset=1000000
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/logs/log" count == 0

# Timestamps are in seconds since the epoch: nothing was logged after 2100-01-01, or before 1970-01-01T00:00:01.
GET http://localhost:9090/plugins/restapi/v1/logs/security?startTime=4102444800
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/logs/log" count == 0

GET http://localhost:9090/plugins/restapi/v1/logs/security?endTime=1
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/logs/log" count == 0

GET http://localhost:9090/plugins/restapi/v1/logs/security?startTime=1&endTime=4102444800
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/logs/log[summary='Successful admin console login attempt']" exists
