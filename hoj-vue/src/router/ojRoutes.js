import Home from "@/views/oj/Home.vue";
import SetNewPassword from "@/views/oj/user/SetNewPassword.vue";
import UserHome from "@/views/oj/user/UserHome.vue";
import Setting from "@/views/oj/user/Setting.vue";
import ProblemLIst from "@/views/oj/problem/ProblemList.vue";
import Logout from "@/views/oj/user/Logout.vue";
import SubmissionList from "@/views/oj/status/SubmissionList.vue";
import SubmissionDetails from "@/views/oj/status/SubmissionDetails.vue";
import ContestList from "@/views/oj/contest/ContestList.vue";
import Problem from "@/views/oj/problem/Problem.vue";
import ACMRank from "@/views/oj/rank/ACMRank.vue";
import OIRank from "@/views/oj/rank/OIRank.vue";
import ContestDetails from "@/views/oj/contest/ContestDetails.vue";
import ACMScoreBoard from "@/views/oj/contest/outside/ACMScoreBoard.vue";
import OIScoreBoard from "@/views/oj/contest/outside/OIScoreBoard.vue";
import ContestProblemList from "@/views/oj/contest/children/ContestProblemList.vue";
import ContestRank from "@/views/oj/contest/children/ContestRank.vue";
import ACMInfoAdmin from "@/views/oj/contest/children/ACMInfoAdmin.vue";
import Announcements from "@/components/oj/common/Announcements.vue";
import ContestComment from "@/views/oj/contest/children/ContestComment.vue";
import ContestPrint from "@/views/oj/contest/children/ContestPrint.vue";
import ContestAdminPrint from "@/views/oj/contest/children/ContestAdminPrint.vue";
import ScrollBoard from "@/views/oj/contest/children/ScrollBoard.vue";
import ContestRejudgeAdmin from "@/views/oj/contest/children/ContestRejudgeAdmin.vue";
import DiscussionList from "@/views/oj/discussion/discussionList.vue";
import Discussion from "@/views/oj/discussion/discussion.vue";
import Introduction from "@/views/oj/about/Introduction.vue";
import Developer from "@/views/oj/about/Developer.vue";
import Message from "@/views/oj/message/message.vue";
import UserMsg from "@/views/oj/message/UserMsg.vue";
import SysMsg from "@/views/oj/message/SysMsg.vue";
import TrainingList from "@/views/oj/training/TrainingList.vue";
import TrainingDetails from "@/views/oj/training/TrainingDetails.vue";
import TrainingProblemList from "@/views/oj/training/TrainingProblemList.vue";
import TrainingRank from "@/views/oj/training/TrainingRank.vue";
import GroupList from "@/views/oj/group/GroupList.vue";
import GroupDetails from "@/views/oj/group/GroupDetails.vue";
import GroupAnnouncementList from "@/views/oj/group/children/GroupAnnouncementList.vue";
import GroupProblemList from "@/views/oj/group/children/GroupProblemList.vue";
import GroupTrainingList from "@/views/oj/group/children/GroupTrainingList.vue";
import GroupContestList from "@/views/oj/group/children/GroupContestList.vue";
import GroupDiscussionList from "@/views/oj/group/children/GroupDiscussionList.vue";
import GroupMemberList from "@/views/oj/group/children/GroupMemberList.vue";
import GroupSetting from "@/views/oj/group/children/GroupSetting.vue";
import GroupRank from "@/views/oj/group/children/GroupRank.vue";
import NotFound from "@/views/404.vue";
import LoginHome from "@/components/oj/common/LoginHome";

