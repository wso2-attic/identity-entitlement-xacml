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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.entitlement.xacml.core.EntitlementConstants;
import org.wso2.carbon.identity.entitlement.xacml.core.dto.PolicyStoreDTO;
import org.wso2.carbon.identity.entitlement.xacml.core.exception.EntitlementException;
import org.wso2.carbon.identity.entitlement.xacml.core.storage.StorageManager;
import org.wso2.carbon.identity.entitlement.xacml.core.storage.StorageManagerImp;

import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.util.Iterator;


/**
 * This manages the storing and managing policies
 */
public class PolicyStore {

    private static final Logger logger = LoggerFactory.getLogger(PolicyStore.class);

    private StorageManager storageManager;
    private final String papStore = EntitlementConstants.PAP_STORE;


    public PolicyStore() {
        storageManager = StorageManagerImp.getStoreManager();
    }

    /**
     * @param policy PolicyStoreDTO which is going to added to the store
     * @throws EntitlementException custom exception
     */
    public void addPolicy(PolicyStoreDTO policy) throws EntitlementException {
        OMElement omElement = null;

//        if (policy.getPolicyType() != null && policy.getPolicyType().trim().length() > 0) {
//            resource.setProperty(PDPConstants.POLICY_TYPE, policy.getPolicyType());
//        } else {
//            try {
//                if (newPolicy) {
//                    omElement = AXIOMUtil.stringToOM(policy.getPolicy());
//                    resource.setProperty(PDPConstants.POLICY_TYPE, omElement.getLocalName());
//                }
//            } catch (XMLStreamException e) {
//                policy.setPolicyType(PDPConstants.POLICY_ELEMENT);
//                logger.warn("Policy Type can not be found. Default type is set");
//            }
//        }

        try {
            omElement = AXIOMUtil.stringToOM(policy.getPolicy());
        } catch (XMLStreamException e) {
//            nothing
        }

        if (omElement != null) {
            Iterator iterator1 = omElement.getChildrenWithLocalName(EntitlementConstants.POLICY_REFERENCE);
            if (iterator1 != null) {
                ArrayList<String> policyReferences = new ArrayList<>();
                while (iterator1.hasNext()) {
                    OMElement policyReference = (OMElement) iterator1.next();
                    policyReferences.add(policyReference.getText());
                }
                policy.setPolicyIdReferences(policyReferences.toArray(new String[policyReferences.size()]));
            }

            Iterator iterator2 = omElement.getChildrenWithLocalName(EntitlementConstants.POLICY_SET_REFERENCE);
            if (iterator2 != null) {
                ArrayList<String> policySetReferences = new ArrayList<>();
                while (iterator2.hasNext()) {
                    OMElement policySetReference = (OMElement) iterator2.next();
                    policySetReferences.add(policySetReference.getText());
                }
                policy.setPolicySetIdReferences(policySetReferences.toArray(new String[policySetReferences.size()]));
            }
        }
        storageManager.store(policy, papStore);
    }

    /**
     * @param policy updated PolicyStoreDTO which is going to replaced in the store
     * @throws EntitlementException custom exception
     */
    public void updatePolicy(PolicyStoreDTO policy) throws EntitlementException {
        PolicyStoreDTO existingPolicy = storageManager.read(policy.getPolicyId(), papStore);
        storageManager.remove(existingPolicy.getPolicyId(), papStore);
        addPolicy(policy);
    }


    /**
     * @param policyId which have to removed from store
     * @throws EntitlementException custom exception
     */
    public void removePolicy(String policyId) throws EntitlementException {
        storageManager.remove(policyId, papStore);
    }

}
