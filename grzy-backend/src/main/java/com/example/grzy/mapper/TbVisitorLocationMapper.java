package com.example.grzy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.grzy.entity.TbVisitorLocation;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface TbVisitorLocationMapper extends BaseMapper<TbVisitorLocation> {

    /**
     * 按城市聚合访客数量（排除空值/未知），按数量倒序
     */
    List<Map<String, Object>> selectCityStats();

    /**
     * 按国家聚合访客数量（排除空值/未知），按数量倒序
     */
    List<Map<String, Object>> selectCountryStats();
}
