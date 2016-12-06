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
import org.wso2.carbon.identity.entitlement.xacml.core.PolicyOrderComparator;
import org.wso2.carbon.identity.entitlement.xacml.core.dto.AttributeDTO;
import org.wso2.carbon.identity.entitlement.xacml.core.dto.PolicyStoreDTO;
import org.wso2.carbon.identity.entitlement.xacml.core.exception.EntitlementException;
import org.wso2.carbon.identity.entitlement.xacml.core.pdp.EntitlementEngine;
import org.wso2.carbon.identity.entitlement.xacml.core.policy.store.PolicyStore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Abstract implementation of a policy finder module. This can be easily extended by any module
 * that support dynamic policy changes.
 */
@Component(
        name = "org.wso2.carbon.identity.entitlement.xacml.core.policy.finder.CarbonPolicyFinderModule",
        immediate = true,
        service = PolicyFinderModule.class
)
public class CarbonPolicyFinderModule implements PolicyFinderModule {

    private static final String MODULE_NAME = "Policy Finder Module";

    private static final Logger logger = LoggerFactory.getLogger(EntitlementEngine.class);
    private PolicyStore policyStore;

    @Reference(
            name = "policy.store.service",
            service = PolicyStore.class,
            cardinality = ReferenceCardinality.AT_LEAST_ONE,
            policy = ReferencePolicy.STATIC
//            unbind = "unregisterPolicyStore"
    )
    protected void registerPolicyStore(PolicyStore policyStore) {
        this.policyStore = policyStore;
    }


    @Override
    public String[] getOrderedPolicyIdentifiers() {

        logger.debug("Start retrieving ordered policy identifiers at : " + new Date());
        String[] policyIdentifiers = getPolicyIdentifiers();
        if (policyIdentifiers != null && !isPolicyOrderingSupport()) {
            PolicyStoreDTO[] policyDTOs = new PolicyStoreDTO[0];
            try {
                policyDTOs = policyStore.readAllPolicyDTOs();
            } catch (EntitlementException e) {
                logger.error(e.getMessage());
            }
            Arrays.sort(policyDTOs, new PolicyOrderComparator());
            List<String> list = new ArrayList<>();
            List<String> finalList = new ArrayList<>();
            // 1st put non -order items
            list.addAll(Arrays.asList(policyIdentifiers));
            for (PolicyStoreDTO dto : policyDTOs) {
                list.remove(dto.getPolicyId());
                finalList.add(dto.getPolicyId());
            }
            finalList.addAll(list);
            return finalList.toArray(new String[finalList.size()]);
        }
        logger.debug("Finish retrieving ordered policy identifiers at : " + new Date());
        return policyIdentifiers;
    }

    @Override
    public String[] getActivePolicies() {
        logger.debug("Start retrieving active policies at : " + new Date());
        List<String> policies = new ArrayList<>();
        String[] policyIdentifiers = getOrderedPolicyIdentifiers();
        if (policyIdentifiers != null) {
            for (String identifier : policyIdentifiers) {
                if (!isPolicyDeActivationSupport()) {
                    PolicyStoreDTO data = null;
                    try {
                        data = policyStore.readPolicyDTO(identifier);
                    } catch (EntitlementException e) {
//                        what to do
                    }
                    if (data != null && data.isActive()) {
                        String policy = getPolicy(identifier);
                        if (policy != null) {
                            policies.add(policy);
                        }
                    }
                } else {
                    String policy = getPolicy(identifier);
                    if (policy != null) {
                        policies.add(policy);
                    }
                }
            }
        }
        logger.debug("Finish retrieving active policies at : " + new Date());
        return policies.toArray(new String[policies.size()]);

    }


    @Override
    public void init(Properties properties) throws EntitlementException {
        //
    }

    @Override
    public String getModuleName() {
        return MODULE_NAME;
    }

    @Override
    public String getPolicy(String policyId) {
        try {
            return (policyStore.readPolicyDTO(policyId)).getPolicy();
        } catch (EntitlementException e) {
            logger.error(e.getMessage());
        }
        return null;
    }

    @Override
    public int getPolicyOrder(String policyId) {
        try {
            return (policyStore.readPolicyDTO(policyId)).getPolicyOrder();
        } catch (EntitlementException e) {
            logger.error(e.getMessage());
        }
        return -1;
    }

    @Override
    public String getReferencedPolicy(String policyId) {

        // retrieve for policies that are not active
        try {
            PolicyStoreDTO dto = policyStore.readPolicyDTO(policyId);
            if (dto != null && dto.getPolicy() != null && !dto.isActive()) {
                return dto.getPolicy();
            }
        } catch (EntitlementException e) {
            logger.error(e.getMessage());
        }
        return null;
    }

    @Override
    public Map<String, Set<AttributeDTO>> getSearchAttributes(String identifier, Set<AttributeDTO> givenAttribute) {
        PolicyStoreDTO[] policyDTOs = null;
        Map<String, Set<AttributeDTO>> attributeMap = null;
        try {
            policyDTOs = policyStore.readAllPolicyDTOs(true, true);
        } catch (Exception e) {
            logger.error("Policies can not be retrieved from registry policy finder module", e);
        }

        if (policyDTOs != null) {
            attributeMap = new HashMap<>();
            for (PolicyStoreDTO policyDTO : policyDTOs) {
                Set<AttributeDTO> attributeDTOs =
                        new HashSet<>(Arrays.asList(policyDTO.getAttributeDTOs()));
                String[] policyIdRef = policyDTO.getPolicyIdReferences();
                String[] policySetIdRef = policyDTO.getPolicySetIdReferences();

                if (policyIdRef != null && policyIdRef.length > 0 || policySetIdRef != null &&
                        policySetIdRef.length > 0) {
                    for (PolicyStoreDTO dto : policyDTOs) {
                        if (policyIdRef != null) {
                            for (String policyId : policyIdRef) {
                                if (dto.getPolicyId().equals(policyId)) {
                                    attributeDTOs.addAll(Arrays.asList(dto.getAttributeDTOs()));
                                }
                            }
                        }
                        for (String policySetId : policySetIdRef) {
                            if (dto.getPolicyId().equals(policySetId)) {
                                attributeDTOs.addAll(Arrays.asList(dto.getAttributeDTOs()));
                            }
                        }
                    }
                }
                attributeMap.put(policyDTO.getPolicyId(), attributeDTOs);
            }
        }

        return attributeMap;
    }

    @Override
    public int getSupportedSearchAttributesScheme() {
        return PolicyFinderModule.COMBINATIONS_BY_CATEGORY_AND_PARAMETER;
    }

    @Override
    public boolean isDefaultCategoriesSupported() {
        return false;
    }

    @Override
    public boolean isPolicyOrderingSupport() {
        return true;
    }

    @Override
    public boolean isPolicyDeActivationSupport() {
        return true;
    }

    protected String[] getPolicyIdentifiers() {
        List<String> policyIds = new ArrayList<>();
        try {
            PolicyStoreDTO[] policyStoreDTOs = policyStore.readAllPolicyDTOs();
            policyIds = Arrays.stream(policyStoreDTOs).map(PolicyStoreDTO::getPolicyId)
                    .collect(Collectors.toList());
        } catch (EntitlementException e) {
            logger.error(e.getMessage());
        }
        return policyIds.toArray(new String[policyIds.size()]);
    }
}
