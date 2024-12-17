<template>
  <div>
    <el-row :gutter="30">
      <el-col :md="15" :sm="24">
        <!--        轮播图-->
        <el-card>
          <div slot="header" class="content-center">
            <span class="panel-title home-title welcome-title"
              >{{ $t("m.Welcome_to") }}{{ websiteConfig.shortName }}</span
            >
          </div>

          <el-carousel
            :interval="interval"
            :height="srcHight"
            class="img-carousel"
            arrow="always"
            indicator-position="outside"
          >
            <el-carousel-item
              v-for="(item, index) in carouselImgList"
              :key="index"
            >
              <el-image :src="item.url" fit="fill">
                <div slot="error" class="image-slot">
                  <i class="el-icon-picture-outline"></i>
                </div>
              </el-image>
            </el-carousel-item>
          </el-carousel>
        </el-card>
        <!--        公告-->
        <Announcements class="card-top"></Announcements>
        <!--        最近一周提交统计-->
        <SubmissionStatistic class="card-top"></SubmissionStatistic>
      </el-col>

      <el-col :md="9" :sm="24" class="phone-margin">
        <!--
                &lt;!&ndash; 灯笼1 &ndash;&gt;
                <div class="deng-box">
                  <div class="deng">
                    <div class="xian"></div>
                    <div class="deng-a">
                      <div class="deng-b"><div class="deng-t">节</div></div>
                    </div>
                    <div class="shui shui-a"><div class="shui-c"></div><div class="shui-b"></div></div>
                  </div>
                </div>

                &lt;!&ndash; 灯笼2 &ndash;&gt;

                <div class="deng-box1">
                  <div class="deng">
                    <div class="xian"></div>
                    <div class="deng-a">
                      <div class="deng-b"><div class="deng-t">春</div></div>
                    </div>
                    <div class="shui shui-a"><div class="shui-c"></div><div class="shui-b"></div></div>
                  </div>
                </div>
        -->

        <!--         倒计时-->
        <el-card>
          <div slot="header" class="clearfix">
            <span class="panel-title home-title">
              <i class="el-icon-chat-line-round"></i>
              {{ $t("m.Count_Down") }}</span
            >
          </div>
          <div class="item">
            <p class="textcolor down">
              2025年元旦倒计时 {{ this.countDown[0] }} 天
            </p>
            <p class="textcolor down">
              2025年春节倒计时 {{ this.countDown[1] }} 天
            </p>
          </div>
        </el-card>

        <!--最近比赛-->
        <template v-if="contests.length">
          <el-card class="card-top">
            <div slot="header" class="clearfix title content-center">
              <div class="home-title home-contest">
                <i class="el-icon-trophy"></i> {{ $t("m.Recent_Contest") }}
              </div>
            </div>
            <el-card
              shadow="hover"
              v-for="(contest, index) in contests"
              :key="index"
              class="contest-card"
              :class="
                contest.status == 0
                  ? 'contest-card-running'
                  : 'contest-card-schedule'
              "
            >
              <div slot="header" class="clearfix contest-header">
                <a class="contest-title" @click="goContest(contest.id)">{{
                  contest.title
                }}</a>
                <div class="contest-status">
                  <el-tag
                    effect="dark"
                    size="medium"
                    :color="CONTEST_STATUS_REVERSE[contest.status]['color']"
                  >
                    <i class="fa fa-circle" aria-hidden="true"></i>
                    {{
                      $t("m." + CONTEST_STATUS_REVERSE[contest.status]["name"])
                    }}
                  </el-tag>
                </div>
              </div>
              <div class="contest-type-auth">
                <template v-if="contest.type == 0">
                  <el-button
                    :type="'primary'"
                    round
                    @click="goContestList(contest.type)"
                    size="mini"
                    style="margin-right: 10px"
                    ><i class="fa fa-trophy"></i>
                    {{ contest.type | parseContestType }}
                  </el-button>
                </template>
                <template v-else>
                  <el-tooltip
                    :content="
                      $t('m.Contest_Rank') +
                      '：' +
                      (contest.oiRankScoreType == 'Recent'
                        ? $t(
                            'm.Based_on_The_Recent_Score_Submitted_Of_Each_Problem'
                          )
                        : $t(
                            'm.Based_on_The_Highest_Score_Submitted_For_Each_Problem'
                          ))
                    "
                    placement="top"
                  >
                    <el-button
                      :type="'warning'"
                      round
                      @click="goContestList(contest.type)"
                      size="mini"
                      style="margin-right: 10px"
                      ><i class="fa fa-trophy"></i>
                      {{ contest.type | parseContestType }}
                    </el-button>
                  </el-tooltip>
                </template>
                <el-tooltip
                  :content="$t('m.' + CONTEST_TYPE_REVERSE[contest.auth].tips)"
                  placement="top"
                  effect="light"
                >
                  <el-tag
                    :type="CONTEST_TYPE_REVERSE[contest.auth]['color']"
                    size="medium"
                    effect="plain"
                  >
                    {{ $t("m." + CONTEST_TYPE_REVERSE[contest.auth]["name"]) }}
                  </el-tag>
                </el-tooltip>
              </div>
              <ul class="contest-info">
                <li>
                  <el-button
                    type="primary"
                    round
                    size="mini"
                    style="margin-top: 4px"
                    ><i class="fa fa-calendar"></i>
                    {{
                      contest.startTime | localtime((format = "MM-DD HH:mm"))
                    }}
                  </el-button>
                </li>
                <li>
                  <el-button
                    type="success"
                    round
                    size="mini"
                    style="margin-top: 4px"
                    ><i class="fa fa-clock-o"></i>
                    {{ getDuration(contest.startTime, contest.endTime) }}
                  </el-button>
                </li>
                <li>
                  <el-button
                    size="mini"
                    round
                    plain
                    v-if="contest.count != null"
                  >
                    <i
                      class="el-icon-user-solid"
                      style="color: rgb(48, 145, 242)"
                    ></i
                    >x{{ contest.count }}
                  </el-button>
                </li>
              </ul>
            </el-card>
          </el-card>
        </template>

        <!--最近一周排行榜单-->
        <!--        <el-card :class="contests.length ? 'card-top' : ''">-->
        <el-card class="card-top">
          <div slot="header" class="clearfix">
            <span class="panel-title home-title">
              <i class="el-icon-s-data"></i> {{ $t("m.Recent_7_Days_AC_Rank") }}
            </span>
          </div>
          <vxe-table
            border="inner"
            stripe
            auto-resize
            align="center"
            :data="recentUserACRecord"
            max-height="500px"
            :loading="loading.recent7ACRankLoading"
          >
            <vxe-table-column type="seq" min-width="50">
              <template v-slot="{ rowIndex }">
                <span :class="getRankTagClass(rowIndex)"
                  >{{ rowIndex + 1 }}
                </span>
                <span :class="'cite no' + rowIndex"></span>
              </template>
            </vxe-table-column>
            <vxe-table-column
              field="username"
              :title="$t('m.Username')"
              min-width="200"
              align="left"
            >
              <template v-slot="{ row }">
                <avatar
                  :username="row.username"
                  :inline="true"
                  :size="25"
                  color="#FFF"
                  :src="row.avatar"
                  class="user-avatar"
                ></avatar>
                <a
                  @click="goUserHome(row.username, row.uid)"
                  style="color: #2d8cf0"
                  >{{ row.username }}</a
                >
                <span style="margin-left: 2px" v-if="row.titleName">
                  <el-tag effect="dark" size="small" :color="row.titleColor">
                    {{ row.titleName }}
                  </el-tag>
                </span>
              </template>
            </vxe-table-column>
            <vxe-table-column
              field="ac"
              :title="$t('m.AC')"
              min-width="50"
              align="left"
            >
            </vxe-table-column>
          </vxe-table>
        </el-card>

        <!--友情链接 -->
        <el-card class="card-top">
          <div slot="header" class="clearfix">
            <span class="panel-title home-title">
              <i class="el-icon-connection"></i> {{ $t("m.Blogroll") }}</span
            >
          </div>
          <div class="item">
            <li
              :md="8"
              :sm="24"
              v-for="(bl, index) in blogrollList"
              :key="index"
            >
              <el-link :underline="false" :href="bl.url" target="_blank">
                {{ bl.name }}
              </el-link>
              <el-divider v-if="bl.num != blogrollList.length"></el-divider>
            </li>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import time from "@/common/time";
