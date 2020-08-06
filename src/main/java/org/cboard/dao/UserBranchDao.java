package org.cboard.dao;

import org.apache.ibatis.annotations.Param;
import org.cboard.pojo.DashboardUser;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserBranchDao {
    List<String> getBranchAuth(@Param("loginName") String loginName);
    List<String> getSiteAuth(@Param("loginName") String loginName);
    List<String> getCheckUpAuth(@Param("loginName") String loginName);
    List<String> getAllSite();
    List<String> getAllBranch();
}
