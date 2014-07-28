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

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.apache.clerezza.jaxrs.utils.TrailingSlash;
import org.apache.clerezza.jaxrs.utils.form.FormFile;
import org.apache.clerezza.jaxrs.utils.form.MultiPartBody;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.impl.PlainLiteralImpl;
import org.apache.clerezza.rdf.core.serializedform.Serializer;
import org.apache.clerezza.rdf.ontologies.RDF;
import org.apache.clerezza.rdf.ontologies.RDFS;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.Service;
import org.apache.stanbol.commons.indexedgraph.IndexedMGraph;
import org.apache.stanbol.commons.web.viewable.RdfViewable;
import org.apache.stanbol.enhancer.servicesapi.Chain;
import org.apache.stanbol.enhancer.servicesapi.ChainManager;
import org.apache.stanbol.enhancer.servicesapi.ContentItem;
import org.apache.stanbol.enhancer.servicesapi.ContentItemFactory;
import org.apache.stanbol.enhancer.servicesapi.ContentSource;
import org.apache.stanbol.enhancer.servicesapi.EnhancementEngine;
import org.apache.stanbol.enhancer.servicesapi.EnhancementException;
import org.apache.stanbol.enhancer.servicesapi.EnhancementJobManager;
import org.apache.stanbol.enhancer.servicesapi.impl.ByteArraySource;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Upload file for which the enhancements are to be computed
 */
@Component
@Service(Object.class)
@Reference(name="enhancementEngine", referenceInterface=EnhancementEngine.class, 
		cardinality=ReferenceCardinality.OPTIONAL_MULTIPLE, 
        policy=ReferencePolicy.DYNAMIC)
@Property(name="javax.ws.rs", boolValue=true)
@Path("transformers")
public class Transformers {
    
    /**
     * Using slf4j for logging
     */
    private static final Logger log = LoggerFactory.getLogger(Transformers.class);
        
    @Reference
    private ContentItemFactory contentItemFactory;
    
    @Reference
    private EnhancementJobManager enhancementJobManager;
    
    @Reference
    private ChainManager chainManager;
    
    @Reference
    private Serializer serializer;
    
    private Map<String, EnhancementEngine> enhancementEngines = new HashMap<String, EnhancementEngine>();
    
    @Activate
    protected void activate(ComponentContext context) {
        log.info("The example service is being activated");
    }
    
    @Deactivate
    protected void deactivate(ComponentContext context) {
        log.info("The example service is being activated");
    }
    
    /**
     * This method return an RdfViewable, this is an RDF serviceUri with associated
     * presentational information.
     */
    @GET    
    public RdfViewable serviceEntry(@Context final UriInfo uriInfo, 
            @HeaderParam("user-agent") String userAgent) throws Exception {
        //this maks sure we are nt invoked with a trailing slash which would affect
        //relative resolution of links (e.g. css)
        TrailingSlash.enforcePresent(uriInfo);
        final String resourcePath = uriInfo.getAbsolutePath().toString();
        //The URI at which this service was accessed accessed, this will be the 
        //central serviceUri in the response
        final UriRef serviceUri = new UriRef(resourcePath);
        //the in memory graph to which the triples for the response are added
        final MGraph responseGraph = new IndexedMGraph();
        //This GraphNode represents the service within our result graph
        final GraphNode node = new GraphNode(serviceUri, responseGraph);
        //The triples will be added to the first graph of the union
        //i.e. to the in-memory responseGraph
        node.addProperty(RDF.type, Ontology.TransformerIndex);
        //node.addProperty(RDFS.comment, new PlainLiteralImpl("have: "+enhancementEngines));
        for (EnhancementEngine enhancementEngine : enhancementEngines.values()) {
            node.addProperty(Ontology.transformer, new UriRef(resourcePath+enhancementEngine.getName()));
        }
        //What we return is the GraphNode we created with a template path
        return new RdfViewable("Transformers", node, Transformers.class);
    }
    
    @Path("{engineName}")
    public EngineWrapper getTransformer(@PathParam("engineName") String engineName) {
        final EnhancementEngine engine = enhancementEngines.get(engineName);
        if (engine == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        return new EngineWrapper(engine, serializer);
    }
    
    
    /**
     * This service returns an RdfVieable describing the enhanced document. 
     */
    @POST
    public RdfViewable enhanceFile(MultiPartBody body) throws IOException, EnhancementException {
        final String[] chainValues = body.getTextParameterValues("chain");
        final String chainName = chainValues.length > 0 ? chainValues[0] : null;
        final FormFile file = body.getFormFileParameterValues("file")[0];
        final ContentSource contentSource = new ByteArraySource(
                file.getContent(),
                file.getMediaType().toString(),
                file.getFileName());
        final ContentItem contentItem = contentItemFactory.createContentItem(contentSource);
        if ((chainName == null) || chainName.trim().equals("")) {
            enhancementJobManager.enhanceContent(contentItem);
        } else {
            final Chain chain = chainManager.getChain(chainName);
            if (chain == null) {
                throw new RuntimeException("No chain by that name: "+chainName);
            }
            enhancementJobManager.enhanceContent(contentItem, chain);
        }
        //this contains the enhancement results
        final MGraph resultGraph = contentItem.getMetadata();
        //this is the IRI assigned to the subitted content
        final UriRef contentIri = contentItem.getUri();
        //this represent the submitted Content within the resultGraph
        final GraphNode node = new GraphNode(contentIri, resultGraph);
        //node is the "root" for rendering the results 
        return new RdfViewable("Enhancements", node, Transformers.class);
    }
    
    protected void bindEnhancementEngine(EnhancementEngine enhancementEngine) {
        enhancementEngines.put(enhancementEngine.getName(), enhancementEngine);
    }
    
    protected void unbindEnhancementEngine(EnhancementEngine enhancementEngine) {
        enhancementEngines.remove(enhancementEngine);
    }
    
}