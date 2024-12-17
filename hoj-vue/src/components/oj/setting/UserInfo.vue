<template>
  <div>
    <div class="section-title">{{ $t("m.UserInfo_Setting") }}</div>
    <el-form ref="formProfile" :model="formProfile">
      <el-row :gutter="30" justify="space-around">
        <el-col :md="11" :xs="24">
          <el-form-item :label="$t('m.RealName')">
            <el-input v-model="formProfile.realname" disabled :maxlength="50" />
          </el-form-item>
          <el-form-item :label="$t('m.Nickname')">
            <el-input v-model="formProfile.nickname" :maxlength="8" />
          </el-form-item>
          <el-form-item :label="$t('m.School')">
            <el-input v-model="formProfile.school" disabled :maxlength="50" />
          </el-form-item>
          <el-form-item :label="$t('m.Class')">
            <el-select
              v-model="formProfile.number"
              :placeholder="$t('m.Select_Class')"
              style="width: 100%"
              disabled
            >
              <el-option label="一年级" value="一年级"></el-option>
              <el-option label="二年级" value="二年级"></el-option>
              <el-option label="三年级" value="三年级"></el-option>
              <el-option label="四年级" value="四年级"></el-option>
              <el-option label="五年级" value="五年级"></el-option>
              <el-option label="六年级" value="六年级"></el-option>
              <el-option label="七年级" value="七年级"></el-option>
              <el-option label="八年级" value="八年级"></el-option>
              <el-option label="九年级" value="九年级"></el-option>
              <el-option label="高一" value="高一"></el-option>
              <el-option label="高二" value="高二"></el-option>
              <el-option label="高三" value="高三"></el-option>
              <el-option label="其他" value="其他"></el-option>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :md="2" :lg="2">
          <p></p>
          <div class="separator hidden-md-and-down"></div>
        </el-col>
        <el-col :md="11" :xs="24">
          <el-form-item :label="$t('m.CF_Username')">
            <el-input v-model="formProfile.cfUsername" :maxlength="50" />
          </el-form-item>
          <el-form-item :label="$t('m.Blog')">
            <el-input v-model="formProfile.blog" :maxlength="255" />
          </el-form-item>
          <el-form-item :label="$t('m.Github')">
            <el-input v-model="formProfile.github" :maxlength="255" />
          </el-form-item>
          <el-form-item :label="$t('m.Gender')">
            <el-radio-group v-model="formProfile.gender">
              <el-radio label="male" border size="small">{{
                $t("m.Male")
              }}</el-radio>
              <el-radio label="female" border size="small">{{
                $t("m.Female")
              }}</el-radio>
              <el-radio label="secrecy" border size="small" disabled>{{
                $t("m.Secrecy")
              }}</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="24">
          <label class="el-form-item__label" style="float: none">{{
            $t("m.Signature")
          }}</label>
          <Editor
            :value.sync="formProfile.signature"
            style="padding: 5px"
          ></Editor>
        </el-col>
      </el-row>
    </el-form>
    <div style="text-align: center; margin-top: 10px">
      <el-button
        type="primary"
        @click="updateUserInfo"
        :loading="loadingSaveBtn"
        >{{ $t("m.Save") }}</el-button
      >
    </div>
  </div>
</template>

<script>
import api from "@/common/api";
import utils from "@/common/utils";
import myMessage from "@/common/message";
import { VueCropper } from "vue-cropper";
import Avatar from "vue-avatar";
import "element-ui/lib/theme-chalk/display.css";
const Editor = () => import("@/components/admin/Editor.vue");
export default {
  components: {
    Avatar,
    VueCropper,
    Editor,
  },
  data() {
    return {
      loadingSaveBtn: false,
      preview: {},
      formProfile: {
        realname: "",
        username: "",
        cfUsername: "",
        gender: "",
        nickname: "",
        signature: "",
        number: "",
        blog: "",
        school: "",
        github: "",
      },
    };
  },
  mounted() {
    let profile = this.$store.getters.userInfo;
    Object.keys(this.formProfile).forEach((element) => {
      if (profile[element] !== undefined) {
        this.formProfile[element] = profile[element];
      }
    });
  },
  methods: {
    updateUserInfo() {
      if (this.formProfile.nickname.length > 8) {
        myMessage.error(this.$i18n.t("m.Enter_the_Nickname_Length"));
        return;
      }
      this.loadingSaveBtn = true;
      /*let updateData = utils.filterEmptyValue(
          Object.assign({}, this.formProfile)
      );*/
      api.changeUserInfo(this.formProfile).then(
        (res) => {
          myMessage.success(this.$i18n.t("m.Update_Successfully"));
          this.$store.dispatch("setUserInfo", res.data.data);
          this.loadingSaveBtn = false;
        },
        (_) => {
          this.loadingSaveBtn = false;
        },
      );
    },
  },
};
</script>

<style scoped>
/deep/ .el-input__inner {
  height: 38px;
}
/deep/ .el-form-item__label {
  font-size: 14px;
  line-height: 20px;
}
.section-title {
  font-size: 21px;
  font-weight: 500;
  padding-top: 10px;
  padding-bottom: 20px;
  line-height: 30px;
  text-align: center;
}
.section-main {
  text-align: center;
  margin-bottom: 20px;
}

/deep/.upload-container .el-upload {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 320px;
}
/deep/.upload-container .el-upload:hover {
  border-color: #409eff;
}
.inline {
  display: inline-block;
}
.cropper-btn {
  margin: 10px 0;
}
.copper-img {
  width: 400px;
  height: 300px;
}

.cropper-main {
  flex: none;
  width: 400px;
  height: 300px;
}
.section-main .cropper-preview {
  flex: none;
  text-align: center;
  box-shadow: 0 0 1px 0;
}
@media screen and (max-width: 1080px) {
  .section-main .cropper-preview {
    margin: 0 auto;
  }
}
.upload-modal .notice {
  font-size: 16px;
  display: inline-block;
  vertical-align: top;
  padding: 10px;
}
/deep/ .el-dialog__body {
  padding: 0;
}
/deep/ .el-upload-dragger {
  width: 100%;
  height: 100%;
}
.upload-modal img {
  box-shadow: 0 0 1px 0;
  border-radius: 50%;
  width: 250px;
  height: 250px;
}
.separator {
  display: block;
  position: absolute;
  top: 0;
  bottom: 0;
  left: 50%;
  border: 1px dashed #eee;
}
</style>
