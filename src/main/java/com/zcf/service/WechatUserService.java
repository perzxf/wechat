package com.zcf.service;

import com.baomidou.mybatisplus.service.IService;
import com.zcf.entity.WechatUser;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * @author zhangchuanfei
 * @create 2020-04-20-21:02
 */
public interface WechatUserService extends IService<WechatUser> {
    /**
     * 获取access_tocken GET方法
     * @return
     * @throws Exception
     */
    String getAccessToken() throws Exception;

    /**
     * 通过openid获取用户信息 GET方法
     * @param openid
     * @return
     * @throws Exception
     */
    Map<String, Object> getUserInfoByOpenid(String openid) throws Exception;

    /**
     * 生成带参数的二维码的ticket
     * @param expire_seconds
     * @param scene_str
     * @return
     * @throws Exception
     */
    String createTempStrTicket(String expire_seconds,String scene_str) throws Exception;

    /**
     * 获取二维码ticket后，通过ticket换取二维码图片展示
     * @param ticket
     * @return
     */
    String showqrcode(String ticket);

    /**
     * 存入数据
     * @param wechatUserMap
     */
    void insertWechatUser(Map<String, Object> wechatUserMap,String scene_str) throws InvocationTargetException, IllegalAccessException;

    Boolean getWechatUser(String scene_str);
}
