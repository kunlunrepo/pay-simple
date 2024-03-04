package io.renren.modules.app.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * description : 订单
 *
 * @author kunlunrepo
 * date :  2024-03-01 14:15
 */
@Data
@TableName("tb_order")
public class OrderEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId
    private Integer id;

    private String code;

    private Integer userId;

    private BigDecimal amount;

    private Integer paymentType;

    private Integer status;

    private Date createTime;

    private String prepayId;

}
