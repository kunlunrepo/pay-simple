

package io.renren.modules.app.form;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * 登录表单
 *
 * 
 */
@Data
@ApiModel(value = "登录表单")
public class LoginForm {

    @ApiModelProperty(value = "用户名")
    @NotBlank(message="用户名不能为空")
    @Pattern(regexp = "^[A-Za-z0-9]{3,20}$", message = "用户名格式错误")
    private String username;

    @ApiModelProperty(value = "密码")
    @NotBlank(message="密码不能为空")
    @Pattern(regexp = "^[A-Za-z0-9]{6,20}$", message = "密码格式错误")
    private String password;

}
