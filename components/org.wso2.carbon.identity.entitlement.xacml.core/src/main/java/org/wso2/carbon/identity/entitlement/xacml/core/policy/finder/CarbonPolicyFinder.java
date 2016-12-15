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
import org.wso2.carbon.identity.entitlement.xacml.core.exception.EntitlementException;
import org.wso2.carbon.identity.entitlement.xacml.core.policy.PolicyReader;
import org.wso2.carbon.identity.entitlement.xacml.core.policy.collection.PolicyCollection;
import org.wso2.carbon.identity.entitlement.xacml.core.policy.collection.SimplePolicyCollection;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

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
    private PolicyCollection policyCollection;
    private List<PolicyFinderModule> finderModules = new ArrayList<>();

    // TODO: 12/14/16 Where to order the policies we have ?

    @Reference(
            name = "policy.finder.module.service",
            service = PolicyFinderModule.class,
            cardinality = ReferenceCardinality.AT_LEAST_ONE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterPolicyFinderModule"
    )
    protected void registerPolicyFinderModule(PolicyFinderModule policyFinderModule) {
        finderModules.add(policyFinderModule);
    }

    private void unregisterPolicyFinderModule(PolicyFinderModule policyFinderModule) {
        finderModules.remove(policyFinderModule);
    }

    @Reference(
            name = "policy.collection.service",
            service = PolicyCollection.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterPolicyCollection"
    )
    protected void registerPolicyCollection(PolicyCollection policyCollection) {
        try {
            PolicyCombiningAlgorithm policyCombiningAlgorithm = EntitlementUtil.
                    getPolicyCombiningAlgorithm(DenyOverridesPolicyAlg.algId);
            policyCollection = new SimplePolicyCollection();
            policyCollection.setPolicyCombiningAlgorithm(policyCombiningAlgorithm);
        } catch (EntitlementException e) {
            logger.error(e.getMessage());
        }
        this.policyCollection = policyCollection;
    }

    protected void unregisterPolicyCollection(PolicyCollection policyCollection) {
    }

    @Override
    public void init(PolicyFinder finder) {
        policyReader = PolicyReader.getInstance(finder);
    }

    @Override
    public PolicyFinderResult findPolicy(EvaluationCtx context) {

        try {
            AbstractPolicy policy = policyCollection.getEffectivePolicy(context);
            if (policy == null) {
                return new PolicyFinderResult();
            } else {
                return new PolicyFinderResult(policy);
            }
        } catch (EntitlementException e) {
            ArrayList<String> code = new ArrayList<>();
            code.add(Status.STATUS_PROCESSING_ERROR);
            Status status = new Status(code, e.getMessage());
            return new PolicyFinderResult(status);
        }
    }

    @Override
    public PolicyFinderResult findPolicy(URI idReference, int type, VersionConstraints constraints,
                                         PolicyMetaData parentMetaData) {

        List<AbstractPolicy> policies = new ArrayList<>();

        if (this.finderModules != null) {
            finderModules.stream().filter(module -> policies.size() == 0).forEach(finderModule -> {
                try {
                    finderModule.getReferencedPolicy(idReference.toString())
                            .ifPresent(policy -> policyReader.getPolicy(policy).ifPresent(policies::add));
                } catch (EntitlementException e) {
                    logger.error(e.getMessage());
                }
            });
        }

        if (policies.size() != 0) {
            // we found a valid version, so see if it's the right kind,
            // and if it is then we return it
            AbstractPolicy policy = policies.get(0);
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
}
