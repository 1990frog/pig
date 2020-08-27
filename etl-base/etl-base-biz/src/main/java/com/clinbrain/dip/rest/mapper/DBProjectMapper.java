package com.clinbrain.dip.rest.mapper;

import com.clinbrain.dip.pojo.KylinProject;
import com.clinbrain.dip.pojo.KylinProjectSchema;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * Created by Liaopan on 2017/10/11.
 */
@org.apache.ibatis.annotations.Mapper
@Repository
public interface DBProjectMapper extends Mapper<KylinProject> {
    KylinProject GetDataBaseByProName(@Param("proId") Integer proId);

    KylinProject selectProjectById(Integer id);

    List<KylinProject> selectAllProject();

    int getProIdByProName(@Param("projectName") String projectName);

    boolean newCreateKylinProjectSchema(KylinProjectSchema kylinProjectSchema);

    void deleteKylinProjectSchema(@Param("projectId") Integer projectId);


    int selectKylinProjectSchema(@Param("projectId") Integer projectId);


    List<String> getKylinProjectSchemaName(@Param("projectId") Integer projectId);


    List<KylinProjectSchema>  getKylinProjectSchema(@Param("projectId") Integer projectId);

    int selectModelCountsByProId(@Param("projectId") Integer projectId);

    int selectCubeCountsByProId(@Param("projectId") Integer projectId);

}
