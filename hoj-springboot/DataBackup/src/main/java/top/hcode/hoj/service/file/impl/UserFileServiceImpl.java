package top.hcode.hoj.service.file.impl;

import com.alibaba.excel.EasyExcel;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.hcode.hoj.common.exception.StatusForbiddenException;
import top.hcode.hoj.dao.user.UserRoleEntityService;
import top.hcode.hoj.manager.file.UserFileManager;
import top.hcode.hoj.pojo.vo.UserRolesVO;
import top.hcode.hoj.service.file.UserFileService;
import top.hcode.hoj.shiro.AccountProfile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/10 15:04
 * @Description:
 */
@Service
public class UserFileServiceImpl implements UserFileService {

    @Resource
    private UserFileManager userFileManager;

    @Autowired
    private UserRoleEntityService userRoleEntityService;

    @Override
    public void generateUserExcel(String key, HttpServletResponse response) throws IOException {
        userFileManager.generateUserExcel(key, response);
    }

    public void downloadUserList(Boolean isRLTime,HttpServletResponse response) throws StatusForbiddenException, IOException {
        // 获取当前登录的用户
        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();

        // 是否为超级管理员
        boolean isRoot = SecurityUtils.getSubject().hasRole("root");

        if (!isRoot) {
            throw new StatusForbiddenException("错误：您的权限不够，无权下载文件！");
        }
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码
        String fileName = URLEncoder.encode("user" + "_list", "UTF-8");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
        response.setHeader("Content-Type", "application/xlsx");


        List<List<String>> headList = new LinkedList<>();

        List<String> head0 = new LinkedList<>();
        head0.add("User Name");
        List<String> head1 = new LinkedList<>();
        head1.add("Real Name");
        List<String> head2 = new LinkedList<>();
        head2.add("School");
        List<String> head3 = new LinkedList<>();
        head3.add("Class");
        List<String> head4 = new LinkedList<>();
        head4.add("Gender");
        List<String> head5 = new LinkedList<>();
        head5.add("Status");
        List<String> head6 = new LinkedList<>();
        head6.add("Email");
        List<String> head7 = new LinkedList<>();
        head7.add("Gmt Create");
        List<String> head8 = new LinkedList<>();
        head8.add("Recent Login Time");
        List<String> head9 = new LinkedList<>();
        head9.add("IP");
        List<String> head10 = new LinkedList<>();
        head10.add("UserAgent");

        headList.add(head0);
        headList.add(head1);
        headList.add(head2);
        headList.add(head3);
        headList.add(head4);
        headList.add(head5);
        headList.add(head6);
        headList.add(head7);
        headList.add(head8);
        headList.add(head9);
        headList.add(head10);

        List<UserRolesVO> userMapperList = userRoleEntityService.downloadUserList(isRLTime);
        List<List<Object>> allRowDataList = new LinkedList<>();
        for (UserRolesVO userRolesVO : userMapperList) {
            List<Object> rowData = new LinkedList<>();
            rowData.add(userRolesVO.getUsername());
            rowData.add(userRolesVO.getRealname());
            rowData.add(userRolesVO.getSchool());
            rowData.add(userRolesVO.getNumber());
            switch (userRolesVO.getGender()){
                case "male":rowData.add("男");break;
                case "female":rowData.add("女");break;
                default:rowData.add("保密");
            }
            if (userRolesVO.getStatus() == 0 ){
                rowData.add("正常");
            }else {
                rowData.add("封禁");
            }
            rowData.add(userRolesVO.getEmail());
            rowData.add(userRolesVO.getGmtCreate());
            rowData.add(userRolesVO.getRecentLoginTime());
            rowData.add(userRolesVO.getIp());
            rowData.add(userRolesVO.getUserAgent());

            allRowDataList.add(rowData);
        }
        EasyExcel.write(response.getOutputStream())
                .head(headList)
                .sheet("user")
                .doWrite(allRowDataList);
    }
}