package top.hcode.hoj.manager.file;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileReader;
import cn.hutool.core.io.file.FileWriter;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ZipUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;
import top.hcode.hoj.annotation.HOJAccessEnum;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.exception.StatusForbiddenException;
import top.hcode.hoj.common.exception.StatusSystemErrorException;
import top.hcode.hoj.common.result.ResultStatus;
import top.hcode.hoj.dao.judge.JudgeCaseEntityService;
import top.hcode.hoj.dao.judge.JudgeEntityService;
import top.hcode.hoj.dao.problem.ProblemCaseEntityService;
import top.hcode.hoj.dao.problem.ProblemEntityService;
import top.hcode.hoj.exception.AccessException;
import top.hcode.hoj.pojo.entity.judge.Judge;
import top.hcode.hoj.pojo.entity.judge.JudgeCase;
import top.hcode.hoj.pojo.entity.problem.Problem;
import top.hcode.hoj.pojo.entity.problem.ProblemCase;
import top.hcode.hoj.shiro.AccountProfile;
import top.hcode.hoj.utils.Constants;
import top.hcode.hoj.utils.RedisUtils;
import top.hcode.hoj.validator.AccessValidator;
import top.hcode.hoj.validator.GroupValidator;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/10 14:57
 * @Description:
 */
@Component
@Slf4j(topic = "hoj")
public class TestCaseManager {
    @Autowired
    private JudgeEntityService judgeEntityService;

    @Autowired
    private ProblemCaseEntityService problemCaseEntityService;

    @Autowired
    private JudgeCaseEntityService judgeCaseEntityService;

    @Autowired
    private ProblemEntityService problemEntityService;

    @Autowired
    private AccessValidator accessValidator;

    @Autowired
    private GroupValidator groupValidator;

    @Resource
    private RedisUtils redisUtils;

    public Map<Object, Object> uploadTestcaseZip(MultipartFile file, Long gid, String mode) throws StatusFailException, StatusSystemErrorException, StatusForbiddenException {
        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();

        boolean isRoot = SecurityUtils.getSubject().hasRole("root");
        boolean isProblemAdmin = SecurityUtils.getSubject().hasRole("problem_admin");
        boolean isAdmin = SecurityUtils.getSubject().hasRole("admin");

        if (!isRoot && !isProblemAdmin && !isAdmin
                && !(gid != null && groupValidator.isGroupAdmin(userRolesVo.getUid(), gid))) {
            throw new StatusForbiddenException("对不起，您无权限操作！");
        }

        //获取文件后缀
        String suffix = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1);
        if (!"zip".toUpperCase().contains(suffix.toUpperCase())) {
            throw new StatusFailException("请上传zip格式的测试数据压缩包！");
        }
        String fileDirId = IdUtil.simpleUUID();
        String fileDir = Constants.File.TESTCASE_TMP_FOLDER.getPath() + File.separator + fileDirId;
        String filePath = fileDir + File.separator + file.getOriginalFilename();
        // 文件夹不存在就新建
        FileUtil.mkdir(fileDir);
        try {
            file.transferTo(new File(filePath));
        } catch (IOException e) {
            log.error("评测数据文件上传异常-------------->{}", e.getMessage());
            throw new StatusSystemErrorException("服务器异常：评测数据上传失败！");
        }

        // 将压缩包压缩到指定文件夹
        ZipUtil.unzip(filePath, fileDir);
        // 删除zip文件
        FileUtil.del(filePath);
        // 检查文件是否存在
        File testCaseFileList = new File(fileDir);
        File[] files = testCaseFileList.listFiles();
        if (files == null || files.length == 0) {
            FileUtil.del(fileDir);
            throw new StatusFailException("评测数据压缩包里文件不能为空！");
        }

        HashMap<String, String> inputData = new HashMap<>();
        HashMap<String, String> outputData = new HashMap<>();

