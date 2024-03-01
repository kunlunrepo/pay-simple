

package io.renren.modules.app.form;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 微信小程序登录表单
 *
 * 
 */
@Data
@ApiModel(value = "微信登录表单")
public class WxLoginForm {

    @ApiModelProperty(value = "临时登录凭证")
    @NotBlank(message="临时登录凭证不能为空")
    private String code;

    @ApiModelProperty(value = "昵称")
    @NotBlank(message="昵称不能为空")
    private String nickname;

    @ApiModelProperty(value = "头像URL")
    @NotBlank(message="头像URL不能为空")
    private String photo;

}
