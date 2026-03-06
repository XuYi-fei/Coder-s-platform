package com.github.paicoding.forum.api.model.vo.user.dto;

import lombok.Data;
import lombok.ToString;

/**
 * 用户主页信息
 *
 * @author XuYifei
 * @since 2024-07-12
 */
@Data
@ToString(callSuper = true)
public class UserStatisticInfoDTO extends BaseUserInfoDTO {

    /**
     * 关注数
     */
    private Integer followCount;

    /**
     * 粉丝数
     */
    private Integer fansCount;

    /**
     * 加入天数
     */
    private Integer joinDayCount;

    /**
     * 是否关注当前用户
     */
    private Boolean followed;

    /**
     * 身份信息完整度百分比
     */
    private Integer infoPercent;
}
