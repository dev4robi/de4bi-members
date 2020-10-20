package com.de4bi.members.manager;

import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.PostConstruct;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Component
@PropertySource("classpath:config.properties")
@AllArgsConstructor
public class CodeMsgManager {

    private static final Logger logger = LoggerFactory.getLogger(CodeMsgManager.class);

    @Getter
    public enum Locale {
        KO(0, "ko", "Korean"),
        EN(1, "en", "English"),
        DEFAULT(KO);

        private int seq;
        private String alias;
        private String name;

        private Locale(int seq, String alias, String name) {
            this.seq = seq;
            this.alias = alias;
            this.name = name;
        }

        private Locale(Locale locale) {
            this.seq = locale.seq;
            this.alias = locale.alias;
            this.name = locale.name;
        }
    }

    private static final List<Map<String, String>> CODE_MSG_MAP_LIST;
    private Environment env;

    static {
        CODE_MSG_MAP_LIST = new ArrayList<>();
    }

    /**
     * 매니저 클래스를 초기화합니다
     * @apiNote <code>@PostConstruct</code>로 Spring DI직전에 수행됩니다.
     */
    @PostConstruct
    public void init() {
        final String localXmlPath = env.getProperty("locale.string-path");
        if (localXmlPath == null) {
            throw new IllegalStateException("'classpath:config.properties:locale.string-path' is null!");
        }

        final DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = null;

        try {
            docBuilder = docBuilderFactory.newDocumentBuilder();
        }
        catch (ParserConfigurationException e) {
            throw new IllegalStateException("Fail to build DocumentBuilder!", e.getCause());
        }

        //  [Note] 로케일 파일('/resources/{locale}/code-msg.xml') 포멧
        //  <?xml version="1.0" encoding="UTF-8"?>
        //  <codes lang="ko">
        //      <code code="GC0000" msg="OK"/>
        //          ...
        //      <code code="GC9999" msg="알 수 없는 오류가 발생했습니다.""/>
        //  </codes>
        final Locale[] locales = Locale.values();
        final int localeCnt = locales.length - 1; // DEFAULT 로케일은 제외

        for (int idx = 0; idx < localeCnt; ++idx) {
            final long loadBgnTime = System.currentTimeMillis();
            final String localeXmlFileName = localXmlPath + "/" + locales[idx].alias + "/code-msg.xml";
            final Map<String, String> localeMap = new HashMap<>();
            
            try {
                FileChannel fileCh = FileChannel.open(Paths.get(localeXmlFileName), StandardOpenOption.READ);
                Document doc = docBuilder.parse(Channels.newInputStream(fileCh));
                NodeList docNodeList = doc.getChildNodes();
                int docNodeListLen = docNodeList.getLength();

                for (int dNodeIdx = 0; dNodeIdx < docNodeListLen; ++dNodeIdx) {
                    Node docNode = docNodeList.item(dNodeIdx);
                    if (docNode.getNodeName().equals("codes") == false) {
                        continue;
                    }

                    NodeList codeNodeList = docNode.getChildNodes();
                    int codeNodeListLen = codeNodeList.getLength();

                    for (int cNodeIdx = 0; cNodeIdx < codeNodeListLen; ++cNodeIdx) {
                        Node codeNode = codeNodeList.item(cNodeIdx);
                        if (codeNode.getNodeName().equals("code") == false) {
                            continue;
                        }

                        NamedNodeMap codeAttrMap = codeNode.getAttributes();
                        String code = codeAttrMap.getNamedItem("code").getNodeValue();
                        String msg = codeAttrMap.getNamedItem("msg").getNodeValue();
                        localeMap.put(code, msg);
                    }
                }
            }
            catch (IOException | SAXException e) {
                if (idx == 0) {
                    throw new IllegalStateException("Fail to open or parse the default locale file '" + localeXmlFileName + "'!", e.getCause());
                }
                
                final String errMsg = ((e instanceof SAXException) ? "Fail to parse locale file '" 
                                                                   : "Fail to open locale file '") + localeXmlFileName + "'!";
                logger.warn(errMsg);
                continue;
            }

            if (localeMap.size() > 0) {
                CODE_MSG_MAP_LIST.add(localeMap);
                logger.info("Locale file '" + localeXmlFileName + "' load complete. (Time: " + (System.currentTimeMillis() - loadBgnTime) + "ms)");
            }
            else {
                CODE_MSG_MAP_LIST.add(null);
                logger.warn("Locale file '" + localeXmlFileName + "' found but, no elements to load! (Time: " + (System.currentTimeMillis() - loadBgnTime) + "ms)");
            }
        }
    }

    /**
     * 기본 Locale을 사용하여, 코드(code)로부터 메시지(msg)를 가져옵니다.
     * @param code 메시지의 코드값
     * @param paramList 메시지에 노출할 파라미터 리스트
     * @return 메시지코드에 해당하는 메시지
     * @apiNote "Hello {0}!" -> paramList.get(0) -> "Hello World!"
     */
    public String getMsg(String code, List<String> paramList) {
        return getMsg(code, paramList, Locale.DEFAULT);
    }

    /**
     * 지정 Locale을 사용하여, 코드(code)로부터 메시지(msg)를 가져옵니다.
     * @param code 메시지의 코드값
     * @param paramList 메시지에 노출할 파라미터 리스트 (nullable)
     * @param locale 로케일 정보 <code>(null -> Locale.DEFAULT)</code>
     * @return 메시지코드에 해당하는 메시지
     * @apiNote "Hello {0}!" -> paramList.get(0) -> "Hello World!"
     */
    public String getMsg(String code, List<String> paramList, Locale locale) {
        Objects.requireNonNull(code);
        if (locale == null) locale = Locale.DEFAULT;
        
        Map<String, String> localeMap = CODE_MSG_MAP_LIST.get(locale.getSeq());
        if (localeMap == null) {
            localeMap = CODE_MSG_MAP_LIST.get(Locale.DEFAULT.getSeq());
        }

        String rtMsg = localeMap.get(code);
        if (rtMsg != null && paramList != null) {
            final int paramLen = paramList.size();
            for (int i = 0; i < paramLen; ++i) {
                String replaceStr = paramList.get(i);
                if (replaceStr == null) replaceStr = "";
                rtMsg = rtMsg.replaceFirst("\\{" + i + "\\}", replaceStr);
            }
        }

        return (rtMsg != null ? rtMsg : "");
    }
}
