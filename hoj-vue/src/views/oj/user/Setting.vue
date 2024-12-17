<template>
  <el-card class="box-card">
    <el-tabs
      v-model="activeName"
      accordion
      :tab-position="tabPosition"
      style="height: auto"
    >
      <el-tab-pane name="Account" :label="$t('m.Change_Password')">
        <component :is="Account"></component>
      </el-tab-pane>
      <el-tab-pane name="ChangeEmail" :label="$t('m.Change_Email')">
        <component :is="ChangeEmail"></component>
      </el-tab-pane>
      <el-tab-pane name="Avatar" :label="$t('m.Avatar_Setting')">
        <component :is="Avatar"></component>
      </el-tab-pane>
      <el-tab-pane name="UserInfo" :label="$t('m.UserInfo_Setting')">
        <component :is="UserInfo"></component>
      </el-tab-pane>
    </el-tabs>
  </el-card>
</template>
<script>
const Account = () => import("@/components/oj/setting/Account");
const ChangeEmail = () => import("@/components/oj/setting/ChangeEmail");
const Avatar = () => import("@/components/oj/setting/Avatar");
const UserInfo = () => import("@/components/oj/setting/UserInfo");
export default {
  components: {
    Account,
    ChangeEmail,
    Avatar,
    UserInfo,
  },
  data() {
    return {
      tabPosition: "left",
      Account: "Account",
      Avatar: "Avatar",
      UserInfo: "UserInfo",
      activeName: "Account",
      ChangeEmail: "ChangeEmail",
    };
  },
};
</script>
<style scoped>
@media screen and (min-width: 1200px) {
  .box-card {
    margin-left: 8%;
    margin-right: 8%;
  }
}
/deep/ .el-collapse-item__header {
  border-radius: 4px;
  font-size: 18px;
  color: #409eff;
}
</style>
