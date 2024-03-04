

package io.renren.modules.app.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.renren.modules.app.entity.OrderEntity;
import io.renren.modules.app.entity.UserEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 订单
 *
 * 
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {


    ArrayList<OrderEntity> searchUserOrderList(HashMap map);

}
