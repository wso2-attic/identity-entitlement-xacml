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
package org.wso2.carbon.identity.entitlement.xacml.core.policy.store;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.balana.AbstractPolicy;
import org.wso2.carbon.identity.entitlement.xacml.core.EntitlementConstants;
import org.wso2.carbon.identity.entitlement.xacml.core.PolicyOrderComparator;
import org.wso2.carbon.identity.entitlement.xacml.core.dto.PolicyStoreDTO;
import org.wso2.carbon.identity.entitlement.xacml.core.exception.EntitlementException;
import org.wso2.carbon.identity.entitlement.xacml.core.policy.PolicyReader;
import org.wso2.carbon.identity.entitlement.xacml.core.storage.StorageManager;
import org.wso2.carbon.identity.entitlement.xacml.core.storage.StorageManagerImp;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This manages the reading of policies from Storage
 */
public class PolicyStoreReader {


    private static final Logger logger = LoggerFactory.getLogger(PolicyStoreReader.class);

    private final StorageManager storageManager;

    public PolicyStoreReader() {
        storageManager = StorageManagerImp.getStoreManager();
    }


    /**
     * Read policy from store for PolicyFinder
     *
     * @param policyId policy id
     * @return AbstractPolicy
     * @throws EntitlementException custom exception
     */
    public synchronized AbstractPolicy readPolicy(String policyId) throws EntitlementException {
        PolicyStoreDTO policyDTO = storageManager.read(policyId, EntitlementConstants.PAP_STORE);
        if (policyDTO == null){
            throw new EntitlementException("policy not found in PAP Store " , null);
        }
        String policy = new String(policyDTO.getPolicy().getBytes(), Charset.forName("UTF-8"));
        return PolicyReader.getInstance(null).getPolicy(policy);
    }

    /**
     * Reads all the policyDTO in the store
     *
     * @return Array of PolicyStoreDTO
     * @throws EntitlementException custom exception
     */
    public PolicyStoreDTO[] readAllPolicyDTOs() throws EntitlementException {
        return storageManager.readAll(EntitlementConstants.PAP_STORE);
    }

    public PolicyStoreDTO[] readAllPolicyDTOs(boolean active, boolean order) throws EntitlementException {
        PolicyStoreDTO[] policyStoreDTOs = storageManager.readAll(EntitlementConstants.PAP_STORE);
        List<PolicyStoreDTO> collect = Arrays.stream(policyStoreDTOs).filter((policyDto -> policyDto.isActive() == active)).collect(Collectors.toList());
        policyStoreDTOs = collect.toArray(new PolicyStoreDTO[collect.size()]);
        if (order){
            Arrays.sort(policyStoreDTOs, new PolicyOrderComparator());
        }
        return policyStoreDTOs;
    }

    /**
     * Reads PolicyStoreDTO for given policy id
     *
     * @param policyId policy id
     * @return PolicyStoreDTO
     * @throws EntitlementException custom exception
     */
    public PolicyStoreDTO readPolicyDTO(String policyId) throws EntitlementException {
        return storageManager.read(policyId, EntitlementConstants.PAP_STORE);
    }

    /**
     * Checks whether policy is exist for given policy id
     *
     * @param policyId policy id
     * @return true of false
     * @throws EntitlementException custom exception
     */
    public boolean isExistPolicy(String policyId) throws EntitlementException {
        PolicyStoreDTO policyDTO = storageManager.read(policyId, EntitlementConstants.PAP_STORE);
        return policyDTO != null;
    }
}
