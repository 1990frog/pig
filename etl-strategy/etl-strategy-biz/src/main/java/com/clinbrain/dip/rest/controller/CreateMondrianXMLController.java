package com.clinbrain.dip.rest.controller;

import com.clinbrain.dip.cube.MondrianCubeSchema;
import com.clinbrain.dip.rest.bean.PropertyBean;
import com.clinbrain.dip.rest.response.ResponseData;
import com.clinbrain.dip.rest.service.CreateMondrianXMLService;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;


@RestController
@RequestMapping("/createMondrianXML")
public class CreateMondrianXMLController {

    private static final Logger logger = LoggerFactory.getLogger(CreateMondrianXMLController.class);
    @Autowired
    private CreateMondrianXMLService createService;

    @Autowired
    private PropertyBean propertyBean;

    @RequestMapping(value = "/create", method = RequestMethod.GET)
    public ResponseData CreateMondrianXML(@RequestParam(value = "projectName") String projectName) {
        try {
            String xmlBegin = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
            MondrianCubeSchema cubeSchema = createService.CreateMondrianXMLService(projectName,propertyBean.getMolapSchema());
            DomDriver domDriver = new DomDriver("UTF-8");
            XStream xstream = new XStream(domDriver);
            xstream.autodetectAnnotations(true);
            //System.out.println(xmlBegin + xstream.toXML(cubeSchema));
            URL pathname = Thread.currentThread().getContextClassLoader().getResource(String.format("%s.xml", propertyBean.getMolapSchema()));
            File file = new File(pathname.getPath().replace("%23","#"));
            //File file = new File("D:\\GIT4\\clb_big_data\\tools\\dip\\server-base\\src\\main\\resources\\test\\2.xml");
            FileOutputStream outPut = new FileOutputStream(file);
            outPut.write((xmlBegin + xstream.toXML(cubeSchema)).getBytes());
            outPut.flush();
            outPut.close();

        } catch (Exception e) {
            logger.error("Create Project Name :" + projectName + " Mondrian XML failed.\nError message: \n" + e);
            e.printStackTrace();
            return new ResponseData.Builder<Boolean>().data(false).error("Create Project Name :" + projectName + " Mondrian XML failed.\n"+ e);
        }
        return new ResponseData.Builder<Boolean>().data(true).success("Create Project Name :" + projectName + " Mondrian XML Successfully!");
    }
}
