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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;
import javax.ws.rs.core.UriInfo;

import org.apache.clerezza.jaxrs.utils.TrailingSlash;
import org.apache.clerezza.rdf.core.Graph;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.serializedform.Parser;
import org.apache.clerezza.rdf.core.serializedform.SupportedFormat;
import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.ConfigurationPolicy;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.json.simple.JSONObject;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuration service
 */
@Component(policy = ConfigurationPolicy.OPTIONAL,
    metatype = true,
    immediate = true)
@Service(Object.class)
@Property(name="javax.ws.rs", boolValue=true)
@Path("fusepool/config")
public class ConfigService {
    
    /**
     * Using slf4j for logging
     */
    private final Logger log = LoggerFactory.getLogger(getClass());
    

    public static final int DEFAULT_LDP_PORT = 8181;
    public static final String DEFAULT_LDP_PATH = "ldp/";
    public static final String[] TRANSFORMER_REGISTRY_PATH = new String[]{"tr-ldpc"};
    
    public static final String TRANSFORMER_ADAPTER_SERVICE = "eu.fusepool.enhancer.adapter.service.Transformers";
    public static final String PROP_TRANSFORMER_REGISTRY_URI = "transformer.registry";
    public static final String PROP_STANBOL_BASE_URI = "stanbol.base.uri";
    
    public static final UriRef TRANSFORMER_REGISTRY_PREDICATE = new UriRef("http://vocab.fusepool.info/fp3#transformerRegistry");
    
    @Reference
    private ConfigurationAdmin configAdmin;
    
    @Reference
    private Parser parser;
    
    @Activate
    protected void activate(ComponentContext context) throws ConfigurationException {
        log.info("> activating {}",getClass().getName());
    }
    
    @Deactivate
    protected void deactivate(ComponentContext context) {
        log.info("> deactivating {}",getClass().getName());
    }
    
