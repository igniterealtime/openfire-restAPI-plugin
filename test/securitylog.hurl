# TEST: An admin console login is recorded as a new event in the security audit log
# The log may already contain events (e.g. from earlier runs). Events are returned newest first, so remember the ID of
# the newest one (or 0 when the log is empty), to be able to tell which events are new.
GET {{restapi_url}}/logs/security?limit=1
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/logs" exists
xpath "/logs/log" count <= 1
[Captures]
last_log_id: xpath "number(concat('0', /logs/log/logId))"


# Openfire records a security audit event for every login to the admin console, so log in to create one.
GET {{adminconsole_url}}/login.jsp
HTTP 200
[Captures]
csrf: cookie "csrf"

POST {{adminconsole_url}}/login.jsp
[Form]
login: true
csrf: {{csrf}}
username: admin
password: admin
HTTP 302
[Asserts]
header "Location" == "/index.jsp"


GET {{restapi_url}}/logs/security
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/logs/log[logId > {{last_log_id}}]" count == 1
xpath "string(/logs/log[logId > {{last_log_id}}]/summary)" == "Successful admin console login attempt"
xpath "string(/logs/log[logId > {{last_log_id}}]/username)" == "admin"
xpath "string(/logs/log[logId > {{last_log_id}}]/node)" == "example.org"
xpath "string(/logs/log[logId > {{last_log_id}}]/details)" startsWith "The user logged in successfully to the admin console"
xpath "/logs/log[logId > {{last_log_id}}]/timestamp" exists

# TEST: Filtering by username returns only that user's events
GET {{restapi_url}}/logs/security?username=admin
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/logs/log[logId > {{last_log_id}}]" count == 1
xpath "/logs/log[username!='admin']" not exists

# TEST: Filtering by an unknown username returns no events
GET {{restapi_url}}/logs/security?username=nonexistent
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/logs/log" count == 0

# TEST: The number of events returned can be limited, newest first
GET {{restapi_url}}/logs/security?limit=1
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/logs/log" count == 1
xpath "/logs/log[logId > {{last_log_id}}]" count == 1

# TEST: Skipping more events than exist returns no events
GET {{restapi_url}}/logs/security?offset=1000000
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/logs/log" count == 0

# TEST: Filtering by a start time in the future returns no events
# Timestamps are in seconds since the epoch: nothing was logged after 2100-01-01, or before 1970-01-01T00:00:01.
GET {{restapi_url}}/logs/security?startTime=4102444800
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/logs/log" count == 0

# TEST: Filtering by an end time in the distant past returns no events
GET {{restapi_url}}/logs/security?endTime=1
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/logs/log" count == 0

# TEST: Filtering by a time range includes the new event
GET {{restapi_url}}/logs/security?startTime=1&endTime=4102444800
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "/logs/log[logId > {{last_log_id}}]" count == 1
