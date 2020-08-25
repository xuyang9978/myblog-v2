package run.xuyang.myblogv2.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import run.xuyang.myblogv2.entity.Category;
import run.xuyang.myblogv2.entity.Link;
import run.xuyang.myblogv2.mapper.LinkMapper;
import run.xuyang.myblogv2.service.LinkService;

import java.util.List;

/**
 * 友情连接业务层接口实现类
 *
 * @author XuYang
 * @date 2020/8/13 19:59
 */
@Service("linkService")
@Transactional
public class LinkServiceImpl implements LinkService {

    private final LinkMapper linkMapper;

    public LinkServiceImpl(LinkMapper linkMapper) {
        this.linkMapper = linkMapper;
    }

    @Override
    public List<Link> findAllLinks() {
        return linkMapper.findAllLinks();
    }

    @Override
    public int addLink(Link link) {
        return linkMapper.insert(link);
    }

    @Override
    public Link findLatestLink() {
        return linkMapper.findLatestLink();
    }

    @Override
    public int batchDeleteLink(List<Long> batchDelIds) {
        return linkMapper.batchDeleteLink(batchDelIds);
    }

    @Override
    public int deleteLink(Long lid) {
        return linkMapper.deleteLink(lid);
    }

    @Override
    public int persistCache2DB(List<Link> allLinks) {
        for (Link link : allLinks) {
            if (linkMapper.updateLink(link) == 0) {
                return 0;
            }
        }
        return allLinks.size();
    }
}
