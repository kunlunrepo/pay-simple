package io.renren.modules.app.form;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * description : 订单付款的表单
 *
 * @author kunlunrepo
 * date :  2024-03-01 14:29
 */
@Data
@ApiModel(value = "订单付款的表单")
public class PayOrderForm {

    @ApiModelProperty(value = "订单编号")
    @Min(1)
    private Integer orderId;

}
