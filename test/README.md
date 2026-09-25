# Tests

The tests contained in this folder are written in Hurl (see [docs](https://hurl.dev/docs/manual.html)).

Install Hurl with instructions as per the documentation, and run the tests from the root of the repository:

```bash
hurl --test --variables-file test/test.env --jobs 1 test/*.hurl
```

Alternatively, run Hurl from its container image, as CI does (`--network host` lets the container reach Openfire on
`localhost`):

```bash
docker run --rm --network host --user "$(id -u):$(id -g)" --volume "$PWD:$PWD" --workdir "$PWD" \
  ghcr.io/orange-opensource/hurl:8.0.1 \
  --test --variables-file test/test.env --jobs 1 test/*.hurl
```

Configure the Rest API:

* Enable it
* Set auth for shared key, and set the value in test.env
* Set `adminConsole.access.allow-wildcards-in-excludes` to true

test.env defines where the tests find Openfire: `restapi_url` (the base URL of the REST API, including its version),
`adminconsole_url` and `bosh_url`, as well as the shared secret (`authkey`).

Some tests create XMPP client sessions by logging in over BOSH (Openfire's HTTP binding, which listens on port 7070 by
default). The BOSH endpoint is configured by the `bosh_url` variable in test.env; when Openfire runs in a container, make
sure that port 7070 is published (or override the variable, e.g. `--variable bosh_url=http://<container-ip>:7070`).
