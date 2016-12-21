package org.wso2.carbon.identity.entitlement.xacml.core.policy.collection;

import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.balana.AbstractPolicy;
import org.wso2.balana.MatchResult;
import org.wso2.balana.Policy;
import org.wso2.balana.PolicyReference;
import org.wso2.balana.PolicySet;
import org.wso2.balana.VersionConstraints;
import org.wso2.balana.combine.PolicyCombiningAlgorithm;
import org.wso2.balana.ctx.EvaluationCtx;
import org.wso2.carbon.identity.entitlement.xacml.core.exception.EntitlementException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * simple implementation of Policy collection interface. This uses in-memory map to maintain policies
 * policy versions are not maintained
 */
@Component(
        name = "org.wso2.carbon.identity.entitlement.xacml.core.policy.collection.SimplePolicyCollection",
        immediate = true,
        service = org.wso2.carbon.identity.entitlement.xacml.core.policy.collection.PolicyCollection.class
)
public class SimplePolicyCollection implements PolicyCollection {

    private static final Logger logger = LoggerFactory.getLogger(SimplePolicyCollection.class);

    /**
     * the actual collection of policies
     * to maintain the order of the policies, <code>LinkedHashMap</code> has been used.
     * Map with  policy identifier policy as <code>AbstractPolicy</code> object
     */
    private static LinkedHashMap<URI, AbstractPolicy> policyCollection = new LinkedHashMap<>();

    /**
     * the optional combining algorithm used when wrapping multiple policies
     * if no algorithm is defined, only one applicable algorithm is used
     */
    private PolicyCombiningAlgorithm combiningAlg;
    /**
     * the optional policy id used when wrapping multiple policies
     */
    private URI parentId;

    @Override
    public void init(Properties properties) throws Exception {
        String parentIdProperty = properties.getProperty("parentId");
        if (parentIdProperty != null) {
            parentId = new URI(parentIdProperty);
        }
    }

    @Override
    public boolean addPolicy(AbstractPolicy policy) {
        logger.debug("Adding policy to SimplePolicyCollection : " + policy.getId().toString());
        return addPolicy(policy.getId(), policy);
    }

    @Override
    public AbstractPolicy getEffectivePolicy(EvaluationCtx context) throws EntitlementException {

        // setup a list of matching policies
        ArrayList<AbstractPolicy> list = new ArrayList<>();

        for (Map.Entry<URI, AbstractPolicy> entry : policyCollection.entrySet()) {

            AbstractPolicy policy = entry.getValue();

            // see if we match
            MatchResult match = policy.match(context);
            int result = match.getResult();

            // if there was an error, we stop right away
            if (result == MatchResult.INDETERMINATE) {
                logger.error(match.getStatus().getMessage());
                throw new EntitlementException(match.getStatus().getMessage());
            }

            // if we matched, we keep track of the matching policy...
            if (result == MatchResult.MATCH) {
                // ...first checking if this is the first match and if
                // we automatically nest policies

                logger.debug("Matching XACML policy found " + policy.getId().toString());

                if ((combiningAlg == null) && (list.size() > 0)) {
                    logger.error("Too many applicable top-level policies");
                    throw new EntitlementException("Too many applicable top-level policies");
                }

                list.add(policy);
            }
        }

        // no errors happened during the search, so now take the right
        // action based on how many policies we found
        switch (list.size()) {
            case 0:
                logger.debug("No matching XACML policy found");
                return null;
            case 1:
                return list.get(0);
            default:
                return new PolicySet(parentId, combiningAlg, null, list);
        }

    }

    @Override
    public AbstractPolicy getPolicy(URI policyId) {
        return policyCollection.get(policyId);
    }

    @Override
    public AbstractPolicy getPolicy(URI identifier, int type, VersionConstraints constraints) {

        AbstractPolicy policy = policyCollection.get(identifier);

        if (policy != null) {
            // we found a valid version, so see if it's the right kind,
            // and if it is then we return it
            if (type == PolicyReference.POLICY_REFERENCE) {
                if (policy instanceof Policy)
                    return policy;
            } else {
                if (policy instanceof PolicySet)
                    return policy;
            }
        }

        return null;
    }

    private synchronized boolean addPolicy(URI identifier, AbstractPolicy policy) {
        return policyCollection.put(identifier, policy) != null;
    }

    @Override
    public void setPolicyCombiningAlgorithm(PolicyCombiningAlgorithm algorithm) {
        this.combiningAlg = algorithm;
    }

    @Override
    public boolean deletePolicy(String policyId) throws EntitlementException {
        logger.debug("Deleting policy from SimplePolicyCollection : " + policyId);
        try {
            return policyCollection.remove(new URI(policyId)) != null;
        } catch (URISyntaxException e) {
            throw new EntitlementException("Error in casting policyId to URI ", e);
        }
    }

    @Override
    public LinkedHashMap getPolicyMap() {
        return policyCollection;
    }

    @Override
    public void setPolicyMap(LinkedHashMap policyMap) {
        policyCollection = policyMap;
    }
}
