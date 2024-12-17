<template>
  <div>
    <el-card>
      <div slot="header">
        <span class="panel-title home-title">{{
          $t("m.Discussion_Admin")
        }}</span>
        <div class="filter-row">
          <span>
            <el-button
              v-if="isSuperAdmin || isProblemAdmin"
              type="danger"
              icon="el-icon-delete-solid"
              @click="deleteDiscussion(null)"
              size="small"
              >{{ $t("m.Delete") }}
            </el-button>
          </span>
          <span>
            <vxe-input
              v-model="keyword"
              :placeholder="$t('m.Enter_keyword')"
              type="search"
              size="medium"
              @search-click="filterByKeyword"
              @keyup.enter.native="filterByKeyword"
            ></vxe-input>
          </span>
          <span>
            <el-switch
              v-model="onlyGroup"
              :active-text="$t('m.Only_Group_Discussion')"
              :width="40"
              @change="filterSwitch"
            >
            </el-switch>
          </span>
          <span>
            <el-switch
              v-model="onlyExplain"
              :active-text="$t('m.onlyExplain')"
              :width="40"
              @change="filterSwitch"
            >
            </el-switch>
          </span>
          <span>
            <el-switch
              v-model="onlyStatus"
              :active-text="$t('m.onlyStatus')"
              :width="40"
              @change="filterSwitch"
            >
            </el-switch>
          </span>
        </div>
      </div>
      <vxe-table
        stripe
        auto-resize
        :data="discussionList"
        ref="xTable"
        align="center"
        :loading="discussionLoadingTable"
        :checkbox-config="{ highlight: true, range: true }"
        @checkbox-change="handleSelectionChange"
        @checkbox-all="handlechangeAll"
      >
        <vxe-table-column type="checkbox" width="60"></vxe-table-column>
        <vxe-table-column field="id" title="ID" width="60"></vxe-table-column>
        <vxe-table-column
          field="gid"
          min-width="80"
          :title="$t('m.Group_Number')"
          sortable
          show-overflow
        ></vxe-table-column>
        <vxe-table-column
          field="pid"
          min-width="120"
          :title="$t('m.Problem_ID')"
          sortable
          show-overflow
        ></vxe-table-column>
        <vxe-table-column
          field="title"
          :title="$t('m.Title')"
          show-overflow
          min-width="150"
        ></vxe-table-column>
        <vxe-table-column
          field="author"
          :title="$t('m.Author')"
          min-width="60"
          show-overflow
        ></vxe-table-column>
        <vxe-table-column
          field="likeNum"
          :title="$t('m.Likes')"
          min-width="60"
          sortable
        ></vxe-table-column>
        <vxe-table-column
          field="viewNum"
          :title="$t('m.Views')"
          min-width="60"
          sortable
        ></vxe-table-column>
        <vxe-table-column
          field="gmtCreate"
          :title="$t('m.Created_Time')"
          min-width="150"
        >
          <template v-slot="{ row }">
            {{ row.gmtCreate | localtime }}
          </template>
        </vxe-table-column>
        <vxe-table-column min-width="150" :title="$t('m.Discussion_Switch')">
          <template v-slot="{ row }">
            <p>
              {{ $t("m.isTop") }}:
              <el-switch
                v-model="row.topPriority"
                :active-value="true"
                :inactive-value="false"
                @change="handleTopSwitch(row)"
              ></el-switch>
            </p>
            <p>
              {{ $t("m.isExplain") }}:
              <el-switch
                v-model="row.isExplain"
                :active-value="true"
                :inactive-value="false"
                @change="handleExplainSwitch(row)"
              ></el-switch>
            </p>
            <p>
              {{ $t("m.isDisable") }}:
              <el-switch
                v-model="row.status"
                :active-value="1"
                :inactive-value="0"
                @change="changeDiscussionStatus(row)"
              ></el-switch>
            </p>
          </template>
        </vxe-table-column>
        <vxe-table-column :title="$t('m.Option')" min-width="150">
          <template v-slot="{ row }">
            <div style="margin-bottom: 10px">
              <el-tooltip
                effect="dark"
                :content="$t('m.Bound_Problem')"
                placement="top"
              >
                <el-button
                  icon="el-icon-connection"
                  size="mini"
                  @click="showAddProblemDialog(row)"
                  type="success"
                >
                </el-button>
              </el-tooltip>
              <el-tooltip
                effect="dark"
                :content="$t('m.View_Discussion')"
                placement="top"
              >
                <el-button
                  icon="el-icon-search"
                  size="mini"
                  @click.native="toDiscussion(row.id, row.gid)"
                  type="primary"
                >
                </el-button>
              </el-tooltip>
            </div>
            <el-tooltip
              v-if="isSuperAdmin || isProblemAdmin"
              effect="dark"
              :content="$t('m.Delete')"
              placement="top"
            >
              <el-button
                icon="el-icon-delete-solid"
                size="mini"
                @click.native="deleteDiscussion([row.id])"
                type="danger"
              >
              </el-button>
            </el-tooltip>
          </template>
        </vxe-table-column>
      </vxe-table>
      <div class="panel-options">
        <el-pagination
          class="page"
          layout="prev, pager, next"
          @current-change="discussionCurrentChange"
          :page-size="pageSize"
          :total="discussionTotal"
        >
        </el-pagination>
      </div>
    </el-card>
    <el-card style="margin-top: 20px">
      <div slot="header">
        <span class="panel-title home-title">{{
          $t("m.Discussion_Report")
        }}</span>
      </div>
      <vxe-table
        :loading="discussionReportLoadingTable"
        ref="table"
        align="center"
        :data="discussionReportList"
        auto-resize
        stripe
      >
        <vxe-table-column min-width="60" field="id" title="ID">
        </vxe-table-column>
        <vxe-table-column
          min-width="100"
          field="did"
          :title="$t('m.Discussion_ID')"
        >
        </vxe-table-column>
        <vxe-table-column
          field="discussionTitle"
          :title="$t('m.Title')"
          show-overflow
          min-width="150"
        ></vxe-table-column>
        <vxe-table-column
          field="discussionAuthor"
          :title="$t('m.Author')"
          min-width="150"
          show-overflow
        ></vxe-table-column>
        <vxe-table-column
          min-width="150"
          field="reporter"
          show-overflow
          :title="$t('m.Reporter')"
        >
        </vxe-table-column>
        <vxe-table-column
          min-width="150"
          field="gmtCreate"
          :title="$t('m.Report_Time')"
        >
          <template v-slot="{ row }">
            {{ row.gmtCreate | localtime }}
          </template>
        </vxe-table-column>
        <vxe-table-column
          min-width="100"
          field="status"
          :title="$t('m.Checked')"
        >
          <template v-slot="{ row }">
            <el-switch
              v-model="row.status"
              active-text=""
              inactive-text=""
              :active-value="true"
              :inactive-value="false"
              @change="handleCheckedSwitch(row)"
            >
            </el-switch>
          </template>
        </vxe-table-column>
        <vxe-table-column :title="$t('m.Option')" min-width="150">
          <template v-slot="{ row }">
            <el-tooltip
              class="item"
              effect="dark"
              :content="$t('m.View_Report_content')"
              placement="top"
            >
              <el-button
                icon="el-icon-document"
                @click.native="openReportDialog(row.content)"
                size="mini"
                type="success"
              ></el-button>
            </el-tooltip>
            <el-tooltip
              effect="dark"
              :content="$t('m.View_Discussion')"
              placement="top"
            >
              <el-button
                icon="el-icon-search"
                size="mini"
                @click.native="toDiscussion(row.did, row.gid)"
                type="primary"
              >
              </el-button>
            </el-tooltip>
          </template>
        </vxe-table-column>
      </vxe-table>

      <div class="panel-options">
        <el-pagination
          class="page"
          layout="prev, pager, next"
          @current-change="discussionReportCurrentChange"
          :page-size="discussionReportPageSize"
          :total="discussionReportTotal"
        >
        </el-pagination>
      </div>
    </el-card>

    <el-dialog
      :title="$t('m.Add_Bound_Problem')"
      width="90%"
      :visible.sync="addProblemDialogVisible"
      :close-on-click-modal="false"
    >
      <div style="text-align: center">
        <vxe-input
          v-model="query.keyword"
          :placeholder="$t('m.Enter_keyword')"
          type="search"
          size="medium"
          @search-click="filterBoundByKeyword"
          @keyup.enter.native="filterBoundByKeyword"
          style="margin-bottom: 10px"
        ></vxe-input>
        <vxe-table
          :data="problemList"
          :loading="loading"
          auto-resize
          stripe
          align="center"
        >
          <vxe-table-column title="ID" min-width="100" field="problemId">
          </vxe-table-column>
          <vxe-table-column
            min-width="150"
            :title="$t('m.Title')"
            field="title"
          >
          </vxe-table-column>
          <vxe-table-column
            :title="$t('m.Option')"
            align="center"
            min-width="100"
          >
            <template v-slot="{ row }">
              <el-tooltip effect="dark" :content="$t('m.Add')" placement="top">
                <el-button
                  icon="el-icon-plus"
                  size="mini"
                  @click.native="handleAddProblem(row.problemId)"
                  type="primary"
                >
                </el-button>
              </el-tooltip>
            </template>
          </vxe-table-column>
        </vxe-table>
        <el-pagination
          class="page"
          layout="prev, pager, next"
          @current-change="getProblemList"
          :page-size.sync="query.pageSize"
          :total.sync="total"
          :current-page.sync="query.currentPage"
        >
        </el-pagination>
      </div>
    </el-dialog>
  </div>
