package io.renren.modules.app.form;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;

/**
 * description : 查询订单付款状态的表单
 *
 * @author kunlunrepo
 * date :  2024-03-01 14:29
 */
@Data
@ApiModel(value = "查询订单付款状态的表单")
public class SearchOrderStatusForm {

    @ApiModelProperty(value = "订单编号")
    @Min(1)
    private Integer orderId;

}
