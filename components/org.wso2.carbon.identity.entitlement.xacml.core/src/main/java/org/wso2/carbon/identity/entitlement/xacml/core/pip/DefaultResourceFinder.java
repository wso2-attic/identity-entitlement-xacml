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
import org.wso2.balana.ctx.EvaluationCtx;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * This is the default implementation of the PIPResourceFinder.
 * todo implement the methods
 */
@Component(
        name = "org.wso2.carbon.identity.entitlement.xacml.core.pip.DefaultResourceFinder",
        immediate = true,
        service = PIPResourceFinder.class
)
public class DefaultResourceFinder implements PIPResourceFinder {


    @Override
    public void init(Properties properties) throws Exception {

    }

    @Override
    public String getModuleName() {
        return "Default Resource Finder";
    }

    @Override
    public Set<String> findDescendantResources(String parentResourceId, EvaluationCtx context) throws Exception {
        Set<String> resourceSet = new HashSet<>();
//        registry = EntitlementServiceComponent.getRegistryService().getSystemRegistry(CarbonContext.
//                getThreadLocalCarbonContext().getTenantId());
//        if (registry.resourceExists(parentResourceId)) {
//            Resource resource = registry.get(parentResourceId);
//            if (resource instanceof Collection) {
//                Collection collection = (Collection) resource;
//                String[] resources = collection.getChildren();
//                for (String res : resources) {
//                    resourceSet.add(res);
//                    getChildResources(res, resourceSet);
//                }
//            } else {
//                return null;
//            }
//        }
        return resourceSet;
    }

    @Override
    public Set<String> findChildResources(String parentResourceId, EvaluationCtx context) throws Exception {
        return null;
    }

    @Override
    public boolean overrideDefaultCache() {
        return false;
    }

    @Override
    public void clearCache() {

    }

    /**
     * This helps to find resources un a recursive manner
     *
     * @param parentResource parent resource Name
     * @param childResources child resource set
     * @return child resource set
     */
    private Set<String> getChildResources(String parentResource, Set<String> childResources) {

//        Resource resource = registry.get(parentResource);
//        if (resource instanceof Collection) {
//            Collection collection = (Collection) resource;
//            String[] resources = collection.getChildren();
//            for (String res : resources) {
//                childResources.add(res);
//                getChildResources(res, childResources);
//            }
//        }
        return childResources;
    }
}
