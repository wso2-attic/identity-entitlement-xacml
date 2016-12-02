package org.wso2.carbon.identity.entitlement.xacml.core.deployer;


import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.deployment.engine.Artifact;
import org.wso2.carbon.deployment.engine.ArtifactType;
import org.wso2.carbon.deployment.engine.Deployer;
import org.wso2.carbon.deployment.engine.exception.CarbonDeploymentException;
import org.wso2.carbon.identity.entitlement.xacml.core.dto.PolicyStoreDTO;
import org.wso2.carbon.identity.entitlement.xacml.core.exception.EntitlementException;
import org.wso2.carbon.identity.entitlement.xacml.core.policy.store.PolicyStore;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Carbon deployment listener listen to deployment/xamcl/policy directory and get the deploy, undeploy and update
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
    private PolicyStore papPolicyStore;

    @Activate
    protected void start(BundleContext bundleContext) {
        papPolicyStore = new PolicyStore();
    }

    @Override
    public void init() {
        logger.debug("Initializing the PolicyDeployer");
        artifactType = new ArtifactType<>("policy");
        try {
            // TODO: 12/2/16 policy location configurable
            repository = new URL("file:xacml/policy");
        } catch (MalformedURLException e) {
            logger.error("Error while initializing deployer", e.getMessage());
        }
    }

    @Override
    public String deploy(Artifact artifact) throws CarbonDeploymentException {
        logger.debug("Deploying : " + artifact.getName());
        try {
            String policyId = artifact.getName();
            if (! policyId.endsWith(".xml")) {
                logger.debug("non-xml file deployed ");
                return null;
            }
            String policyPath = artifact.getFile().getAbsolutePath();
            String content = new String(Files.readAllBytes(Paths.get(policyPath)), "UTF-8");
            PolicyStoreDTO policyDTO = new PolicyStoreDTO();
            policyDTO.setPolicyId(policyId.substring(0, policyId.lastIndexOf(".")));
            policyDTO.setPolicy(content);
            policyDTO.setActive(true);
            papPolicyStore.addPolicy(policyDTO);
            return artifact.getName();
        } catch (IOException e) {
            logger.error("Error in reading the policy : ", e);
//            throw new EntitlementException("Error in reading the policy : ", e);
        } catch (EntitlementException e) {
//            throw new EntitlementException("", e);
        }
        return null;
    }

    @Override
    public void undeploy(Object key) throws CarbonDeploymentException {
        if (!(key instanceof String)) {
            throw new CarbonDeploymentException("Error while Un Deploying : " + key + "is not a String value");
        }
        logger.debug("Undeploying : " + key);
        try {
            papPolicyStore.removePolicy((String) key);
        } catch (EntitlementException e) {
//            throw new EntitlementException("", e);
        }
    }

    @Override
    public Object update(Artifact artifact) throws CarbonDeploymentException {
        logger.debug("Updating : " + artifact.getName());
        try {
            String policyId = artifact.getName();
            if (! policyId.endsWith(".xml")) {
                logger.debug("non-xml file updated ");
                return null;
            }
            String policyPath = artifact.getFile().getAbsolutePath();
            String content = new String(Files.readAllBytes(Paths.get(policyPath)), "UTF-8");
            logger.debug("policy in the file : " + content);
            PolicyStoreDTO policyDTO = new PolicyStoreDTO();
            policyDTO.setPolicyId(policyId.substring(0, policyId.lastIndexOf(".")));
            policyDTO.setPolicy(content);
            papPolicyStore.updatePolicy(policyDTO);
            return artifact.getName();
        } catch (IOException e) {
            logger.error("Error in reading the policy : ", e);
//            throw new EntitlementException("Error in reading the policy : ", e);
        } catch (EntitlementException e) {
//            throw new EntitlementException("", e);
        }
        return null;
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
