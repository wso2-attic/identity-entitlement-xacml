package org.wso2.carbon.identity.entitlement.xacml.core.deployer;


import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.balana.AbstractPolicy;
import org.wso2.carbon.deployment.engine.Artifact;
import org.wso2.carbon.deployment.engine.ArtifactType;
import org.wso2.carbon.deployment.engine.Deployer;
import org.wso2.carbon.deployment.engine.exception.CarbonDeploymentException;
import org.wso2.carbon.identity.entitlement.xacml.core.EntitlementConstants;
import org.wso2.carbon.identity.entitlement.xacml.core.policy.PolicyReader;
import org.wso2.carbon.identity.entitlement.xacml.core.policy.collection.PolicyCollection;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Carbon deployment listener listen to provided policy store directory and get the deploy, undeploy and update
 * events and keep the policy store updated.
 */
@Component(
        name = "org.wso2.carbon.identity.entitlement.xacml.core.deployer.PolicyDeployer",
        immediate = true
)
public class PolicyDeployer implements Deployer {

    private static final Logger logger = LoggerFactory.getLogger(PolicyDeployer.class);
    private ArtifactType artifactType;
    private URL repository;
    private PolicyCollection policyCollection;

    @Reference(
            name = "policy.collection.service",
            service = PolicyCollection.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterPolicyCollection"
    )
    protected void registerPolicyCollection(PolicyCollection policyCollection) {
        this.policyCollection = policyCollection;
    }

    protected void unregisterPolicyCollection(PolicyCollection policyCollection) {
    }

    @Override
    public void init() {
        logger.debug("Initializing the PolicyDeployer");
        artifactType = new ArtifactType<>("policy");
        try {
            // TODO: 12/2/16 policy location configurable
            repository = new URL("file:" + EntitlementConstants.POLICY_STORE_LOCATION);
        } catch (MalformedURLException e) {
            logger.error("Error while initializing deployer", e.getMessage());
        }
    }

    @Override
    public String deploy(Artifact artifact) throws CarbonDeploymentException {
        logger.debug("Deploying : " + artifact.getName());
        try {
            String policyId = artifact.getName();
            if (policyId.endsWith(EntitlementConstants.POLICY_BUNDLE_EXTENSTION)) {
                String policyPath = artifact.getFile().getAbsolutePath();
                String content = new String(Files.readAllBytes(Paths.get(policyPath)), "UTF-8");

                AbstractPolicy abstractPolicy = PolicyReader.getInstance(null).getPolicy(content
                        .replaceAll(">\\s+<", "><").replaceAll("\n", " ").replaceAll("\r", " "));
                if (abstractPolicy != null) {
                    policyCollection.addPolicy(abstractPolicy);
                }
            }
        } catch (IOException e) {
            logger.error("Error in reading the policy : ", e);
        }
        return artifact.getName();
    }

    @Override
    public void undeploy(Object key) throws CarbonDeploymentException {
        if (!(key instanceof String)) {
            throw new CarbonDeploymentException("Error while Un Deploying : " + key + "is not a String value");
        }
        logger.debug("Undeploying : " + key);
        // TODO: 12/7/16 if the file name doesn't match this policyId then it will fail
        policyCollection.deletePolicy((String) key);
    }

    @Override
    public Object update(Artifact artifact) throws CarbonDeploymentException {
        logger.debug("Updating : " + artifact.getName());
        try {
            String policyId = artifact.getName();
            if (policyId.endsWith(EntitlementConstants.POLICY_BUNDLE_EXTENSTION)) {
                String policyPath = artifact.getFile().getAbsolutePath();
                String content = new String(Files.readAllBytes(Paths.get(policyPath)), "UTF-8");

                AbstractPolicy abstractPolicy = PolicyReader.getInstance(null).getPolicy(content.replaceAll(">\\s+<", "><"));
                if (abstractPolicy != null) {
                    policyCollection.addPolicy(abstractPolicy);
                }
            }
        } catch (IOException e) {
            logger.error("Error in reading the policy : ", e);
        }
        return artifact.getName();
    }

    @Override
    public URL getLocation() {
        return repository;
    }

    @Override
    public ArtifactType getArtifactType() {
        return artifactType;
    }
}
