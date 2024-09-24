package top.hcode.hoj.manager.admin.problem;

import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.exception.StatusForbiddenException;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.crawler.problem.ProblemStrategy;
import top.hcode.hoj.dao.judge.JudgeEntityService;
import top.hcode.hoj.dao.problem.ProblemCaseEntityService;
import top.hcode.hoj.dao.problem.ProblemEntityService;
import top.hcode.hoj.judge.Dispatcher;
import top.hcode.hoj.pojo.dto.CompileDTO;
import top.hcode.hoj.pojo.dto.ProblemDTO;
import top.hcode.hoj.pojo.entity.judge.Judge;
import top.hcode.hoj.pojo.entity.problem.Problem;
import top.hcode.hoj.pojo.entity.problem.ProblemCase;
import top.hcode.hoj.shiro.AccountProfile;
import top.hcode.hoj.utils.Constants;
import top.hcode.hoj.validator.ProblemValidator;

import javax.annotation.Resource;
import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/9 16:32
 * @Description:
 */

@Component
@RefreshScope
@Slf4j(topic = "hoj")
public class AdminProblemManager {
    @Autowired
    private ProblemEntityService problemEntityService;

    @Autowired
    private ProblemCaseEntityService problemCaseEntityService;

    @Autowired
    private Dispatcher dispatcher;

    @Value("${hoj.judge.token}")
    private String judgeToken;

    @Resource
    private JudgeEntityService judgeEntityService;

    @Resource
    private ProblemValidator problemValidator;

    @Autowired
    private RemoteProblemManager remoteProblemManager;

    public IPage<Problem> getProblemList(Integer limit, Integer currentPage, String keyword, Integer auth,Integer type, String oj) {
        if (currentPage == null || currentPage < 1) currentPage = 1;
        if (limit == null || limit < 1) limit = 10;
        IPage<Problem> iPage = new Page<>(currentPage, limit);
        IPage<Problem> problemList;

        QueryWrapper<Problem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_group", false)
                .orderByDesc("id");

        // 根据oj筛选过滤
        if (oj != null && !"All".equals(oj)) {
            if (!Constants.RemoteOJ.isRemoteOJ(oj)) {
                queryWrapper.eq("is_remote", false);
            } else {
                queryWrapper.eq("is_remote", true).likeRight("problem_id", oj);
            }
        }

        if (auth != null && auth != 0) {
            queryWrapper.eq("auth", auth);
        }

        if (type != null && type != 0) {
            queryWrapper.eq("type", type-1);
        }

        if (!StringUtils.isEmpty(keyword)) {
            final String key = keyword.trim();
            queryWrapper.and(wrapper -> wrapper.like("title", key).or()
                    .like("author", key).or()
                    .like("problem_id", key));
            problemList = problemEntityService.page(iPage, queryWrapper);
        } else {
            problemList = problemEntityService.page(iPage, queryWrapper);
        }
        return problemList;
    }

    public Problem getProblem(Long pid) throws StatusForbiddenException, StatusFailException {
        Problem problem = problemEntityService.getById(pid);

        if (problem != null) { // 查询成功
            // 获取当前登录的用户
            AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();

            boolean isRoot = SecurityUtils.getSubject().hasRole("root");
            boolean isProblemAdmin = SecurityUtils.getSubject().hasRole("problem_admin");
            // 只有超级管理员和题目管理员、题目创建者才能操作
            if (!isRoot && !isProblemAdmin && !userRolesVo.getUsername().equals(problem.getAuthor())) {
                throw new StatusForbiddenException("对不起，你无权限查看题目！");
            }

            return problem;
        } else {
            throw new StatusFailException("查询失败！");
        }
    }

