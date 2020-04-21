package com.zcf.common;

/**
 * @author zhangchuanfei
 * @create 2020-04-20-21:07
 */
public class WechatConfig {
    //第三方用户唯一凭证，即appid
    public static final String APPID = "wxee746fb0e8f48f44";
    //第三方用户唯一凭证密钥，即appsecret
    public static final String SECRET = "bed208cba5e32cf8e92bbc2c96bd16da";
    //获取access_token
    public static final String ACCESS_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=APPID&secret=SECRET";
    // 临时二维码
    public static final String QR_SCENE = "QR_SCENE";
    // 临时的字符串参数值
    public static final String QR_STR_SCENE = "QR_STR_SCENE";
    // 永久二维码
    public static final String QR_LIMIT_SCENE = "QR_LIMIT_SCENE";
    // 永久二维码(字符串)
    public static final String QR_LIMIT_STR_SCENE = "QR_LIMIT_STR_SCENE";
    // 通过accessToken 以及json 创建二维码  获取ticket
    public static final String CREATE_TICKET_PATH = "https://api.weixin.qq.com/cgi-bin/qrcode/create?access_token=ACCESS_TOKEN";
    // 通过ticket换取二维码
    public static final String SHOWQR_CODE_PATH = "https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=TICKET";
    //此接口用于获取用户个人信息 UnionID机制
    public static final String GET_UNIONID_URL = "https://api.weixin.qq.com/cgi-bin/user/info?access_token=ACCESS_TOKEN&openid=OPENID";

}
