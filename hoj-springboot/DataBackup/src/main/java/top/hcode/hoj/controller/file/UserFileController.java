package top.hcode.hoj.controller.file;


import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.exception.StatusForbiddenException;
import top.hcode.hoj.service.file.UserFileService;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @Author: Himit_ZH
 * @Date: 2021/10/5 19:48
 * @Description:
 */
@Controller
@RequestMapping("/api/file")
public class UserFileController {

    @Autowired
    private UserFileService userFileService;

    @RequestMapping("/generate-user-excel")
    @RequiresAuthentication
    @RequiresRoles("root")
    public void generateUserExcel(@RequestParam("key") String key, HttpServletResponse response) throws IOException {
        userFileService.generateUserExcel(key, response);
    }

    @GetMapping("/download-user-list")
    @RequiresAuthentication
    @RequiresRoles("root")
    public void downloadUserList(
            @RequestParam(value = "isRLTime", defaultValue = "true") Boolean isRLTime,
            HttpServletResponse response) throws StatusFailException, IOException, StatusForbiddenException {
        userFileService.downloadUserList(isRLTime,response);
    }
}