</template>
<script>
import api from "@/common/api";
import myMessage from "@/common/message";
import { mapGetters } from "vuex";
export default {
  name: "discussion",
  data() {
    return {
      pageSize: 5,
      discussionTotal: 0,
      discussionList: [],
      selectedDiscussions: [],
      keyword: "",
      discussionLoadingTable: false,
      discussionCurrentPage: 1,
      discussionReportList: [],
      discussionReportTotal: 0,
      discussionReportCurrentPage: 1,
      discussionReportPageSize: 10,
      discussionReportLoadingTable: false,
      total: 0,
      query: {
        problemListAuth: 0,
        problemListType: 0,
        oj: "All",
        pageSize: 10,
        keyword: "",
        currentPage: 1,
        contestId: null,
      },
      discussionID: 1,
      isExplain: false,
      addProblemDialogVisible: false,
      problemList: [],
      loading: false,
      Bound_keyword: "",
      onlyGroup: false,
      onlyExplain: false,
      onlyStatus: false,
    };
  },
  mounted() {
    this.getDiscussionList(1);
    this.getDiscussionReportList();
    this.getProblemList();
  },
  methods: {
    discussionCurrentChange(page) {
      this.discussionCurrentPage = page;
      this.getDiscussionList(page);
    },
    discussionReportCurrentChange(page) {
      this.discussionReportCurrentPage = page;
      this.getDiscussionReportList();
    },
    getDiscussionList(page) {
      this.discussionLoadingTable = true;
      let searchParams = {
        currentPage: page,
        keyword: this.keyword,
        onlyGroup: this.onlyGroup,
        onlyExplain: this.onlyExplain,
        onlyStatus: this.onlyStatus,
        admin: true,
      };
      api.getDiscussionList(this.pageSize, searchParams).then(
        (res) => {
          this.discussionLoadingTable = false;
          this.discussionTotal = res.data.data.total;
          this.discussionList = res.data.data.records;
        },
        (res) => {
          this.discussionLoadingTable = false;
        },
      );
    },
    getDiscussionReportList() {
      this.discussionReportLoadingTable = true;
      api
        .admin_getDiscussionReport(
          this.discussionReportCurrentPage,
          this.discussionReportPageSize,
        )
        .then(
          (res) => {
            this.discussionReportLoadingTable = false;
            this.discussionReportList = res.data.data.records;
            this.discussionReportTotal = res.data.data.total;
          },
          (err) => {
            this.discussionReportLoadingTable = false;
          },
        );
    },
    getProblemList() {
      let params = {
        limit: this.query.pageSize,
        currentPage: this.query.currentPage,
        keyword: this.query.keyword,
        cid: this.query.contestId,
        oj: this.query.oj,
      };
      this.loading = true;
      this.showPagination = false;
      api.admin_getProblemList(params).then(
        (res) => {
          this.loading = false;
          this.total = res.data.data.total;
          this.problemList = res.data.data.records;
          this.showPagination = true;
        },
        (err) => {
          this.loading = false;
          this.showPagination = true;
        },
      );
    },
    filterSwitch() {
      this.discussionCurrentChange(1);
    },
    filterByKeyword() {
      this.discussionCurrentChange(1);
      this.keyword = "";
    },
    filterBoundByKeyword() {
      this.query.currentPage = 1;
      this.getProblemList();
    },
    // 用户表部分勾选 改变选中的内容
    handleSelectionChange({ records }) {
      this.selectedDiscussions = [];
      for (let num = 0; num < records.length; num++) {
        this.selectedDiscussions.push(records[num].id);
      }
    },
    // 一键全部选中，改变选中的内容列表
    handlechangeAll() {
      let discussion = this.$refs.xTable.getCheckboxRecords();
      this.selectedDiscussions = [];
      for (let num = 0; num < discussion.length; num++) {
        this.selectedDiscussions.push(discussion[num].id);
      }
    },
    changeDiscussionStatus(row) {
      let discussion = {
        id: row.id,
        pid: row.pid,
        status: row.status,
        isExplain: row.isExplain,
      };
      api.admin_updateDiscussion(discussion).then((res) => {
        myMessage.success(this.$i18n.t("m.Update_Successfully"));
      });
    },
    handleTopSwitch(row) {
      let discussion = {
        id: row.id,
        pid: row.pid,
        topPriority: row.topPriority,
        isExplain: row.isExplain,
      };
      api.admin_updateDiscussion(discussion).then((res) => {
        myMessage.success(this.$i18n.t("m.Update_Successfully"));
      });
    },
    handleExplainSwitch(row) {
      if (!row.pid) {
        myMessage.warning(this.$i18n.t("m.Need_Bound_Problem"));
        row.isExplain = !row.isExplain;
        return;
      }
      let discussion = {
        id: row.id,
        pid: row.pid,
        isExplain: row.isExplain,
      };
      api.admin_updateDiscussion(discussion).then((res) => {
        myMessage.success(this.$i18n.t("m.Update_Successfully"));
      });
    },
    showAddProblemDialog(row) {
      this.Bound_keyword = "";
      this.discussionID = row.id;
      this.isExplain = row.isExplain;
      this.addProblemDialogVisible = true;
    },
    handleAddProblem(problemId) {
      let discussion = {
        id: this.discussionID,
        pid: problemId,
        isExplain: this.isExplain,
      };
      api.admin_updateDiscussion(discussion).then((res) => {
        myMessage.success(this.$i18n.t("m.Update_Successfully"));
        this.addProblemDialogVisible = false;
        this.getDiscussionList(this.discussionCurrentPage);
      });
    },
    handleCheckedSwitch(row) {
      let discussionReport = {
        id: row.id,
        status: row.status,
      };
      api.admin_updateDiscussionReport(discussionReport).then((res) => {
        myMessage.success(this.$i18n.t("m.Update_Successfully"));
      });
    },
    toDiscussion(did, gid) {
      if (gid != null) {
        window.open("/group/" + gid + "/discussion-detail/" + did);
      } else {
        window.open("/discussion-detail/" + did);
      }
    },
    deleteDiscussion(didList) {
      if (!didList) {
        didList = this.selectedDiscussions;
      }
      if (didList.length > 0) {
        this.$confirm(this.$i18n.t("m.Delete_Discussion_Tips"), "Tips", {
          type: "warning",
        }).then(
          () => {
            api
              .admin_deleteDiscussion(didList)
              .then((res) => {
                myMessage.success(this.$i18n.t("m.Delete_successfully"));
                this.selectedDiscussions = [];
                this.getDiscussionList(this.currentPage);
              })
              .catch(() => {
                this.selectedDiscussions = [];
                this.getDiscussionList(this.currentPage);
              });
          },
          () => {},
        );
      } else {
        myMessage.warning(
          this.$i18n.t("m.The_number_of_discussions_selected_cannot_be_empty"),
        );
      }
    },
    openReportDialog(content) {
      let reg = "#(.*?)# ";
      let re = RegExp(reg, "g");
      let tmp;
      let showContent = "<strong>" + this.$i18n.t("m.Tags") + "</strong>：";
      while ((tmp = re.exec(content))) {
        showContent += tmp[1] + " ";
      }
      showContent +=
        "<br><br><strong>" +
        this.$i18n.t("m.Content") +
        "</strong>：" +
        content.replace(/#(.*?)# /g, "");
      this.$alert(showContent, this.$i18n.t("m.Report_Content"), {
        confirmButtonText: this.$i18n.t("m.OK"),
        dangerouslyUseHTMLString: true,
      });
    },
  },
  computed: {
    ...mapGetters(["isSuperAdmin", "isProblemAdmin"]),
  },
};
</script>
<style scoped>
.filter-row {
  margin-top: 10px;
}
@media screen and (max-width: 768px) {
  .filter-row span {
    margin-right: 5px;
  }
}
@media screen and (min-width: 768px) {
  .filter-row span {
    margin-right: 20px;
  }
}
</style>
