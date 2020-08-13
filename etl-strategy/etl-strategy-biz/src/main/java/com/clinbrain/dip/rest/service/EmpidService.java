package com.clinbrain.dip.rest.service;

import com.clinbrain.dip.pojo.EtlEmpid;
import com.clinbrain.dip.rest.bean.SetWhere;
import com.clinbrain.dip.rest.mapper.DBETLEmpidMapper;
import com.clinbrain.dip.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmpidService extends BaseService<EtlEmpid> {
    @Autowired
    private DBETLEmpidMapper dbetlEmpidMapper;

    public Pair<Integer,Integer> save(List<SetWhere> empids) {
        if(!empids.isEmpty()) {
            // 先更新重复记录：
            List<String> update4WhereList = empids.stream().map(SetWhere::getWhere).collect(Collectors.toList());
            int updated = dbetlEmpidMapper.updateStatusBatch(update4WhereList);
            // 插入新的记录
            List<EtlEmpid> collect = empids.stream().map(s -> new EtlEmpid(s.getSet(), s.getWhere()))
                    .collect(Collectors.toList());
            int inserted = dbetlEmpidMapper.batchInsert(collect);
            return new Pair<>(updated, inserted);
        }
        return new Pair<>(0, 0);
    }

}
