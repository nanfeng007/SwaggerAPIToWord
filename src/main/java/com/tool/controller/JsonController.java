package com.tool.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tool.dto.Table;
import com.tool.service.TableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by XiuYin.Cui on 2018/1/11.
 */
@Controller
public class JsonController {

    @Autowired
    private TableService tableService;

    @RequestMapping("/getJson")
    public String getJson(Model model){
        List<Table> list = tableService.tableList();
        model.addAttribute("table",list);
//        try {
//        	ClassLoader classLoader = TableService.class.getClassLoader();
//        	URL resource = classLoader.getResource("data.json");
//        	Map map = new ObjectMapper().readValue(resource, Map.class);
//        	//解析paths
//        	LinkedHashMap info = (LinkedHashMap) map.get("info");
//        	System.out.println();
//        }catch (Exception e) {
//        	// TODO: handle exception
//        }
		
        return "json";
    }
    
    @RequestMapping("/getJson2")
    public String getJson2(Model model){
        List<Table> list = tableService.tableList2();
        model.addAttribute("table",list);
		
        return "json2";
    }


}
