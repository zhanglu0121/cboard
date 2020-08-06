package org.cboard.controller;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.cboard.dto.DashboardMenu;
import org.cboard.dto.User;
import org.cboard.services.*;
import org.cboard.util.IPUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yfyuan on 2016/7/25.
 */
@RestController
@RequestMapping("/commons")
public class CommonsController extends BaseController {

    @Autowired
    private MenuService menuService;

    @Autowired
    private AdminSerivce adminSerivce;

    @Autowired
    private PersistService persistService;

    @Autowired
    private SystemUrl systemUrl;

    @Autowired
    private RedisTemplate redisTemplate;

    //获取cookie中的N10登录用户名，回传前台登录用户名密码
    @RequestMapping(value = "/getCurrentLoginName")
    public Map<String, String> getCurrentLoginName(HttpServletRequest request){
        String loginInfo = "";
        Map<String,String> map = new HashMap<>();
        String addr = IPUtil.getIpAddr(request);
        try{
            loginInfo = (String) redisTemplate.boundHashOps("CboardUser").get(addr);
        }catch (Exception e){
            map.put("username","");
            return map;
        }
        map = this.getUserNameAndPassWord(loginInfo);
        this.setCurrentLoginName(loginInfo,"0",request);
        map.put("currentLoginName",loginInfo);
        return map;
    }

    //将N10登录用户名存到session中
    @RequestMapping(value = "/setCurrentLoginName")
    public void setCurrentLoginName(String loginName,String flag ,HttpServletRequest request){
        if("1".equals(flag)){
            return;
        }
            request.getSession().setAttribute("currentLoginName",loginName);
    }

    //根据N10登录名获取  用户名密码
    public Map<String,String> getUserNameAndPassWord(String loginName){
        Map<String,String> map = new HashMap<>();
        if(StringUtils.isEmpty(loginName)){
            map.put("username","");
        }else {
            if("admin".equals(loginName)){
                map.put("username","admin");
                map.put("password","root123");
            }else{
                map = systemUrl.getUsernameAndPassword();
            }
        }
        return map;
    }

    @RequestMapping(value = "/getUserDetail")
    public User getUserDetail() {
        return authenticationService.getCurrentUser();
    }

    @RequestMapping(value = "/getMenuList")
    public List<DashboardMenu> getMenuList() {
        return menuService.getMenuList();
    }

    @RequestMapping(value = "/changePwd")
    public ServiceStatus changePwd(@RequestParam(name = "curPwd") String curPwd, @RequestParam(name = "newPwd") String newPwd, @RequestParam(name = "cfmPwd") String cfmPwd) {
        return adminSerivce.changePwd(tlUser.get().getUserId(), curPwd, newPwd, cfmPwd);
    }

    @RequestMapping(value = "/persist")
    public String persist(@RequestBody String dataStr) {
        JSONObject data = JSONObject.parseObject(dataStr);
        return persistService.persistCallback(data.getString("persistId"), data.getJSONObject("data"));
    }
}