    public void deleteProblem(Long pid) throws StatusFailException {
        boolean isOk = problemEntityService.removeById(pid);
        /*
        problem的id为其他表的外键的表中的对应数据都会被一起删除！
         */
        if (isOk) { // 删除成功
            FileUtil.del(Constants.File.TESTCASE_BASE_FOLDER.getPath() + File.separator + "problem_" + pid);
            AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();
            log.info("[{}],[{}],pid:[{}],operatorUid:[{}],operatorUsername:[{}]",
                    "Admin_Problem", "Delete", pid, userRolesVo.getUid(), userRolesVo.getUsername());
        } else {
            throw new StatusFailException("删除失败！");
        }
    }

    public void addProblem(ProblemDTO problemDto) throws StatusFailException {

        problemValidator.validateProblem(problemDto.getProblem());

        QueryWrapper<Problem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("problem_id", problemDto.getProblem().getProblemId().toUpperCase());
        Problem problem = problemEntityService.getOne(queryWrapper);
        if (problem != null) {
            throw new StatusFailException("该题目的Problem ID已存在，请更换！");
        }

        boolean isOk = problemEntityService.adminAddProblem(problemDto);
        if (!isOk) {
            throw new StatusFailException("添加失败");
        }
        // 获取当前登录的用户
        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();

        log.info("[{}],[{}],problemDtoId:[{}],operatorUid:[{}],operatorUsername:[{}]",
                "Admin_Problem", "Add_Problem", problemDto.getProblem().getProblemId().toUpperCase(), userRolesVo.getUid(), userRolesVo.getUsername());
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateProblem(ProblemDTO problemDto) throws StatusForbiddenException, StatusFailException {

        problemValidator.validateProblemUpdate(problemDto.getProblem());

        // 获取当前登录的用户
        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();

        boolean isRoot = SecurityUtils.getSubject().hasRole("root");
        boolean isProblemAdmin = SecurityUtils.getSubject().hasRole("problem_admin");
        // 只有超级管理员和题目管理员、题目创建者才能操作
        if (!isRoot && !isProblemAdmin && !userRolesVo.getUsername().equals(problemDto.getProblem().getAuthor())) {
            throw new StatusForbiddenException("对不起，你无权限修改题目！");
        }

        String problemId = problemDto.getProblem().getProblemId().toUpperCase();
        QueryWrapper<Problem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("problem_id", problemId);
        Problem problem = problemEntityService.getOne(queryWrapper);

        // 如果problem_id不是原来的且已存在该problem_id，则修改失败！
        if (problem != null && problem.getId().longValue() != problemDto.getProblem().getId()) {
            throw new StatusFailException("当前的Problem ID 已被使用，请重新更换新的！");
        }

        // 记录修改题目的用户
        problemDto.getProblem().setModifiedUser(userRolesVo.getUsername());

        boolean result = problemEntityService.adminUpdateProblem(problemDto);
        if (result) { // 更新成功
            if (problem == null) { // 说明改了problemId，同步一下judge表
                UpdateWrapper<Judge> judgeUpdateWrapper = new UpdateWrapper<>();
                judgeUpdateWrapper.eq("pid", problemDto.getProblem().getId())
                        .set("display_pid", problemId);
                judgeEntityService.update(judgeUpdateWrapper);
            }
            log.info("[{}],[{}],problemId:[{}],operatorUid:[{}],operatorUsername:[{}]",
                    "Admin_Problem", "Update_Problem", problemId, userRolesVo.getUid(), userRolesVo.getUsername());
        } else {
            throw new StatusFailException("修改失败");
        }
    }

    public List<ProblemCase> getProblemCases(Long pid, Boolean isUpload) {
        QueryWrapper<ProblemCase> problemCaseQueryWrapper = new QueryWrapper<>();
        problemCaseQueryWrapper.eq("pid", pid).eq("status", 0);
        if (isUpload) {
            problemCaseQueryWrapper.last("order by length(input) asc,input asc");
        }
        return problemCaseEntityService.list(problemCaseQueryWrapper);
    }

    public CommonResult compileSpj(CompileDTO compileDTO) {
        if (StringUtils.isEmpty(compileDTO.getCode()) ||
                StringUtils.isEmpty(compileDTO.getLanguage())) {
            return CommonResult.errorResponse("参数不能为空！");
        }

        compileDTO.setToken(judgeToken);
        return dispatcher.dispatch(Constants.TaskType.COMPILE_SPJ, compileDTO);
    }

    public CommonResult compileInteractive(CompileDTO compileDTO) {
        if (StringUtils.isEmpty(compileDTO.getCode()) ||
                StringUtils.isEmpty(compileDTO.getLanguage())) {
            return CommonResult.errorResponse("参数不能为空！");
        }

        compileDTO.setToken(judgeToken);
        return dispatcher.dispatch(Constants.TaskType.COMPILE_INTERACTIVE, compileDTO);
    }

    @Transactional(rollbackFor = Exception.class)
    public void importRemoteOJProblem(String name, String problemId) throws StatusFailException {
        QueryWrapper<Problem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("problem_id", name.toUpperCase() + "-" + problemId);
        Problem problem = problemEntityService.getOne(queryWrapper);
        if (problem != null) {
            throw new StatusFailException("该题目已添加，请勿重复添加！");
        }

        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();
        try {
            ProblemStrategy.RemoteProblemInfo otherOJProblemInfo = remoteProblemManager.getOtherOJProblemInfo(name.toUpperCase(), problemId, userRolesVo.getUsername());
            if (otherOJProblemInfo != null) {
                Problem importProblem = remoteProblemManager.adminAddOtherOJProblem(otherOJProblemInfo, name);
                if (importProblem == null) {
                    throw new StatusFailException("导入新题目失败！请重新尝试！");
                }
            } else {
                throw new StatusFailException("导入新题目失败！原因：可能是与该OJ链接超时或题号格式错误！");
            }
        } catch (Exception e) {
            throw new StatusFailException(e.getMessage());
        }
    }

    public void changeProblemAuth(Problem problem) throws StatusFailException, StatusForbiddenException {
        // 普通管理员只能将题目变成隐藏题目和比赛题目
        boolean root = SecurityUtils.getSubject().hasRole("root");

        boolean problemAdmin = SecurityUtils.getSubject().hasRole("problem_admin");

        if (!problemAdmin && !root && problem.getAuth() == 1) {
            throw new StatusForbiddenException("修改失败！你无权限公开题目！");
        }

        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();

        UpdateWrapper<Problem> problemUpdateWrapper = new UpdateWrapper<>();
        problemUpdateWrapper.eq("id", problem.getId())
                .set("auth", problem.getAuth())
                .set("modified_user", userRolesVo.getUsername());

        boolean isOk = problemEntityService.update(problemUpdateWrapper);
        if (!isOk) {
            throw new StatusFailException("修改失败");
        }
    }

    public void changeProblemType(Problem problem) throws StatusFailException, StatusForbiddenException {
        // 普通管理员只能将题目变成隐藏题目和比赛题目
        boolean root = SecurityUtils.getSubject().hasRole("root");

        boolean problemAdmin = SecurityUtils.getSubject().hasRole("problem_admin");

        if(problem.getIsRemote()){
            throw new StatusForbiddenException("远程题目不能修改！");
        }

        if (!problemAdmin && !root) {
            throw new StatusForbiddenException("您无权修改！");
        }

        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();

        UpdateWrapper<Problem> problemUpdateWrapper = new UpdateWrapper<>();
        problemUpdateWrapper.eq("id", problem.getId())
                .set("type", problem.getType())
                .set("modified_user", userRolesVo.getUsername());

        if(problem.getType() == 1){
            // 查找对应题目的 case列表
            QueryWrapper<ProblemCase> problemCaseQueryWrapper = new QueryWrapper<>();
            problemCaseQueryWrapper.eq("pid", problem.getId()).eq("status", 0);
            List<ProblemCase> problemCaseList = problemCaseEntityService.list(problemCaseQueryWrapper);
            // 需要修改的case列表
            List<ProblemCase> needUpdateProblemCaseList = new LinkedList<>();
            // 修改 case列表 的 score
            int length = problemCaseList.size();
            int aver = 100 / length;
            int add_1_num = 100 - aver * length;
            int cntLen = 0;
            for(ProblemCase problemCase : problemCaseList){
                if (cntLen >= length - add_1_num) {
                    problemCase.setScore(aver + 1);
                }else {
                    problemCase.setScore(aver);
                }
                needUpdateProblemCaseList.add(problemCase);
                cntLen++;
            }
            // 执行批量修改操作
            boolean isOk = problemCaseEntityService.saveOrUpdateBatch(needUpdateProblemCaseList);
            if (!isOk) {
                throw new StatusFailException("修改失败");
            }
        }
        boolean isOk = problemEntityService.update(problemUpdateWrapper);
        if (!isOk) {
            throw new StatusFailException("修改失败");
        }
    }

    public void changeProblemsAuth(List<Long> problemIdList, Integer problemAuth) throws StatusFailException, StatusForbiddenException {
        // 普通管理员只能将题目变成隐藏题目和比赛题目
        boolean root = SecurityUtils.getSubject().hasRole("root");

        boolean problemAdmin = SecurityUtils.getSubject().hasRole("problem_admin");

        if (!problemAdmin && !root) {
            throw new StatusForbiddenException("您无权修改！");
        }

        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();
        UpdateWrapper<Problem> problemUpdateWrapper = new UpdateWrapper<>();
        problemUpdateWrapper.in("id", problemIdList)
                .set("auth", problemAuth)
                .set("modified_user", userRolesVo.getUsername());

        boolean isOk = problemEntityService.update(problemUpdateWrapper);
        if (!isOk) {
            throw new StatusFailException("修改失败");
        }
    }

    public void changeProblemsType(List<Long> problemIdList, Integer problemType) throws StatusFailException, StatusForbiddenException {

        boolean root = SecurityUtils.getSubject().hasRole("root");

        boolean problemAdmin = SecurityUtils.getSubject().hasRole("problem_admin");

        if (!problemAdmin && !root) {
            throw new StatusForbiddenException("您无权修改！");
        }
        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();
        UpdateWrapper<Problem> problemUpdateWrapper = new UpdateWrapper<>();

        problemUpdateWrapper.in("id", problemIdList)
                .eq("is_remote",false)
                .set("type", problemType)
                .set("modified_user", userRolesVo.getUsername());

        if (problemType == 1){
            // 需要修改的case列表
            List<ProblemCase> needUpdateProblemCaseList = new LinkedList<>();
            for(Object pid : problemIdList){
                // 查找对应题目的 case列表
                QueryWrapper<ProblemCase> problemCaseQueryWrapper = new QueryWrapper<>();
                problemCaseQueryWrapper.eq("pid", pid).eq("status", 0);
                List<ProblemCase> problemCaseList = problemCaseEntityService.list(problemCaseQueryWrapper);
                // 修改 case列表 的 score
                int length = problemCaseList.size();
                int aver = 100 / length;
                int add_1_num = 100 - aver * length;
                int cntLen = 0;
                for(ProblemCase problemCase : problemCaseList){
                    if (cntLen >= length - add_1_num) {
                        problemCase.setScore(aver + 1);
                    }else {
                        problemCase.setScore(aver);
                    }
                    needUpdateProblemCaseList.add(problemCase);
                    cntLen++;
                }
            }
            // 执行批量修改操作
            boolean isOk = problemCaseEntityService.saveOrUpdateBatch(needUpdateProblemCaseList);
            if (!isOk) {
                throw new StatusFailException("修改失败");
            }
        }
        boolean isOk = problemEntityService.update(problemUpdateWrapper);
        if (!isOk) {
            throw new StatusFailException("修改失败");
        }
    }


}