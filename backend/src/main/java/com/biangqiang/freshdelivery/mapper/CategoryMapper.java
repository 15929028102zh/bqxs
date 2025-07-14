package com.biangqiang.freshdelivery.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.biangqiang.freshdelivery.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 商品分类Mapper接口
 */
@Mapper
public interface CategoryMapper extends BaseMapper<Category> {

    /**
     * 根据父分类ID查询子分类列表
     * @param parentId 父分类ID
     * @return 子分类列表
     */
    @Select("SELECT * FROM `tb_category` WHERE parent_id = #{parentId} AND deleted = 0 ORDER BY sort_order ASC")
    List<Category> selectByParentId(Long parentId);

    /**
     * 查询所有分类（树形结构用）
     * @return 所有分类列表
     */
    @Select("SELECT * FROM `tb_category` WHERE deleted = 0 ORDER BY parent_id ASC, sort_order ASC")
    List<Category> selectAllForTree();
}