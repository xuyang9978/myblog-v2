package run.xuyang.myblogv2.controller.admin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import run.xuyang.myblogv2.cache.ArticleCache;
import run.xuyang.myblogv2.cache.CategoryCache;
import run.xuyang.myblogv2.entity.Article;
import run.xuyang.myblogv2.entity.Category;
import run.xuyang.myblogv2.response.LayuiResponse;
import run.xuyang.myblogv2.response.ResultResponse;
import run.xuyang.myblogv2.service.ArticleService;
import run.xuyang.myblogv2.service.CategoryService;

import java.util.*;
import java.util.stream.Collectors;

import static run.xuyang.myblogv2.util.CategoryUtils.notIsCompleteCategory;

/**
 * 博客分类 接口
 *
 * @author XuYang
 * @date 2020/7/19 13:41
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/v2/category")
public class CategoryController {

    private final CategoryService categoryService;

    private final CategoryCache categoryCache;

    private final ArticleCache articleCache;

    private final ArticleService articleService;

    public CategoryController(CategoryService categoryService, CategoryCache categoryCache, ArticleCache articleCache, ArticleService articleService) {
        this.categoryService = categoryService;
        this.categoryCache = categoryCache;
        this.articleCache = articleCache;
        this.articleService = articleService;
    }

    /**
     * 将 所有分类 按照 分类id : 分类名 的方式进行映射并返回
     *
     * @return 分类id 对应的 分类名 集合
     */
    @GetMapping("/findAllCategoriesMap")
    public Map<Long, String> findAllCategoriesMap() {
        Map<Long, String> res = new HashMap<>();
        List<Category> allCategories = categoryCache.getAllCategories();
        if (allCategories != null) {
            for (Category category : allCategories) {
                res.put(category.getCid(), category.getCategoryName());
            }
        }
        return res;
    }

    /**
     * 分页获取 所有 分类
     *
     * @param page  第几页
     * @param limit 每页多少条数据
     * @return 分页后的分类集合
     */
    @GetMapping("/findCategoriesByPage")
    public Map<String, Object> findCategoriesByPage(@RequestParam(name = "page", defaultValue = "1") Integer page,
                                                    @RequestParam(name = "limit", defaultValue = "10") Integer limit) {
        List<Category> categoryList = categoryCache.getCategoriesByPage(page, limit);
        if (categoryList != null) {
            return LayuiResponse.getSuccessResponse(
                    200,
                    "分类获取成功!",
                    categoryCache.getCount(),
                    categoryList
            );
        } else {
            return LayuiResponse.getFailResponse(
                    500,
                    "分类获取失败!",
                    0L,
                    null
            );
        }
    }

    /**
     * 根据 分类id 逻辑删除(关闭) 一个分类, 同时会将 该分类下的所有文章页逻辑删除(关闭)
     *
     * @param cid 分类id
     * @return 关闭分类是否成功的信息
     */
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EDITOR')")
    @PostMapping("/logicalDeleteCategory/{cid}")
    public Map<String, Object> logicalDeleteCategory(@PathVariable(name = "cid") Long cid) {
        // 逻辑删除分类
        categoryCache.logicDeleteCategory(cid);
        return ResultResponse.getSuccessResponse(
                200,
                "关闭分类以及该分类下的所有文章成功");
    }

    /**
     * 根据 分类id 开启 分类及该分类下的所有文章
     *
     * @param cid 分类id
     * @return 开启分类是否成功的信息
     */
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EDITOR')")
    @PostMapping("/openCategory/{cid}")
    public Map<String, Object> openCategory(@PathVariable(name = "cid") Long cid) {
        // 开启分类
        categoryCache.openCategory(cid);
        return ResultResponse.getSuccessResponse(
                200,
                "开启分类以及该分类下的所有文章成功");
    }

    /**
     * 保存一个分类
     *
     * @param categoryName 分类名
     * @return 保存分类是否成功的信息
     */
    @PreAuthorize("hasAnyAuthority('ADMIN', 'WRITER')")
    @PostMapping("/saveCategory/{categoryName}")
    public Map<String, Object> saveCategory(@PathVariable(name = "categoryName") String categoryName) {
        Category category = new Category();
        category.setCategoryName(categoryName);
        if (categoryService.addCategory(category) != 0) {
            categoryCache.insert(categoryService.findCategoryByName(categoryName));
            return ResultResponse.getSuccessResponse(200, "分类添加成功!");
        } else {
            return ResultResponse.getSuccessResponse(500, "分类添加失败!");
        }
    }

    /**
     * 编辑一个分类
     *
     * @param category 分类对象
     * @return 编辑分类是否成功的信息
     */
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EDITOR')")
    @PostMapping("/editCategory")
    public Map<String, Object> editCategory(Category category) {
        if (notIsCompleteCategory(category)) {
            return ResultResponse.getFailResponse(500, "修改分类必要参数不完整!");
        }
        // 更新分类
        categoryCache.updateCategory(category);
        return ResultResponse.getSuccessResponse(200, "分类修改成功!");
    }

    /**
     * 根据 分类id 删除一个 分类
     *
     * @param cid 分类id
     * @return 删除分类是否成功的信息
     */
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DELETE')")
    @DeleteMapping("/deleteCategory/{cid}")
    public Map<String, Object> deleteCategory(@PathVariable(name = "cid") Long cid) {
        List<Article> articles = articleCache.getArticlesByCid(cid);
        // 如果该分类下有文章
        if (articles.size() != 0) {
            // 获取该分类下的文章 id 列表
            List<Long> articleIds = articles
                    .stream()
                    .map(Article::getAid)
                    .collect(Collectors.toList());
            int deletedANum = articleService.batchDeleteArticle(articleIds);
            if (deletedANum == 0) {
                return ResultResponse.getFailResponse(500, "删除分类下的文章失败！");
            }
            // 更新文章缓存
            articleCache.batchDeleteArticle(articleIds);
        }
        // 删除该分类
        int deletedCNum = categoryService.deleteCategory(cid);
        if (deletedCNum != 0) {
            // 更新分类缓存
            categoryCache.deleteCategory(cid);
            return ResultResponse.getSuccessResponse(
                    200,
                    "删除分类以及其包括的" + articles.size() + "篇文章成功");
        } else {
            return ResultResponse.getFailResponse(500, "删除分类失败！");
        }
    }

    /**
     * 批量删除 分类
     *
     * @param batchDelIdsJSON 要删除的分类id列表的JSON数组
     * @return 批量删除分类是否成功的信息
     */
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DELETE')")
    @DeleteMapping("/batchDeleteCategory")
    public Map<String, Object> batchDeleteCategory(@RequestParam(name = "batchDelIdsJSON") String batchDelIdsJSON) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        CollectionType listType = objectMapper.getTypeFactory().constructCollectionType(ArrayList.class, Long.class);
        List<Long> batchDelIds = objectMapper.readValue(batchDelIdsJSON, listType);
        if (batchDelIds.size() == 0) {
            return ResultResponse.getFailResponse(500, "批量删除参数错误!");
        }

        List<Article> articles;
        List<Long> articleIds;
        int batchDelANum, totalANum = 0;
        int batchDelCNum, totalCNum = batchDelIds.size();
        for (Long cid : batchDelIds) {
            batchDelANum = 0;
            articles = articleCache.getArticlesByCid(cid);
            // 如果该分类下有文章
            if (articles.size() != 0) {
                articleIds = articles
                        .stream()
                        .map(Article::getAid)
                        .collect(Collectors.toList());
                totalANum += articleIds.size();
                // 删除该分类下的所有文章
                for (Long articleId : articleIds) {
                    batchDelANum += articleService.deleteArticle(articleId);
                }
                if (batchDelANum == articleIds.size()) {
                    // 更新文章缓存
                    articleCache.batchDeleteArticle(articleIds);
                } else {
                    return ResultResponse.getFailResponse(500, "批量删除文章失败!");
                }
            }
            // 删除该分类
            batchDelCNum = categoryService.deleteCategory(cid);
            if (batchDelCNum != 0) {
                // 更新分类缓存
                categoryCache.deleteCategory(cid);
            } else {
                return ResultResponse.getFailResponse(500, "删除分类失败!");
            }
        }
        return ResultResponse.getSuccessResponse(200, "批量删除" + totalCNum + "个分类下总计" + totalANum + "篇文章成功!");
    }
}
