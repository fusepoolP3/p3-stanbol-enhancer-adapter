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

Apache Stanbol Enhancer Transformer Service
===========

This module provides an adapter service that allows to use active [Enhancement Chains](http://stanbol.apache.org/docs/trunk/components/enhancer/chains/) and [Enhancement Engines](http://stanbol.apache.org/docs/trunk/components/enhancer/engines/) as Fusepool Transformers.

The root path of the Transformers component is `/transformers` relative to the Apache Stanbol root. It provides Fusepool Transformer endpoints

* for active engines under `/transformers/engine/{engine-name}`
* for active chains under `/transformers/chain/{chain-name}`

Installation:
----

This module uses Apache Maven as build system. You can build it by calling

    mvn install

By doing so you will find the JAR file of this module in the `target` folder. This JAR is an OSGI bundle that can be installed to any Stanbol Environment (e.g. by using the Felix Webconsole or copying the bundle to the Stanbol `fileinstall` folder.

By using the Maven Sling Plugin the module can be directly installed to a running Stanbol instance by calling

    mvn -o clean install -P installBundle \
        -Dsling.url=http://localhost:8080/system/console

After installing the module the Stanbol UI will show an additional component called `/transformers` that provides a simple UI for testing its functionality.


Usage
----

As the Stanbol Transformer implements the [Fusepool Transfomer API](https://github.com/fusepoolP3/overall-architecture/blob/master/transformer-api.md) communication is expected as specified by the Fusepool.

The capabilities of a transformer can be requested by a simple GET request at the base URI. The following listing shows the response for the enhancement chain with the name 'default':

    curl http://localhost:8089/transformers/chain/default

    <http://localhost:8089/transformers/chain/default>
        a <http://vocab.fusepool.info/transformer#Transformer> ;
        <http://vocab.fusepool.info/transformer#supportedInputFormat>
            "*/*" ;
        <http://vocab.fusepool.info/transformer#supportedOutputFormat>
            "application/rdf+json" , "application/ld+json" , "text/turtle" , 
            "text/rdf+n3" , "application/rdf+xml" , "application/x-turtle" , 
            "text/rdf+nt" , "application/json" .

The above RDF specifies that the Transformer accepts any kind of content and will return the enhancements results as RDF.

The kind of RDF returned depends on the Stanbol component called by the request. In the case of Fusepool a typical Enhancement Chain will use the [FISE to FAM converter engine](https://github.com/fusepoolP3/p3-stanbol-engine-fam) to ensure that results will use the [Fusepool Annotation Model](https://github.com/fusepoolP3/overall-architecture/blob/master/wp3/fp-anno-model/fp-anno-model.md).

To execute a Stanbol component using the Transformer service one needs to send a POST request with the content to process as payload.

    curl -v -X "POST" -H "Content-Type: plain/text;charset=UTF-8" \
        -H "Content-Location: http://www.example.org/fusepool/example.txt" 
        -T "test/resources/example.txt" \
        http://localhost:8089/transformers/chain/default

The Stanbol Transformer does support the use of the `Content-Location` header to specify the URI of the parsed content starting with [STANBOL-1404](https://issues.apache.org/jira/browse/STANBOL-1404) (revision: 1638109). In case an earlier version is used the URI can be parsed by using the `uri` query parameter. The `Content-Type` header is expected to be defined and set to the media type of the parsed content. Some Engines (e.g. the [Tika Engine](http://stanbol.apache.org/docs/trunk/components/enhancer/engines/tikaengine) are capable of detecting the content type. In this case `application/octet-stream` can be set to indicate that the content type of the parsed media is not known.


