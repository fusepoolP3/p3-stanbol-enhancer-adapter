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
    <script src="scripts/almond-0.0.2-alpha-1.js" type="text/javascript"> </script>
    <script src="scripts/jquery-amd-1.7.1-alpha-1.js" type="text/javascript"> </script>
    <script src="scripts/transformer.js" type="text/javascript"> </script>
  </head>

  <body>
    <h1>Transformer</h1>
    URI: <@ldpath path="."/><br />
    <@ldpath path="rdfs:comment"/><br/>
    <h2>Supported Output Formats</h2>
    <ul><@ldpath path="trans:supportedOutputFormat">
        <li><@ldpath path="."/></li>
    </@ldpath></ul>
    <h2>Supported Input Formats</h2>
    <ul><@ldpath path="trans:supportedInputFormat">
        <li><@ldpath path="."/></li>
    </@ldpath></ul>
    <h2>Post content</h2>
    <form>
        Media Type: <input type="text" name="mediaType" value="text/plain"/><br/>
        Content:<br/>
        <textarea cols="50" rows="7" name="content"></textarea>
        <button onclick="Transformer.post(); return false;">POST</button>
    </form>
  </body>
</html>

