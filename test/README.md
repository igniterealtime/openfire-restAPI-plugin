# Tests

The tests contained in this folder are written in Hurl (see [docs](https://hurl.dev/docs/manual.html)).

Install Hurl with instructions as per the documentation.

Configure the Rest API:

* Enable it
* Set auth for shared key, and set the value in test.env
* Set `adminConsole.access.allow-wildcards-in-excludes` to true

Some tests create XMPP client sessions by logging in over BOSH (Openfire's HTTP binding, which listens on port 7070 by
default). The BOSH endpoint is configured by the `bosh_url` variable in test.env; when Openfire runs in a container, make
sure that port 7070 is published (or override the variable, e.g. `--variable bosh_url=http://<container-ip>:7070`).
