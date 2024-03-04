package io.renren.modules.app.controller;


import cn.hutool.extra.qrcode.QrCodeUtil;
import cn.hutool.extra.qrcode.QrConfig;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.github.wxpay.sdk.MyWXPayConfig;
import com.github.wxpay.sdk.WXPay;
import com.github.wxpay.sdk.WXPayConfig;
import com.github.wxpay.sdk.WXPayUtil;
import io.renren.common.utils.R;
import io.renren.common.validator.ValidatorUtils;
import io.renren.modules.app.annotation.Login;
import io.renren.modules.app.entity.OrderEntity;
import io.renren.modules.app.entity.UserEntity;
import io.renren.modules.app.form.PayOrderForm;
import io.renren.modules.app.form.UpdateOrderStatusForm;
import io.renren.modules.app.form.WxLoginForm;
import io.renren.modules.app.service.OrderService;
import io.renren.modules.app.service.UserService;
import io.renren.modules.app.utils.JwtUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 微信相关接口
 */
@RestController
@RequestMapping("/app/wx")
@Api("微信相关接口")
@Slf4j
public class WxController {

    @Value("${application.app-id}")
    private String appId;

    @Value("${application.app-secret}")
    private String appSecret;

    @Value("${application.mch-id}")
    private String mchId;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private OrderService orderService;

    @Autowired
    private MyWXPayConfig myWXPayConfig;

    @Value("${application.key}")
    private String key;

    /**
     * 微信登录
     */
    @PostMapping("login")
    @ApiOperation("微信登录")
    public R login(@RequestBody WxLoginForm form){
        log.info("******************【微信登录-开始】******************");
        // 校验
        ValidatorUtils.validateEntity(form);

        // 提交参数
        String url = "https://api.weixin.qq.com/sns/jscode2session";
        HashMap map = new HashMap();
        map.put("appid", appId);
        map.put("secret", appSecret);
        map.put("js_code", form.getCode());
        map.put("grant_type", "authorization_code");

        // 发起请求
        String response = HttpUtil.post(url, map);

        // 解析结果
        JSONObject json = JSONUtil.parseObj(response);
        String openid = json.getStr("openid");
        if (openid == null || openid.length() == 0) {
            return R.error("临时登录凭证错误");
        }

        // 查询或保存用户信息
        UserEntity user = new UserEntity();
        user.setOpenId(openid);
        QueryWrapper queryWrapper = new QueryWrapper(user);
        int count = userService.count(queryWrapper);
        if (count == 0) {
            user.setNickname(form.getNickname());
            user.setPhoto(form.getPhoto());
            user.setType(2); // 微信登录
            user.setCreateTime(new Date());
            userService.save(user);
        }

        // 生成token
        UserEntity userOpenid = new UserEntity();
        userOpenid.setOpenId(openid);
        QueryWrapper queryWrapperOpenid = new QueryWrapper(userOpenid);
        UserEntity userOpen = userService.getOne(queryWrapperOpenid);
        Long id = userOpen.getUserId();
        String token = jwtUtils.generateToken(id);

        // 返回参数
        HashMap result = new HashMap();
        result.put("token", token);
        result.put("expire", jwtUtils.getExpire());
        log.info("******************【微信登录-结束】******************");
        return R.ok(result);
    }

