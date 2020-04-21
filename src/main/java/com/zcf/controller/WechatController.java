package com.zcf.controller;

import com.zcf.service.WechatUserService;
import com.zcf.utils.MessageUtil;
import com.zcf.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zhangchuanfei
 * @create 2020-04-20-20:53
 */
@Controller
public class WechatController {
    @Autowired
    private WechatUserService wechatService;

    /**
     * 跳转页面
     * @return
     */
    @RequestMapping("/")
    public String showLogin() {
        return "login.html";
    }

    /**
     * 生成带参数的二维码，扫描关注微信公众号，自动登录网站
     * @param modelMap
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/wechat/login")
    @ResponseBody
    public Result wechatMpLogin(ModelMap modelMap) throws Exception {
//        String accessToken = wechatService.getAccessToken();
        String scene_str = "perFei." + new Date().getTime();
        String ticket = wechatService.createTempStrTicket("600", scene_str);
        if(ticket != null){
            String qrcodeUrl = wechatService.showqrcode(ticket);
            modelMap.put("qrcodeUrl", qrcodeUrl);
        }
        modelMap.put("scene_str", scene_str);
        return Result.ok(null, modelMap);
    }

    /**
     * 检测登录
     * @param scene_str
     * @return
     */
    @RequestMapping("/wechat/checkLogin")
    @ResponseBody
    public  Map<String, Object> wechatMpCheckLogin(String scene_str) {
        // 根据scene_str查询数据库，获取对应记录
        Boolean falg = wechatService.getWechatUser(scene_str);
        Map<String, Object> returnMap = new HashMap<String, Object>();
        if (falg) {
            returnMap.put("result", "true");
        } else {
            returnMap.put("result", "false");
        }
        return returnMap;
    }

    /**
     * 自定义token, 用作生成签名,从而验证安全性
     */
    private static final String TOKEN = "perFei";

    /**
     * 回调函数
     * @param request
     * @throws Exception
     */
    @RequestMapping(value = "/wechat/callback")
    public void callback(HttpServletRequest request, HttpServletResponse response) throws Exception{
        // TODO 验证接口配置信息
       /*
        System.out.println("-----开始校验签名-----");
        // 接收微信服务器发送请求时传递过来的参数

        //微信加密签名，signature结合了开发者填写的token参数和请求中的timestamp参数、nonce参数。
        String signature = request.getParameter("signature");
        String timestamp = request.getParameter("timestamp"); //时间戳
        String nonce = request.getParameter("nonce"); //随机数
        String echostr = request.getParameter("echostr");//随机字符串

        //将token、timestamp、nonce三个参数进行字典序排序
        //并拼接为一个字符串
        String sortStr = sort(TOKEN,timestamp,nonce);
        //字符串进行shal加密
        String mySignature = shal(sortStr);
        // 校验微信服务器传递过来的签名 和  加密后的字符串是否一致, 若一致则签名通过
        if(!"".equals(signature) && !"".equals(mySignature) && signature.equals(mySignature)){
            System.out.println("-----签名校验通过-----");
            response.getWriter().write(echostr);
            response.getWriter().flush();
        }else {
            System.out.println("-----校验签名失败-----");
        }
        */

        // TODO 接收、处理、响应由微信服务器转发的用户发送给公众帐号的消息
        // 将请求、响应的编码均设置为UTF-8（防止中文乱码）
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        System.out.println("请求进入");
        String result = "";
        try {
            //获得解析微信发来的请求
            Map<String,String> map = MessageUtil.parseXml(request);
            if (map != null && map.get("FromUserName").toString() != null){
                // 通过openid获取用户信息
                Map<String, Object> wechatUserMap = wechatService.getUserInfoByOpenid(map.get("FromUserName"));
                // 将数据写入到数据库中
                String event = map.get("EventKey");
                wechatService.insertWechatUser(wechatUserMap,event);
            }
            System.out.println("开始构造消息");
            //根据消息类型 构造返回消息
            result = MessageUtil.buildXml(map);
            System.out.println(result);
            if(result.equals("")){
                result = "未正确响应";
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("发生异常："+ e.getMessage());
        }
        response.getWriter().println(result);

    }

    /**
     * 参数排序
     * @param token
     * @param timestamp
     * @param nonce
     * @return
     */
    public String sort(String token, String timestamp, String nonce) {
        String[] strArray = {token, timestamp, nonce};
        Arrays.sort(strArray);
        StringBuilder sb = new StringBuilder();
        for (String str : strArray) {
            sb.append(str);
        }
        return sb.toString();
    }

    /**
     * 字符串进行shal加密
     * @param str
     * @return
     */
    public String shal(String str){
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            digest.update(str.getBytes());
            byte messageDigest[] = digest.digest();

            StringBuffer hexString = new StringBuffer();
            // 字节数组转换为 十六进制 数
            for (int i = 0; i < messageDigest.length; i++) {
                String shaHex = Integer.toHexString(messageDigest[i] & 0xFF);
                if (shaHex.length() < 2) {
                    hexString.append(0);
                }
                hexString.append(shaHex);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
}
