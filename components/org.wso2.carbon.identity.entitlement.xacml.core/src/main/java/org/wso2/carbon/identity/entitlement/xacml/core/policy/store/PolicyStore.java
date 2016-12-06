package org.wso2.carbon.identity.entitlement.xacml.core.policy.store;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.wso2.balana.AbstractPolicy;
import org.wso2.carbon.identity.entitlement.xacml.core.EntitlementConstants;
import org.wso2.carbon.identity.entitlement.xacml.core.EntitlementUtil;
import org.wso2.carbon.identity.entitlement.xacml.core.PolicyOrderComparator;
import org.wso2.carbon.identity.entitlement.xacml.core.dto.PolicyStoreDTO;
import org.wso2.carbon.identity.entitlement.xacml.core.exception.EntitlementException;
import org.wso2.carbon.identity.entitlement.xacml.core.policy.PolicyReader;

import javax.xml.stream.XMLStreamException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
public abstract class PolicyStore {

    /**
     * @param policy PolicyStoreDTO which is going to added to the store
     * @throws EntitlementException custom exception
     */
    public void addPolicy(PolicyStoreDTO policy) throws EntitlementException {

        if (policy == null) {
            throw new EntitlementException("Policy is null ");
        }

        if (!EntitlementUtil.validatePolicy(policy)) {
            throw new EntitlementException("Invalid Entitlement Policy. " +
                    "Policy is not valid according to XACML schema");
        }

        AbstractPolicy policyObj = PolicyReader.getInstance(null).getPolicy(policy.getPolicy());
        if (policyObj == null) {
            throw new EntitlementException("Unsupported Entitlement Policy. Policy can not be parsed");
        }

        // TODO: 12/5/16 which is going to be policy id ??
//        String policyId = policyObj.getId().toASCIIString();
//        policy.setPolicyId(policyId);
        String policyId = policy.getPolicyId();

        if (policyId.contains("/")) {
            throw new EntitlementException(
                    " Policy Id cannot contain / characters. Please correct and upload again");
        }
//                if (!policyId.matches(regString)) {
//                    throw new EntitlementException(
//                            "An Entitlement Policy Id is not valid. It contains illegal characters");
//                }


        if (isExistPolicy(policyId)) {
            throw new EntitlementException(
                    "An Entitlement Policy with the given Id already exists");
        }
//            try {
//                String version = versionManager.createVersion(policyDTO);
//                policyDTO.setVersion(version);
//            } catch (EntitlementException e) {
//                logger.error("Policy versioning is not supported", e);
//            }


        OMElement omElement;
        try {
            omElement = AXIOMUtil.stringToOM(policy.getPolicy());
            policy.setPolicyType(omElement.getLocalName());
        } catch (XMLStreamException e) {
            throw new EntitlementException("Error in reading from policy", e);
        }

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
        doAdd(policy);
    }

    /**
     * @param policy updated PolicyStoreDTO which is going to replaced in the store
     * @throws EntitlementException custom exception
     */
    public void updatePolicy(PolicyStoreDTO policy) throws EntitlementException {
        PolicyStoreDTO existingPolicy = readPolicyDTO(policy.getPolicyId());
        removePolicy(existingPolicy.getPolicyId());
        addPolicy(policy);
    }

    /**
     * Read policy from store for PolicyFinder
     *
     * @param policyId policy id
     * @return AbstractPolicy
     * @throws EntitlementException custom exception
     */
    public synchronized AbstractPolicy readAbstractPolicy(String policyId) throws EntitlementException {
        PolicyStoreDTO policyDTO = readPolicyDTO(policyId);
        if (policyDTO == null){
            throw new EntitlementException("policy not found in PAP Store " , null);
        }
        String policy = new String(policyDTO.getPolicy().getBytes(), Charset.forName("UTF-8"));
        return PolicyReader.getInstance(null).getPolicy(policy);
    }

    public PolicyStoreDTO[] readAllPolicyDTOs(boolean active, boolean order) throws EntitlementException {
        PolicyStoreDTO[] policyStoreDTOs = readAllPolicyDTOs();
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
    public abstract PolicyStoreDTO readPolicyDTO(String policyId) throws EntitlementException;

    /**
     * Checks whether policy is exist for given policy id
     *
     * @param policyId policy id
     * @return true of false
     * @throws EntitlementException custom exception
     */
    public abstract boolean isExistPolicy(String policyId) throws EntitlementException;

    /**
     * Reads all the policyDTO in the store
     *
     * @return Array of PolicyStoreDTO
     * @throws EntitlementException custom exception
     */
    public abstract PolicyStoreDTO[] readAllPolicyDTOs() throws EntitlementException;

    /**
     * Remove a particular policy
     * 
     * @param policyId policyId going to remove
     * @throws EntitlementException custom exception
     */
    public abstract void removePolicy(String policyId) throws EntitlementException;

    /**
     *  The implementation of the addPolicy
     * 
     * @param policy policy 
     * @throws EntitlementException custom exception
     */
    protected abstract void doAdd(PolicyStoreDTO policy) throws EntitlementException;
}
