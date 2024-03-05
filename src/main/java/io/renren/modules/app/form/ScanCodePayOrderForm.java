package io.renren.modules.app.form;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * description : 收款码付款的表单
 *
 * @author kunlunrepo
 * date :  2024-03-01 14:29
 */
@Data
@ApiModel(value = "收款码付款的表单")
public class ScanCodePayOrderForm {

    @ApiModelProperty(value = "付款码")
    @NotBlank
    @Pattern(regexp = "^1[0-5][0-9]{16}$", message = "付款码格式不正确")
    private String authCode;

    @ApiModelProperty(value = "订单编号")
    @Min(1)
    private Integer orderId;

}
