package org.cboard.pojo;

/**
 * Created by yfyuan on 2016/12/2.
 */
public class DashboardUser {
    private String userId;
    private String loginName;
    private String userName;
    private String userPassword;
    private String userStatus;
    private String siteAuthority;
    private String branchAuthority;
    private String checkUpList;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    public String getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(String userStatus) {
        this.userStatus = userStatus;
    }

    public String getSiteAuthority() {
        return siteAuthority;
    }

    public void setSiteAuthority(String siteAuthority) {
        this.siteAuthority = siteAuthority;
    }

    public String getBranchAuthority() {
        return branchAuthority;
    }

    public void setBranchAuthority(String branchAuthority) {
        this.branchAuthority = branchAuthority;
    }

    public String getCheckUpList() {
        return checkUpList;
    }

    public void setCheckUpList(String checkUpList) {
        this.checkUpList = checkUpList;
    }
}
