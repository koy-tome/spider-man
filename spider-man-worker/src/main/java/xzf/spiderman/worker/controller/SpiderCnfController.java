package xzf.spiderman.worker.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import xzf.spiderman.common.Ret;
import xzf.spiderman.worker.data.AddSpiderCnfReq;
import xzf.spiderman.worker.service.SpiderCnfService;

import javax.validation.Valid;

@RestController
public class SpiderCnfController
{
    @Autowired
    private SpiderCnfService spiderCnfService;

    @PostMapping("/worker/spider-cnf/add")
    public Ret<Void> add(@Valid @RequestBody AddSpiderCnfReq req)
    {
        spiderCnfService.add(req);
        return Ret.success();
    }

}
