package com.clinbrain.dip.rest.mapper;

import com.clinbrain.dip.pojo.KylinCube;
import com.clinbrain.dip.pojo.KylinCubeAggGroup;
import com.clinbrain.dip.pojo.KylinCubeDictionary;
import com.clinbrain.dip.pojo.KylinCubeProperty;
import com.clinbrain.dip.pojo.KylinDimension;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * Created by Liaopan on 2017/10/11.
 */
@org.apache.ibatis.annotations.Mapper
@Repository("CubesMapper")
public interface DBCubeMapper extends Mapper<KylinCube> {

    /**
     * @param searchText
     * @param orderBy    这里在@Param("orderBy")里面使用“orderBy”这个参数名，会在sql后面自动加入一个order by ? 需要注意
     * @param offset
     * @param limit
     * @return
     */
    List<KylinCube> selectAllCubes(@Param("projectId") Integer projectId,
                                   @Param("searchText") String searchText,
                                   @Param("order") String orderBy,
                                   @Param("offset") Integer offset,
                                   @Param("limit") Integer limit);

    long selectCustomCount(@Param("projectId") Integer projectId, @Param("searchText") String searchText);

    KylinCube selectOneByPrimaryKey(@Param("cubeId") Integer cubeId);

    List<KylinCube> selectCubes();

    default List<KylinCubeAggGroup> selectCubeAggGroups() {
        return null;
    }

    List<KylinCubeDictionary> selectCubeDictionaries();
    List<KylinCubeProperty> selectCubeProperties();
    List<KylinDimension> selectCubeDimensions();
}
