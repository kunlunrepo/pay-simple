package io.renren.modules.app.controller;


import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.renren.common.utils.R;
import io.renren.common.validator.ValidatorUtils;
import io.renren.modules.app.entity.UserEntity;
import io.renren.modules.app.form.WxLoginForm;
import io.renren.modules.app.service.UserService;
import io.renren.modules.app.utils.JwtUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * 登录
     */
    @PostMapping("login")
    @ApiOperation("登录")
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
        return R.ok(openid);
    }

}
