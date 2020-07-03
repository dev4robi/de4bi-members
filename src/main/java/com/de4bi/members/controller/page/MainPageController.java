package com.de4bi.members.controller.page;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Controller
public class MainPageController {
    
    private static final Logger logger = LoggerFactory.getLogger(MainPageController.class);

    @RequestMapping(value = {"", "/login"})
    public ModelAndView loginPage() {
        return new ModelAndView("login");
    }
}