import api from "@/common/api";
import axios from "axios";
import {
  CONTEST_STATUS_REVERSE,
  CONTEST_TYPE_REVERSE,
} from "@/common/constants";
import { mapState, mapGetters } from "vuex";
import Avatar from "vue-avatar";
import myMessage from "@/common/message";
const Announcements = () => import("@/components/oj/common/Announcements.vue");
const SubmissionStatistic = () =>
  import("@/components/oj/home/SubmissionStatistic.vue");

export default {
  name: "home",
  components: {
    Announcements,
    SubmissionStatistic,
    Avatar,
  },
  data() {
    return {
      interval: 5000,
      hitokoto: "加载中....",
      countDown: [],
      recentUserACRecord: [],
      CONTEST_STATUS_REVERSE: {},
      CONTEST_TYPE_REVERSE: {},
      contests: [],
      loading: {
        recent7ACRankLoading: false,
        recentContests: false,
      },
      carouselImgList: [
        {
          url: require("@/assets/home1.jpg"),
        },
        {
          url: require("@/assets/home2.jpg"),
        },
      ],
      srcHight: "440px",
      blogrollList: [
        {
          num: 1,
          url: "https://www.noi.cn",
          name: "全国青少年信息学奥林匹克竞赛",
        },
        {
          num: 2,
          url: "https://oi-wiki.org",
          name: "OI Wiki 信息学竞赛知识百科",
        },
        {
          num: 3,
          url: "https://www.hello-algo.com",
          name: "Hello Algo 动画图解算法",
        },
        {
          num: 4,
          url: "https://kz.ksecloud.cn/",
          name: "江苏省昆山中学",
        },
        {
          num: 5,
          url: "https://sm.myapp.com/original/Development/Dev-Cpp_5.11_TDM-GCC_4.9.2_Setup.exe",
          name: "Dev-Cpp 5.11安装包",
        },
        {
          num: 6,
          url: "https://code.visualstudio.com",
          name: "Visual Studio Code",
        },
      ],
    };
  },
  mounted() {
    let screenWidth = window.screen.width;
    if (screenWidth < 768) {
      this.srcHight = "200px";
    } else {
      this.srcHight = "440px";
    }
    this.CONTEST_STATUS_REVERSE = Object.assign({}, CONTEST_STATUS_REVERSE);
    this.CONTEST_TYPE_REVERSE = Object.assign({}, CONTEST_TYPE_REVERSE);
    this.getHomeCarousel();
    this.getRecentContests();
    this.getRecent7ACRank();
    this.getCountDown();
  },
  methods: {
    getHomeCarousel() {
      api.getHomeCarousel().then((res) => {
        if (res.data.data != null && res.data.data.length > 0) {
          this.carouselImgList = res.data.data;
        }
      });
    },
    getCountDown() {
      const now = new Date();
      const day =
        new Date("Wed Jan 01 2025 00:00:00 GMT+0800 (中国标准时间)").getTime() -
        new Date(now).getTime(); //日期转时间戳 ;
      this.countDown[0] = Math.floor(day / 86400000) + 1; //时间戳获取天数
      if (this.countDown[0] <= 0) this.countDown[0] = 0;
      const day2 =
        new Date("Wed Jan 29 2025 00:00:00 GMT+0800 (中国标准时间)").getTime() -
        new Date(now).getTime(); //日期转时间戳 ;
      this.countDown[1] = Math.floor(day2 / 86400000) + 1; //时间戳获取天数
      if (this.countDown[1] <= 0) this.countDown[1] = 0;
    },

    getRecentContests() {
      this.loading.recentContests = true;
      api.getRecentContests().then(
        (res) => {
          this.contests = res.data.data;
          this.loading.recentContests = false;
        },
        (err) => {
          this.loading.recentContests = false;
        }
      );
    },
    getRecent7ACRank() {
      this.loading.recent7ACRankLoading = true;
      api.getRecent7ACRank().then(
        (res) => {
          this.recentUserACRecord = res.data.data;
          this.loading.recent7ACRankLoading = false;
        },
        (err) => {
          this.loading.recent7ACRankLoading = false;
        }
      );
    },
    goContest(cid) {
      if (!this.isAuthenticated) {
        myMessage.warning(this.$i18n.t("m.Please_login_first"));
        this.$store.dispatch("changeModalStatus", { visible: true });
      } else {
        this.$router.push({
          name: "ContestProblemList",
          params: { contestID: cid },
        });
      }
    },
    goContestList(type) {
      this.$router.push({
        name: "ContestList",
        query: {
          type,
        },
      });
    },
    goProblem(event) {
      this.$router.push({
        name: "ProblemDetails",
        params: {
          problemID: event.row.problemId,
        },
      });
    },
    goUserHome(username, uid) {
      this.$router.push({
        path: "/user-home",
        query: { uid, username },
      });
    },
    getDuration(startTime, endTime) {
      return time.formatSpecificDuration(startTime, endTime);
    },
    getRankTagClass(rowIndex) {
      return "rank-tag no" + (rowIndex + 1);
    },
  },
  computed: {
    ...mapState(["websiteConfig"]),
    ...mapGetters(["isAuthenticated"]),
  },
};
</script>
<style>
.contest-card-running {
  border-color: rgb(25, 190, 107);
}
.contest-card-schedule {
  border-color: #f90;
}
</style>
<style scoped>
/deep/.el-card__header {
  padding: 0.6rem 1.25rem !important;
}
.card-top {
  margin-top: 20px;
}
.home-contest {
  text-align: left;
  font-size: 21px;
  font-weight: 500;
  line-height: 30px;
}
.oj-logo {
  border: 1px solid rgba(0, 0, 0, 0.15);
  border-radius: 4px;
  margin-bottom: 1rem;
  padding: 0.5rem 1rem;
  background: rgb(255, 255, 255);
  min-height: 47px;
}
.oj-normal {
  border-color: #409eff;
}
.oj-error {
  border-color: #e65c47;
}

