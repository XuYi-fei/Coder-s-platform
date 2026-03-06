package com.github.paicoding.forum.web.controller.user.vo;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.paicoding.forum.api.model.vo.user.dto.FollowUserInfoDTO;
import com.github.paicoding.forum.api.model.vo.user.dto.UserStatisticInfoDTO;
import lombok.Data;

/**
 * @author XuYifei
 */
@Data
public class UserHomeInfoVo {
    private IPage<FollowUserInfoDTO> followList;
    private IPage<FollowUserInfoDTO> fansList;
    private UserStatisticInfoDTO userHome;
}
