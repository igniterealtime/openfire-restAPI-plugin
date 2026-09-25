GET http://localhost:9090/plugins/restapi/v1/archive/messages/unread/john@example.org
Authorization: {{authkey}}
HTTP 200
[Asserts]
xpath "string(/archive/jid)" == "john@example.org"
xpath "string(/archive/count)" == "0"

# TODO: The unread message count is read from the ofMessageArchive table, which is created and filled by the Monitoring
# plugin. That plugin is not installed on the test server, so the query fails (Openfire logs a warning) and 0 is
# returned. To test a count above 0: install the Monitoring plugin (with message archiving enabled), log in as john
# over BOSH and log out again (to record an offline date in ofPresence), send john a message from jane over BOSH, and
# then expect a count of 1.