const ojRoutes = [
  {
    path: "/",
    name: "LoginHome",
    component: LoginHome,
    meta: { title: "LoginHome" },
  },
  {
    path: "/login",
    name: "LoginHome",
    component: LoginHome,
    meta: { title: "LoginHome" },
  },
  {
    path: "/home",
    name: "Home",
    component: Home,
    meta: { title: "Home", requireAuth: true },
  },
  {
    path: "/problem",
    name: "ProblemList",
    component: ProblemLIst,
    meta: { title: "Problem", requireAuth: true },
  },
  {
    path: "/problem/:problemID",
    name: "ProblemDetails",
    component: Problem,
    meta: { title: "Problem Details", requireAuth: true },
  },
  {
    name: "TrainingFullProblemDetails",
    path: "/training/:trainingID/problem/:problemID/full-screen",
    component: Problem,
    meta: {
      title: "Training Problem Details",
      fullScreenSource: "training",
      requireAuth: true,
    },
  },
  {
    name: "ContestFullProblemDetails",
    path: "/contest/:contestID/problem/:problemID/full-screen",
    component: Problem,
    meta: {
      title: "Contest Problem Details",
      fullScreenSource: "contest",
      requireAuth: true,
    },
  },
  {
    name: "GroupFullProblemDetails",
    path: "/group/:groupID/problem/:problemID/full-screen",
    component: Problem,
    meta: {
      title: "Group Problem Details",
      fullScreenSource: "group",
      requireAuth: true,
    },
  },
  {
    name: "GroupTrainingFullProblemDetails",
    path: "/group/:groupID/training/:trainingID/problem/:problemID/full-screen",
    component: Problem,
    meta: {
      title: "Group Training Problem Details",
      fullScreenSource: "training",
      requireAuth: true,
    },
  },
  {
    path: "/training",
    name: "TrainingList",
    component: TrainingList,
    meta: { title: "Training", requireAuth: true },
  },
  {
    name: "TrainingDetails",
    path: "/training/:trainingID/",
    component: TrainingDetails,
    meta: {
      title: "Training Details",
      fullScreenSource: "training",
      requireAuth: true,
    },
    children: [
      {
        name: "TrainingProblemList",
        path: "problems",
        component: TrainingProblemList,
        meta: {
          title: "Training Problem",
          fullScreenSource: "training",
          requireAuth: true,
        },
      },
      {
        name: "TrainingProblemDetails",
        path: "problem/:problemID",
        component: Problem,
        meta: {
          title: "Training Problem Details",
          fullScreenSource: "training",
          requireAuth: true,
        },
      },
      {
        name: "TrainingRank",
        path: "rank",
        component: TrainingRank,
        meta: {
          title: "Training Rank",
          fullScreenSource: "training",
          requireAuth: true,
        },
      },
    ],
  },
  {
    path: "/contest",
    name: "ContestList",
    component: ContestList,
    meta: { title: "Contest", requireAuth: true },
  },
  {
    path: "/contest/acm-scoreboard/:contestID",
    name: "ACMScoreBoard",
    component: ACMScoreBoard,
    meta: { title: "ACM Contest ScoreBoard" },
  },
  {
    path: "/contest/oi-scoreboard/:contestID",
    name: "OIScoreBoard",
    component: OIScoreBoard,
    meta: { title: "OI Contest ScoreBoard" },
  },
  {
    name: "ContestDetails",
    path: "/contest/:contestID/",
    component: ContestDetails,
    meta: {
      title: "Contest Details",
      fullScreenSource: "contest",
      requireAuth: true,
    },
    children: [
      {
        name: "ContestSubmissionList",
        path: "submissions",
        component: SubmissionList,
        meta: {
          title: "Contest Submission",
          fullScreenSource: "contest",
          requireAuth: true,
        },
      },
      {
        name: "ContestSubmissionDetails",
        path: "problem/:problemID/submission-detail/:submitID",
        component: SubmissionDetails,
        meta: {
          title: "Contest Submission Details",
          fullScreenSource: "contest",
          requireAuth: true,
        },
      },
      {
        name: "ContestProblemList",
        path: "problems",
        component: ContestProblemList,
        meta: {
          title: "Contest Problem",
          fullScreenSource: "contest",
          requireAuth: true,
        },
      },
      {
        name: "ContestProblemDetails",
        path: "problem/:problemID/",
        component: Problem,
        meta: {
          title: "Contest Problem Details",
          fullScreenSource: "contest",
          requireAuth: true,
        },
      },
      {
        name: "ContestAnnouncementList",
        path: "announcements",
        component: Announcements,
        meta: {
          title: "Contest Announcement",
          fullScreenSource: "contest",
          requireAuth: true,
        },
      },
      {
        name: "ContestRank",
        path: "rank",
        component: ContestRank,
        meta: {
          title: "Contest Rank",
          fullScreenSource: "contest",
          requireAuth: true,
        },
      },
      {
        name: "ContestACInfo",
        path: "ac-info",
        component: ACMInfoAdmin,
        meta: {
          title: "Contest AC Info",
          fullScreenSource: "contest",
          requireAuth: true,
        },
      },
      {
        name: "ContestRejudgeAdmin",
        path: "rejudge",
        component: ContestRejudgeAdmin,
        meta: {
          title: "Contest Rejudge",
          fullScreenSource: "contest",
          requireSuperAdmin: true,
        },
      },
      {
        name: "ContestComment",
        path: "comment",
        component: ContestComment,
        meta: {
          title: "Contest Comment",
          access: "contestComment",
          fullScreenSource: "contest",
          requireAuth: true,
        },
      },
      {
        name: "ContestPrint",
        path: "print",
        component: ContestPrint,
        meta: {
          title: "Contest Print",
          fullScreenSource: "contest",
          requireAuth: true,
        },
      },
      {
        name: "ContestAdminPrint",
        path: "admin-print",
        component: ContestAdminPrint,
        meta: {
          title: "Contest Admin Print",
          fullScreenSource: "contest",
          requireAuth: true,
        },
      },
      {
        name: "ScrollBoard",
        path: "scroll-board",
        component: ScrollBoard,
        meta: {
          title: "Contest Scroll Board",
          fullScreenSource: "contest",
          requireAuth: true,
        },
      },
    ],
  },
  {
    path: "/status",
    name: "SubmissionList",
    component: SubmissionList,
    meta: { title: "Status", requireAuth: true },
  },
  {
    path: "/submission-detail/:submitID",
    name: "SubmissionDetails",
    component: SubmissionDetails,
    meta: { title: "Submission Details", requireAuth: true },
  },
  {
    path: "/acm-rank",
    name: "ACM Rank",
    component: ACMRank,
    meta: { title: "ACM Rank", requireAuth: true },
  },
  {
    path: "/oi-rank",
    name: "OI Rank",
    component: OIRank,
    meta: { title: "OI Rank", requireAuth: true },
  },
  {
    path: "/reset-password",
    name: "SetNewPassword",
    component: SetNewPassword,
    meta: { title: "Reset Password" },
  },
  {
    name: "UserHome",
    path: "/user-home",
    component: UserHome,
    meta: { title: "User Home", requireAuth: true },
  },
  {
    name: "Setting",
    path: "/setting",
    component: Setting,
    meta: { requireAuth: true, title: "Setting" },
  },
  {
    name: "Logout",
    path: "/logout",
    component: Logout,
    meta: { requireAuth: true, title: "Logout" },
  },
  {
    path: "/discussion",
    name: "AllDiscussion",
    meta: { title: "Discussion", access: "discussion", requireAuth: true },
    component: DiscussionList,
  },
  {
    path: "/discussion/:problemID",
    name: "ProblemDiscussion",
    meta: { title: "Discussion", access: "discussion", requireAuth: true },
    component: DiscussionList,
  },
  {
    path: "/discussion-detail/:discussionID",
    name: "DiscussionDetails",
    meta: {
      title: "Discussion Details",
      access: "discussion",
      requireAuth: true,
    },
    component: Discussion,
  },
  {
    path: "/group",
    name: "GroupList",
    component: GroupList,
    meta: { title: "Group", requireAuth: true },
  },
  {
    path: "/group/:groupID",
    name: "GroupDetails",
    component: GroupDetails,
    meta: { title: "Group Details", requireAuth: true },
    children: [
      {
        path: "announcement",
        name: "GroupAnnouncementList",
        component: GroupAnnouncementList,
        meta: { title: "Group Announcement", requireAuth: true },
      },
      {
        path: "problem",
        name: "GroupProblemList",
        component: GroupProblemList,
        meta: { title: "Group Problem", requireAuth: true },
      },
      {
        name: "GroupProblemDetails",
        path: "problem/:problemID/",
        component: Problem,
        meta: { title: "Group Problem Details", requireAuth: true },
      },
      {
        path: "training",
        name: "GroupTrainingList",
        component: GroupTrainingList,
        meta: { title: "Group Training", requireAuth: true },
      },
      {
        name: "GroupTrainingDetails",
        path: "training/:trainingID/",
        component: TrainingDetails,
        meta: { title: "Group Training Details", requireAuth: true },
        children: [
          {
            name: "GroupTrainingProblemList",
            path: "problems",
            component: TrainingProblemList,
            meta: { title: "Group Training Problem", requireAuth: true },
          },
          {
            name: "GroupTrainingProblemDetails",
            path: "problem/:problemID/",
            component: Problem,
            meta: {
              title: "Group Training Problem Details",
              requireAuth: true,
            },
          },
          {
            name: "GroupTrainingRank",
            path: "rank",
            component: TrainingRank,
            meta: { title: "Group Training Rank", requireAuth: true },
          },
        ],
      },
      {
        path: "contest",
        name: "GroupContestList",
        component: GroupContestList,
        meta: { title: "Group Contest", requireAuth: true },
      },
      {
        path: "status",
        name: "GroupSubmissionList",
        component: SubmissionList,
        meta: { title: "Group Status", requireAuth: true },
      },
      {
        path: "submission-detail/:submitID",
        name: "GroupSubmissionDetails",
        component: SubmissionDetails,
        meta: { title: "Group Submission Details", requireAuth: true },
      },
      {
        path: "discussion",
        name: "GroupDiscussionList",
        component: GroupDiscussionList,
        meta: {
          title: "Group Discussion",
          access: "groupDiscussion",
          requireAuth: true,
        },
      },
      {
        path: "discussion/:problemID",
        name: "GroupProblemDiscussion",
        meta: {
          title: "Group Discussion",
          access: "groupDiscussion",
          requireAuth: true,
        },
        component: GroupDiscussionList,
      },
      {
        path: "discussion-detail/:discussionID",
        name: "GroupDiscussionDetails",
        meta: {
          title: "Group Discussion Details",
          access: "groupDiscussion",
          requireAuth: true,
        },
        component: Discussion,
      },
      {
        path: "member",
        name: "GroupMemberList",
        component: GroupMemberList,
        meta: { title: "Group Member", requireAuth: true },
      },
      {
        path: "setting",
        name: "GroupSetting",
        component: GroupSetting,
        meta: { title: "Group Setting", requireAuth: true },
      },
      {
        path: "rank",
        name: "GroupRank",
        component: GroupRank,
        meta: { title: "Group Rank", requireAuth: true },
      },
    ],
  },
  {
    path: "/introduction",
    meta: { title: "Introduction", requireAuth: true },
    component: Introduction,
  },
  {
    path: "/developer",
    meta: { title: "Developer", requireAuth: true },
    component: Developer,
  },
  {
    name: "Message",
    path: "/message/",
    component: Message,
    meta: { requireAuth: true, title: "Message" },
    children: [
      {
        name: "DiscussMsg",
        path: "discuss",
        component: UserMsg,
        meta: { requireAuth: true, title: "Discuss Message" },
      },
      {
        name: "ReplyMsg",
        path: "reply",
        component: UserMsg,
        meta: { requireAuth: true, title: "Reply Message" },
      },
      {
        name: "LikeMsg",
        path: "like",
        component: UserMsg,
        meta: { requireAuth: true, title: "Like Message" },
      },
      {
        name: "SysMsg",
        path: "sys",
        component: SysMsg,
        meta: { requireAuth: true, title: "System Message" },
      },
      {
        name: "MineMsg",
        path: "mine",
        component: SysMsg,
        meta: { requireAuth: true, title: "Mine Message" },
      },
    ],
  },
  {
    path: "*",
    component: NotFound,
    meta: { title: "404" },
  },
];
export default ojRoutes;
