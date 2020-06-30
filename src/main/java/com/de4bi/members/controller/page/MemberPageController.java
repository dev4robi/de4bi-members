package com.de4bi.members.controller.page;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Controller
@RequestMapping("/members")
public class MemberPageController {
    
    private static final Logger logger = LoggerFactory.getLogger(MemberPageController.class);

    @RequestMapping("")
    public ModelAndView getLoginPage() {
        return new ModelAndView("login");
    }
}