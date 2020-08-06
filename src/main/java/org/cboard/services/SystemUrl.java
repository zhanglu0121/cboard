package org.cboard.services;

import org.cboard.pojo.DashboardUser;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Service
public class SystemUrl {
    private static Properties urlProperties;
    static{
        FileInputStream in = null;
        urlProperties = new Properties();
        URL resource = DashboardUser.class.getClassLoader().getResource("user&pass.properties");
        try {
            in = new FileInputStream(resource.getPath());
            urlProperties.load(in);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public Map<String,String> getUsernameAndPassword(){
        String username = urlProperties.getProperty("username");
        String password = urlProperties.getProperty("password");
        Map<String, String> map = new HashMap<>();
        map.put("username",username);
        map.put("password",password);
        return map;
    }
 }
