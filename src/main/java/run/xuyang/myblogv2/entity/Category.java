package run.xuyang.myblogv2.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

import java.util.Date;
import java.util.List;

/**
 * 分类实体
 *
 * @author XuYang
 * @date 2020/7/17 13:00
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Category extends BaseEntity{

    /**
     * <p>主键, 使用自增策略</p>
     * 分类 id
     */
    @TableId(type = IdType.AUTO)
    private Long cid;

    /**
     * 分类 名字
     */
    private String categoryName;

    /**
     * 该分类下所有的文章列表
     */
    @TableField(exist = false)
    private List<Article> articles;
}
