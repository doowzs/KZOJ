package top.hcode.hoj.manager.group;

import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.exception.StatusForbiddenException;
import top.hcode.hoj.common.exception.StatusNotFoundException;
import top.hcode.hoj.config.NacosSwitchConfig;
import top.hcode.hoj.config.SwitchConfig;
import top.hcode.hoj.dao.group.GroupEntityService;
import top.hcode.hoj.dao.group.GroupMemberEntityService;
import top.hcode.hoj.dao.problem.ProblemEntityService;
import top.hcode.hoj.dao.user.UserAcproblemEntityService;
import top.hcode.hoj.dao.user.UserInfoEntityService;
import top.hcode.hoj.manager.email.EmailManager;
import top.hcode.hoj.pojo.dto.VerifyDTO;
import top.hcode.hoj.pojo.entity.group.Group;
import top.hcode.hoj.pojo.entity.group.GroupMember;
import top.hcode.hoj.pojo.entity.problem.Problem;
import top.hcode.hoj.pojo.entity.user.UserAcproblem;
import top.hcode.hoj.pojo.entity.user.UserInfo;
import top.hcode.hoj.pojo.vo.AccessVO;
import top.hcode.hoj.pojo.vo.GroupVO;
import top.hcode.hoj.shiro.AccountProfile;
import top.hcode.hoj.utils.Constants;
import top.hcode.hoj.utils.RedisUtils;
import top.hcode.hoj.validator.GroupValidator;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Author: LengYun
 * @Date: 2022/3/11 13:36
 * @Description:
 */
@Component
@Slf4j(topic = "hoj")
public class GroupManager {

    @Autowired
    private GroupEntityService groupEntityService;

    @Autowired
    private UserAcproblemEntityService userAcproblemEntityService;

    @Autowired
    private UserInfoEntityService userInfoEntityService;

    @Autowired
    private GroupMemberEntityService groupMemberEntityService;

    @Autowired
    private ProblemEntityService problemEntityService;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private GroupValidator groupValidator;

    @Autowired
    private NacosSwitchConfig nacosSwitchConfig;

    @Autowired
    private EmailManager emailManager;

    public IPage<GroupVO> getGroupList(Integer limit, Integer currentPage, String keyword, Integer auth, boolean onlyMine) {
        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();

        if (currentPage == null || currentPage < 1) currentPage = 1;
        if (limit == null || limit < 1) limit = 10;
        if (auth == null || auth < 1) auth = 0;

        if (!StringUtils.isEmpty(keyword)) {
            keyword = keyword.trim();
        }

        String uid = "";
        boolean isRoot = false;
        if (userRolesVo != null) {
            uid = userRolesVo.getUid();
            isRoot = SecurityUtils.getSubject().hasRole("root");
        }
        return groupEntityService.getGroupList(limit, currentPage, keyword, auth, uid, onlyMine, isRoot);
    }

    public Group getGroup(Long gid) throws StatusNotFoundException, StatusForbiddenException {
        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();

        boolean isRoot = SecurityUtils.getSubject().hasRole("root");

        Group group = groupEntityService.getById(gid);
        if (group == null || group.getStatus() == 1 && !isRoot) {
            throw new StatusNotFoundException("访问团队失败，该团队不存在或已被封禁！");
        }
        if (!group.getVisible() && !isRoot && !groupValidator.isGroupMember(userRolesVo.getUid(), gid)) {
            throw new StatusForbiddenException("该团队并非公开团队，不支持访问！");
        }

        if (!isRoot && !groupValidator.isGroupRoot(userRolesVo.getUid(), gid)) {
            group.setCode(null);
        }

        return group;
    }

    public AccessVO getGroupAccess(Long gid) throws StatusFailException, StatusNotFoundException, StatusForbiddenException {
        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();

        boolean isRoot = SecurityUtils.getSubject().hasRole("root");

        boolean access = false;

        if (groupValidator.isGroupMember(userRolesVo.getUid(), gid) || isRoot) {
            access = true;
            Group group = groupEntityService.getById(gid);
            if (group == null || group.getStatus() == 1 && !isRoot) {
                throw new StatusNotFoundException("获取权限失败，该团队不存在或已被封禁！");
            }
            if (!isRoot && !group.getVisible() && !groupValidator.isGroupMember(userRolesVo.getUid(), gid)) {
                throw new StatusForbiddenException("该团队并非公开团队，不支持访问！");
            }
        }

        AccessVO accessVo = new AccessVO();
        accessVo.setAccess(access);
        return accessVo;
    }

