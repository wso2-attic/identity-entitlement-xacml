package org.wso2.carbon.identity.entitlement.xacml.core.policy.store;

import org.wso2.carbon.identity.entitlement.xacml.core.dto.PolicyDTO;
import org.wso2.carbon.identity.entitlement.xacml.core.dto.PolicyStoreDTO;
import org.wso2.carbon.identity.entitlement.xacml.core.exception.EntitlementException;

/**
 * PolicyStore is an extension point where XACML policies can be stored and
 * loaded from different sources. This is specially written for storing policies.
 * There can be only one policy store module
 */
public interface PolicyStore {

    /**
     * @param policy PolicyDTO which is going to added to the store
     * @throws EntitlementException custom exception
     */
    public void addPolicy(PolicyDTO policy) throws EntitlementException;

    /**
     * @param policy updated PolicyDTO which is going to replaced in the store
     * @throws EntitlementException custom exception
     */
    public void updatePolicy(PolicyDTO policy) throws EntitlementException;

    /**
     * @param active filter only active policy or not
     * @param order  order policy by policyOrder or not
     * @throws EntitlementException custom exception
     */
    public PolicyDTO[] readAllPolicyDTOs(boolean active, boolean order) throws EntitlementException;

    PolicyStoreDTO[] readAllPolicyStoreDTOs(boolean active, boolean order);

    /**
     * Reads PolicyStoreDTO for given policy id
     *
     * @param policyId policy id
     * @return PolicyStoreDTO
     * @throws EntitlementException custom exception
     */
    public abstract PolicyDTO readPolicyDTO(String policyId) throws EntitlementException;

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
    public abstract PolicyDTO[] readAllPolicyDTOs() throws EntitlementException;

    /**
     * Remove a particular policy
     *
     * @param policyId policyId going to remove
     * @throws EntitlementException custom exception
     */
    public abstract void removePolicy(String policyId) throws EntitlementException;

    public String[] getPolicyIdentifiers();
}
