package top.hcode.hoj.manager.admin.discussion;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.dao.discussion.DiscussionEntityService;
import top.hcode.hoj.dao.discussion.DiscussionReportEntityService;
import top.hcode.hoj.pojo.entity.discussion.Discussion;
import top.hcode.hoj.pojo.entity.discussion.DiscussionReport;
import top.hcode.hoj.pojo.vo.DiscussionReportVO;
import top.hcode.hoj.shiro.AccountProfile;

import java.util.List;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/9 16:02
 * @Description:
 */
@Component
@Slf4j(topic = "hoj")
public class AdminDiscussionManager {

    @Autowired
    private DiscussionEntityService discussionEntityService;

    @Autowired
    private DiscussionReportEntityService discussionReportEntityService;

    public void updateDiscussion(Discussion discussion) throws StatusFailException {
        if(discussion.getPid() == null && discussion.getIsExplain()){
            throw new StatusFailException("需要绑定题目后才能设置题解精选");
        }
        boolean isOk = discussionEntityService.updateById(discussion);
        if (!isOk) {
            throw new StatusFailException("修改失败");
        }
        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();
        log.info("[{}],[{}],discussionID:[{}],operatorUid:[{}],operatorUsername:[{}]",
                "Admin_Discussion", "Update", discussion, userRolesVo.getUid(), userRolesVo.getUsername());
    }

    public void removeDiscussion(List<Integer> didList) throws StatusFailException {
        boolean isOk = discussionEntityService.removeByIds(didList);
        if (!isOk) {
            throw new StatusFailException("删除失败");
        }
        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();
        log.info("[{}],[{}],didList:[{}],operatorUid:[{}],operatorUsername:[{}]",
                "Admin_Discussion", "Delete", didList, userRolesVo.getUid(), userRolesVo.getUsername());
    }

    public IPage<DiscussionReportVO> getDiscussionReport(Integer limit, Integer currentPage) {
        return discussionReportEntityService.getDiscussionReportList(limit, currentPage);
    }

    public void updateDiscussionReport(DiscussionReport discussionReport) throws StatusFailException {
        boolean isOk = discussionReportEntityService.updateById(discussionReport);
        if (!isOk) {
            throw new StatusFailException("修改失败");
        }
        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();
        log.info("[{}],[{}],discussionReportID:[{}],operatorUid:[{}],operatorUsername:[{}]",
                "Admin_Discussion", "Update", discussionReport, userRolesVo.getUid(), userRolesVo.getUsername());

    }

}