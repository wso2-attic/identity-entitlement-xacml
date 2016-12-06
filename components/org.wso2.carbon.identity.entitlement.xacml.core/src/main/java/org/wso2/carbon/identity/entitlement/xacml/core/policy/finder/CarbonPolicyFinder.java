/*
*  Copyright (c)  WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.entitlement.xacml.core.policy.finder;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.balana.AbstractPolicy;
import org.wso2.balana.Policy;
import org.wso2.balana.PolicyMetaData;
import org.wso2.balana.PolicyReference;
import org.wso2.balana.PolicySet;
import org.wso2.balana.VersionConstraints;
import org.wso2.balana.combine.PolicyCombiningAlgorithm;
import org.wso2.balana.combine.xacml3.DenyOverridesPolicyAlg;
import org.wso2.balana.ctx.EvaluationCtx;
import org.wso2.balana.ctx.Status;
import org.wso2.balana.finder.PolicyFinder;
import org.wso2.balana.finder.PolicyFinderResult;
import org.wso2.carbon.identity.entitlement.xacml.core.EntitlementConstants;
import org.wso2.carbon.identity.entitlement.xacml.core.EntitlementUtil;
import org.wso2.carbon.identity.entitlement.xacml.core.exception.EntitlementException;
import org.wso2.carbon.identity.entitlement.xacml.core.policy.PolicyReader;
import org.wso2.carbon.identity.entitlement.xacml.core.policy.collection.PolicyCollection;
import org.wso2.carbon.identity.entitlement.xacml.core.policy.collection.SimplePolicyCollection;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Policy finder of the WSO2 entitlement engine.  This an implementation of <code>PolicyFinderModule</code>
 * of Balana engine. Extensions can be plugged with this.
 */
@Component(
        name = "org.wso2.carbon.identity.entitlement.xacml.core.policy.finder.CarbonPolicyFinder",
        immediate = true,
        service = org.wso2.balana.finder.PolicyFinderModule.class
)
public class CarbonPolicyFinder extends org.wso2.balana.finder.PolicyFinderModule {

    private static final Logger logger = LoggerFactory.getLogger(CarbonPolicyFinder.class);
    public PolicyReader policyReader;
    private PolicyCollection policyCollection;
    private PolicyFinder finder;
    private List<PolicyFinderModule> finderModules = new ArrayList<>();
    /**
     * this is a flag to keep whether init it has finished or not.
     */
    private volatile boolean initFinish;
    private LinkedHashMap<URI, AbstractPolicy> policyReferenceCache = null;
    private int maxReferenceCacheEntries = EntitlementConstants.MAX_NO_OF_IN_MEMORY_POLICIES;

    @Reference(
            name = "policy.finder.module.service",
            service = PolicyFinderModule.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterPolicyFinderModule"
    )
    protected void registerPolicyFinderModule(PolicyFinderModule policyFinderModule) {
        finderModules.add(policyFinderModule);
    }

    private void unregisterPolicyFinderModule(PolicyFinderModule policyFinderModule) {
        finderModules.remove(policyFinderModule);
    }

    @Override
    public void init(PolicyFinder finder) {
        initFinish = false;
        this.finder = finder;
        init();
        policyReferenceCache.clear();
    }

    private synchronized void init() {

        if (initFinish) {
            return;
        }

        logger.info("Initializing of policy store is started at :  " + new Date());


        policyReferenceCache = new LinkedHashMap<URI, AbstractPolicy>() {

            @Override
            protected boolean removeEldestEntry(Map.Entry eldest) {
                // oldest entry of the cache would be removed when max cache size become, i.e 50
                return size() > maxReferenceCacheEntries;
            }

        };

        PolicyCombiningAlgorithm policyCombiningAlgorithm = null;

        // get policy reader
        policyReader = PolicyReader.getInstance(finder);

        try {
            policyCombiningAlgorithm = EntitlementUtil.getPolicyCombiningAlgorithm(DenyOverridesPolicyAlg.algId);
            policyCollection = new SimplePolicyCollection();
            policyCollection.setPolicyCombiningAlgorithm(policyCombiningAlgorithm);
        } catch (EntitlementException e) {
            logger.error(e.getMessage());
        }

        initFinish = true;
        logger.info("Initializing of policy store is finished at :  " + new Date());
    }

    @Override
    public String getIdentifier() {
        return super.getIdentifier();
    }

    @Override
    public boolean isRequestSupported() {
        return true;
    }

    @Override
    public boolean isIdReferenceSupported() {
        return true;
    }

    @Override
    public PolicyFinderResult findPolicy(EvaluationCtx context) {

        try {
            AbstractPolicy policy = policyCollection.getEffectivePolicy(context);
            if (policy == null) {
                return new PolicyFinderResult();
            } else {
                return new PolicyFinderResult(policy);
            }
        } catch (EntitlementException e) {
            ArrayList<String> code = new ArrayList<String>();
            code.add(Status.STATUS_PROCESSING_ERROR);
            Status status = new Status(code, e.getMessage());
            return new PolicyFinderResult(status);
        }
    }

    @Override
    public PolicyFinderResult findPolicy(URI idReference, int type, VersionConstraints constraints,
                                         PolicyMetaData parentMetaData) {

        AbstractPolicy policy = policyReferenceCache.get(idReference);

        if (policy == null) {
            if (this.finderModules != null) {
                for (PolicyFinderModule finderModule : this.finderModules) {
                    String policyString = finderModule.getReferencedPolicy(idReference.toString());
                    if (policyString != null) {
                        policy = policyReader.getPolicy(policyString);
                        if (policy != null) {
                            policyReferenceCache.put(idReference, policy);
                            break;
                        }
                    }
                }
            }
        }

        if (policy != null) {
            // we found a valid version, so see if it's the right kind,
            // and if it is then we return it
            if (type == PolicyReference.POLICY_REFERENCE) {
                if (policy instanceof Policy) {
                    return new PolicyFinderResult(policy);
                }
            } else {
                if (policy instanceof PolicySet) {
                    return new PolicyFinderResult(policy);
                }
            }
        }

        return new PolicyFinderResult();
    }
}
