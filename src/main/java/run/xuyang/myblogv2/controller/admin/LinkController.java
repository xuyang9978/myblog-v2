package run.xuyang.myblogv2.controller.admin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import run.xuyang.myblogv2.cache.LinkCache;
import run.xuyang.myblogv2.entity.Category;
import run.xuyang.myblogv2.entity.Link;
import run.xuyang.myblogv2.response.LayuiResponse;
import run.xuyang.myblogv2.response.ResultResponse;
import run.xuyang.myblogv2.service.LinkService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static cn.hutool.core.util.StrUtil.isBlank;
import static run.xuyang.myblogv2.util.CategoryUtils.notIsCompleteCategory;
import static run.xuyang.myblogv2.util.LinkUtils.notIsCompleteLink;

/**
 * 友情链接接口
 *
 * @author XuYang
 * @date 2020/8/13 20:00
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/v2/link")
public class LinkController {

    private final LinkCache linkCache;

    private final LinkService linkService;

    public LinkController(LinkCache linkCache, LinkService linkService) {
        this.linkCache = linkCache;
        this.linkService = linkService;
    }

    /**
     * 分页获取 所有 友链
     *
     * @param page  第几页
     * @param limit 每页多少条数据
     * @return 分页后的友链集合
     */
    @GetMapping("/findLinksByPage")
    public Map<String, Object> findLinksByPage(@RequestParam(name = "page", defaultValue = "1") Integer page,
                                               @RequestParam(name = "limit", defaultValue = "10") Integer limit) {
        List<Link> links = linkCache.getLinksByPage(page, limit);
        if (links != null) {
            return LayuiResponse.getSuccessResponse(
                    200,
                    "友链获取成功!",
                    linkCache.getCount(),
                    links
            );
        } else {
            return LayuiResponse.getFailResponse(
                    500,
                    "友链获取失败!",
                    0L,
                    null
            );
        }
    }

    /**
     * 保存一个友链
     *
     * @param linkName 友链名字
     * @param linkUrl  友链地址
     * @return 友链是否保存成功的信息
     */
    @PreAuthorize("hasAnyAuthority('ADMIN', 'WRITER')")
    @PostMapping("/saveLink")
    public Map<String, Object> saveLink(
            @RequestParam(name = "linkName") String linkName,
            @RequestParam(name = "linkUrl") String linkUrl) {
        if (isBlank(linkName) || isBlank(linkUrl)) {
            return ResultResponse.getFailResponse(500, "参数不完整!");
        }
        Link link = new Link(null, linkName, linkUrl, 0);
        if (linkService.addLink(link) != 0) {
            linkCache.insert(linkService.findLatestLink());
            return ResultResponse.getSuccessResponse(200, "友链添加成功!");
        } else {
            return ResultResponse.getSuccessResponse(500, "友链添加失败!");
        }
    }
    /**
     * 根据 文章id列表 批量删除 文章
     *
     * @param batchDelIdsJSON 要删除的文章id列表的JSON数组
     * @return 批量删除文章是否成功的信息
     */
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DELETE')")
    @DeleteMapping("/batchDeleteLink")
    public Map<String, Object> batchDeleteLink(@RequestParam(name = "batchDelIdsJSON") String batchDelIdsJSON) throws JsonProcessingException {
        // 将 Json 数据转为 lid 的集合
        ObjectMapper objectMapper = new ObjectMapper();
        CollectionType listType = objectMapper.getTypeFactory().constructCollectionType(ArrayList.class, Long.class);
        List<Long> batchDelIds = objectMapper.readValue(batchDelIdsJSON, listType);
        if (batchDelIds.size() == 0) {
            return ResultResponse.getFailResponse(500, "批量删除参数错误!");
        }
        // 批量删除友链
        if (linkService.batchDeleteLink(batchDelIds) == batchDelIds.size()) {
            // 更新缓存
            linkCache.batchDeleteLink(batchDelIds);
            return ResultResponse.getSuccessResponse(200, "批量删除" + batchDelIds.size() + "个友链成功!");
        }
        return ResultResponse.getSuccessResponse(500, "批量删除" + batchDelIds.size() + "个友链失败!");
    }

    /**
     * 根据 友链id 删除一个 友链
     *
     * @param lid 友链id
     * @return 友链是否删除成功的信息
     */
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DELETE')")
    @DeleteMapping("/deleteLink/{lid}")
    public Map<String, Object> deleteLink(@PathVariable(name = "lid") Long lid) {
        // 删除友链
        if (linkService.deleteLink(lid) != 0) {
            // 更新缓存
            linkCache.deleteLink(lid);
            return ResultResponse.getSuccessResponse(200, "友链删除成功!");
        }
        return ResultResponse.getSuccessResponse(500, "友链删除失败!");
    }

    /**
     * 编辑一个友链
     *
     * @param link 友链对象
     * @return 编辑友链是否成功的信息
     */
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EDITOR')")
    @PostMapping("/editLink")
    public Map<String, Object> editLink(Link link) {
        if (notIsCompleteLink(link)) {
            return ResultResponse.getFailResponse(500, "修改友链必要参数不完整!");
        }
        // 更新分类
        linkCache.updateLink(link);
        return ResultResponse.getSuccessResponse(200, "友链修改成功!");
    }

    /**
     * 根据 友链id 逻辑删除(关闭) 一个友链
     *
     * @param lid 友链id
     * @return 友链是否逻辑删除成功的信息
     */
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EDITOR')")
    @PostMapping("/logicalDeleteLink/{lid}")
    public Map<String, Object> logicalDeleteLink(@PathVariable(name = "lid") Long lid) {
        // 关闭友链
        linkCache.logicalDeleteLink(lid);
        return ResultResponse.getSuccessResponse(200, "友链关闭成功!");
    }

    /**
     * 根据 友链id 开启 一个友链
     *
     * @param lid 友链id
     * @return 友链是否开启成功的信息
     */
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EDITOR')")
    @PostMapping("/openLink/{lid}")
    public Map<String, Object> openLink(@PathVariable(name = "lid") Long lid) {
        // 开启友链
        linkCache.openLink(lid);
        return ResultResponse.getSuccessResponse(200, "友链开启成功!");
    }

}
