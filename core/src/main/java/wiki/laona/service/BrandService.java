package wiki.laona.service;

import wiki.laona.core.pojo.entity.PageResult;
import wiki.laona.core.pojo.good.Brand;

import java.util.List;
import java.util.Map;

/**
 * @description: 品牌服务
 * @author: laona
 * @create: 2021-02-03 16:34
 **/
public interface BrandService {

    /**
     * 获取所有品牌
     *
     * @return 品牌信息列表
     */
    List<Brand> findAllBrands();

    /**
     * 分页查找品牌数据
     *
     * @param page     当前页码
     * @param pageSize 每页数据
     */
    PageResult<Brand> findPageBrands(Integer page, Integer pageSize, Brand brand);

    /**
     * 添加品牌
     *
     * @param brand 品牌
     */
    void addBrand(Brand brand);

    /**
     * 通过 id 获取品牌信息
     *
     * @param id 品牌 id
     * @return 品牌信息
     */
    Brand getBrandById(Long id);

    /**
     * 更新品牌信息
     *
     * @param brand 品牌信息
     */
    void updateBrand(Brand brand);

    /**
     * 根据id批量删除品牌信息
     *
     * @param ids id集合
     */
    void deleteBrandByIds(Long[] ids);

    /**
     * 查询所有品牌信息 {"id": 1, "name": "apple"}
     *
     * @return [{"id": 1, "name": "apple"}]
     */
    List<Map> findAllBrandsMap();

}
