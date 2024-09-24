package top.hcode.hoj.config;

import lombok.Data;
import top.hcode.hoj.utils.IpUtils;

/**
 * @Author Himit_ZH
 * @Date 2022/10/26
 */
@Data
public class WebConfig {

    // 邮箱配置
    private String emailUsername;

    private String emailPassword;

    private String emailHost;

    private Integer emailPort;

    private Boolean emailSsl = true;

    private String emailBGImg = "http://cdn.xingyi.ac.cn/assest/XINGYI.png";

    // 网站前端显示配置
    private String baseUrl = "http://" + IpUtils.getServiceIp();

    private String name = "星熠 Online Judge";

    private String shortName = "星熠OJ";

    private String description;

    private Boolean register = true;

    private String recordName;

    private String recordUrl;

    private String projectName = "星熠OJ";

    private String projectUrl = "https://xingyi.ac.cn";
}