    /**
     * 小程序支付订单
     */
    @PostMapping("microAppPayOrder")
    @ApiOperation("小程序支付订单")
    @Login
    public R microAppPayOrder(@RequestBody PayOrderForm form,
                 @RequestHeader HashMap header){
        log.info("******************【小程序支付订单-开始】******************");
        // 校验
        ValidatorUtils.validateEntity(form);
        String token = header.get("token").toString();
        Long userId = Long.parseLong(jwtUtils.getClaimByToken(token).getSubject());
        int orderId = form.getOrderId();

        // 判断用户
        UserEntity user = new UserEntity();
        user.setUserId(userId);
        QueryWrapper wrapper = new QueryWrapper(user);
        int count = userService.count(wrapper);
        if (count == 0) {
            return R.error("用户不存在");
        }
        String openId = userService.getOne(wrapper).getOpenId();

        // 查询订单
        OrderEntity order = new OrderEntity();
        order.setUserId(userId.intValue());
        order.setId(orderId);
        order.setStatus(1);
        wrapper = new QueryWrapper(order);
        int count1 = orderService.count(wrapper);
        if (count1 == 0) {
            return R.error("订单不存在");
        }

        // 查询订单内容
        order = new OrderEntity();
        order.setId(orderId);
        wrapper = new QueryWrapper(order);
        OrderEntity queryOrder = orderService.getOne(wrapper);

        // 向微信平台发出请求，创建支付订单
        String amount = queryOrder.getAmount().multiply(new BigDecimal("100")).intValue()+"";
        log.info("支付金额:{}", amount);
//        String amount = "30";
        // 创建WXpay
        try {
            WXPay wxPay = new WXPay(myWXPayConfig);
            HashMap map = new HashMap();
            map.put("nonce_str", WXPayUtil.generateNonceStr()); // 随机字符串
            map.put("body", "订单备注");
            map.put("out_trade_no", queryOrder.getCode()); // 微信要求唯一
            map.put("total_fee", amount);
            map.put("spbill_create_ip", "127.0.0.1");
            map.put("notify_url", "http://localhost:8080/app/wx/notify");
            map.put("trade_type", "JSAPI");
            map.put("openid", openId);
            // 调用微信支付
            Map<String, String> result = wxPay.unifiedOrder(map);
            /**
             * return_code 状态码,  app_id 小程序ID,  mch_id 商品ID, nonce_str: 随机字符串
             * sign 数字签名, result_code 业务结果, trade_type 交易类型, prepay_id支付订单号
             */
            String prepayId = result.get("prepay_id");
            log.info("微信返回的prepayId: {}", prepayId);
            if (prepayId != null) {
                // 保存支付订单ID
                queryOrder.setPrepayId(prepayId);
                UpdateWrapper updateWrapper = new UpdateWrapper();
                updateWrapper.eq("id", queryOrder.getId());
                orderService.update(queryOrder, updateWrapper);
                // 生成数字签名
                Map signMap = new HashMap();
                signMap.put("appId", appId);
                String timeStamp = new Date().getTime()+"";
                signMap.put("timeStamp", timeStamp);
                String nonStr = WXPayUtil.generateNonceStr();
                signMap.put("nonceStr", nonStr);
                signMap.put("package", "prepay_id=" + prepayId);
                signMap.put("signType", "MD5");
                String paySign = WXPayUtil.generateSignature(signMap, key);
                log.info("******************【小程序支付订单-结束】******************");
                return R.ok()
                        .put("package", "prepay_id=" + prepayId)
                        .put("timeStamp", timeStamp)
                        .put("nonceStr", nonStr)
                        .put("paySign", paySign);
            } else {
                return R.error("微信支付单生成失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("小程序付款-异常", e);
            return R.error("微信支付模块故障");
        }
    }

    /**
     * 支付回调接口
     */
    @RequestMapping("/recieveMessage")
    @ApiOperation("支付回调接口")
    public void recieveMessage(HttpServletRequest request, HttpServletResponse response) throws Exception {
        log.info("******************【支付回调接口-开始】******************");
        request.setCharacterEncoding("utf-8");

        Reader reader = request.getReader();
        BufferedReader buffer = new BufferedReader(reader);
        String line = buffer.readLine();
        StringBuffer temp = new StringBuffer();
        while (line != null) {
            temp.append(line);
            line = buffer.readLine();
        }
        buffer.close();
        reader.close();

        Map<String, String> map = WXPayUtil.xmlToMap(temp.toString());
        String resultCode = map.get("result_code");
        String returnCode = map.get("return_code");
        if (returnCode.equals("SUCCESS") && resultCode.equals("SUCCESS")) {
            String outTradeNo = map.get("out_trade_no"); // 支付流水号
            UpdateWrapper wrapper = new UpdateWrapper();
            wrapper.eq("code", outTradeNo);
            wrapper.set("status", 2);
            orderService.update(wrapper);

            // 向微信平台返回响应
            response.setCharacterEncoding("utf-8");
            response.setContentType("application/xml");
            Writer writer = response.getWriter();
            BufferedWriter bufferedWriter = new BufferedWriter(writer);
            bufferedWriter.write("<xml><return_code><![CDATA[SUCCESS]]></return_code> <return_msg><![CDATA[OK]]></return_msg></xml>");

            bufferedWriter.close();
            writer.close();
            log.info("******************【支付回调接口-结束】******************");
        }
    }

    /**
     * 更新商品订单状态 (主动查询支付结果)
     */
    @Login
    @PostMapping("/updateOrderStatus")
    @ApiOperation("更新商品订单状态")
    public R updateOrderStatus(@RequestBody UpdateOrderStatusForm form, @RequestHeader HashMap header) throws Exception {
        log.info("******************【更新商品订单状态-开始】******************");
        // 校验
        ValidatorUtils.validateEntity(form);
        String token = header.get("token").toString();
        int userId = Integer.parseInt(jwtUtils.getClaimByToken(token).getSubject());
        int orderId = form.getOrderId();

        // 查询订单
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setUserId(userId);
        orderEntity.setId(orderId);
        QueryWrapper wrapper = new QueryWrapper(orderEntity);
        int count = orderService.count(wrapper);
        if (count == 0) {
            return R.error("用户与订单不匹配");
        }

        orderEntity = orderService.getOne(wrapper);
        String code = orderEntity.getCode();
        HashMap map = new HashMap();
        map.put("appid", appId);
        map.put("mch_id", mchId);
        map.put("out_trade_no", code);
        map.put("nonce_str", WXPayUtil.generateNonceStr());
        String sign = WXPayUtil.generateSignature(map, key);
        map.put("sign", sign);

        WXPay wxPay = new WXPay(myWXPayConfig);
        Map<String, String> result = wxPay.orderQuery(map);
        String resultCode = result.get("result_code");
        String returnCode = result.get("return_code");
        if (returnCode.equals("SUCCESS") && resultCode.equals("SUCCESS")) {
            String tradeState = result.get("trade_state");
            if (tradeState.equals("SUCCESS")) {
                UpdateWrapper updateWrapper = new UpdateWrapper();
                updateWrapper.eq("code", code);
                updateWrapper.set("status", 2);
                orderService.update(updateWrapper);
                log.info("******************【更新商品订单状态-结束_订单状态已修改】******************");
                return R.ok("订单状态已修改");
            } else {
                log.info("******************【更新商品订单状态-结束_订单状态未修改】******************");
                return R.ok("订单状态未修改");
            }
        } else {
            log.info("******************【更新商品订单状态-结束_微信支付单查询失败】******************");
            return R.error("微信支付单查询失败");
        }
    }


    /**
     * Native支付订单
     */
    @PostMapping("/nativePayOrder")
    @ApiOperation("native支付订单")
    @Login
    public R nativePayOrder(@RequestBody PayOrderForm form,
                              @RequestHeader HashMap header){
        log.info("******************【native支付订单-开始】******************");
        // 校验
        ValidatorUtils.validateEntity(form);
        String token = header.get("token").toString();
        Long userId = Long.parseLong(jwtUtils.getClaimByToken(token).getSubject());
        int orderId = form.getOrderId();

        // 判断用户
        UserEntity user = new UserEntity();
        user.setUserId(userId);
        QueryWrapper wrapper = new QueryWrapper(user);
        int count = userService.count(wrapper);
        if (count == 0) {
            return R.error("用户不存在");
        }
//        String openId = userService.getOne(wrapper).getOpenId();

        // 查询订单
        OrderEntity order = new OrderEntity();
        order.setUserId(userId.intValue());
        order.setId(orderId);
        order.setStatus(1);
        wrapper = new QueryWrapper(order);
        int count1 = orderService.count(wrapper);
        if (count1 == 0) {
            return R.error("订单不存在");
        }

        // 查询订单内容
        order = new OrderEntity();
        order.setId(orderId);
        wrapper = new QueryWrapper(order);
        OrderEntity queryOrder = orderService.getOne(wrapper);

        // 向微信平台发出请求，创建支付订单
        String amount = queryOrder.getAmount().multiply(new BigDecimal("100")).intValue()+"";
        log.info("支付金额:{}", amount);
//        String amount = "30";
        // 创建WXpay
        try {
            WXPay wxPay = new WXPay(myWXPayConfig); // 会帮助我们生成签名
            HashMap map = new HashMap();
            map.put("nonce_str", WXPayUtil.generateNonceStr()); // 随机字符串
            map.put("body", "订单备注");
            map.put("out_trade_no", queryOrder.getCode()); // 微信要求唯一
            map.put("total_fee", amount);
            map.put("spbill_create_ip", "127.0.0.1");
            map.put("notify_url", "http://localhost:8080/app/wx/recieveMessage");
            map.put("trade_type", "NATIVE");
            String sign = WXPayUtil.generateSignature(map, key);
            map.put("sign", sign);
            // 调用微信支付
            Map<String, String> result = wxPay.unifiedOrder(map);
            /**
             * return_code 状态码,  app_id 小程序ID,  mch_id 商品ID, nonce_str: 随机字符串
             * sign 数字签名, result_code 业务结果, trade_type 交易类型, prepay_id支付订单号
             */
            String prepayId = result.get("prepay_id");
            log.info("微信返回的prepayId: {}", prepayId);
            // 付款地址
            String codeUrl = result.get("code_url");
            if (prepayId != null) {
                // 保存支付订单ID
                queryOrder.setPrepayId(prepayId);
                UpdateWrapper updateWrapper = new UpdateWrapper();
                updateWrapper.eq("id", queryOrder.getId());
                orderService.update(queryOrder, updateWrapper);

                log.info("******************【native支付订单-结束】******************");
                return R.ok()
                        .put("codeUrl", codeUrl);
            } else {
                return R.error("微信支付单生成失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("native付款-异常", e);
            return R.error("微信支付模块故障");
        }
    }

    /**
     * Native支付二维码
     */
    @GetMapping("/qrcode")
    public void  qrcode(HttpServletRequest request, HttpServletResponse response) throws IOException {
        log.info("******************【native支付二维码-开始】******************");
        String codeUrl = request.getParameter("codeUrl");
        if (codeUrl != null && codeUrl.length() > 0) {
            //二维码
            QrConfig qrConfig = new QrConfig();
            qrConfig.setWidth(250);
            qrConfig.setHeight(250);
            qrConfig.setMargin(2);
            // 二维码要写入的流
            OutputStream out = response.getOutputStream();
            QrCodeUtil.generate(codeUrl, qrConfig, "jpg", out);
            out.close();
        }

    }
}
