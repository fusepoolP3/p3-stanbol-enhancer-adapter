package eu.fusepool.enhancer.adapter.service;

import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static org.apache.clerezza.rdf.core.serializedform.SupportedFormat.TURTLE;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.ws.rs.core.HttpHeaders;

import org.apache.clerezza.rdf.core.serializedform.SupportedFormat;
import org.apache.http.Header;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.stanbol.enhancer.servicesapi.Chain;
import org.apache.stanbol.enhancer.servicesapi.EnhancementEngine;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransformerRegistrationManager implements ServiceTrackerCustomizer {

    private final Logger log = LoggerFactory.getLogger(getClass());
    
    private static final Charset UTF8 = Charset.forName("UTF-8");

    private static final String REGISTRATION_GRAPH_TEMPLATE = 
            "@prefix dct: <http://purl.org/dc/terms/> .\n" + 
            "@prefix trldpc: <http://vocab.fusepool.info/trldpc#> . \n" +
            "\n" +
            "<> a trldpc:TransformerRegistration ; \n" +
            "\ttrldpc:transformer <%s> ; \n" +
            "\tdct:title \"%s\"@en ;\n" +
            "\tdct:description \"%s\"@en .";

    
    private final BundleContext bc;
    private final URI baseUri;
    private final URI transformerRegistry;
    private CloseableHttpClient httpClient;
    private Map<ServiceReference<?>, URI> registeredServices = new HashMap<>();
    private ReadWriteLock registeredServicesLock = new ReentrantReadWriteLock();

    public TransformerRegistrationManager(BundleContext bc, CloseableHttpClient httpClient, URI baseUri, URI transformerRegistry) {
        this.bc = bc;
        this.baseUri = baseUri;
        this.transformerRegistry = transformerRegistry;
        this.httpClient = httpClient;
        
    }

    @Override
    public Object addingService(ServiceReference reference) {
        Object service = bc.getService(reference);
        URI registrationUri = null;
        if(service instanceof Chain){
            //register a new Chain Transformer
            registrationUri = register((Chain) service);
        } else if(service instanceof EnhancementEngine){
            //register a new Engine Transformer
            registrationUri = register((EnhancementEngine) service);
        }
        if(registrationUri != null){
            registeredServicesLock.writeLock().lock();
            try {
                registeredServices.put(reference,registrationUri);
            } finally {
                registeredServicesLock.writeLock().unlock();
            }
        }
        return service;
    }

    private URI register(EnhancementEngine engine){
        log.debug("> register Enhancement Engine {} with TransformerRegistry", engine.getName());
        String registrationData = String.format(REGISTRATION_GRAPH_TEMPLATE, 
                baseUri.toString() + "transformers/engine/" + engine.getName(),
                "Stanbol Engine " + engine.getName() + "Transformer",
                "Transformer for the Stanbol Enhancement Engine " + engine.getName() +".");
        log.trace(" - Graph: \n {}", registrationData);
        String slugName = "stanbol-engine-"+engine.getName();
        log.debug(" - Slug: {}", slugName);
        return registerTransformer(slugName, registrationData);
    }
    private URI register(Chain chain){
        log.debug("> register Enhancement Engine {} with TransformerRegistry", chain.getName());
        String registrationData = String.format(REGISTRATION_GRAPH_TEMPLATE, 
                baseUri.toString() + "transformers/chain/" + chain.getName(),
                "Stanbol Chain " + chain.getName() + "Transformer",
                "Transformer for the Stanbol Enhancement Chain " + chain.getName() +".");
        log.trace(" - Graph: \n {}", registrationData);
        String slugName = "stanbol-chain-"+chain.getName();
        log.debug(" - Slug: {}", slugName);
        return registerTransformer(slugName, registrationData);
    }

    private URI registerTransformer(String slugName, String registrationData) {
        HttpPost post = new HttpPost(transformerRegistry);
        post.setEntity(new StringEntity(registrationData,UTF8));
        post.setHeader(CONTENT_TYPE, TURTLE +";charset="+UTF8.name());
        post.setHeader("Slug", slugName);
        log.debug(" - execute {}",post);
        try {
            CloseableHttpResponse response = httpClient.execute(post);
            try {
                StatusLine status = response.getStatusLine();
                if(status.getStatusCode() == 201){
                    Header location = response.getFirstHeader("Location");
                    log.debug(" - registered with TransformerRegistry {} as {}",
                            transformerRegistry,location.getValue());
                    try {
                        return new URI(location.getValue());
                    } catch (URISyntaxException e){
                        log.warn("Unable to parse Location heacer returned by the "
                                + "TransformerRegistry " + baseUri +"!", e);
                        return null;
                    }
                } else {
                    log.warn("Unable to register {} with TransformerRegistry {} "
                            + "because of a {} response",new Object[]{slugName,
                                    transformerRegistry, status});
                    return null;
                }
            } finally {
                //we do not use the entity
                EntityUtils.consumeQuietly(response.getEntity());
                response.close();
            }
        } catch(IOException e){
            log.warn("Unable to register "+slugName+" with TransformerRegistry "
                    + baseUri +" because of an " + e.getClass().getSimpleName()
                    + "(Message: "+e.getMessage()+")", e);
            return null;
        }
    }
    
    
    
    @Override
    public void modifiedService(ServiceReference reference, Object service) {
    }

    @Override
    public void removedService(ServiceReference reference, Object service) {
        registeredServicesLock.writeLock().lock();
        URI registrationUri;
        try {
            registrationUri = registeredServices.remove(reference);
        } finally {
            registeredServicesLock.writeLock().unlock();
        }
        if(registrationUri != null){
            boolean state = unregisterTransformer(registrationUri);
            if(!state){
                log.warn("failed to unregister TransformerRegistry Entry {} for Service: {}",
                        registrationUri, service);
            } else {
                log.debug(" - unregistered TransformerRegistry Entry for Service {} ",service);
            }
        } else {
            log.debug(" - no TransformerRegistry Entry for Service {} present", service);
        }
        if(reference != null){
            bc.ungetService(reference);
        }
    }

    private boolean unregisterTransformer(URI registrationUri) {
        HttpDelete delete = new HttpDelete(registrationUri);
        log.debug(" - execute {}",delete);
        try {
            CloseableHttpResponse response = httpClient.execute(delete);
            try {
                StatusLine status = response.getStatusLine();
                if(status.getStatusCode() >= 200 && status.getStatusCode() < 400){
                    return true;
                } else {
                    log.warn("Unable to unregister {} with TransformerRegistry {} "
                            + "because of a {} response",new Object[]{registrationUri,
                                    transformerRegistry, status});
                    return false;
                }
            } finally {
                response.close();
            }
        } catch(IOException e){
            log.warn("Unable to unregister "+registrationUri+" with TransformerRegistry "
                    + baseUri +" because of an " + e.getClass().getSimpleName()
                    + "(Message: "+e.getMessage()+")", e);
            return false;
        }
    }

}
