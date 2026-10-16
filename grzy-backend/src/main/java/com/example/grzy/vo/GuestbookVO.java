package com.example.grzy.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 留言 VO — 过滤 ip 等敏感字段
 *
 * @author sodasmell
 */
@Data
@Schema(description = "留言展示对象")
public class GuestbookVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "留言ID")
    private Long id;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "留言内容")
    private String content;

    @Schema(description = "卡片颜色")
    private String color;

    @Schema(description = "点赞数")
    private Integer likeCount;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
