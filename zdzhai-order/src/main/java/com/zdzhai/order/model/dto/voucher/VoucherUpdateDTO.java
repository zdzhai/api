package com.zdzhai.order.model.dto.voucher;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 
 * @TableName voucher
 */
@TableName(value ="voucher")
@Data
public class VoucherUpdateDTO implements Serializable {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 接口id
     */
    private Long interfaceId;

    /**
     * 代金券标题
     */
    private String title;

    /**
     * 副标题
     */
    private String subTitle;

    /**
     * 使用规则
     */
    private String rules;

    /**
     * 支付金额，单位是分。例如200代表2元
     */
    private String payValue;

    /**
     * 抵扣金额，单位是分。例如200代表2元
     */
    private String actualValue;

    /**
     * 0,普通券；1,秒杀券
     */
    private Integer type;

    /**
     * 库存
     */
    private Integer stock;

    /**
     * 生效时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime beginTime;

    /**
     * 失效时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime endTime;

    /**
     * 1,上架; 2,下架; 3,过期
     */
    private Integer status;


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        VoucherUpdateDTO other = (VoucherUpdateDTO) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getInterfaceId() == null ? other.getInterfaceId() == null : this.getInterfaceId().equals(other.getInterfaceId()))
            && (this.getTitle() == null ? other.getTitle() == null : this.getTitle().equals(other.getTitle()))
            && (this.getSubTitle() == null ? other.getSubTitle() == null : this.getSubTitle().equals(other.getSubTitle()))
            && (this.getRules() == null ? other.getRules() == null : this.getRules().equals(other.getRules()))
            && (this.getPayValue() == null ? other.getPayValue() == null : this.getPayValue().equals(other.getPayValue()))
            && (this.getActualValue() == null ? other.getActualValue() == null : this.getActualValue().equals(other.getActualValue()))
            && (this.getType() == null ? other.getType() == null : this.getType().equals(other.getType()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getInterfaceId() == null) ? 0 : getInterfaceId().hashCode());
        result = prime * result + ((getTitle() == null) ? 0 : getTitle().hashCode());
        result = prime * result + ((getSubTitle() == null) ? 0 : getSubTitle().hashCode());
        result = prime * result + ((getRules() == null) ? 0 : getRules().hashCode());
        result = prime * result + ((getPayValue() == null) ? 0 : getPayValue().hashCode());
        result = prime * result + ((getActualValue() == null) ? 0 : getActualValue().hashCode());
        result = prime * result + ((getType() == null) ? 0 : getType().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", interfaceId=").append(interfaceId);
        sb.append(", title=").append(title);
        sb.append(", subTitle=").append(subTitle);
        sb.append(", rules=").append(rules);
        sb.append(", payValue=").append(payValue);
        sb.append(", actualValue=").append(actualValue);
        sb.append(", type=").append(type);
        sb.append(", status=").append(status);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}