package com.example.grzy.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.example.grzy.common.constant.CommonConstant;
import com.example.grzy.common.exception.BusinessException;
import com.example.grzy.common.result.PageResult;
import com.example.grzy.common.result.ResultCode;
import com.example.grzy.entity.TbAlbumGroup;
import com.example.grzy.entity.TbAlbumPhoto;
import com.example.grzy.mapper.TbAlbumGroupMapper;
import com.example.grzy.mapper.TbAlbumPhotoMapper;
import com.example.grzy.service.AlbumService;
import com.example.grzy.service.OperLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * 相册服务实现类
 *
 * @author sodasmell
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlbumServiceImpl extends ServiceImpl<TbAlbumPhotoMapper, TbAlbumPhoto> implements AlbumService {

    private final TbAlbumPhotoMapper albumPhotoMapper;
    private final TbAlbumGroupMapper albumGroupMapper;
    private final OperLogService operLogService;

    @Value("${file.upload.path:./uploads}")
    private String uploadPath;

    @Override
    public List<TbAlbumPhoto> getVisiblePhotos(Long groupId) {
        LambdaQueryWrapper<TbAlbumPhoto> wrapper = new LambdaQueryWrapper<TbAlbumPhoto>()
                .eq(TbAlbumPhoto::getDeleted, CommonConstant.NOT_DELETED)
                .eq(TbAlbumPhoto::getStatus, CommonConstant.STATUS_NORMAL);
        if (groupId != null) {
            wrapper.eq(TbAlbumPhoto::getGroupId, groupId);
        }
        wrapper.orderByAsc(TbAlbumPhoto::getSort)
                .orderByDesc(TbAlbumPhoto::getShootDate);
        return albumPhotoMapper.selectList(wrapper);
    }

    @Override
    public List<TbAlbumGroup> getVisibleGroups() {
        return albumGroupMapper.selectList(
                new LambdaQueryWrapper<TbAlbumGroup>()
                        .eq(TbAlbumGroup::getDeleted, CommonConstant.NOT_DELETED)
                        .eq(TbAlbumGroup::getStatus, CommonConstant.STATUS_NORMAL)
                        .orderByAsc(TbAlbumGroup::getSort)
                        .orderByAsc(TbAlbumGroup::getId)
        );
    }

    @Override
    public List<TbAlbumGroup> getAllGroups() {
        return albumGroupMapper.selectList(
                new LambdaQueryWrapper<TbAlbumGroup>()
                        .eq(TbAlbumGroup::getDeleted, CommonConstant.NOT_DELETED)
                        .orderByAsc(TbAlbumGroup::getSort)
                        .orderByAsc(TbAlbumGroup::getId)
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createGroup(TbAlbumGroup group) {
        group.setId(null);
        group.setStatus(group.getStatus() != null ? group.getStatus() : CommonConstant.STATUS_NORMAL);
        group.setSort(group.getSort() != null ? group.getSort() : 0);
        albumGroupMapper.insert(group);
        operLogService.log("相册管理", "创建了相册分组: " + group.getName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateGroup(Long id, TbAlbumGroup group) {
        if (albumGroupMapper.selectById(id) == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        group.setId(id);
        albumGroupMapper.updateById(group);
        operLogService.log("相册管理", "更新了相册分组: " + group.getName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteGroup(Long id) {
        if (albumGroupMapper.selectById(id) == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        albumGroupMapper.deleteById(id);
        // 该分组下的照片回归"未分组"
        albumPhotoMapper.update(null, new LambdaUpdateWrapper<TbAlbumPhoto>()
                .eq(TbAlbumPhoto::getGroupId, id)
                .set(TbAlbumPhoto::getGroupId, null));
        operLogService.log("相册管理", "删除了相册分组: " + id);
    }

    @Override
    public PageResult<TbAlbumPhoto> getPhotoPage(Integer pageNum, Integer pageSize, String title, Integer status, Long groupId) {
        LambdaQueryWrapper<TbAlbumPhoto> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TbAlbumPhoto::getDeleted, CommonConstant.NOT_DELETED);
        if (StrUtil.isNotBlank(title)) {
            wrapper.like(TbAlbumPhoto::getTitle, title);
        }
        if (status != null) {
            wrapper.eq(TbAlbumPhoto::getStatus, status);
        }
        if (groupId != null) {
            wrapper.eq(TbAlbumPhoto::getGroupId, groupId);
        }
        wrapper.orderByAsc(TbAlbumPhoto::getSort).orderByDesc(TbAlbumPhoto::getCreateTime);

        IPage<TbAlbumPhoto> page = new Page<>(pageNum, pageSize);
        IPage<TbAlbumPhoto> result = albumPhotoMapper.selectPage(page, wrapper);
        return PageResult.of(result.getTotal(), result.getRecords(), pageNum, pageSize);
    }

    @Override
    public TbAlbumPhoto getById(Long id) {
        TbAlbumPhoto photo = albumPhotoMapper.selectById(id);
        if (photo == null || CommonConstant.DELETED.equals(photo.getDeleted())) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        return photo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TbAlbumPhoto createPhoto(TbAlbumPhoto photo) {
        photo.setId(null);
        photo.setStatus(photo.getStatus() != null ? photo.getStatus() : CommonConstant.STATUS_NORMAL);
        photo.setSort(photo.getSort() != null ? photo.getSort() : 0);
        // EXIF 拍摄参数：本地存储的图片直接解析，失败不影响保存
        fillExifFromLocalFile(photo);
        albumPhotoMapper.insert(photo);
        operLogService.log("相册管理", "上传了照片: " + (StrUtil.isBlank(photo.getTitle()) ? photo.getImage() : photo.getTitle()));
        return photo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePhoto(Long id, TbAlbumPhoto photo) {
        TbAlbumPhoto existing = albumPhotoMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        photo.setId(id);
        albumPhotoMapper.updateById(photo);
        operLogService.log("相册管理", "更新了照片: " + (StrUtil.isBlank(photo.getTitle()) ? id : photo.getTitle()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePhoto(Long id) {
        TbAlbumPhoto existing = albumPhotoMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        albumPhotoMapper.deleteById(id);
        operLogService.log("相册管理", "删除了照片: " + (StrUtil.isBlank(existing.getTitle()) ? existing.getImage() : existing.getTitle()));
    }

    /**
     * 从本地上传的图片解析 EXIF 拍摄参数（相机/光圈/快门/ISO/焦距）
     * 仅处理本站 /uploads/ 路径的图片，解析失败静默跳过
     */
    private void fillExifFromLocalFile(TbAlbumPhoto photo) {
        try {
            String image = photo.getImage();
            if (StrUtil.isBlank(image) || !image.startsWith("/uploads/")) {
                return;
            }
            // URL 映射本地文件：/uploads/2026/08/x.jpg → {uploadPath}/2026/08/x.jpg
            // 防止 ../ 路径穿越：规范化后校验仍位于上传目录内
            Path root = Paths.get(uploadPath).toAbsolutePath().normalize();
            Path file = root.resolve(image.substring("/uploads/".length())).normalize();
            if (!file.startsWith(root) || !Files.isRegularFile(file)) {
                return;
            }
            Metadata metadata = ImageMetadataReader.readMetadata(file.toFile());

            // 相机厂商 + 型号
            ExifIFD0Directory ifd0 = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
            if (ifd0 != null) {
                String make = ifd0.getString(ExifIFD0Directory.TAG_MAKE);
                String model = ifd0.getString(ExifIFD0Directory.TAG_MODEL);
                if (model != null) {
                    // 厂商名通常已含在型号前缀里（如 Apple iPhone / Canon EOS），避免重复
                    photo.setCamera(make != null && !model.startsWith(make.trim()) ? make.trim() + " " + model.trim() : model.trim());
                }
            }
            // 光圈/快门/ISO/焦距
            ExifSubIFDDirectory exif = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
            if (exif != null) {
                if (exif.containsTag(ExifSubIFDDirectory.TAG_FNUMBER)) {
                    Double fNumber = exif.getDoubleObject(ExifSubIFDDirectory.TAG_FNUMBER);
                    if (fNumber != null && fNumber > 0) {
                        photo.setAperture("f/" + trimDouble(fNumber));
                    }
                }
                Double exposureTime = exif.getDoubleObject(ExifSubIFDDirectory.TAG_EXPOSURE_TIME);
                if (exposureTime != null && exposureTime > 0) {
                    photo.setShutter(exposureTime >= 1
                            ? trimDouble(exposureTime) + "s"
                            : "1/" + Math.round(1 / exposureTime) + "s");
                }
                Integer iso = exif.getInteger(ExifSubIFDDirectory.TAG_ISO_EQUIVALENT);
                if (iso != null && iso > 0) {
                    photo.setIso(String.valueOf(iso));
                }
                Double focalLength = exif.getDoubleObject(ExifSubIFDDirectory.TAG_FOCAL_LENGTH);
                if (focalLength != null && focalLength > 0) {
                    photo.setFocalLength(trimDouble(focalLength) + "mm");
                }
            }
            log.info("照片 EXIF 解析成功: image={}, camera={}, aperture={}, shutter={}, iso={}, focal={}",
                    image, photo.getCamera(), photo.getAperture(), photo.getShutter(), photo.getIso(), photo.getFocalLength());
        } catch (Exception e) {
            // 无 EXIF（截图/网络图/微信压缩图等）属正常情况，静默跳过
            log.debug("照片 EXIF 解析跳过: image={}, reason={}", photo.getImage(), e.getMessage());
        }
    }

    /**
     * 去掉小数末尾多余的 0（2.8 → "2.8"，50.0 → "50"）
     */
    private String trimDouble(double value) {
        if (value == Math.floor(value)) {
            return String.valueOf((long) value);
        }
        return String.valueOf(Math.round(value * 10) / 10.0);
    }
}
