<#--
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
<@namespace rdfs="http://www.w3.org/2000/01/rdf-schema#" />
<@namespace ont="http://fusepool.eu/ontology/enhancer-adapter#" />
<@namespace ehub="http://stanbol.apache.org/ontology/entityhub/entityhub#" />
<@namespace cc="http://creativecommons.org/ns#" />
<@namespace dct="http://purl.org/dc/terms/" />
<@namespace trans="http://vocab.fusepool.info/transformer#" />

<html>
  <head>
    <title>Transformers</title>
    <link type="text/css" rel="stylesheet" href="styles/multi-enhancer.css" />
  </head>

  <body>
    <h1>Transformers</h1>
    <@ldpath path="ont:transformer">
    Tramsformer: <a href="<@ldpath path="."/>"><@ldpath path="."/></a><br/>
    </@ldpath>
    <@ldpath path="rdfs:comment"/>
    
  </body>
</html>

