package run.xuyang.myblogv2.controller.admin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import run.xuyang.myblogv2.cache.ArticleCache;
import run.xuyang.myblogv2.dto.ArticlePageSearchDTO;
import run.xuyang.myblogv2.entity.Article;
import run.xuyang.myblogv2.response.LayuiResponse;
import run.xuyang.myblogv2.response.ResultResponse;
import run.xuyang.myblogv2.service.ArticleService;
import run.xuyang.myblogv2.util.ArticleUtils;

import javax.annotation.security.RolesAllowed;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static cn.hutool.core.util.StrUtil.isBlank;
import static run.xuyang.myblogv2.util.ArticleUtils.notIsCompleteArticle;

/**
 * 博客文章 接口
 *
 * @author XuYang
 * @date 2020/7/19 13:40
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/v2/article")
public class ArticleController {

    private final ArticleService articleService;

    private final ArticleCache articleCache;

    public ArticleController(ArticleService articleService, ArticleCache articleCache) {
        this.articleService = articleService;
        this.articleCache = articleCache;
    }

    /**
     * 保存一篇文章
     *
     * @param article 一篇文章
     * @return 文章是否添加成功的信息
     */
    @PreAuthorize("hasAnyAuthority('ADMIN', 'WRITER')")  // spring 表达式注解
    @PostMapping("/saveArticle")
    public Map<String, Object> saveArticle(Article article) {
        // 必填字段不能为空
        if (notIsCompleteArticle(article)) {
            return ResultResponse.getFailResponse(500, "文章必要数据不完整!");
        }
        // 添加文章
        if (articleService.addArticle(article) != 0) {
            // 更新缓存
            articleCache.insert(articleService.getLatestArticle());
            return ResultResponse.getSuccessResponse(200, "文章添加成功!");
        }
        return ResultResponse.getSuccessResponse(500, "文章添加失败!");
    }

    /**
     * 根据 文章id 逻辑删除(关闭) 一篇文章
     *
     * @param aid 文章id
     * @return 文章是否逻辑删除成功的信息
     */
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EDITOR')")
    @PostMapping("/logicalDeleteArticle/{aid}")
    public Map<String, Object> logicalDeleteArticle(@PathVariable(name = "aid") Long aid) {
        // 关闭文章
        articleCache.logicalDeleteArticle(aid);
        return ResultResponse.getSuccessResponse(200, "文章关闭成功!");
    }

    /**
     * 根据 文章id 将 已被逻辑删除 了的文章 恢复(开启)
     *
     * @param aid 文章id
     * @return 文章是否开启成功的信息
     */
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EDITOR')")
    @PostMapping("/openArticle/{aid}")
    public Map<String, Object> openArticle(@PathVariable(name = "aid") Long aid) {
        // 开启文章
        articleCache.openArticle(aid);
        return ResultResponse.getSuccessResponse(200, "文章开启成功!");
    }

    /**
     * 更新一篇文章信息
     *
     * @param article 完整的一篇文章
     * @return 文章是否更新成功的信息
     */
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EDITOR')")
    @PostMapping("/updateArticle")
    public Map<String, Object> updateArticle(Article article) {
        // 文章必填字段不能为空
        if (notIsCompleteArticle(article)) {
            return ResultResponse.getFailResponse(500, "文章必要数据不完整!");
        }
        // 更新文章
        articleCache.updateArticle(article);
        return ResultResponse.getSuccessResponse(200, "文章修改成功!");
    }

    /**
     * 根据 文章id 删除 一篇文章
     *
     * @param aid 文章id
     * @return 文章是否删除成功的信息
     */
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DELETE')")
    @DeleteMapping("/deleteArticle/{aid}")
    public Map<String, Object> deleteArticle(@PathVariable(name = "aid") Long aid) {
        // 删除文章
        if (articleService.deleteArticle(aid) != 0) {
            // 更新缓存
            articleCache.deleteArticle(aid);
            return ResultResponse.getSuccessResponse(200, "文章删除成功!");
        }
        return ResultResponse.getSuccessResponse(500, "文章删除失败!");
    }

    /**
     * 根据 文章id列表 批量删除 文章
     *
     * @param batchDelIdsJSON 要删除的文章id列表的JSON数组
     * @return 批量删除文章是否成功的信息
     */
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DELETE')")
    @DeleteMapping("/batchDeleteArticle")
    public Map<String, Object> batchDeleteArticle(@RequestParam(name = "batchDelIdsJSON") String batchDelIdsJSON) throws JsonProcessingException {
        // 将 Json 数据转为 文章id 的集合
        ObjectMapper objectMapper = new ObjectMapper();
        CollectionType listType = objectMapper.getTypeFactory().constructCollectionType(ArrayList.class, Long.class);
        List<Long> batchDelIds = objectMapper.readValue(batchDelIdsJSON, listType);
        if (batchDelIds.size() == 0) {
            return ResultResponse.getFailResponse(500, "批量删除参数错误!");
        }
        // 批量删除文章
        if (articleService.batchDeleteArticle(batchDelIds) == batchDelIds.size()) {
            // 更新缓存
            articleCache.batchDeleteArticle(batchDelIds);
            return ResultResponse.getSuccessResponse(200, "批量删除" + batchDelIds.size() + "篇文章成功!");
        }
        return ResultResponse.getSuccessResponse(500, "批量删除" + batchDelIds.size() + "篇文章失败!");
    }

    /**
     * 根据搜索条件分页获取文章数据
     *
     * @param searchDTO 查询参数
     * @return 分页文章数据
     */
    @GetMapping("/findArticlesByPageAndCondition")
    public Map<String, Object> findArticlesByPage(ArticlePageSearchDTO searchDTO) throws JsonProcessingException {
        String author = searchDTO.getAuthor();
        String title = searchDTO.getTitle();
        Long cid = searchDTO.getCid();
        Integer deleted = searchDTO.getDeleted();
        List<Article> articleList = null;
        // 满足搜索条件的文章总数
        Long count = 0L;

        // 查询条件不全为空
        if (!(isBlank(author) && isBlank(title) && cid == null && deleted == null)) {
            articleList = articleCache.findArticlesByPageCondition(searchDTO);
            count = articleCache.countArticlesByPageConditionCount(searchDTO);
        } else {
            // 否则就只进行分页查询
            articleList = articleCache.getArticlesByPage(searchDTO.getPage(), searchDTO.getLimit());
            count = articleCache.getCount();
        }
        if (articleList == null) {
            return LayuiResponse.getFailResponse(
                    404,
                    "文章获取失败!",
                    count,
                    null);
        } else {
            // 将 html 字符进行转义,避免前端显示的时候出错
            for (Article article : articleList) {
                article.setHtmlContent(ArticleUtils.removeTag(article.getHtmlContent()));
            }
            return LayuiResponse.getSuccessResponse(
                    200,
                    "文章获取成功!",
                    count,
                    articleList);
        }
    }
}
