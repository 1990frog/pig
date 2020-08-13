package com.clinbrain.dip.rest.controller;

import com.clinbrain.dip.pojo.ETLToken;
import com.clinbrain.dip.pojo.ETLTokenCategory;
import com.clinbrain.dip.rest.response.ResponseData;
import com.clinbrain.dip.rest.service.TokenCategoryService;
import com.clinbrain.dip.rest.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Created by Liaopan on 2018/1/15.
 */
@RestController
@RequestMapping("/etl/tokenCategory")
public class TokenCategoryController {

    @Autowired
    private TokenCategoryService dipTokenCategoryService;

    @Autowired
    private TokenService tokenService;

    @RequestMapping(value = "/all", method = RequestMethod.GET)
    public ResponseData getAllTokenCategorys() {
        List<ETLTokenCategory> dipTokenCategories = dipTokenCategoryService.selectAll();
        return new ResponseData.Builder<List>(dipTokenCategories).success();
    }

    @DeleteMapping(value = "/{code}")
    public ResponseData deleteTokenCategory(@PathVariable("code") String code){
        if(code==null){
            return new ResponseData.Builder<>().error("参数为空");
        }
        ETLTokenCategory category=new ETLTokenCategory();
        category.setTokenCategoryCode(code);

        ETLToken token=new ETLToken();
        token.setTokenCategoryCode(code);
        try {
            if(tokenService.selectCount(token)>0){
                return new ResponseData.Builder<>().error("已经被引用，不能删除");
            }else {
                return new ResponseData.Builder<>(dipTokenCategoryService.deleteByPrimaryKey(category)).success();
            }
        }catch (Exception e){
           return new ResponseData.Builder<>().error("删除失败");
        }
    }

    @PutMapping
    public ResponseData renovateTokenCategory(@RequestBody ETLTokenCategory dipTokenCategory){
        if(dipTokenCategory==null){
            return new ResponseData.Builder<>().error("数据接收失败");
        }
        return new ResponseData.Builder<>(dipTokenCategoryService.putTokenCategory(dipTokenCategory)).success();
    }

    @PostMapping
    public ResponseData appendTopic(@RequestBody ETLTokenCategory dipTokenCategory){
        if(dipTokenCategory==null){
            return new ResponseData.Builder<>().error("数据接收失败");
        }
        return new ResponseData.Builder<>(dipTokenCategoryService.appendTokenCategory(dipTokenCategory)).success();
    }
}
