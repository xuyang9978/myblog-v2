package run.xuyang.myblogv2.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

/**
 * 实体类的公共属性
 *
 * @author XuYang
 * @date 2020/7/31 20:58
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createdTime;

    /**
     * 修改时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updatedTime;

    /**
     * 逻辑删除, 0 表示未删除, 1 表示已删除
     */
    @TableLogic
    private Integer deleted;
}