    public Integer getGroupAuth(Long gid) {
        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();

        QueryWrapper<GroupMember> groupMemberQueryWrapper = new QueryWrapper<>();
        groupMemberQueryWrapper.eq("gid", gid).eq("uid", userRolesVo.getUid());

        GroupMember groupMember = groupMemberEntityService.getOne(groupMemberQueryWrapper);

        Integer auth = 0;
        if (groupMember != null) {
            auth = groupMember.getAuth();
        }
        return auth;
    }

    @Transactional(rollbackFor = Exception.class)
    public void addGroup(Group group) throws StatusFailException, StatusForbiddenException {
        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();

        boolean isRoot = SecurityUtils.getSubject().hasRole("root");
        boolean isProblemAdmin = SecurityUtils.getSubject().hasRole("problem_admin");
        boolean isAdmin = SecurityUtils.getSubject().hasRole("admin");

        if (!isRoot && !isAdmin && !isProblemAdmin) {

            QueryWrapper<UserAcproblem> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("uid", userRolesVo.getUid()).select("distinct pid");
            int userAcProblemCount = userAcproblemEntityService.count(queryWrapper);
            SwitchConfig switchConfig = nacosSwitchConfig.getSwitchConfig();
            if (userAcProblemCount < switchConfig.getDefaultCreateGroupACInitValue()) {
                throw new StatusForbiddenException("对不起，您暂时无权限创建团队！请先去提交题目通过" +
                        switchConfig.getDefaultCreateGroupACInitValue() + "道以上!");
            }

            String lockKey = Constants.Account.GROUP_ADD_NUM_LOCK.getCode() + userRolesVo.getUid();
            Integer num = (Integer) redisUtils.get(lockKey);
            if (num == null) {
                redisUtils.set(lockKey, 1, 3600 * 24);
            } else if (num >= switchConfig.getDefaultCreateGroupDailyLimit()) {
                throw new StatusForbiddenException("对不起，您今天创建团队次数已超过" + switchConfig.getDefaultCreateGroupDailyLimit() + "次，已被限制！");
            } else {
                redisUtils.incr(lockKey, 1);
            }

            QueryWrapper<Group> existedGroupQueryWrapper = new QueryWrapper<>();
            existedGroupQueryWrapper.eq("owner", userRolesVo.getUsername());
            int existedGroupNum = groupEntityService.count(existedGroupQueryWrapper);

            if (existedGroupNum >= switchConfig.getDefaultCreateGroupLimit()) {
                throw new StatusForbiddenException("对不起，您总共已创建了" + switchConfig.getDefaultCreateGroupLimit() + "个团队，不可再创建，已被限制！");
            }

        }
        group.setOwner(userRolesVo.getUsername());
        group.setUid(userRolesVo.getUid());

        if (!StringUtils.isEmpty(group.getName()) && (group.getName().length() < 5 || group.getName().length() > 25)) {
            throw new StatusFailException("团队名称的长度应为 5 到 25！");
        }

        if (!StringUtils.isEmpty(group.getShortName()) && (group.getShortName().length() < 5 || group.getShortName().length() > 10)) {
            throw new StatusFailException("团队简称的长度应为 5 到 10！");
        }

        if (!StringUtils.isEmpty(group.getBrief()) && (group.getBrief().length() < 5 || group.getBrief().length() > 50)) {
            throw new StatusFailException("团队简介的长度应为 5 到 50！");
        }

        if (group.getAuth() == null || group.getAuth() < 1 || group.getAuth() > 3) {
            throw new StatusFailException("团队权限不能为空且应为1~3！");
        }

        if ( group.getAuth() == 3) {
            if (StringUtils.isEmpty(group.getCode()) || group.getCode().length() != 6) {
                throw new StatusFailException("团队邀请码不能为空且长度应为 6！");
            }
        }

        if (!StringUtils.isEmpty(group.getDescription()) && (group.getDescription().length() < 5 || group.getDescription().length() > 1000)) {
            throw new StatusFailException("团队描述的长度应为 5 到 1000！");
        }

        QueryWrapper<Group> groupQueryWrapper = new QueryWrapper<>();
        groupQueryWrapper.eq("name", group.getName());
        int sameNameGroupCount = groupEntityService.count(groupQueryWrapper);
        if (sameNameGroupCount > 0) {
            throw new StatusFailException("团队名称已存在，请修改后重试！");
        }

        groupQueryWrapper = new QueryWrapper<>();
        groupQueryWrapper.eq("short_name", group.getShortName());
        int sameShortNameGroupCount = groupEntityService.count(groupQueryWrapper);
        if (sameShortNameGroupCount > 0) {
            throw new StatusFailException("团队简称已存在，请修改后重试！");
        }
        boolean isOk = groupEntityService.save(group);
        if (!isOk) {
            throw new StatusFailException("创建失败，请重新尝试！");
        } else {
            groupMemberEntityService.save(new GroupMember()
                    .setUid(userRolesVo.getUid())
                    .setGid(group.getId())
                    .setAuth(5));
        }
        log.info("[{}],[{}],Gid:[{}],Gname:[{}],operatorUid:[{}],operatorUsername:[{}]",
                "Group", "Add", group.getId(),group.getName(),userRolesVo.getUid(), userRolesVo.getUsername());
    }

