<!--
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
-->

Apache Stanbol Configuration Service
===========

This module provides configuration service that allows the Fusepool P3 initialization script to configure a Stanbol instance for it.

This service will configure the [Fusepool Transformer Adapter](../service) service with the base URI of Apache Stanbol and the Transformer Registry of the Fusepool Plattform.

Installation:
----

This module uses Apache Maven as build system. You can build it by calling

    mvn install

By doing so you will find the JAR file of this module in the `target` folder. This JAR is an OSGI bundle that can be installed to any Stanbol Environment (e.g. by using the Felix Webconsole or copying the bundle to the Stanbol `fileinstall` folder.

If you have a Stanbol instance running at `localhost:8080` you can directly install this module by calling
    mvn -o clean install -P installBundle \
        -Dsling.url=http://localhost:8080/system/console

The [Fusepool Stanbol Launcher](http://github.com/fusepoolP3/p3-stanbol-launcher) also includes this module.

Usage
-----

This module provide a configuration service under `{stanbol-root}/fusepool/config`. A GET request to this URL can be used for the (re-)configuration of the [Fusepool Transformer Adapter](../service) service.

A typical request to this service needs to provide the LDP context providing the configuration of the Fusepool Plattform via the `fusepool` parameter. The following listing shows a typical configuration request

    curl "http://sandbox.fusepool.info:8304/fusepool/config/?fusepool=http%3A%2F%2Fsandbox.fusepool.info%3A8181%2Fldp%2Fplatform"

The service responses with the JSON encoded map of the used configuration parameters set to the [Fusepool Transformer Adapter](../service) service.

    {
        "transformer.registry":"http:\/\/sandbox.fusepool.info:8181\/ldp\/tr-ldpc",
        "stanbol.base.uri":"http:\/\/sandbox.fusepool.info:8304\/"
    }

As mentioned above the value of the `transformer.registry` parameter is retrieved from the parsed Fusepool Platform LDP context. The `stanbol.base.uri` is extracted from the request URI of the caller from the configuration service. In the above example this is `http://sandbox.fusepool.info:8304`.

### Parameters

In addition to the above usage the configuration service also supports different configuration modes. Those can be used by using the following query paremtners

* `fusepool`: The URL of the LDP context with the meta information of the Fusepool Plattform. This context MUST contain a value for the `http://vocab.fusepool.info/fp3#transformerRegistry` property. If not the service will return with `BAD_REQUEST`.
* `stanbol`: The base URL for the Stanbol service. This allows to explicitly set the base URL for the Stanbol instance. This base URL is used for registering Transformers for configured [Enhancement Chains](http://stanbol.apache.org/docs/trunk/components/enhancer/chains/) with the Transformer Registry of the Fusepool Plattform. As default the base URL of the Request to the configuration service is used.
* `ldp`: As alternative to the `fusepool` parameter this allows to configure the base URL of the LDP. The Transformer Registry will be configured to its default context relative to the parsed base URL (`{ldp}/tr-ldpc`). This parameter is ignored if the `fusepool` parameter is present.

### Fallback Mode:

If neither `fusepool` nor `ldp` are present the URL of the transformer registry will be set relative to the base URL of the request by using the default ports as specified for Fusepool - `{host}:{ldp-default-port}/ldp/{tr-default-context}`. where

* `{host}` is the host of the request to the config service (assuming that Stanbol and the Fusepool LDP do run on the same host
* `{ldp-default-port}` is specified as {8181}
* `{tr-default-context}` is `tr-ldpc`.

So a request to `http://sandbox.fusepool.info:8304/fusepool/config/` without any configuration parameter will set the `transformer.registry` to `http://sandbox.fusepool.info:8181/ldp/tr-ldpc`

