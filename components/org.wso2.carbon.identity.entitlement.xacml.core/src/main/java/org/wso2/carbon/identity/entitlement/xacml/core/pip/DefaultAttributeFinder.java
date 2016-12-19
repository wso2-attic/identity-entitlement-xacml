package org.wso2.carbon.identity.entitlement.xacml.core.pip;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.entitlement.xacml.core.exception.EntitlementException;
import org.wso2.carbon.identity.entitlement.xacml.core.pip.dummy.DummyUser;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * DefaultAttributeFinder talks to the underlying user store to read user attributes.
 * DefaultAttributeFinder is by default registered for all the claims defined under
 *  todo : its using dummy user store - have to use user store
 */
@Component(
        name = "org.wso2.carbon.identity.entitlement.xacml.core.pip.PIPAttributeFinder",
        immediate = true,
        service = PIPAttributeFinder.class
)
public class DefaultAttributeFinder extends AbstractPIPAttributeFinder {

    private static final Logger logger = LoggerFactory.getLogger(DefaultAttributeFinder.class);
    private Set<String> supportedAttrs = new HashSet<>();

    private HashMap<String, DummyUser> dummyUserStore = new HashMap<String, DummyUser>();

    /**
     * Loads all the claims defined under http://wso2.org/claims dialect.
     *
     * @throws Exception
     */
    public void init(Properties properties) throws Exception {
        logger.debug("DefaultAttributeFinder is initialized successfully");
        DummyUser dummyUser = new DummyUser("admin", "admin", "admin", "0123456789");
        dummyUserStore.put("admin", dummyUser);
        supportedAttrs.add("http://wso2.org/claims/role");
        supportedAttrs.add("http://wso2.org/claims/username");
        supportedAttrs.add("http://wso2.org/claims/userType");
        supportedAttrs.add("http://wso2.org/claims/mobile");
    }

    @Override
    public String getModuleName() {
        return "Default Attribute Finder";
    }


    public Set<String> getAttributeValues(String subjectId, String resourceId, String actionId,
                                          String environmentId, String attributeId, String issuer) throws EntitlementException {
        Set<String> values = new HashSet<>();

        DummyUser dummyUser = dummyUserStore.get(subjectId);

        if (dummyUser == null) {
            throw new EntitlementException("The user is not found in the DummyUserStore " + subjectId);
        }

        switch (attributeId) {
            case "http://wso2.org/claims/role":
                values.add(dummyUser.getRole());
                break;
            case "http://wso2.org/claims/username":
                values.add(dummyUser.getUserName());
                break;
            case "http://wso2.org/claims/mobile":
                values.add(dummyUser.getMobile());
                break;
            case "http://wso2.org/claims/userType":
                values.add(dummyUser.getUserType());
                break;
        }

        return values;
    }

    public Set<String> getSupportedAttributes() {
        return supportedAttrs;
    }
}
