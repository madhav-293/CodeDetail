package com.codepost.CodePost.controller;


import com.codepost.CodePost.entity.CodeEntity;
import com.codepost.CodePost.service.CodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/code")
public class CodeController {

    @Autowired
    CodeService codeService;


    @PostMapping("/post")
    public CodeEntity addCode(@RequestBody CodeEntity codeEntity){
        return codeService.addCode(codeEntity);
    }

    @GetMapping("/get")
    public List<CodeEntity> getActive(@RequestParam("value") String value){
        return codeService.getActive(value);
    }

    @GetMapping("/getLatestVersion")
    public CodeEntity getLatestVersion(@RequestParam("cName") String cName){
        return codeService.getLatestVersion(cName);
    }

    @GetMapping("/getAllLatestCode")
    public List<CodeEntity> getAllLatestCode(@RequestParam("status") String status){
        return codeService.getAllLatestCode(status);
    }

    @GetMapping("/current-user")
    public String getLoggedInUser(Principal principal){
        return principal.getName(); //Principal is such a representation it represents the current user
    }

}
