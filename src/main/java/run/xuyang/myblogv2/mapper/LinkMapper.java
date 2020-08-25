package run.xuyang.myblogv2.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;
import run.xuyang.myblogv2.entity.Link;

import java.util.List;

/**
 * 友情链接持久层接口
 *
 * @author XuYang
 * @date 2020/8/13 19:58
 */
@Repository
public interface LinkMapper extends BaseMapper<Link> {

    /**
     * 查询全部友链, 包括被关闭了的
     *
     * @return 全部友链
     */
    @Results(id = "linkMap", value = {
            @Result(id = true, column = "lid", property = "lid", javaType = Long.class),
            @Result(column = "link_name", property = "linkName", javaType = String.class),
            @Result(column = "link_url", property = "linkUrl", javaType = String.class),
            @Result(column = "deleted", property = "deleted", javaType = Integer.class)
    })
    @Select(" select lid, link_name, link_url, deleted " +
            " from link ")
    List<Link> findAllLinks();

    /**
     * 获取刚刚添加的链接
     *
     * @return 最新添加的链接
     */
    @ResultMap("linkMap")
    @Select(" select lid, link_name, link_url, deleted " +
            " from link " +
            " order by lid desc " +
            " limit 1")
    Link findLatestLink();

    /**
     * 批量删除 友链
     *
     * @param batchDelIds 友链id集合
     * @return 删除成功的友链数量
     */
    int batchDeleteLink(@Param(value = "batchDelIds") List<Long> batchDelIds);

    /**
     * 根据 lid 删除 友链
     *
     * @param lid 友链id
     * @return 删除成功的数目
     */
    @Delete(" delete from link where lid = #{lid} ")
    int deleteLink(@Param(value = "lid") Long lid);

    /**
     * 更新友链
     *
     * @param link 友链
     * @return 更新成功的条数
     */
    @Update(" update link " +
            " set link_name = #{link.linkName}, link_url = #{link.linkUrl}, deleted = #{link.deleted} " +
            " where lid = #{link.lid} ")
    int updateLink(@Param(value = "link") Link link);
}
