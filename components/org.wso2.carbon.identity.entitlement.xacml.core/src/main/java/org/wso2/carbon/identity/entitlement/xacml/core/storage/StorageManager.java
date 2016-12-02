package org.wso2.carbon.identity.entitlement.xacml.core.storage;

import org.wso2.carbon.identity.entitlement.xacml.core.dto.PolicyStoreDTO;
import org.wso2.carbon.identity.entitlement.xacml.core.exception.EntitlementException;

/**
 * Interface for StorageManager used in entitlement
 * New storage management have to implement ths interface
 */
public interface StorageManager {


    /**
     *
     * @param policy PolicyStoreDTO object going to store
     * @param store whether its PAP or PDP
     * @throws EntitlementException custom exception
     */
    public void store(PolicyStoreDTO policy, String store) throws EntitlementException;

    /**
     *
     * @param policyId policyId going to read
     * @param store whether its PAP or PDP
     * @return PolicyStoreDTO object
     * @throws EntitlementException custom exception
     */
    public PolicyStoreDTO read(String policyId, String store) throws EntitlementException;

    /**
     *
     * @param policyId policyId going to remove
     * @param store whether its PAP or PDP
     * @throws EntitlementException custom exception
     */
    public void remove(String policyId, String store) throws EntitlementException;

    /**
     *
     * @param store whether its PAP or PDP
     * @return list of policyId going to readPolicyDTO
     * @throws EntitlementException custom exception
     */
    public PolicyStoreDTO[] readAll(String store) throws EntitlementException;
}
