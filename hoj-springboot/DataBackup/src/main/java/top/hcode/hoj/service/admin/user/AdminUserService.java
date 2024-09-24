package top.hcode.hoj.service.admin.user;

import com.baomidou.mybatisplus.core.metadata.IPage;
import top.hcode.hoj.common.exception.StatusForbiddenException;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.dto.AdminEditUserDTO;
import top.hcode.hoj.pojo.vo.UserRolesVO;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/9 21:31
 * @Description:
 */
public interface AdminUserService {

    public CommonResult<IPage<UserRolesVO>> getUserList(Integer limit, Integer currentPage, Boolean onlyAdmin, String keyword, Boolean onlyStatus,Boolean showLoginTime);

    public CommonResult<Void> editUser(AdminEditUserDTO adminEditUserDto);

    public CommonResult<Void> deleteUser(List<String> deleteUserIdList);

    public CommonResult<Void> insertBatchUser(List<List<String>> users);

    public CommonResult<Map<Object,Object>> generateUser(Map<String, Object> params);

}