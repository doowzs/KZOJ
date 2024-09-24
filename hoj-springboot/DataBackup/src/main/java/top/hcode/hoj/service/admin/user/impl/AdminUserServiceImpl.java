package top.hcode.hoj.service.admin.user.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.exception.StatusForbiddenException;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.dao.user.UserRoleEntityService;
import top.hcode.hoj.manager.admin.user.AdminUserManager;
import top.hcode.hoj.pojo.dto.AdminEditUserDTO;
import top.hcode.hoj.pojo.entity.contest.Contest;
import top.hcode.hoj.pojo.entity.contest.ContestProblem;
import top.hcode.hoj.pojo.entity.discussion.Discussion;
import top.hcode.hoj.pojo.vo.ACMContestRankVO;
import top.hcode.hoj.pojo.vo.OIContestRankVO;
import top.hcode.hoj.pojo.vo.UserRolesVO;
import top.hcode.hoj.service.admin.user.AdminUserService;
import top.hcode.hoj.shiro.AccountProfile;
import top.hcode.hoj.utils.Constants;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/9 21:34
 * @Description:
 */
@Service
public class AdminUserServiceImpl implements AdminUserService {

    @Resource
    private AdminUserManager adminUserManager;

    @Override
    public CommonResult<IPage<UserRolesVO>> getUserList(Integer limit, Integer currentPage, Boolean onlyAdmin, String keyword,Boolean onlyStatus,Boolean showLoginTime) {
        return CommonResult.successResponse(adminUserManager.getUserList(limit, currentPage, onlyAdmin, keyword, onlyStatus,showLoginTime));
    }

    @Override
    public CommonResult<Void> editUser(AdminEditUserDTO adminEditUserDto) {
        try {
            adminUserManager.editUser(adminEditUserDto);
            return CommonResult.successResponse();
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        }
    }

    @Override
    public CommonResult<Void> deleteUser(List<String> deleteUserIdList) {
        try {
            adminUserManager.deleteUser(deleteUserIdList);
            return CommonResult.successResponse();
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        }
    }

    @Override
    public CommonResult<Void> insertBatchUser(List<List<String>> users) {
        try {
            adminUserManager.insertBatchUser(users);
            return CommonResult.successResponse();
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        }
    }

    @Override
    public CommonResult<Map<Object, Object>> generateUser(Map<String, Object> params) {
        try {
            return CommonResult.successResponse(adminUserManager.generateUser(params));
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        }
    }
}