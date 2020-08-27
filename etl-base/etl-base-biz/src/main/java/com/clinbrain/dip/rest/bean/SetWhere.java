package com.clinbrain.dip.rest.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by Liaopan on 2020/5/14 0014.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SetWhere {
    private String set;
    private String where;
}
