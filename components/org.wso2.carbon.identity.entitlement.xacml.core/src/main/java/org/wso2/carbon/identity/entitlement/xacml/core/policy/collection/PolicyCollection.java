
package org.wso2.carbon.identity.entitlement.xacml.core.policy.collection;

import org.wso2.balana.AbstractPolicy;
import org.wso2.balana.VersionConstraints;
import org.wso2.balana.combine.PolicyCombiningAlgorithm;
import org.wso2.balana.ctx.EvaluationCtx;
import org.wso2.carbon.identity.entitlement.xacml.core.exception.EntitlementException;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Properties;

/**
 * Policy collection for all the policies defined in entitlement engine. This collection is created
 * by finding each and every policies that have been configured with each policy finder modules.
 * There can be different implementation to maintain policies.
 */
public interface PolicyCollection {

    /**
     * initializes policy finder collection
     *
     * @param properties Properties, that need to initialize the module
     * @throws Exception throws when initialization is failed
     */
    public void init(Properties properties) throws Exception;

    /**
     * adds policy to policy collection
     *
     * @param policy policy as AbstractPolicy object of Balana
     * @return whether policy is added successfully or not
     */
    public boolean addPolicy(AbstractPolicy policy);

    /**
     * returns the effective policy for given XACML request
     *
     * @param context XACML request ctx
     * @return effective policy set as AbstractPolicy object of Balana
     * @throws EntitlementException if any error, while policy is retrieved
     */
    public AbstractPolicy getEffectivePolicy(EvaluationCtx context) throws EntitlementException;

    /**
     * returns policy by given identifier
     *
     * @param identifier policy identifier
     * @return policy as AbstractPolicy object of Balana
     */
    public AbstractPolicy getPolicy(URI identifier);

    /**
     * returns policy by identifier type and version
     *
     * @param identifier  policy identifier
     * @param type        policy type whether policy or policy set
     * @param constraints policy version constraints
     * @return policy as AbstractPolicy object of Balana
     */
    public AbstractPolicy getPolicy(URI identifier, int type, VersionConstraints constraints);

    /**
     * sets global policy combining algorithm
     *
     * @param algorithm PolicyCombiningAlgorithm object of Balana
     */
    public void setPolicyCombiningAlgorithm(PolicyCombiningAlgorithm algorithm);

    /**
     * deletes the AbstractPolicy of the policyId
     * @param policyId  PolicyId as String
     * @return whether delete is sucess or not
     * @throws EntitlementException if any error, while removing policy
     */
    public boolean deletePolicy(String policyId) throws EntitlementException;

    public LinkedHashMap getPolicyMap() ;

    public void setPolicyMap(LinkedHashMap policyMap) ;

}
