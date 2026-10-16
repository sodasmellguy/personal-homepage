package com.example.grzy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.grzy.common.result.PageResult;
import com.example.grzy.entity.TbArticle;
import com.example.grzy.vo.ArticleDetailVO;
import com.example.grzy.vo.ArticleVO;

import java.util.List;
import java.util.Map;

/**
 * 文章服务接口
 *
 * @author sodasmell
 */
public interface ArticleService extends IService<TbArticle> {

    /**
     * 分页查询文章列表（后台）
     */
    PageResult<ArticleVO> getArticlePage(Integer pageNum, Integer pageSize, String title, Integer status, String category);

    /**
     * 获取前台文章列表
     */
    PageResult<ArticleVO> getFrontArticlePage(Integer pageNum, Integer pageSize, String category, String keyword, String sortType, String tag);

    /**
     * 获取文章详情
     */
    ArticleDetailVO getArticleDetail(Long articleId);

    /**
     * 创建文章
     */
    void createArticle(TbArticle article);

    /**
     * 更新文章
     */
    void updateArticle(Long articleId, TbArticle article);

    /**
     * 删除文章
     */
    void deleteArticle(Long articleId);

    /**
     * 点赞文章
     */
    void likeArticle(Long articleId, String visitorId);

    /**
     * 取消点赞
     */
    void unlikeArticle(Long articleId, String visitorId);

    /**
     * 收藏文章
     */
    void collectArticle(Long articleId, String visitorId);

    /**
     * 取消收藏
     */
    void uncollectArticle(Long articleId, String visitorId);

    /**
     * 获取收藏列表
     */
    PageResult<ArticleVO> getCollectedArticles(String visitorId, Integer pageNum, Integer pageSize);

    /**
     * 获取归档数据（按年月分组）
     */
    List<Map<String, Object>> getArchives();

    /**
     * 文章分类统计
     */
    List<Map<String, Object>> getArticleStats();

    /**
     * 热门文章排行
     */
    List<Map<String, Object>> getTopArticles(Integer limit);

    /**
     * 递增文章阅读量
     */
    void incrementArticleView(Long articleId);

    /**
     * 获取热门标签
     */
    List<Map<String, Object>> getHotTags();
}
