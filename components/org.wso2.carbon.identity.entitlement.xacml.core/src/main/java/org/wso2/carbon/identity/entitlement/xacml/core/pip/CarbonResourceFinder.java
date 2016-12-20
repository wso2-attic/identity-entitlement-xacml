/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.identity.entitlement.xacml.core.pip;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.wso2.balana.attr.AttributeValue;
import org.wso2.balana.ctx.EvaluationCtx;
import org.wso2.balana.finder.ResourceFinderModule;
import org.wso2.balana.finder.ResourceFinderResult;
import org.wso2.carbon.identity.entitlement.xacml.core.EntitlementUtil;
import org.wso2.carbon.identity.entitlement.xacml.core.exception.EntitlementException;


import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * CarbonResourceFinder implements the ResourceFinderModule in the sum-xacml. This class would find
 * children and descendant resources
 */
@Component(
        name = "org.wso2.carbon.identity.entitlement.xacml.core.pip.CarbonResourceFinder",
        immediate = true,
        service = ResourceFinderModule.class
)
public class CarbonResourceFinder extends ResourceFinderModule {

    private static final Logger logger = LoggerFactory.getLogger(CarbonResourceFinder.class);
    boolean isResourceCachingEnabled = false;
    private Set<PIPResourceFinder> resourceFinders = new HashSet<>();
    //private Cache<IdentityCacheKey,IdentityCacheEntry> resourceCache = null;
//    private EntitlementBaseCache<IdentityCacheKey, IdentityCacheEntry> resourceCache = null;


    /**
     * initializes the Carbon resource finder by listing the registered resource finders
     */
    public void init() {
//        Properties properties = EntitlementServiceComponent.getEntitlementConfig().getEngineProperties();
//        if ("true".equals(properties.getProperty(PDPConstants.RESOURCE_CACHING))) {
//            resourceCache = EntitlementUtil
//                    .getCommonCache(PDPConstants.PIP_RESOURCE_CACHE);
//            isResourceCachingEnabled = true;
//        }
    }

    @Reference(
            name = "resource.finder",
            service = PIPResourceFinder.class,
            cardinality = ReferenceCardinality.AT_LEAST_ONE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterResourceFinder"
    )
    protected void registerResourceFinder(PIPResourceFinder pipResourceFinder) throws Exception {
        pipResourceFinder.init(new Properties());
        resourceFinders.add(pipResourceFinder);
    }

    protected void unregisterResourceFinder(PIPResourceFinder pipResourceFinder) {
        resourceFinders.remove(pipResourceFinder);
    }

    @Override
    public boolean isChildSupported() {
        return true;
    }

    @Override
    public boolean isDescendantSupported() {
        return true;
    }

    @Override
    public ResourceFinderResult findDescendantResources(AttributeValue parentResourceId,
                                                        EvaluationCtx context) {

        ResourceFinderResult resourceFinderResult = null;
        Set<AttributeValue> resources = null;
        String dataType = parentResourceId.getType().toString();

        for (PIPResourceFinder finder : resourceFinders) {
            try {
                Set<String> resourceNames = null;
                if (isResourceCachingEnabled && !finder.overrideDefaultCache()) {
//                    IdentityCacheKey cacheKey = null;
//                    String key = EntitlementConstants.RESOURCE_DESCENDANTS + parentResourceId.encode() +
//                            domToString(context.getRequestRoot());
//                    cacheKey = new IdentityCacheKey(tenantId, key);
//                    IdentityCacheEntry cacheEntry = (IdentityCacheEntry) resourceCache.getValueFromCache(cacheKey);
//                    if (cacheEntry != null) {
//                        String[] values = cacheEntry.getCacheEntryArray();
//                        resourceNames = new HashSet<String>(Arrays.asList(values));
//                            logger.debug("Carbon Resource Cache Hit");
//                    }
//
//                    if (resourceNames != null) {
//                        resourceNames = finder.findDescendantResources(parentResourceId.encode(), context);
//                            logger.debug("Carbon Resource Cache Miss");
//                        cacheEntry = new IdentityCacheEntry(resourceNames.toArray(new String[resourceNames.size()]));
//                        resourceCache.addToCache(cacheKey, cacheEntry);
//                    }
                } else {
                    resourceNames = finder.findDescendantResources(parentResourceId.encode(), context);
                }

                if (resourceNames != null && !resourceNames.isEmpty()) {
                    resources = new HashSet<>();
                    for (String resourceName : resourceNames) {
                        resources.add(EntitlementUtil.getAttributeValue(resourceName, dataType));
                    }
                }
            } catch (EntitlementException e) {
                logger.error("Error while finding descendant resources", e);
            } catch (TransformerException e) {
                logger.error("Error while finding descendant resources", e);
            } catch (Exception e) {
                logger.error("Error while finding descendant resources", e);
            }
        }

        if (resources != null) {
            resourceFinderResult = new ResourceFinderResult(resources);
        } else {
            resourceFinderResult = new ResourceFinderResult();
        }

        return resourceFinderResult;
    }

