package run.xuyang.myblogv2.dto;

import lombok.Data;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;

/**
 * 博客后台进行文章的动态搜索+分页展示条件
 *
 * @author XuYang
 * @date 2020/7/26 9:52
 */
@Data
public class ArticlePageSearchDTO {

    /**
     * 第几页， 默认为 1
     */
    private Integer page = 1;

    /**
     * 每页多少条数据， 默认为 10
     */
    private Integer limit = 10;

    /**
     * 作者
     */
    private String author;

    /**
     * 文章标题
     */
    private String title;

    /**
     * 文章所属分类 id
     */
    private Long cid;

    /**
     * 文章是否被逻辑删除
     */
    private Integer deleted;
}
