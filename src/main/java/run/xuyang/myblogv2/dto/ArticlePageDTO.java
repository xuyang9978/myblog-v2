package run.xuyang.myblogv2.dto;

import lombok.Data;


/**
 * 博客前台进行分页展示文章的分页参数
 *
 * @author XuYang
 * @date 2020/8/1 21:55
 */
@Data
public class ArticlePageDTO {

    /**
     * 第几页, 默认第一页
     */
    private Integer page = 1;

    /**
     * 每页多少条数据, 默认每页 10 条
     */
    private Integer limit = 10;
}
