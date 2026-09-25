# vCards are tested on a dedicated user, which is removed at the end.
POST {{restapi_url}}/users
Authorization: {{authkey}}
Content-Type: application/xml
```
<?xml version="1.0" encoding="UTF-8"?>
<user>
	<username>troi</username>
	<password>imzadi</password>
</user>
```
HTTP 201


# TEST: A user without a vCard has no content
GET {{restapi_url}}/users/troi/vcard
Authorization: {{authkey}}
HTTP 204

# TEST: A stored vCard can be retrieved
PUT {{restapi_url}}/users/troi/vcard
Authorization: {{authkey}}
Content-Type: application/xml
```
<vCard xmlns="vcard-temp"><FN>Deanna Troi</FN><NICKNAME>Counselor</NICKNAME><EMAIL><INTERNET/><USERID>troi@example.com</USERID></EMAIL></vCard>
```
HTTP 200

GET {{restapi_url}}/users/troi/vcard
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "string(/_:vCard/_:FN)" == "Deanna Troi"
xpath "string(/_:vCard/_:NICKNAME)" == "Counselor"
xpath "string(/_:vCard/_:EMAIL/_:USERID)" == "troi@example.com"

# TEST: Storing a vCard replaces the previous one
PUT {{restapi_url}}/users/troi/vcard
Authorization: {{authkey}}
Content-Type: application/xml
```
<vCard xmlns="vcard-temp"><FN>Deanna Troi</FN><TITLE>Ship's Counselor</TITLE></vCard>
```
HTTP 200

GET {{restapi_url}}/users/troi/vcard
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "string(/_:vCard/_:TITLE)" == "Ship's Counselor"
xpath "/_:vCard/_:NICKNAME" not exists

# TEST: Delete a vCard
DELETE {{restapi_url}}/users/troi/vcard
Authorization: {{authkey}}
HTTP 200

GET {{restapi_url}}/users/troi/vcard
Authorization: {{authkey}}
HTTP 204

# TEST: Deleting a vCard that does not exist is fine
DELETE {{restapi_url}}/users/troi/vcard
Authorization: {{authkey}}
HTTP 200

# TEST: A user that does not exist has no vCard
GET {{restapi_url}}/users/nonexistent/vcard
Authorization: {{authkey}}
HTTP 204

# TEST: Storing a vCard for a user that does not exist fails
# Disabled until https://github.com/igniterealtime/openfire-restAPI-plugin/issues/281 is fixed.
PUT {{restapi_url}}/users/nonexistent/vcard
Authorization: {{authkey}}
Content-Type: application/xml
[Options]
skip: true
```
<vCard xmlns="vcard-temp"><FN>Nobody</FN></vCard>
```
HTTP 404

GET {{restapi_url}}/users/nonexistent/vcard
Authorization: {{authkey}}
[Options]
skip: true
HTTP 204

# TEST: Storing a document that is not a vCard is rejected
# Disabled until https://github.com/igniterealtime/openfire-restAPI-plugin/issues/282 is fixed.
PUT {{restapi_url}}/users/troi/vcard
Authorization: {{authkey}}
Content-Type: application/xml
[Options]
skip: true
```
<notavcard/>
```
HTTP 400

PUT {{restapi_url}}/users/troi/vcard
Authorization: {{authkey}}
Content-Type: application/xml
[Options]
skip: true
```
<vCard xmlns="wrong-namespace"><FN>Deanna Troi</FN></vCard>
```
HTTP 400

GET {{restapi_url}}/users/troi/vcard
Authorization: {{authkey}}
[Options]
skip: true
HTTP 204


DELETE {{restapi_url}}/users/troi
Authorization: {{authkey}}
HTTP 200