        // 遍历读取与检查是否in和out文件一一对应，否则报错
        for (File tmp : files) {
            String tmpPreName = null;
            if (tmp.getName().endsWith(".in")) {
                tmpPreName = tmp.getName().substring(0, tmp.getName().lastIndexOf(".in"));
                inputData.put(tmpPreName, tmp.getName());
            } else if (tmp.getName().endsWith(".out")) {
                tmpPreName = tmp.getName().substring(0, tmp.getName().lastIndexOf(".out"));
                outputData.put(tmpPreName, tmp.getName());
            } else if (tmp.getName().endsWith(".ans")) {
                tmpPreName = tmp.getName().substring(0, tmp.getName().lastIndexOf(".ans"));
                outputData.put(tmpPreName, tmp.getName());
            } else if (tmp.getName().endsWith(".txt")) {
                tmpPreName = tmp.getName().substring(0, tmp.getName().lastIndexOf(".txt"));
                if (tmpPreName.contains("input")) {
                    inputData.put(tmpPreName.replaceAll("input", "$*$"), tmp.getName());
                } else if (tmpPreName.contains("output")) {
                    outputData.put(tmpPreName.replaceAll("output", "$*$"), tmp.getName());
                }
            }
        }

        // 进行数据对应检查,同时生成返回数据
        List<HashMap<String, Object>> problemCaseList = new LinkedList<>();
        for (String key : inputData.keySet()) {
            HashMap<String, Object> testcaseMap = new HashMap<>();
            String inputFileName = inputData.get(key);
            testcaseMap.put("input", inputFileName);

            // 若有名字对应的out文件不存在的，直接生成对应的out文件
            String oriOutputFileName = outputData.getOrDefault(key, null);
            if (oriOutputFileName == null) {
                oriOutputFileName = key + ".out";
                if (inputFileName.endsWith(".txt")) {
                    oriOutputFileName = inputFileName.replaceAll("input", "output");
                }
                FileWriter fileWriter = new FileWriter(fileDir + File.separator + oriOutputFileName);
                fileWriter.write("");
            }

            testcaseMap.put("output", oriOutputFileName);
            if (Objects.equals(Constants.JudgeCaseMode.SUBTASK_LOWEST.getMode(), mode)
                    || Objects.equals(Constants.JudgeCaseMode.SUBTASK_AVERAGE.getMode(), mode)) {
                testcaseMap.put("groupNum", 1);
            }
            problemCaseList.add(testcaseMap);
        }

        List<HashMap<String, Object>> fileList = problemCaseList.stream()
                .sorted((o1, o2) -> {
                    String input1 = (String) o1.get("input");
                    String input2 = (String) o2.get("input");
                    String a = input1.split("\\.")[0];
                    String b = input2.split("\\.")[0];
                    if (a.length() > b.length()) {
                        return 1;
                    } else if (a.length() < b.length()) {
                        return -1;
                    }
                    return a.compareTo(b);
                })
                .collect(Collectors.toList());

        log.info("[{}],[{}],gid:[{}],operatorUid:[{}],operatorUsername:[{}]",
                "Upload_Testcase_Zip", "Insert", gid, userRolesVo.getUid(), userRolesVo.getUsername());

