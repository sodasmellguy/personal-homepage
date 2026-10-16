package com.example.grzy.controller.content;

import com.example.grzy.common.constant.CommonConstant;
import com.example.grzy.entity.TbArticle;
import com.example.grzy.mapper.TbArticleMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * RSS 订阅控制器
 *
 * @author sodasmell
 */
@Tag(name = "RSS订阅", description = "RSS 2.0 订阅源")
@RestController
@RequiredArgsConstructor
public class RssController {

    @Value("${app.site-url:http://localhost:8080}")
    private String siteUrl;

    private final TbArticleMapper articleMapper;

    /**
     * 获取 RSS 订阅源
     */
    @Operation(summary = "RSS 订阅源", description = "生成 RSS 2.0 XML 格式的订阅源")
    @GetMapping(value = "/rss.xml", produces = MediaType.APPLICATION_XML_VALUE)
    public String rss() {
        List<TbArticle> articles = articleMapper.selectList(
                new LambdaQueryWrapper<TbArticle>()
                        .eq(TbArticle::getStatus, CommonConstant.STATUS_NORMAL)
                        .eq(TbArticle::getDeleted, CommonConstant.NOT_DELETED)
                        .orderByDesc(TbArticle::getCreateTime)
                        .last("LIMIT 20")
        );

        DateTimeFormatter rfc822 = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss Z",
                java.util.Locale.ENGLISH);

        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<rss version=\"2.0\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n");
        sb.append("  <channel>\n");
        sb.append("    <title>赵先生 - 技术博客</title>\n");
        sb.append("    <link>").append(siteUrl).append("/article</link>\n");
        sb.append("    <description>分享技术心得，记录成长历程</description>\n");
        sb.append("    <language>zh-CN</language>\n");
        sb.append("    <atom:link href=\"").append(siteUrl).append("/rss.xml\" rel=\"self\" type=\"application/rss+xml\"/>\n");

        for (TbArticle a : articles) {
            String pubDate = a.getCreateTime() != null
                    ? rfc822.format(a.getCreateTime().atZone(ZoneId.systemDefault()))
                    : rfc822.format(java.time.ZonedDateTime.now());
            sb.append("    <item>\n");
            sb.append("      <title><![CDATA[").append(a.getTitle()).append("]]></title>\n");
            sb.append("      <link>").append(siteUrl).append("/article/").append(a.getId()).append("</link>\n");
            sb.append("      <guid>").append(siteUrl).append("/article/").append(a.getId()).append("</guid>\n");
            sb.append("      <pubDate>").append(pubDate).append("</pubDate>\n");
            if (a.getSummary() != null) {
                sb.append("      <description><![CDATA[").append(a.getSummary()).append("]]></description>\n");
            }
            if (a.getCategory() != null) {
                sb.append("      <category>").append(a.getCategory()).append("</category>\n");
            }
            sb.append("    </item>\n");
        }

        sb.append("  </channel>\n");
        sb.append("</rss>");
        return sb.toString();
    }
}
