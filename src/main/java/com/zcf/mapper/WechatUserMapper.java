package com.zcf.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.zcf.entity.WechatUser;

/**
 * @author zhangchuanfei
 * @create 2020-04-20-21:03
 */
public interface WechatUserMapper extends BaseMapper<WechatUser> {
    WechatUser selectBySceneStr(String scene_str);
}
