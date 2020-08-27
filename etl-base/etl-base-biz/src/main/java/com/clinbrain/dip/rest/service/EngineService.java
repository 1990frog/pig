package com.clinbrain.dip.rest.service;

import com.clinbrain.dip.pojo.ETLConnection;
import com.clinbrain.dip.pojo.ETLEngine;
import com.clinbrain.dip.rest.mapper.DBETLEngineMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class EngineService extends BaseService<ETLEngine> {
    @Autowired
    private DBETLEngineMapper dipEngineMapper;

    @Autowired
    private ConnectionService connectionService;

    public boolean putEngine(ETLEngine engine) {
        return dipEngineMapper.updateEngine(engine);
    }

    public boolean appendEngine(ETLEngine engine) {
        engine.setCreatedAt(new Date());
        engine.setUpdatedAt(new Date());
        return dipEngineMapper.insert(engine) > 0;
    }

    @Override
    public List<ETLEngine> selectAll() {
        List<ETLEngine> engines = super.selectAll();
        List<ETLConnection> connections = connectionService.selectAll();
        Optional.ofNullable(engines).ifPresent(engineList ->
                engineList.forEach(engine -> engine.setConnection(connections.stream()
                        .filter(conn -> conn.getEngineId() == engine.getId())
                        .findFirst().orElse(null))));
        return engines;
    }
}
