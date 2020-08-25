package run.xuyang.myblogv2.service;

import run.xuyang.myblogv2.entity.Link;

import java.util.List;

/**
 * 友情链接业务层接口
 *
 * @author XuYang
 * @date 2020/8/13 19:58
 */
public interface LinkService {

    List<Link> findAllLinks();

    int addLink(Link link);

    Link findLatestLink();

    int batchDeleteLink(List<Long> batchDelIds);

    int deleteLink(Long lid);

    int persistCache2DB(List<Link> allLinks);
}