    @Override
    public ResourceFinderResult findChildResources(AttributeValue parentResourceId,
                                                   EvaluationCtx context) {
        ResourceFinderResult resourceFinderResult = null;
        Set<AttributeValue> resources = null;
        String dataType = parentResourceId.getType().toString();

        for (PIPResourceFinder finder : resourceFinders) {
            try {
                Set<String> resourceNames = null;
                if (isResourceCachingEnabled && !finder.overrideDefaultCache()) {
//                    IdentityCacheKey cacheKey = null;
//                    String key = PDPConstants.RESOURCE_CHILDREN + parentResourceId.encode() +
//                            domToString(context.getRequestRoot());
//                    cacheKey = new IdentityCacheKey(tenantId, key);
//                    IdentityCacheEntry cacheEntry = (IdentityCacheEntry) resourceCache.getValueFromCache(cacheKey);
//                    if (cacheEntry != null) {
//                        String cacheEntryString = cacheEntry.getCacheEntry();
//                        String[] attributes = cacheEntryString.split(PDPConstants.ATTRIBUTE_SEPARATOR);
//                        if (attributes != null && attributes.length > 0) {
//                            List<String> list = Arrays.asList(attributes);
//                            resourceNames = new HashSet<String>(list);
//                        }
//                        logger.debug("Carbon Resource Cache Hit");
//                    } else {
//                        resourceNames = finder.findChildResources(parentResourceId.encode(), context);
//                        logger.debug("Carbon Resource Cache Miss");
//                        String cacheEntryString = "";
//                        if (resourceNames != null && resourceNames.size() > 0) {
//                            for (String attribute : resourceNames) {
//                                if (cacheEntryString.equals("")) {
//                                    cacheEntryString = attribute;
//                                } else {
//                                    cacheEntryString = cacheEntryString + PDPConstants.ATTRIBUTE_SEPARATOR + attribute;
//                                }
//                            }
//                        }
//                        cacheEntry = new IdentityCacheEntry(cacheEntryString);
//                        resourceCache.addToCache(cacheKey, cacheEntry);
//                    }
                } else {
                    resourceNames = finder.findChildResources(parentResourceId.encode(), context);
                }

                if (resourceNames != null && !resourceNames.isEmpty()) {
                    resources = new HashSet<>();
                    for (String resourceName : resourceNames) {
                        resources.add(EntitlementUtil.getAttributeValue(resourceName, dataType));
                    }
                }
            } catch (EntitlementException e) {
                logger.error("Error while finding child resources", e);
            } catch (TransformerException e) {
                logger.error("Error while finding child resources", e);
            } catch (Exception e) {
                logger.error("Error while finding child resources", e);
            }
        }

        if (resources != null) {
            resourceFinderResult = new ResourceFinderResult(resources);
        } else {
            resourceFinderResult = new ResourceFinderResult();
        }

        return resourceFinderResult;
    }

    /**
     * Disables resource Caches
     */
    public void disableAttributeCache() {
//        resourceCache = null;
    }

    /**
     * Enables resource caches
     */
    public void enableAttributeCache() {
//        resourceCache = EntitlementUtil
//                .getCommonCache(PDPConstants.PIP_RESOURCE_CACHE);
    }

    /**
     * Clears attribute cache
     */
    public void clearAttributeCache() {
//        if (resourceCache != null) {
//            resourceCache.clear();
//                logger.debug("Resource cache is cleared for tenant ");
//        }
    }

    /**
     * Converts DOM object to String. This is a helper method for creating cache key
     *
     * @param node Node value
     * @return String Object
     * @throws TransformerException Exception throws if fails
     */
    private String domToString(Node node) throws TransformerException {
        TransformerFactory transFactory = TransformerFactory.newInstance();
        Transformer transformer = transFactory.newTransformer();
        StringWriter buffer = new StringWriter();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.transform(new DOMSource(node),
                new StreamResult(buffer));
        return buffer.toString();
    }
}
