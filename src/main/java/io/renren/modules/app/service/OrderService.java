

package io.renren.modules.app.service;


import com.baomidou.mybatisplus.extension.service.IService;
import io.renren.modules.app.entity.OrderEntity;
import io.renren.modules.app.entity.UserEntity;
import io.renren.modules.app.form.LoginForm;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 订单
 *
 * 
 */
public interface OrderService extends IService<OrderEntity> {

	ArrayList<OrderEntity> searchUserOrderList(HashMap map);

}
