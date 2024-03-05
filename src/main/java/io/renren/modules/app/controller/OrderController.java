package io.renren.modules.app.controller;


import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.renren.common.utils.R;
import io.renren.common.validator.ValidatorUtils;
import io.renren.modules.app.annotation.Login;
import io.renren.modules.app.entity.OrderEntity;
import io.renren.modules.app.entity.UserEntity;
import io.renren.modules.app.form.SearchOrderForm;
import io.renren.modules.app.form.UserOrderForm;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * 订单相关接口
 */
@RestController
@RequestMapping("/app/order")
@Api("订单相关接口")
@Slf4j
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * 查询用户订单列表
     */
    @PostMapping("searchUserOrderList")
    @ApiOperation("查询用户订单列表")
    @Login
    public R searchUserOrderList(@RequestBody UserOrderForm form,
                                 @RequestHeader HashMap header){
        log.info("******************【查询用户订单列表-开始】******************");
        // 校验
        ValidatorUtils.validateEntity(form);
        String token = header.get("token").toString();
        Integer userId = Integer.parseInt(jwtUtils.getClaimByToken(token).getSubject());
        int page = form.getPage();
        int length = form.getLength();
        int start = (page - 1) * length; // 分页起始记录

        HashMap map = new HashMap();
        map.put("userId", userId);
        map.put("start", start);
        map.put("length", length);
        ArrayList<OrderEntity> list = orderService.searchUserOrderList(map);

        log.info("******************【查询用户订单列表-结束】******************");
        return R.ok().put("list", list);
    }

    /**
     * 根据订单编号查询订单详情
     */
    @PostMapping("searchOrderById")
    @ApiOperation("根据订单编号查询订单详情")
    @Login
    public R searchOrderById(@RequestBody SearchOrderForm form,
                                @RequestHeader HashMap header){
        log.info("******************【根据订单编号查询订单详情-开始】******************");
        // 校验
        ValidatorUtils.validateEntity(form);
        String token = header.get("token").toString();
        Integer userId = Integer.parseInt(jwtUtils.getClaimByToken(token).getSubject());

        QueryWrapper<OrderEntity> wrapper = new QueryWrapper<OrderEntity>();
        wrapper.eq("id", form.getOrderId());
        OrderEntity order = orderService.getOne(wrapper);
        return R.ok().put("order", order);
    }

}
