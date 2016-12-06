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
import org.wso2.carbon.identity.entitlement.xacml.core.PolicyOrderComparator;
import org.wso2.carbon.identity.entitlement.xacml.core.dto.PolicyStoreDTO;
import org.wso2.carbon.identity.entitlement.xacml.core.exception.EntitlementException;
import org.wso2.carbon.identity.entitlement.xacml.core.policy.PolicyReader;
import org.wso2.carbon.identity.entitlement.xacml.core.policy.collection.PolicyCollection;
import org.wso2.carbon.identity.entitlement.xacml.core.policy.collection.SimplePolicyCollection;
import org.wso2.carbon.identity.entitlement.xacml.core.policy.store.PolicyStore;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

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
    private PolicyStore policyStore;
    private List<PolicyFinderModule> finderModules = null;

    private PolicyCollection policyCollection;

    private List<PolicyStoreDTO> policyCollectionOrder = new ArrayList<>();
    private PolicyFinder finder;
    /**
     * this is a flag to keep whether init it has finished or not.
     */
    private volatile boolean initFinish;
    private LinkedHashMap<URI, AbstractPolicy> policyReferenceCache = null;
    private int maxReferenceCacheEntries = EntitlementConstants.MAX_NO_OF_IN_MEMORY_POLICIES;

    @Reference(
            name = "policy.store.service",
            service = PolicyStore.class,
            cardinality = ReferenceCardinality.AT_LEAST_ONE,
            policy = ReferencePolicy.STATIC
//            unbind = "unregisterPolicyStore"
    )
    protected void registerDeployer(PolicyStore policyStore) {
        this.policyStore = policyStore;
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

//        String maxEntries = EntitlementServiceComponent.getEntitlementConfig().getEngineProperties().
//                getProperty(PDPConstants.MAX_POLICY_REFERENCE_ENTRIES);
//
//        if (maxEntries != null) {
//            try {
//                maxReferenceCacheEntries = Integer.parseInt(maxEntries.trim());
//            } catch (Exception e) {
//                //ignore
//            }
//        }

        policyReferenceCache = new LinkedHashMap<URI, AbstractPolicy>() {

            @Override
            protected boolean removeEldestEntry(Map.Entry eldest) {
                // oldest entry of the cache would be removed when max cache size become, i.e 50
                return size() > maxReferenceCacheEntries;
            }

        };

        PolicyCombiningAlgorithm policyCombiningAlgorithm = null;
        // get registered finder modules
//        Map<PolicyFinderModule, Properties> finderModules = EntitlementServiceComponent.
//                getEntitlementConfig().getPolicyFinderModules();

//        if (finderModules != null) {
//            this.finderModules = new ArrayList<>(finderModules.keySet());
//        }
        this.finderModules = new ArrayList<>();
        this.finderModules.add(new CarbonPolicyFinderModule(policyStore));

        PolicyCollection tempPolicyCollection = null;

        // get policy collection
//        Map<PolicyCollection, Properties> policyCollections = EntitlementServiceComponent.
//                getEntitlementConfig().getPolicyCollections();
        Map<PolicyCollection, Properties>  policyCollections = null;
        if (policyCollections != null && policyCollections.size() > 0) {
            tempPolicyCollection = policyCollections.entrySet().iterator().next().getKey();
        } else {
            tempPolicyCollection = new SimplePolicyCollection();
        }

        // get policy reader
        policyReader = PolicyReader.getInstance(finder);

        if (this.finderModules != null && this.finderModules.size() > 0) {
            // find policy combining algorithm.

            // here we can get policy data store by using EntitlementAdminEngine. But we are not
            // use it here.  As we need not to have a dependant on EntitlementAdminEngine
//            PolicyDataStore policyDataStore;
//            Map<PolicyDataStore, Properties> dataStoreModules = EntitlementServiceComponent.
//                    getEntitlementConfig().getPolicyDataStore();
//            if (dataStoreModules != null && dataStoreModules.size() > 0) {
//                policyDataStore = dataStoreModules.entrySet().iterator().next().getKey();
//            } else {
//                policyDataStore = new DefaultPolicyDataStore();
//            }
//            policyCombiningAlgorithm = policyDataStore.getGlobalPolicyAlgorithm();
//            urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:deny-overrides
            try {
                policyCombiningAlgorithm = EntitlementUtil.getPolicyCombiningAlgorithm(DenyOverridesPolicyAlg.algId);
            } catch (EntitlementException e) {
//
            }

            tempPolicyCollection.setPolicyCombiningAlgorithm(policyCombiningAlgorithm);

            for (PolicyFinderModule finderModule : this.finderModules) {
                logger.info("Start retrieving policies from " + finderModule + " at : " + new Date());
                String[] policies = finderModule.getActivePolicies();
                for (int a = 0; a < policies.length; a++) {
                    String policy = policies[a];
                    AbstractPolicy abstractPolicy = policyReader.getPolicy(policy);
                    if (abstractPolicy != null) {
                        PolicyStoreDTO policyDTO = new PolicyStoreDTO();
                        policyDTO.setPolicyId(abstractPolicy.getId().toString());
                        policyDTO.setPolicyOrder(a);
                        policyCollectionOrder.add(policyDTO);
                        tempPolicyCollection.addPolicy(abstractPolicy);
                    }
                }
                logger.info("Finish retrieving policies from " + finderModule + " at : " + new Date());
            }
        } else {
            logger.warn("No Carbon policy finder modules are registered");

        }

        policyCollection = tempPolicyCollection;
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


    private void orderPolicyCache() {
        LinkedHashMap<URI, AbstractPolicy> policyMap = policyCollection.getPolicyMap();
        Collections.sort(policyCollectionOrder, new PolicyOrderComparator());
        LinkedHashMap<URI, AbstractPolicy> newPolicyMap = new LinkedHashMap<URI, AbstractPolicy>();
        Iterator<PolicyStoreDTO> policyDTOIterator = policyCollectionOrder.iterator();
        while (policyDTOIterator.hasNext()) {
            try {
                URI policyURI = new URI(policyDTOIterator.next().getPolicyId());
                newPolicyMap.put(policyURI, policyMap.get(policyURI));

            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public PolicyFinderResult findPolicy(EvaluationCtx context) {

//        if (EntitlementEngine.getInstance().getPolicyCache().isInvalidate()) {
//
//            init(this.finder);
//            policyReferenceCache.clear();
//            EntitlementEngine.getInstance().clearDecisionCache();
//            if (logger.isDebugEnabled()) {
//                int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
//                logger.debug("Invalidation cache message is received. " +
//                          "Re-initialized policy finder module of current node and invalidate decision " +
//                          "caching for tenantId : " + tenantId);
//            }
//        } else {
//            Collection<PolicyStatus> policies =
//                    EntitlementEngine.getInstance().getPolicyCache().getInvalidatedPolicies();
//            if (policies != null) {
//                if (policies.size() > 0) {
//                    synchronized (policies) {
//                        boolean isReorder = false;
//                        policyReferenceCache.clear();
//                        EntitlementEngine.getInstance().clearDecisionCache();
//                        for (PolicyStatus policyStatus : policies) {
//
//                            if (EntitlementConstants.PolicyPublish.ACTION_DELETE
//                                    .equals(policyStatus.getPolicyAction())) {
//                                policyCollection.deletePolicy(policyStatus.getPolicyId());
//                                policyCollectionOrder.remove(new PolicyStoreDTO(policyStatus.getPolicyId()));
//                            } else if (EntitlementConstants.PolicyPublish.ACTION_UPDATE
//                                    .equals(policyStatus.getPolicyAction())) {
//                                AbstractPolicy abstractPolicy = loadPolicy(policyStatus.getPolicyId());
//                                policyCollection.addPolicy(abstractPolicy);
//                            } else if (EntitlementConstants.PolicyPublish.ACTION_CREATE
//                                    .equals(policyStatus.getPolicyAction())) {
//                                AbstractPolicy abstractPolicy = loadPolicy(policyStatus.getPolicyId());
//                                policyCollection.addPolicy(abstractPolicy);
//                                isReorder = true;
//                            } else if (EntitlementConstants.PolicyPublish.ACTION_ORDER
//                                    .equals(policyStatus.getPolicyAction())) {
//                                int order = getPolicyOrder(policyStatus.getPolicyId());
//                                if (order != -1) {
//                                    PolicyStoreDTO policyDTO = new PolicyStoreDTO(policyStatus.getPolicyId());
//                                    if (policyCollectionOrder.indexOf(policyDTO) != -1) {
//                                        policyCollectionOrder.get(policyCollectionOrder.indexOf(policyDTO))
//                                                .setPolicyOrder(order);
//                                        isReorder = true;
//                                    }
//                                }
//                            }
//
//                        }
//                        if (isReorder) {
//                            orderPolicyCache();
//                        }
//                        policies.clear();
//                    }
//
//
//                }
//            }
//        }

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


    private AbstractPolicy loadPolicy(String policyId) {
        if (this.finderModules != null) {
            for (PolicyFinderModule finderModule : this.finderModules) {
                String policyString = finderModule.getPolicy(policyId);
                if (policyString != null) {
                    AbstractPolicy policy = policyReader.getPolicy(policyString);
                    if (policy != null) {
                        return policy;
                    }
                }
            }
        }
        return null;
    }

    private int getPolicyOrder(String policyId) {
        int order = -1;
        if (this.finderModules != null) {

            for (PolicyFinderModule finderModule : this.finderModules) {
                if ((order = finderModule.getPolicyOrder(policyId)) != -1) {
                    break;
                }
            }
        }
        return order;
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

//    public void clearPolicyCache() {
//        EntitlementEngine.getInstance().getPolicyCache().clear();
//    }
}
