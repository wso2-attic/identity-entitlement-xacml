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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.balana.XACMLConstants;
import org.wso2.balana.attr.AttributeValue;
import org.wso2.balana.attr.BagAttribute;
import org.wso2.balana.attr.StringAttribute;
import org.wso2.balana.cond.EvaluationResult;
import org.wso2.balana.ctx.EvaluationCtx;
import org.wso2.carbon.identity.entitlement.xacml.core.EntitlementConstants;

import java.net.URI;
import java.util.Set;

/**
 * Abstract implementation of the PIPResourceFinder.
 */
public abstract class AbstractPIPResourceFinder implements PIPResourceFinder {

    private static final Logger logger = LoggerFactory.getLogger(AbstractPIPResourceFinder.class);
    //    private EntitlementBaseCache<IdentityCacheKey, IdentityCacheEntry> abstractResourceCache = null;
    private boolean isAbstractResourceCacheEnabled = false;

    /**
     * This is the overloaded simplify version of the findDescendantResources() method. Any one who extends the
     * <code>AbstractPIPResourceFinder</code> can implement this method and get use of the default
     * implementation of the findDescendantResources() method which has been implemented within
     * <code>AbstractPIPResourceFinder</code> class
     *
     * @param parentResourceId parent resource value
     * @param environmentId    environment name
     * @return Returns a <code>Set</code> of <code>String</code>s that represent the descendant resources
     * @throws Exception throws if any failure is occurred
     */
    public abstract Set<String> findDescendantResources(String parentResourceId, String environmentId)
            throws Exception;

    @Override
    public Set<String> findDescendantResources(String parentResourceId, EvaluationCtx context)
            throws Exception {

        EvaluationResult environment;
        String environmentId = null;
        Set<String> resourceNames = null;

        NodeList children = context.getRequestRoot().getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child != null) {
                if (EntitlementConstants.ENVIRONMENT_ELEMENT.equals(child.getLocalName())) {
                    if (child.getChildNodes() != null && child.getChildNodes().getLength() > 0) {
                        environment = context.getAttribute(new URI(StringAttribute.identifier),
                                new URI(EntitlementConstants.ENVIRONMENT_ID_DEFAULT), null,
                                new URI(XACMLConstants.ENT_CATEGORY));
                        if (environment != null && environment.getAttributeValue() != null &&
                                environment.getAttributeValue().isBag()) {
                            BagAttribute attr = (BagAttribute) environment.getAttributeValue();
                            environmentId = ((AttributeValue) attr.iterator().next()).encode();
                        }
                    }
                }
            }
        }

        if (isAbstractResourceCacheEnabled) {
//            IdentityCacheKey cacheKey;
//            String key = EntitlementConstants.RESOURCE_DESCENDANTS + parentResourceId +
//                    (environmentId != null ? environmentId : "");
//            tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
//            cacheKey = new IdentityCacheKey(tenantId, key);
//            IdentityCacheEntry cacheEntry = (IdentityCacheEntry) abstractResourceCache.getValueFromCache(cacheKey);
//            if (cacheEntry != null) {
//                String[] values = cacheEntry.getCacheEntryArray();
//                resourceNames = new HashSet<String>(Arrays.asList(values));
//                    logger.debug("Carbon Resource Cache Hit");
//            }
//
//            if (resourceNames != null) {
//                resourceNames = findDescendantResources(parentResourceId, environmentId);
//                    logger.debug("Carbon Resource Cache Miss");
//                if (resourceNames != null && !resourceNames.isEmpty()) {
//                    cacheEntry = new IdentityCacheEntry(resourceNames.toArray(new String[resourceNames.size()]));
//                    abstractResourceCache.addToCache(cacheKey, cacheEntry);
//                }
//            }
        } else {
            resourceNames = findDescendantResources(parentResourceId, environmentId);
        }

        return resourceNames;
    }

    @Override
    public boolean overrideDefaultCache() {
//        Properties properties = EntitlementServiceComponent.getEntitlementConfig().getEngineProperties();
//        if ("true".equals(properties.getProperty(EntitlementConstants.RESOURCE_CACHING))) {
//            abstractResourceCache = EntitlementUtil
//                    .getCommonCache(EntitlementConstants.PIP_ABSTRACT_RESOURCE_CACHE);
//            isAbstractResourceCacheEnabled = true;
//            return true;
//        } else {
//            return false;
//        }
        return false;
    }

    @Override
    public void clearCache() {
//        if (abstractResourceCache != null) {
//            abstractResourceCache.clear();
//        }
    }

    @Override
    public Set<String> findChildResources(String parentResourceId, EvaluationCtx context)
            throws Exception {
        return null;
    }
}