    public void updateGroup(Group group) throws StatusFailException, StatusForbiddenException {
        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();

        boolean isRoot = SecurityUtils.getSubject().hasRole("root");

        if (!groupValidator.isGroupRoot(userRolesVo.getUid(), group.getId()) && !isRoot) {
            throw new StatusForbiddenException("对不起，您无权限操作！");
        }

        if (!StringUtils.isEmpty(group.getName()) && (group.getName().length() < 5 || group.getName().length() > 25)) {
            throw new StatusFailException("团队名称的长度应为 5 到 25！");
        }

        if (!StringUtils.isEmpty(group.getShortName()) && (group.getShortName().length() < 5 || group.getShortName().length() > 10)) {
            throw new StatusFailException("团队简称的长度应为 5 到 10！");
        }

        if (!StringUtils.isEmpty(group.getBrief()) && (group.getBrief().length() < 5 || group.getBrief().length() > 50)) {
            throw new StatusFailException("团队简介的长度应为 5 到 50！");
        }

        if (!StringUtils.isEmpty(group.getCode()) && group.getCode().length() != 6) {
            throw new StatusFailException("团队邀请码的长度应为 6！");
        }

        if (!StringUtils.isEmpty(group.getDescription()) && (group.getDescription().length() < 5 || group.getDescription().length() > 1000)) {
            throw new StatusFailException("团队描述的长度应为 5 到 1000！");
        }

        QueryWrapper<Group> groupQueryWrapper = new QueryWrapper<>();
        groupQueryWrapper.eq("name", group.getName())
                .ne("id", group.getId());
        int sameNameGroupCount = groupEntityService.count(groupQueryWrapper);
        if (sameNameGroupCount > 0) {
            throw new StatusFailException("团队名称已存在，请修改后重试！");
        }

        groupQueryWrapper = new QueryWrapper<>();
        groupQueryWrapper.eq("short_name", group.getShortName())
                .ne("id", group.getId());
        int sameShortNameGroupCount = groupEntityService.count(groupQueryWrapper);
        if (sameShortNameGroupCount > 0) {
            throw new StatusFailException("团队简称已存在，请修改后重试！");
        }

        boolean isOk = groupEntityService.updateById(group);
        if (!isOk) {
            throw new StatusFailException("更新失败，请重新尝试！");
        }
        log.info("[{}],[{}],Gid:[{}],operatorUid:[{}],operatorUsername:[{}]",
                "Group", "Update", group.getId(),userRolesVo.getUid(), userRolesVo.getUsername());
    }