.el-carousel__item h3 {
  color: #475669;
  font-size: 14px;
  opacity: 0.75;
  line-height: 200px;
  margin: 0;
}

.contest-card {
  margin-bottom: 20px;
}
.contest-title {
  font-size: 1.15rem;
  font-weight: 600;
}
.contest-type-auth {
  text-align: center;
  margin-top: -10px;
  margin-bottom: 5px;
}
ul,
li {
  padding: 0;
  margin: 0;
  list-style: none;
}
.item li {
  padding-right: 25px;
}

.textcolor {
  color: #4d9afe;
  font-size: 20px;
  height: 30px;
  text-align: center;
}
.textcolor.down {
  background-image: linear-gradient(to right, #4d9afe, #a600ff, #ff0000);
  color: transparent;
  -webkit-background-clip: text;
}

.contest-info {
  text-align: center;
}
.contest-info li {
  display: inline-block;
  padding-right: 10px;
}

/deep/.contest-card-running .el-card__header {
  border-color: rgb(25, 190, 107);
  background-color: rgba(94, 185, 94, 0.15);
}
.contest-card-running .contest-title {
  color: #5eb95e;
}

/deep/.contest-card-schedule .el-card__header {
  border-color: #f90;
  background-color: rgba(243, 123, 29, 0.15);
}

.contest-card-schedule .contest-title {
  color: #f37b1d;
}

.content-center {
  text-align: center;
}
.clearfix:before,
.clearfix:after {
  display: table;
  content: "";
}
.clearfix:after {
  clear: both;
}
.welcome-title {
  font-weight: 600;
  font-size: 25px;
  font-family: "Raleway";
}
.contest-status {
  float: right;
}
.img-carousel {
  height: auto;
}

@media screen and (max-width: 768px) {
  .contest-status {
    text-align: center;
    float: none;
    margin-top: 5px;
  }
  .contest-header {
    text-align: center;
  }
  .img-carousel {
    height: auto;
    overflow: hidden;
  }
  .phone-margin {
    margin-top: 20px;
  }
}
.title .el-link {
  font-size: 21px;
  font-weight: 500;
  color: #444;
}
.clearfix h2 {
  color: #409eff;
}
.el-link.el-link--default:hover {
  color: #409eff;
  transition: all 0.28s ease;
}
.contest .content-info {
  padding: 0 70px 40px 70px;
}
.contest .contest-description {
  margin-top: 25px;
}
span.rank-tag.no1 {
  line-height: 24px;
  background: #bf2c24;
}

span.rank-tag.no2 {
  line-height: 24px;
  background: #e67225;
}

span.rank-tag.no3 {
  line-height: 24px;
  background: #e6bf25;
}

span.rank-tag {
  font: 16px/22px FZZCYSK;
  min-width: 14px;
  height: 22px;
  padding: 0 4px;
  text-align: center;
  color: #fff;
  background: #000;
  background: rgba(0, 0, 0, 0.6);
}
.user-avatar {
  margin-right: 5px !important;
  vertical-align: middle;
}
.cite {
  display: block;
  width: 14px;
  height: 0;
  margin: 0 auto;
  margin-top: -3px;
  border-right: 11px solid transparent;
  border-bottom: 0 none;
  border-left: 11px solid transparent;
}
.cite.no0 {
  border-top: 5px solid #bf2c24;
}
.cite.no1 {
  border-top: 5px solid #e67225;
}
.cite.no2 {
  border-top: 5px solid #e6bf25;
}
@media screen and (min-width: 1050px) {
  /deep/ .vxe-table--body-wrapper {
    overflow-x: hidden !important;
  }
}

/*灯笼 start

.deng-box {
  position: fixed;
  top: 20px;
  right: -20px;
  z-index: 999;
}

.deng-box1 {
  position: fixed;
  top: 30px;
  right: 10px;
  z-index: 999;
}

.deng-box1 .deng {
  position: relative;
  width: 120px;
  height: 90px;
  margin: 50px;
  background: #d8000f;
  background: rgba(216, 0, 15, 0.8);
  border-radius: 50% 50%;
  -webkit-transform-origin: 50% -100px;
  -webkit-animation: swing 5s infinite ease-in-out;
  box-shadow: -5px 5px 30px 4px rgba(252, 144, 61, 1);
}

.deng {
  position: relative;
  width: 120px;
  height: 90px;
  margin: 50px;
  background: #d8000f;
  background: rgba(216, 0, 15, 0.8);
  border-radius: 50% 50%;
  -webkit-transform-origin: 50% -100px;
  -webkit-animation: swing 3s infinite ease-in-out;
  box-shadow: -5px 5px 50px 4px rgba(250, 108, 0, 1);
}

.deng-a {
  width: 100px;
  height: 90px;
  background: #d8000f;
  background: rgba(216, 0, 15, 0.1);
  margin: 12px 8px 8px 8px;
  border-radius: 50% 50%;
  border: 2px solid #dc8f03;
}

.deng-b {
  width: 45px;
  height: 90px;
  background: #d8000f;
  background: rgba(216, 0, 15, 0.1);
  margin: -4px 8px 8px 26px;
  border-radius: 50% 50%;
  border: 2px solid #dc8f03;
}

.xian {
  position: absolute;
  top: -20px;
  left: 60px;
  width: 2px;
  height: 20px;
  background: #dc8f03;
}

.shui-a {
  position: relative;
  width: 5px;
  height: 20px;
  margin: -5px 0 0 59px;
  -webkit-animation: swing 4s infinite ease-in-out;
  -webkit-transform-origin: 50% -45px;
  background: #ffa500;
  border-radius: 0 0 5px 5px;
}

.shui-b {
  position: absolute;
  top: 14px;
  left: -2px;
  width: 10px;
  height: 10px;
  background: #dc8f03;
  border-radius: 50%;
}

.shui-c {
  position: absolute;
  top: 18px;
  left: -2px;
  width: 10px;
  height: 35px;
  background: #ffa500;
  border-radius: 0 0 0 5px;
}

.deng:before {
  position: absolute;
  top: -7px;
  left: 29px;
  height: 12px;
  width: 60px;
  content: " ";
  display: block;
  z-index: 999;
  border-radius: 5px 5px 0 0;
  border: solid 1px #dc8f03;
  background: #ffa500;
  background: linear-gradient(to right, #dc8f03, #ffa500, #dc8f03, #ffa500, #dc8f03);
}

.deng:after {
  position: absolute;
  bottom: -7px;
  left: 10px;
  height: 12px;
  width: 60px;
  content: " ";
  display: block;
  margin-left: 20px;
  border-radius: 0 0 5px 5px;
  border: solid 1px #dc8f03;
  background: #ffa500;
  background: linear-gradient(to right, #dc8f03, #ffa500, #dc8f03, #ffa500, #dc8f03);
}

.deng-t {
  font-family: 华文行楷,Arial,Lucida Grande,Tahoma,sans-serif;
  font-size: 3.2rem;
  color: #dc8f03;
  font-weight: bold;
  line-height: 85px;
  text-align: center;
}

.night .deng-t,
.night .deng-box,
.night .deng-box1 {
  background: transparent !important;
}

@-moz-keyframes swing {
  0% {
    -moz-transform: rotate(-10deg)
  }

  50% {
    -moz-transform: rotate(10deg)
  }

  100% {
    -moz-transform: rotate(-10deg)
  }
}

@-webkit-keyframes swing {
  0% {
    -webkit-transform: rotate(-10deg)
  }

  50% {
    -webkit-transform: rotate(10deg)
  }

  100% {
    -webkit-transform: rotate(-10deg)
  }
}
灯笼 end*/
</style>
