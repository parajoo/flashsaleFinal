package cn.wolfcode.domain;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * 积分变更日志表
 */
@Setter@Getter
public class AccountLog implements Serializable {
    public static final int TYPE_DECR = 0;
    public static final int TYPE_INCR = 1;
    private String pkValue;//业务主键
    private int type;//积分变更类型. 0是减少 1是增加
    private Long amount;//此次变化金额
    private Date gmtTime;//日志插入时间
    private String info;//备注信息
}
