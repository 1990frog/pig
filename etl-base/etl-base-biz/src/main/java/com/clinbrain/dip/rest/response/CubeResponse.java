package com.clinbrain.dip.rest.response;

import com.clinbrain.dip.kylinmetadata.CubeDesc2;
import com.clinbrain.dip.pojo.KylinModel;
import com.clinbrain.dip.pojo.KylinProject;
import lombok.Data;

/**
 * Created by Liaopan on 2017/12/20.
 */
@Data
public class CubeResponse {

    private CubeDesc2 cubeDesc;

    private KylinModel model;

    private KylinProject project;
}
