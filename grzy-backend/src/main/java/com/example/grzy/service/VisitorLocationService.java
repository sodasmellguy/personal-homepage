package com.example.grzy.service;

import com.example.grzy.vo.VisitorLocationVO;

import java.util.List;
import java.util.Map;

/**
 * 访客位置服务
 *
 * @author sodasmell
 */
public interface VisitorLocationService {

    /**
     * 记录访客位置
     */
    void recordVisitorLocation(String visitorId, String clientIp);

    /**
     * 获取所有访客位置列表（地图打点用，脱敏 VO，最多 500 条）
     */
    List<VisitorLocationVO> getAllLocations();

    /**
     * 获取最近访客（脱敏 VO）
     */
    List<VisitorLocationVO> getRecentLocations(int limit);

    /**
     * 按城市统计访客数量
     */
    List<Map<String, Object>> getCityStats();

    /**
     * 按国家统计访客数量
     */
    List<Map<String, Object>> getCountryStats();
}
