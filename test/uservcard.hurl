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

# TODO: Storing a vCard for a user that does not exist returns 200 (and stores the vCard), and any XML document (e.g.
# <notavcard/>) is accepted and stored as a vCard. Add tests expecting a 404 and a 400 respectively, once it is decided
# that the API should validate these.


DELETE {{restapi_url}}/users/troi
Authorization: {{authkey}}
HTTP 200
