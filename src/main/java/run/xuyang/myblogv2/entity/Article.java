package run.xuyang.myblogv2.entity;


import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import org.springframework.data.annotation.Transient;

import java.util.Date;

/**
 * 文章实体类
 *
 * @author XuYang
 * @date 2020/7/17 12:56
 */
@ToString
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Article extends BaseEntity {

    /**
     * <p>主键, 使用自增策略</p>
     * 文章 id
     */
    @TableId(type = IdType.AUTO)
    private Long aid;

    /**
     * 文章 标题
     */
    private String title;

    /**
     * 文章 内容
     */
    private String content;

    /**
     * html 格式的文章内容
     */
    private String htmlContent;

    /**
     * 文章 作者
     */
    private String author;

    /**
     * 文章 所属分类 id
     */
    private Long cid;

    /**
     * 文章 所属分类对象
     */
    @TableField(exist = false)
    private Category category;

}
