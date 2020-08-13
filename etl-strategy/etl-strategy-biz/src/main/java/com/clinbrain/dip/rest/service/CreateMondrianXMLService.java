package com.clinbrain.dip.rest.service;

import com.clinbrain.dip.cube.MondrianCubeSchema;
import com.clinbrain.dip.rest.mapper.DBModelsMapper;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class CreateMondrianXMLService {

    @Autowired
    private DBModelsMapper dbModelsMapper;


    public MondrianCubeSchema CreateMondrianXMLService(String projectName, String schemaName) {
        MondrianCubeSchema cubeSchema = new MondrianCubeSchema(schemaName,"4.0");
        List<MondrianCubeSchema.Table> tables = Lists.newArrayList();
        List<String> table = dbModelsMapper.selectProjectRelatedTables(projectName);
        for (String tableName : table) {
            tables.add(new MondrianCubeSchema.Table(tableName, null, null, null));
        }
        cubeSchema.physicalSchema = new MondrianCubeSchema.PhysicalSchema(tables);
        List<MondrianCubeSchema.Dimension> dimensions = Lists.newArrayList();
        List<Map> dimension = dbModelsMapper.selectProjectRelatedDimensions(projectName);
        for (Map m : dimension) {
            MondrianCubeSchema.Dimension dim = new MondrianCubeSchema.Dimension();
            dim.name = (String) m.get("dimTableName");
            dim.table = (String) m.get("dimTable");
            dim.source = (String) m.get("dimTableName");
            List<MondrianCubeSchema.Attribute> attributes = Lists.newArrayList();
            List<Map> attribute = dbModelsMapper.selectProjectRelatedDimensionAttributes(projectName, dim.table);
            for (Map attrMap : attribute) {
                MondrianCubeSchema.Attribute attr = new MondrianCubeSchema.Attribute();
                attr.name = (String) attrMap.get("dimension_name_alias");
                attr.keyColumn = (String) attrMap.get("column");
                attr.nameColumn = (String) attrMap.get("column");
                attributes.add(attr);
            }
            dim.attributes = attributes;
            List<MondrianCubeSchema.Hierarchy> hierarchies = Lists.newArrayList();
            List<String> Hierarchy = dbModelsMapper.selectProjectRelatedDimensionHierarchy(projectName);
            if (dim.table.equals("OLAP_DIM_DATE")) {
                MondrianCubeSchema.Hierarchy h1 = new MondrianCubeSchema.Hierarchy("年-半年-季-月-日", true, "All");
                List<MondrianCubeSchema.Level> levels1 = Lists.newArrayList();
                levels1.add(new MondrianCubeSchema.Level("年", null));
                levels1.add(new MondrianCubeSchema.Level("半年", null));
                levels1.add(new MondrianCubeSchema.Level("季", null));
                levels1.add(new MondrianCubeSchema.Level("月", null));
                levels1.add(new MondrianCubeSchema.Level("日", "日"));
                h1.levels = levels1;
                hierarchies.add(h1);
                MondrianCubeSchema.Hierarchy h2 = new MondrianCubeSchema.Hierarchy("年-半年-季-月-天", true, "All");
                List<MondrianCubeSchema.Level> levels2 = Lists.newArrayList();
                levels2.add(new MondrianCubeSchema.Level("年", null));
                levels2.add(new MondrianCubeSchema.Level("半年", null));
                levels2.add(new MondrianCubeSchema.Level("季", null));
                levels2.add(new MondrianCubeSchema.Level("月", null));
                levels2.add(new MondrianCubeSchema.Level("日", "天"));
                h2.levels = levels2;
                hierarchies.add(h2);
            }
            for (Map layer : attribute) {
                MondrianCubeSchema.Hierarchy h = new MondrianCubeSchema.Hierarchy((String) layer.get("dimension_name_alias") + "层次结构", true, "All");
                List<MondrianCubeSchema.Level> levels = Lists.newArrayList();
                levels.add(new MondrianCubeSchema.Level((String) layer.get("dimension_name_alias"), null));
                h.levels = levels;
                hierarchies.add(h);
            }
            dim.hierarchies = hierarchies;
            dimensions.add(dim);
        }
        cubeSchema.dimensions = dimensions;
        MondrianCubeSchema.Cube defCube = new MondrianCubeSchema.Cube();
        List<MondrianCubeSchema.Cube> cubes = Lists.newArrayList();
        defCube.name = "CLB_Hospital_Cube";

        List<MondrianCubeSchema.Dimension> cubeDimensions = Lists.newArrayList();
        for (MondrianCubeSchema.Dimension dim : dimensions) {
            cubeDimensions.add(new MondrianCubeSchema.Dimension(dim.source));
        }
        defCube.dimensions = cubeDimensions;

        List<MondrianCubeSchema.MeasureGroup> measureGroups = Lists.newArrayList();
        List<Map> mg = dbModelsMapper.selectProjectRelatedMGs(projectName);
        for (Map map : mg) {
            MondrianCubeSchema.MeasureGroup mgp = new MondrianCubeSchema.MeasureGroup((String) map.get("MGName"), (String) map.get("MGTable"));
            List<Map> measures = dbModelsMapper.selectProjectRelatedMeasures(projectName, (String) map.get("MGName"));
            List<MondrianCubeSchema.Measure> ms = Lists.newArrayList();
            for (Map m : measures) {
                MondrianCubeSchema.Measure measure = new MondrianCubeSchema.Measure();
                measure.name = (String) m.get("measure_name");
                switch ((String) m.get("measure_expression")) {
                    case "COUNT":
                        measure.column = "*";break;
                    default:
                        measure.column = (String) m.get("measure_column");break;
                }
                switch ((String) m.get("measure_expression")) {
                    case "COUNT_DISTINCT":
                        measure.aggregator = "distinct-count";break;
                    default:
                        measure.aggregator = (String) m.get("measure_expression");break;
                }
                switch ((String) m.get("measure_column_datatype")) {
                    case "bitmap":
                        measure.datatype = "####0";break;
                    case "bigint":
                        measure.datatype = "####0";break;
                    case "integer":
                        measure.datatype = "####0";break;
                    default:
                        measure.datatype = "####0.0000";break;
                }
                ms.add(measure);
            }
            mgp.measures = ms;
            List<MondrianCubeSchema.DimensionLink> dimensionLinks = Lists.newArrayList(); // DimensionLinks
            List<Map> links = dbModelsMapper.selectProjectRelatedFL(projectName, (String) map.get("MGName"));
            for (Map m : links) {
                MondrianCubeSchema.ForeignKeyLink foreignKeyLink = new MondrianCubeSchema.ForeignKeyLink((String) m.get("dimension_name"), (String) m.get("foreign_key"));
                dimensionLinks.add(foreignKeyLink);

            }
            mgp.dimensionLinks = dimensionLinks;
            measureGroups.add(mgp);
        }
        defCube.measureGroups = measureGroups;
        List<MondrianCubeSchema.CalculatedMember> calculatedMembers = Lists.newArrayList();
        List<Map> calMeasures = dbModelsMapper.selectProjectRelatedCalMeasures(projectName);
        for (Map m : calMeasures) {
            MondrianCubeSchema.CalculatedMember calculatedMember = new MondrianCubeSchema.CalculatedMember();
            calculatedMember.name = (String) m.get("measure_name");
            calculatedMember.Formula = (String) m.get("measure_expression");
            calculatedMember.dimension = "Measures";
            switch ((String) m.get("measure_column_datatype")) {
                case "bigint":
                    calculatedMember.formatString = "####0";break;
                case "integer":
                    calculatedMember.formatString = "####0";break;
                case "int":
                    calculatedMember.formatString = "####0";break;
                default:
                    calculatedMember.formatString = "####0.0000";break;
            }
            calculatedMembers.add(calculatedMember);
        }
        defCube.calculatedMembers = calculatedMembers;
        cubes.add(defCube);
        cubeSchema.cubes = cubes;
        return cubeSchema;
    }
}
