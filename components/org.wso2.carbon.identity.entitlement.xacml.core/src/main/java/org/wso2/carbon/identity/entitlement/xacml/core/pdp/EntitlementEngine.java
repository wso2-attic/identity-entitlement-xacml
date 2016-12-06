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
package org.wso2.carbon.identity.entitlement.xacml.core.pdp;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.balana.Balana;
import org.wso2.balana.PDP;
import org.wso2.balana.PDPConfig;
import org.wso2.balana.finder.AttributeFinder;
import org.wso2.balana.finder.AttributeFinderModule;
import org.wso2.balana.finder.PolicyFinder;
import org.wso2.balana.finder.PolicyFinderModule;
import org.wso2.balana.finder.ResourceFinder;
import org.wso2.balana.finder.ResourceFinderModule;
import org.wso2.carbon.identity.entitlement.xacml.core.EntitlementUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Component(
        name = "org.wso2.carbon.identity.entitlement.pdp.EntitlementEngine",
        immediate = true
)
public class EntitlementEngine {

    private static PDP pdp;
    private Balana balana;
    private PolicyFinder carbonPolicyFinder = new PolicyFinder();
    private Set<PolicyFinderModule> policyModules = new HashSet<>();
    private List<AttributeFinderModule> attributeModules = new ArrayList<>();
    private List<ResourceFinderModule> resourceModules = new ArrayList<>();
    private static EntitlementEngine entitlementEngine = new EntitlementEngine();

    private static final Logger logger = LoggerFactory.getLogger(EntitlementEngine.class);

    @Reference(
            name = "policy.finder.service",
            service = PolicyFinderModule.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterPolicyFinder"
    )
    protected void registerPolicyFinder(PolicyFinderModule policyFinderModule) {
        policyModules.add(policyFinderModule);
        carbonPolicyFinder.setModules(policyModules);
        carbonPolicyFinder.init();

        AttributeFinder attributeFinder = new AttributeFinder();
        attributeFinder.setModules(attributeModules);

        ResourceFinder resourceFinder = new ResourceFinder();
        resourceFinder.setModules(resourceModules);

        PDPConfig pdpConfig =
                new PDPConfig(attributeFinder, carbonPolicyFinder, resourceFinder, false);
        pdp = new PDP(pdpConfig);
    }

    protected void unregisterPolicyFinder(PolicyFinderModule policyFinderModule) {
        policyModules.remove(policyFinderModule);
        carbonPolicyFinder.setModules(policyModules);
        carbonPolicyFinder.init();

        AttributeFinder attributeFinder = new AttributeFinder();
        attributeFinder.setModules(attributeModules);

        ResourceFinder resourceFinder = new ResourceFinder();
        resourceFinder.setModules(resourceModules);

        PDPConfig pdpConfig =
                new PDPConfig(attributeFinder, carbonPolicyFinder, resourceFinder, false);
        pdp = new PDP(pdpConfig);
    }

    /**
     * Get a EntitlementEngine instance. This method will return an
     * EntitlementEngine instance if exists, or creates a new one
     *
     * @return EntitlementEngine instance for that tenant
     */
    public static EntitlementEngine getInstance() {
        return entitlementEngine;
    }


    /**
     * Test request for PDP
     *
     * @param xacmlRequest XACML request as String
     * @return response as String
     */
    public String evaluate(String xacmlRequest) {

        logger.debug("XACML Request : " + xacmlRequest);

        String xacmlResponse = pdp.evaluate(xacmlRequest);

        logger.debug("XACML Response : " + xacmlResponse);

        return xacmlResponse;
    }

    public String testPolicy(String action, String resource, String subject, String environment) {
        String xacmlRequest = EntitlementUtil.createSimpleXACMLRequest(subject, resource, action, environment);
        return evaluate(xacmlRequest);
    }
}