package wiki.laona.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import wiki.laona.core.dao.ad.ContentDao;
import wiki.laona.core.pojo.ad.Content;
import wiki.laona.core.pojo.ad.ContentQuery;
import wiki.laona.core.pojo.entity.PageResult;
import wiki.laona.utils.Constants;

import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * @description: 广告分类服务实现类
 * @author: laona
 * @create: 2021-03-05 14:31
 **/
@Service
@Transactional
public class ContentServiceImpl implements ContentService {

    @Autowired
    private ContentDao contentDao;
    @Autowired
    private RedisTemplate<String, Content> redisTemplate;

    private Logger logger = Logger.getLogger(ContentServiceImpl.class.getName());

    @Override
    public PageResult<Content> findPage(Content content, Integer page, Integer pageSize) {
        PageHelper.startPage(page, pageSize);
        ContentQuery query = new ContentQuery();
        ContentQuery.Criteria criteria = query.createCriteria();
        if (content != null) {
            if (content.getTitle() != null && !"".equals(content.getTitle())) {
                criteria.andTitleLike("%" + content.getTitle() + "%");
            }
        }
        Page<Content> contentList = (Page<Content>) contentDao.selectByExample(query);
        return new PageResult<>(contentList.getTotal(), contentList.getResult());
    }

    @Override
    public void add(Content content) {
        //  删除 redis 中的缓存数据，等待下次请求的时候重新加载
        redisTemplate.boundHashOps(Constants.REDIS_CONTENT_BANNER_LIST).delete(content.getCategoryId());
        // 1. 将新广告添加到数据库中
        contentDao.insertSelective(content);
    }

    @Override
    public Content findOne(Long id) {
        return contentDao.selectByPrimaryKey(id);
    }

    @Override
    public void update(Content content) {
        // 先删除旧的数据
        Content oldContent = contentDao.selectByPrimaryKey(content.getId());
        redisTemplate.boundHashOps(Constants.REDIS_CONTENT_BANNER_LIST).delete(oldContent.getCategoryId());
        // 再删除新的数据
        redisTemplate.boundHashOps(Constants.REDIS_CONTENT_BANNER_LIST).delete(content.getCategoryId());
        //将新的广告对象更新到数据库中
        contentDao.updateByPrimaryKeySelective(content);
    }

    @Override
    public void delete(Long[] ids) {
        if (Objects.nonNull(ids)) {
            for (Long id : ids) {
                //  删除 redis 中的缓存数据，等待下次请求的时候重新加载
                Content content = contentDao.selectByPrimaryKey(id);
                redisTemplate.boundHashOps(Constants.REDIS_CONTENT_BANNER_LIST).delete(content.getCategoryId());
                //3. 根据广告id删除数据库中的广告数据
                contentDao.deleteByPrimaryKey(id);
            }
        }
    }

    @Override
    public List<Content> findByCategoryId(Long categoryId) {
        ContentQuery contentQuery = new ContentQuery();
        ContentQuery.Criteria criteria = contentQuery.createCriteria();
        criteria.andCategoryIdEqualTo(categoryId);
        return contentDao.selectByExample(contentQuery);
    }

    @Override
    public List<Content> findCategoryFromRedisById(Long categoryId) {
        // 从 redis 中取出，有数据就返回，
        List<Content> contentList =
                (List<Content>) redisTemplate.boundHashOps(Constants.REDIS_CONTENT_BANNER_LIST).get(categoryId);
        // 没有数据就到数据库中查询
        if (Objects.isNull(contentList)) {
            contentList = this.findByCategoryId(categoryId);
            redisTemplate.boundHashOps(Constants.REDIS_CONTENT_BANNER_LIST).put(categoryId, contentList);
        }
        return contentList;
    }
}
