Apache Stanbol Enhancer Transformer
========================

This provides a component for [Apache Stanbol](http://stanbol.apache.org) that implements the [Fusepool Transformer](https://github.com/fusepoolP3/overall-architecture/blob/master/transformer-api.md) specification.

It contains as three modules:

* [Transformer Service](service): This adds a component with the path `/transformers` to the Stanbol web services that exposes all active [Enhancement Chains](http://stanbol.apache.org/docs/trunk/components/enhancer/chains/) and [Enhancement Engines](http://stanbol.apache.org/docs/trunk/components/enhancer/engines/) as Fusepool Transformers. Chains are available at `transformers/chain/{chainName}` and Engines under `transformers/engine/{engineName}`
* [Transformer Bundlelist](bundlelist): This provides [Apache Sling](http://sling.apache.org) partial bundlelist that can be used to add all the modules required to use the Stanbol Transformer service in any Stanbol Launcher.
* [Transformer Launcher](launcher): A rather minimal Stanbol runable JAR launcher with the Stanbol Transformer included. Intended to be used for testing and as an example for building [custom Stanbol Launcher](http://stanbol.apache.org/docs/trunk/production-mode/your-launcher.html) with this feature included.

For further information on how to actually use the Stanbol Transfomer Service please have a look at the [README](service) of the `service` module.