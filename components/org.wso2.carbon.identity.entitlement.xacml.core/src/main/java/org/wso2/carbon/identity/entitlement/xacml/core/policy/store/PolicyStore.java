package org.wso2.carbon.identity.entitlement.xacml.core.policy.store;

import org.wso2.carbon.identity.entitlement.xacml.core.dto.PolicyStoreDTO;
import org.wso2.carbon.identity.entitlement.xacml.core.exception.EntitlementException;

/**
 *
 */
public interface PolicyStore {

    /**
     * @param policy PolicyStoreDTO which is going to added to the store
     * @throws EntitlementException custom exception
     */
    public void addPolicy(PolicyStoreDTO policy) throws EntitlementException;

    /**
     * @param policy updated PolicyStoreDTO which is going to replaced in the store
     * @throws EntitlementException custom exception
     */
    public void updatePolicy(PolicyStoreDTO policy) throws EntitlementException;


    public PolicyStoreDTO[] readAllPolicyDTOs(boolean active, boolean order) throws EntitlementException;

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

}
