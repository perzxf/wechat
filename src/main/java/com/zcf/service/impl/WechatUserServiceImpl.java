package com.zcf.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.zcf.common.WechatConfig;
import com.zcf.entity.WechatUser;
import com.zcf.mapper.WechatUserMapper;
import com.zcf.service.WechatUserService;
import com.zcf.utils.HttpClient;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zhangchuanfei
 * @create 2020-04-20-21:02
 */
@Service
@Slf4j
public class WechatUserServiceImpl extends ServiceImpl<WechatUserMapper, WechatUser> implements WechatUserService {
    @Autowired
    private WechatUserMapper wechatMapper;

    /**
     * 获取access_tocken GET方法
     * @return
     * @throws Exception
     */
    @Override
    public String getAccessToken() throws Exception {
        String url = WechatConfig.ACCESS_TOKEN_URL;
        url = url.replaceAll("APPID", WechatConfig.APPID);
        url = url.replaceAll("SECRET", WechatConfig.SECRET);
        // 发送请求
        HttpClient client = new HttpClient(url);
        // 发送get请求
        client.get();
        // 获取到请求的结果  json格式的字符串，把json格式的字符串转换成对象或者Map集合
        String token_content = client.getContent();
        Map<String, Object> map = JSON.parseObject(token_content, Map.class);
        return map.get("access_token").toString();
    }

    /**
     * 通过openid获取用户信息 GET方法
     * @param openid
     * @return
     * @throws Exception
     */
    @Override
    public Map<String, Object> getUserInfoByOpenid(String openid) throws Exception {
        String accessToken = this.getAccessToken();
        String url = WechatConfig.GET_UNIONID_URL;
        url = url.replaceAll("ACCESS_TOKEN", accessToken);
        url = url.replaceAll("OPENID", openid);
        // 发送请求
        HttpClient client = new HttpClient(url);
        // 发送get请求
        client.get();
        // 获取到请求的结果  json格式的字符串，把json格式的字符串转换成对象或者Map集合
        String token_content = client.getContent();
        Map<String, Object> map = JSON.parseObject(token_content, Map.class);
        return map;
    }

    /**
     * 生成带参数的二维码的ticket
     * @param expire_seconds
     * @param scene_str
     * @return
     * @throws Exception
     */
    @Override
    public String createTempStrTicket(String expire_seconds,String scene_str) throws Exception {
        String access_token = this.getAccessToken();
        String url = WechatConfig.CREATE_TICKET_PATH;
        url = url.replaceAll("ACCESS_TOKEN",access_token);

        Map<String,String> strMap = new HashMap<String,String>();
        strMap.put("scene_str",scene_str);

        Map<String,Map<String,String>> mapMap = new HashMap<String,Map<String,String>>();
        mapMap.put("scene", strMap);

        Map<String,Object> paramsMap = new HashMap<String,Object>();
        paramsMap.put("expire_seconds", expire_seconds);
        paramsMap.put("action_name", WechatConfig.QR_STR_SCENE);
        paramsMap.put("action_info", mapMap);
        String data = new Gson().toJson(paramsMap);
        // 发送请求
        Map<String, Object> map = HttpClient.httpClientPost(url, data);
        String ticket = (String) map.get("ticket");

        return ticket==null?null:ticket;
    }

    /**
     * 获取二维码ticket后，通过ticket换取二维码图片展示
     * @param ticket
     * @return
     */
    @Override
    public String showqrcode(String ticket) {
        String qrcodeUrl = WechatConfig.SHOWQR_CODE_PATH;
        try {
            String encode = URLEncoder.encode(ticket, "utf-8");
            qrcodeUrl = qrcodeUrl.replaceAll("TICKET",encode);
        }catch (UnsupportedEncodingException e){
            e.printStackTrace();
        }
        return qrcodeUrl;
    }

    @Override
    public void insertWechatUser(Map<String, Object> map,String scene_str) throws InvocationTargetException, IllegalAccessException {
        WechatUser user = new WechatUser();
        BeanUtils.populate(user, map);
        user.setQr_scene_str(scene_str);
        WechatUser wechatUser = wechatMapper.selectById(user.getOpenid());
        if(wechatUser != null){
            wechatMapper.updateById(user);
        }else{
            wechatMapper.insert(user);
        }
    }

    @Override
    public Boolean getWechatUser(String scene_str) {
        WechatUser wechatUser = wechatMapper.selectBySceneStr(scene_str);
        if(wechatUser != null){
            return true;
        }
        return false;
    }
}
