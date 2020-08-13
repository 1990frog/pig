package com.clinbrain.dip.rest.controller;

import com.clinbrain.dip.pojo.ETLToken;
import com.clinbrain.dip.rest.response.ResponseData;
import com.clinbrain.dip.rest.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Created by Liaopan on 2018/1/15.
 */
@RestController
@RequestMapping("/etl/token")
public class TokenController {

    @Autowired
    public TokenService dipTokenService;

    @RequestMapping(value = "/all", method = RequestMethod.GET)
    public ResponseData getAllTokens( @RequestParam(value = "offset",required = false) Integer offset,
                                      @RequestParam(value = "limit", required = false) Integer limit) {
        List<ETLToken> dipTokens = dipTokenService.selectAll(offset,limit);
        ResponseData.Page pages=new ResponseData.Page(dipTokenService.selectAll().size(),dipTokens);
        return new ResponseData.Builder<ResponseData.Page>(pages).success();
    }

    @DeleteMapping(value = "/{code}")
    public ResponseData deleteToken(@PathVariable("code") String code){
        try {
            if(dipTokenService.checkTokenByWorkflowToken(code)>0){
                return new ResponseData.Builder<>(null).error("占位符已经被引用，无法删除");
            }
            ETLToken token=new ETLToken();
            token.setTokenCode(code);
            return new ResponseData.Builder<>(dipTokenService.deleteByPrimaryKey(token)).success();
        }catch (Exception e){
            return new ResponseData.Builder<>(null).error(e.getMessage());
        }
    }

    @PostMapping
    public ResponseData appendToken(@RequestBody ETLToken token){
        if(token==null){
            return new ResponseData.Builder<>().error("数据接收失败");
        }
        return new ResponseData.Builder<>(dipTokenService.appendToken(token)).success();
    }

    @PutMapping
    public ResponseData renovateTokenCategory(@RequestBody ETLToken token){
        if(token==null){
            return new ResponseData.Builder<>().error("数据接收失败");
        }
        return new ResponseData.Builder<>(dipTokenService.putToken(token)).success();
    }

}
