package com.de4bi.members.controller.page;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Controller
public class TestPageController {
    
    private static final Logger logger = LoggerFactory.getLogger(TestPageController.class);

    @RequestMapping(value= {"", "/test"})
    public ModelAndView getTest() {
        Map<String, String> map = new HashMap<>();
        map.put("key1", "value1_" + System.currentTimeMillis());
        map.put("key2", " ");
        map.put("key3", null);

        List<String> list = new ArrayList<>();
        list.add("list1_" + System.currentTimeMillis());
        list.add(" ");
        list.add(null);

        final class DataModel {
            private String d = "data_model_d_" + System.currentTimeMillis();
            public String getD() { return d; }
            @Override public String toString() { return DataModel.class.getName() + "{d:" + d + "}";}
        }

        ModelAndView mav = new ModelAndView("test");
        mav.addObject("string", "string_value_" + System.currentTimeMillis());
        mav.addObject("map", map);
        mav.addObject("list", list);
        mav.addObject("object", new DataModel());

        return mav;
    }
}