    /**
     * 
     * @param ldpBaseUri allows to directly configure the LDP base URI
     * @param stanbolBaseUri allows to directly configure the Stanbol Base URI
     * @param platformUri The Fusepool Platform URI. A LDP context containing
     * all information about the Fusepool Platform. If present this has
     * preferrence over the ldpBaseUri
     * @param uriInfo
     * @param headers
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response config(@QueryParam("ldp") String ldpBaseUri,
            @QueryParam("stanbol") String stanbolBaseUri, 
            @QueryParam("fusepool") String platformUri,
            @Context final UriInfo uriInfo, 
            @Context HttpHeaders headers) {
        TrailingSlash.enforcePresent(uriInfo);
        
        String transformerRegistryUri;
        
        log.info("> configuration Reuqest");
        URI baseUri = uriInfo.getBaseUri();
        if(StringUtils.isBlank(stanbolBaseUri)){
            log.debug(" ... using base URI of request for setting Stanbol base URI");
            stanbolBaseUri = baseUri.toString();
        }
        log.info(" - stanbol: {}", stanbolBaseUri);
        if(!StringUtils.isBlank(platformUri)){
            log.info(" - fusepool: {}", platformUri);
            final URL platformURL;
            try {
                platformURL = new URL(platformUri);
            } catch (MalformedURLException e) {
                throw new WebApplicationException("The value of the 'fusepool' "
                        + "parameter MUST BE a valid URL",e,Status.BAD_REQUEST);
            }
            final Graph fusepoolInfo;
            try {
                URLConnection con = platformURL.openConnection();
                con.addRequestProperty("Accept",SupportedFormat.TURTLE);
                con.getInputStream();
                fusepoolInfo = parser.parse(con.getInputStream(), con.getContentType());
            } catch (IOException e) {
                throw new WebApplicationException("Unable to load Fusepool Platform "
                        + "Information from the parsed URL '"+platformUri+"'!",
                        e,Status.NOT_FOUND);
            }
            //TODO: collect the required infor from the Fusepool platform context
            Iterator<Triple> it = fusepoolInfo.filter(null, TRANSFORMER_REGISTRY_PREDICATE, null);
            if(it.hasNext()){
                Resource val = it.next().getObject();
                if(val instanceof UriRef){
                    transformerRegistryUri = ((UriRef)val).getUnicodeString();
                } else {
                    throw new WebApplicationException("Fusepool Platform "
                            + "Information at '" + platformUri + "'has an invalid "
                            + " Transformer Registry URI (value: '"
                            + val+"', type: " + val.getClass() + ")!", Status.BAD_REQUEST);
                }
            } else {
                throw new WebApplicationException("Fusepool Platform "
                        + "Information at '" + platformUri + "'is missing the "
                        + "requred Transformer Registry URI (property: '"
                        + TRANSFORMER_REGISTRY_PREDICATE+"')!", Status.BAD_REQUEST);
            }
            
            
        } else { //fall back to the LDP base URI
            log.info(" - no Fusepool Platform URI parsed in configuration request");
            if(ldpBaseUri == null){
                log.debug(" ... using default for the LDP base URI");
                try {
                    ldpBaseUri = new URI(baseUri.getScheme(), baseUri.getUserInfo(),
                            baseUri.getHost(), DEFAULT_LDP_PORT, DEFAULT_LDP_PATH, 
                            null, null).toString();
                } catch (URISyntaxException e) {
                    throw new WebApplicationException(e);
                }
            }
            log.info(" - ldp base: {}", ldpBaseUri);
            try {
                transformerRegistryUri = UriBuilder.fromUri(new URI(ldpBaseUri)).segment(TRANSFORMER_REGISTRY_PATH).build().toString();
            } catch (UriBuilderException e) {
                throw new WebApplicationException(e);
            } catch (URISyntaxException e) {
                throw new WebApplicationException("Value of the 'ldp' parameter needs to be a valid URI!",e, Status.BAD_REQUEST);
            }
        }
        log.info(" - transformer registry: {}", transformerRegistryUri);
        
        
        Configuration transformerAdapterConfig;
        try {
            transformerAdapterConfig = configAdmin.getConfiguration(TRANSFORMER_ADAPTER_SERVICE);
        } catch (IOException e) {
            log.warn("Unable to lookup configuration of the '"+TRANSFORMER_ADAPTER_SERVICE+",!",e);
            transformerAdapterConfig = null;
        }
        if(transformerAdapterConfig.getProperties() != null && 
                stanbolBaseUri.equals(transformerAdapterConfig.getProperties().get(PROP_STANBOL_BASE_URI)) &&
                transformerRegistryUri.equals(transformerAdapterConfig.getProperties().get(PROP_TRANSFORMER_REGISTRY_URI))){
            //this request parses the same config as the existing one
            log.info(" ... parsed config already present (nothing to do)");
        } else {
            if(transformerAdapterConfig.getProperties() != null) {
                log.info("> current config:");
                log.info(" - stanbol: {}", transformerAdapterConfig.getProperties().get(PROP_STANBOL_BASE_URI));
                log.info(" - tr-ldpc: {}", transformerAdapterConfig.getProperties().get(PROP_TRANSFORMER_REGISTRY_URI));
            } else {
                log.info("> no config present");
            }
            log.info(" ... updating configuration ...");
            Dictionary<String, Object> config = new Hashtable<>();
            config.put(PROP_STANBOL_BASE_URI, stanbolBaseUri);
            config.put(PROP_TRANSFORMER_REGISTRY_URI, transformerRegistryUri);
            try {
                transformerAdapterConfig.setBundleLocation(null);
                transformerAdapterConfig.update(config);
            } catch (IOException e) {
                throw new WebApplicationException("Unable to set configuration!", e);
            }
            log.info(" ... config {} updated!",transformerAdapterConfig);
        }
        Map<String,String> config = new HashMap<String, String>();
        config.put(PROP_STANBOL_BASE_URI, stanbolBaseUri);
        config.put(PROP_TRANSFORMER_REGISTRY_URI, transformerRegistryUri);
        return Response.ok(JSONObject.toJSONString(config),MediaType.APPLICATION_JSON_TYPE).build();
    }
    
}