        return MapUtil.builder()
                .put("fileList", fileList)
                .put("fileListDir", fileDir)
                .map();
    }


    public void downloadTestcase(Long pid, HttpServletResponse response) throws StatusFailException, StatusForbiddenException {
        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();

        boolean isRoot = SecurityUtils.getSubject().hasRole("root");
        boolean isProblemAdmin = SecurityUtils.getSubject().hasRole("problem_admin");

        Problem problem = problemEntityService.getById(pid);

        Long gid = problem.getGid();

        if (gid != null) {
            if (!isRoot && !problem.getAuthor().equals(userRolesVo.getUsername())
                    && !groupValidator.isGroupMember(userRolesVo.getUid(), gid)) {
                throw new StatusForbiddenException("对不起，您无权限操作！");
            }
        } else {
            if (!isRoot && !isProblemAdmin && !problem.getAuthor().equals(userRolesVo.getUsername())) {
                throw new StatusForbiddenException("对不起，您无权限操作！");
            }
        }

        String lockKey = Constants.Account.DOWNLOAD_TESTCASE_NUM + userRolesVo.getUid();
        Integer DownloadCount = (Integer) redisUtils.get(lockKey);
        if (isProblemAdmin && DownloadCount != null && DownloadCount >= 5) {
            throw new StatusFailException("对不起！今日下载次数已经超过 5 次！请明天再进行下载！");
        }

        String workDir = Constants.File.TESTCASE_BASE_FOLDER.getPath() + File.separator + "problem_" + pid;
        File file = new File(workDir);

        if (!file.exists()) { // 本地为空 尝试去数据库查找
            QueryWrapper<ProblemCase> problemCaseQueryWrapper = new QueryWrapper<>();
            problemCaseQueryWrapper.eq("pid", pid);
            List<ProblemCase> problemCaseList = problemCaseEntityService.list(problemCaseQueryWrapper);

            if (CollectionUtils.isEmpty(problemCaseList)) {
                throw new StatusFailException("对不起，该题目的评测数据为空！");
            }

            boolean hasTestCase = true;
            if (problemCaseList.get(0).getInput().endsWith(".in") && (problemCaseList.get(0).getOutput().endsWith(".out") ||
                    problemCaseList.get(0).getOutput().endsWith(".ans"))) {
                hasTestCase = false;
            }
            if (!hasTestCase) {
                throw new StatusFailException("对不起，该题目的评测数据为空！");
            }

            FileUtil.mkdir(workDir);
            // 写入本地
            for (int i = 0; i < problemCaseList.size(); i++) {
                String filePreName = workDir + File.separator + (i + 1);
                String inputName = filePreName + ".in";
                String outputName = filePreName + ".out";
                FileWriter infileWriter = new FileWriter(inputName);
                infileWriter.write(problemCaseList.get(i).getInput());
                FileWriter outfileWriter = new FileWriter(outputName);
                outfileWriter.write(problemCaseList.get(i).getOutput());
            }
        }

        String fileName = "problem_" + pid + "_testcase_" + System.currentTimeMillis() + ".zip";
        // 将对应文件夹的文件压缩成zip
        ZipUtil.zip(workDir, Constants.File.FILE_DOWNLOAD_TMP_FOLDER.getPath() + File.separator + fileName);
        // 将zip变成io流返回给前端
        FileReader fileReader = new FileReader(Constants.File.FILE_DOWNLOAD_TMP_FOLDER.getPath() + File.separator + fileName);
        BufferedInputStream bins = new BufferedInputStream(fileReader.getInputStream());//放到缓冲流里面
        OutputStream outs = null;//获取文件输出IO流
        BufferedOutputStream bouts = null;
        try {
            outs = response.getOutputStream();
            bouts = new BufferedOutputStream(outs);
            response.setContentType("application/x-download");
            response.setHeader("Content-disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
            int bytesRead = 0;
            byte[] buffer = new byte[1024 * 10];
            //开始向网络传输文件流
            while ((bytesRead = bins.read(buffer, 0, 1024 * 10)) != -1) {
                bouts.write(buffer, 0, bytesRead);
            }
            bouts.flush();
        } catch (IOException e) {
            log.error("下载题目测试数据的压缩文件异常------------>{}", e.getMessage());
            response.reset();
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
            Map<String, Object> map = new HashMap<>();
            map.put("status", ResultStatus.SYSTEM_ERROR);
            map.put("msg", "下载文件失败，请重新尝试！");
            map.put("data", null);
            try {
                response.getWriter().println(JSONUtil.toJsonStr(map));
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        } finally {
            try {
                bins.close();
                if (outs != null) {
                    outs.close();
                }
                if (bouts != null) {
                    bouts.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            Date currentDate = new Date(); // 获取当前时间
            Calendar calendar = Calendar.getInstance(); // 创建Calendar对象
            calendar.setTime(currentDate); // 设置Calendar对象的时间为当前时间

            int hour = calendar.get(Calendar.HOUR_OF_DAY); // 获取当前小时
            int minute = calendar.get(Calendar.MINUTE); // 获取当前分钟
            int second = calendar.get(Calendar.SECOND); // 获取当前秒钟
            int totalSeconds = hour * 360 + minute * 60 + second;

            if (DownloadCount == null) {
                redisUtils.set(lockKey, 1, 86400 - totalSeconds);
            } else {
                redisUtils.set(lockKey, DownloadCount + 1, 86400 - totalSeconds);
            }
            log.info("[{}],[{}],pid:[{}],operatorUid:[{}],operatorUsername:[{}]",
                    "Test_Case", "Download", pid, userRolesVo.getUid(), userRolesVo.getUsername());
            // 清空临时文件
            FileUtil.del(Constants.File.FILE_DOWNLOAD_TMP_FOLDER.getPath() + File.separator + fileName);
        }
    }

    public void downloadWrongCaseTest(Long pid, String caseIn, String caseOut, HttpServletResponse response) throws StatusFailException, StatusForbiddenException {

        // 压缩到的位置
        String fileName = "problem_" + pid + "_testcase_" + System.currentTimeMillis() + ".zip";
        File zipFile = new File(Constants.File.FILE_DOWNLOAD_TMP_FOLDER.getPath() + File.separator + fileName);

        // 压缩文件中包含的文件列表
        String workDir1 = Constants.File.TESTCASE_BASE_FOLDER.getPath() + File.separator + "problem_" + pid + File.separator + caseIn;
        String workDir2 = Constants.File.TESTCASE_BASE_FOLDER.getPath() + File.separator + "problem_" + pid + File.separator + caseOut;
        List<File> fileList = CollUtil.newArrayList();
        fileList.add(new File(workDir1));
        fileList.add(new File(workDir2));

        // 压缩多个文件,压缩后会将压缩临时文件删除
        ZipUtil.zip(zipFile, false, fileList.toArray(new File[fileList.size()]));

        // 将zip变成io流返回给前端
        FileReader fileReader = new FileReader(Constants.File.FILE_DOWNLOAD_TMP_FOLDER.getPath() + File.separator + fileName);
        BufferedInputStream bins = new BufferedInputStream(fileReader.getInputStream());//放到缓冲流里面
        OutputStream outs = null;//获取文件输出IO流
        BufferedOutputStream bouts = null;
        try {
            outs = response.getOutputStream();
            bouts = new BufferedOutputStream(outs);
            response.setContentType("application/x-download");
            response.setHeader("Content-disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
            int bytesRead = 0;
            byte[] buffer = new byte[1024 * 10];
            //开始向网络传输文件流
            while ((bytesRead = bins.read(buffer, 0, 1024 * 10)) != -1) {
                bouts.write(buffer, 0, bytesRead);
            }
            bouts.flush();
        } catch (IOException e) {
            log.error("下载题目测试数据的压缩文件异常------------>{}", e.getMessage());
            response.reset();
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
            Map<String, Object> map = new HashMap<>();
            map.put("status", ResultStatus.SYSTEM_ERROR);
            map.put("msg", "下载文件失败，请重新尝试！");
            map.put("data", null);
            try {
                response.getWriter().println(JSONUtil.toJsonStr(map));
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        } finally {
            try {
                bins.close();
                if (outs != null) {
                    outs.close();
                }
                if (bouts != null) {
                    bouts.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            // 清空临时文件
            FileUtil.del(Constants.File.FILE_DOWNLOAD_TMP_FOLDER.getPath() + File.separator + fileName);
        }
    }

    public void downloadWrongCase(Long submitId, HttpServletResponse response) throws StatusFailException, StatusForbiddenException {

        Judge judge = judgeEntityService.getById(submitId);

        Long pid = judge.getPid();
        String workDir = Constants.File.TESTCASE_BASE_FOLDER.getPath() + File.separator + "problem_" + pid;
        File file = new File(workDir);
        if (!file.exists()) { // 本地为空，数据库数据懒得处理
            throw new StatusFailException("对不起，该题目的评测数据为空，或者不支持下载！");
        }

        if (judge.getCid() == 0) {
            // 比赛外的提交代码，需要检查网站是否开启隐藏代码功能
            try {
                accessValidator.validateAccess(HOJAccessEnum.HIDE_NON_CONTEST_SUBMISSION_CODE);
            } catch (AccessException e) {
                throw new StatusFailException("当前开启了隐藏非比赛提交代码不显示的功能，不支持下载查看！");
            }

            Problem problem = problemEntityService.getById(pid);
            // 如果该题不支持开放测试点结果查看
            if (!problem.getOpenCaseResult()) {
                throw new StatusFailException("该题未公开评测点数据结果，不支持下载查看！");
            }

            AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();
            boolean isRoot = SecurityUtils.getSubject().hasRole("root");
            boolean isProblemAdmin = SecurityUtils.getSubject().hasRole("problem_admin");
            boolean isAdmin = SecurityUtils.getSubject().hasRole("admin");

            if(!userRolesVo.getUid().equals(judge.getUid())){
                throw new StatusFailException("不能通过他人提交信息，下载测试数据！");
            }

            String lockKey = Constants.Account.DOWNLOAD_TESTCASE_NUM + userRolesVo.getUid();
            Integer DownloadCount = (Integer) redisUtils.get(lockKey);
            // 普通用户只能一天下载 2 次
            if (!isRoot && !isProblemAdmin && !isAdmin && DownloadCount != null && DownloadCount >= 2) {
                throw new StatusFailException("对不起！今日下载次数已经超过 2 次！请明天再进行下载！");
            }
            // 除超管以外，其他管理员只能一天下载 5 次
            if ((isProblemAdmin || isAdmin) && DownloadCount != null && DownloadCount >= 5) {
                throw new StatusFailException("对不起！今日下载次数已经超过 5 次！请明天再进行下载！");
            }

            QueryWrapper<JudgeCase> judgeCaseQueryWrapper = new QueryWrapper<>();
            judgeCaseQueryWrapper.eq("submit_id", submitId).orderByAsc("seq");;
            List<JudgeCase> judgeCaseList = judgeCaseEntityService.list(judgeCaseQueryWrapper);

            if (CollectionUtils.isEmpty(judgeCaseList)) {
                throw new StatusFailException("提交的错误类型不支持下载查看！");
            }

            Integer idxWrong = -1;
            String caseIn = null, caseOut = null;
            for (JudgeCase judgeCase : judgeCaseList) {
                // 排除 AC
                if (judgeCase.getStatus() != 0) {
                    idxWrong = judgeCase.getSeq();
                    caseIn = judgeCase.getInputData();
                    caseOut = judgeCase.getOutputData();
                    break;
                }
            }
            if (idxWrong == -1) {
                throw new StatusFailException("对不起！该提交没有未通过的测试点！不支持下载");
            }

            // 开始下载
            downloadWrongCaseTest(pid, caseIn, caseOut, response);

            Date currentDate = new Date(); // 获取当前时间
            Calendar calendar = Calendar.getInstance(); // 创建Calendar对象
            calendar.setTime(currentDate); // 设置Calendar对象的时间为当前时间

            int hour = calendar.get(Calendar.HOUR_OF_DAY); // 获取当前小时
            int minute = calendar.get(Calendar.MINUTE); // 获取当前分钟
            int second = calendar.get(Calendar.SECOND); // 获取当前秒钟
            int totalSeconds = hour * 360 + minute * 60 + second;

            if (DownloadCount == null) {
                redisUtils.set(lockKey, 1, 86400 - totalSeconds);
            } else {
                redisUtils.set(lockKey, DownloadCount + 1, 86400 - totalSeconds);
            }
        } else {
            throw new StatusFailException("对不起，比赛中不能下载查看！");
        }
    }
}