package com.de4bi.members.manager;

import java.io.File;
import java.io.FileInputStream;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystems;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;

@Component
@PropertySource("classpath:config.properties")
@AllArgsConstructor
public class CodeMsgManager {

    private static final Logger logger = LoggerFactory.getLogger(CodeMsgManager.class);

    public enum Locale {
        KO(0, "ko", "Korean"),
        EN(1, "en", "English");

        private int seq;
        private String alias;
        private String name;

        private Locale(int seq, String alias, String name) {
            this.seq = seq;
            this.alias = alias;
            this.name = name;
        }
    }

    private static final List<Map<String, String>> codeMsgMapList;
    private Environment env;

    static {
        codeMsgMapList = new ArrayList<>();
        final int localeCnt = Locale.values().length;
        for (int i = 0; i < localeCnt; ++i) {
            codeMsgMapList.add(new HashMap<>());
        }
    }

    @PostConstruct
    public void init() {
        final String localXmlPath = env.getProperty("locale.string-path");
        if (localXmlPath == null) {
            throw new IllegalStateException("'classpath:config.properties:locale.string-path' is null!");
        }

        final Locale[] locales = Locale.values();
        for (int idx = 0; idx < locales.length; ++idx) {
            final String localXmlFileName = localXmlPath + "/" + locales[idx].alias + "/code-msg.xml";
            try (final FileChannel localXmlFile = FileChannel.open(FileSystems.getDefault().getPath(localXmlFileName), StandardOpenOption.READ)) {
                // 여기부터 @@
            }
            catch (Exception e) {

            }
            
            if (localXmlFile.exists() == false) {
                logger.warn("No such locale file '" + localXmlFile + "'!");
                if (idx == 0) {
                    throw new IllegalStateException("Default locale file '" + localXmlFile + "' must exist!");
                }
                continue;
            }


        }

        try (final FileInputStream fis = new FileInputStream(localXmlFile)) {

        }
    }
}
