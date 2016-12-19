package org.wso2.carbon.identity.entitlement.xacml.core.pip.dummy;

/**
 *
 */
public class DummyUser {

    private String role;

    private String userName;

    private String mobile;

    private String userType;

    public DummyUser(String userName, String role, String userType, String mobile) {
        this.role = role;
        this.userName = userName;
        this.mobile = mobile;
        this.userType = userType;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }
}
