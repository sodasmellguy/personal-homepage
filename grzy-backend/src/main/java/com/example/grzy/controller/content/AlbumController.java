package com.example.grzy.controller.content;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.example.grzy.common.result.PageResult;
import com.example.grzy.common.result.Result;
import com.example.grzy.entity.TbAlbumGroup;
import com.example.grzy.entity.TbAlbumPhoto;
import com.example.grzy.service.AlbumService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 相册管理控制器（后台，需要认证）
 *
 * @author sodasmell
 */
@Tag(name = "相册管理", description = "相册照片与分组管理接口")
@RestController
@RequestMapping("/api/album")
@RequiredArgsConstructor
@SaCheckPermission("content:album:view")
public class AlbumController {

    private final AlbumService albumService;

    /**
     * 分页查询照片列表
     */
    @Operation(summary = "照片列表", description = "分页查询照片列表（后台）")
    @GetMapping("/list")
    public Result<PageResult<TbAlbumPhoto>> list(
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "12") Integer pageSize,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "groupId", required = false) Long groupId) {
        return Result.success(albumService.getPhotoPage(pageNum, pageSize, title, status, groupId));
    }

    /**
     * 获取照片详情
     */
    @Operation(summary = "照片详情", description = "根据ID获取照片详情")
    @GetMapping("/{id}")
    public Result<TbAlbumPhoto> get(@PathVariable Long id) {
        return Result.success(albumService.getById(id));
    }

    /**
     * 创建照片（自动解析 EXIF 拍摄参数）
     */
    @Operation(summary = "上传照片", description = "创建新照片记录，返回保存后的实体（含EXIF解析结果）")
    @PostMapping
    public Result<TbAlbumPhoto> create(@RequestBody TbAlbumPhoto photo) {
        return Result.success(albumService.createPhoto(photo));
    }

    /**
     * 更新照片
     */
    @Operation(summary = "更新照片", description = "根据ID更新照片信息")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody TbAlbumPhoto photo) {
        albumService.updatePhoto(id, photo);
        return Result.success();
    }

    /**
     * 删除照片
     */
    @Operation(summary = "删除照片", description = "根据ID删除照片")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        albumService.deletePhoto(id);
        return Result.success();
    }

    /**
     * 批量删除照片
     */
    @Operation(summary = "批量删除照片", description = "根据ID列表批量删除照片")
    @DeleteMapping("/batch")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        ids.forEach(albumService::deletePhoto);
        return Result.success();
    }

    // ==================== 分组管理 ====================

    /**
     * 获取全部分组（后台）
     */
    @Operation(summary = "分组列表", description = "获取全部相册分组（后台，含隐藏）")
    @GetMapping("/group/list")
    public Result<List<TbAlbumGroup>> groupList() {
        return Result.success(albumService.getAllGroups());
    }

    /**
     * 创建分组
     */
    @Operation(summary = "创建分组", description = "创建相册分组")
    @PostMapping("/group")
    public Result<Void> createGroup(@RequestBody TbAlbumGroup group) {
        albumService.createGroup(group);
        return Result.success();
    }

    /**
     * 更新分组
     */
    @Operation(summary = "更新分组", description = "根据ID更新相册分组")
    @PutMapping("/group/{id}")
    public Result<Void> updateGroup(@PathVariable Long id, @RequestBody TbAlbumGroup group) {
        albumService.updateGroup(id, group);
        return Result.success();
    }

    /**
     * 删除分组
     */
    @Operation(summary = "删除分组", description = "删除分组，分组下照片回归未分组")
    @DeleteMapping("/group/{id}")
    public Result<Void> deleteGroup(@PathVariable Long id) {
        albumService.deleteGroup(id);
        return Result.success();
    }
}