    public void deleteGroup(VerifyDTO verifyDTO, Long gid) throws StatusFailException, StatusNotFoundException, StatusForbiddenException {
        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();

        boolean isRoot = SecurityUtils.getSubject().hasRole("root");

        Group group = groupEntityService.getById(gid);

        if (group == null || group.getStatus() == 1 && !isRoot) {
            throw new StatusNotFoundException("删除失败，该团队不存在或已被封禁！");
        }

        if (!isRoot && !userRolesVo.getUid().equals(group.getUid())) {
            throw new StatusForbiddenException("对不起，您无权限操作！");
        }
        if (!Validator.isEmail(verifyDTO.getEmail())) {
            throw new StatusFailException("邮箱格式错误！");
        }

        // 如果已经被锁定半小时不能修改
        String lockKey = Constants.Account.CODE_VERIFY_EMAIL_LOCK + userRolesVo.getUid();
        // 统计失败的key
        String countKey = Constants.Account.CODE_VERIFY_EMAIL_FAIL + userRolesVo.getUid();

        if (redisUtils.hasKey(lockKey)) {
            long expire = redisUtils.getExpire(lockKey);
            Date now = new Date();
            long minute = expire / 60;
            long second = expire % 60;
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date afterDate = new Date(now.getTime() + expire * 1000);
            String msg = "由于您多次验证失败，解散团队功能已锁定，请在" + minute + "分" + second + "秒后(" + formatter.format(afterDate) + ")再进行尝试！";
            throw new StatusFailException(msg);
        }

        String cacheCodeKey = Constants.Email.VERIFY_EMAIL_KEY_PREFIX.getValue() + verifyDTO.getEmail();
        String cacheCode = (String) redisUtils.get(cacheCodeKey);
        if (cacheCode == null) {
            throw new StatusFailException("邮箱验证码不存在或已过期，请重新发送！");
        }
        if (!Objects.equals(cacheCode, verifyDTO.getCode())) {
            Integer count = (Integer) redisUtils.get(countKey);
            if (count == null) {
                redisUtils.set(countKey, 1, 60 * 30); // 三十分钟不尝试，该限制会自动清空消失
                count = 0;
            } else if (count < 5) {
                redisUtils.incr(countKey, 1);
            }
            count++;
            if (count == 5) {
                redisUtils.del(countKey); // 清空统计
                redisUtils.set(lockKey, "lock", 60 * 30); // 设置锁定更改
            }
            throw new StatusFailException("验证码错误！您已累计验证失败" + count + "次...");
        }

        QueryWrapper<GroupMember> groupMemberQueryWrapper = new QueryWrapper<>();
        groupMemberQueryWrapper.eq("gid", gid).in("auth", 3, 4, 5);
        List<GroupMember> groupMemberList = groupMemberEntityService.list(groupMemberQueryWrapper);
        List<String> groupMemberUidList = groupMemberList.stream()
                .map(GroupMember::getUid)
                .collect(Collectors.toList());

        UpdateWrapper<Problem> problemUpdateWrapper = new UpdateWrapper<>();
        problemUpdateWrapper.eq("gid",gid).eq("apply_public_progress",2);
        int sameProblemGIDCount = problemEntityService.count(problemUpdateWrapper);
        if (sameProblemGIDCount > 0) { // 查询成功
            problemUpdateWrapper.set("gid",null);
            problemUpdateWrapper.set("apply_public_progress", null);
            boolean isOk = problemEntityService.update(problemUpdateWrapper);
            if (!isOk) {
                throw new StatusFailException("删除失败，请重新尝试！");
            }
        }
        boolean isOk = groupEntityService.removeById(gid);
        if (!isOk) {
            throw new StatusFailException("删除失败，请重新尝试！");
        } else {
            groupMemberEntityService.addDissolutionNoticeToGroupMember(gid,
                    group.getName(),
                    groupMemberUidList,
                    userRolesVo.getUsername());
        }
        log.info("[{}],[{}],Gid:[{}],operatorUid:[{}],operatorUsername:[{}]",
                "Group", "Delete", group.getId(),userRolesVo.getUid(), userRolesVo.getUsername());
    }

    public void getVerifyEmailCode(String email) throws StatusFailException {

        String lockKey = Constants.Email.VERIFY_EMAIL_LOCK + email;
        if (redisUtils.hasKey(lockKey)) {
            throw new StatusFailException("对不起，您的操作频率过快，请在" + redisUtils.getExpire(lockKey) + "秒后再次发送修改邮件！");
        }

        // 获取当前登录的用户
        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();
        QueryWrapper<UserInfo> emailUserInfoQueryWrapper = new QueryWrapper<>();
        emailUserInfoQueryWrapper.select("uuid", "email")
                .eq("email", email);
        UserInfo emailUserInfo = userInfoEntityService.getOne(emailUserInfoQueryWrapper, false);
        boolean isOK = (emailUserInfo != null && Objects.equals(emailUserInfo.getUuid(), userRolesVo.getUid()));
        if (!isOK) {
            throw new StatusFailException("当前邮箱与绑定当邮箱不一致");
        }
        String numbers = RandomUtil.randomNumbers(6); // 随机生成6位数字的组合
        redisUtils.set(Constants.Email.VERIFY_EMAIL_KEY_PREFIX.getValue() + email, numbers, 10 * 60); //默认验证码有效10分钟
        emailManager.verifyEmailCode(email, userRolesVo.getUsername(), numbers);
        redisUtils.set(lockKey, 0, 30);
    }
}
