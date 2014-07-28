<@namespace rdfs="http://www.w3.org/2000/01/rdf-schema#" />
<@namespace ont="http://fusepool.eu/ontology/enhancer-adapter#" />
<@namespace ehub="http://stanbol.apache.org/ontology/entityhub/entityhub#" />
<@namespace cc="http://creativecommons.org/ns#" />
<@namespace dct="http://purl.org/dc/terms/" />
<@namespace trans="http://vocab.fusepool.info/transformer#" />

<html>
  <head>
    <title>Transformer</title>
    <link type="text/css" rel="stylesheet" href="styles/multi-enhancer.css" />
  </head>

  <body>
    <h1>Transformer</h1>
    URI: <@ldpath path="."/><br />
    <@ldpath path="rdfs:comment"/><br/>
    
    <@ldpath path="trans:supportedOutputFormat">
          Supported Output Format: <@ldpath path="."/><br/>
    </@ldpath>
    <@ldpath path="trans:supportedInputFormat">
          Supported Input Format: <@ldpath path="."/><br/>
    </@ldpath>
  </body>
</html>

