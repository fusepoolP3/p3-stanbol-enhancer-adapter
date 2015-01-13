/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package eu.fusepool.enhancer.adapter.service;

import org.apache.clerezza.rdf.core.UriRef;


/**
 * Ideally this should be a dereferenceable ontology on the web. Given such 
 * an ontology a class of constant (similar to this) can be generated with
 * the org.apache.clerezza:maven-ontologies-plugin
 */
public class Ontology {

    private static final String TRLDPC_NS = "http://vocab.fusepool.info/trldpc#";

    //classes
    public static final UriRef TransformerIndex = new UriRef("http://fusepool.eu/ontology/enhancer-adapter#transformerIndex");
    /**
     * The Transformer Registration type
     */
    public static final UriRef TransformerRegistration = new UriRef(TRLDPC_NS + "TransformerRegistration");
    
    //properties
    public static final UriRef transformer = new UriRef("http://fusepool.eu/ontology/enhancer-adapter#transformer");
    /**
     * The transformer property of a {@link #TransformerRegistration} instance
     */
    public static final UriRef registeredTransformer = new UriRef(TRLDPC_NS + "transformer");
}
