<template>
  <div class="login">
    <!--    登陆-->
    <el-form
      :model="formLogin"
      :rules="rulesLogin"
      ref="formLogin"
      label-position="left"
      label-width="0px"
      class="demo-ruleForm login-container"
      v-if="mode === 'Login'"
    >
      <h1 class="title">{{ $t("m.Welcome_to_Login") }}</h1>
      <el-form-item prop="username">
        <el-input
          v-model="formLogin.username"
          prefix-icon="el-icon-user-solid"
          :placeholder="$t('m.Login_Username')"
          @keyup.enter.native="handleLogin"
        ></el-input>
      </el-form-item>
      <el-form-item prop="password">
        <el-input
          v-model="formLogin.password"
          prefix-icon="el-icon-lock"
          type="password"
          :placeholder="$t('m.Login_Password')"
          @keyup.enter.native="handleLogin"
        ></el-input>
      </el-form-item>
      <el-form-item style="width: 100%">
        <el-button
          type="primary"
          style="width: 100%"
          @click="handleLogin"
          :loading="btnLoginLoading"
          >{{ $t("m.Login_Btn") }}
        </el-button>
      </el-form-item>
    </el-form>
    <!--    找回密码-->
    <el-form
      :model="formResetPassword"
      :rules="rulesResetPwd"
      ref="formResetPassword"
      v-if="mode === 'ResetPwd'"
      label-position="left"
      label-width="0px"
      class="demo-ruleForm login-container"
    >
      <h1 class="title">{{ $t("m.Welcome_to_Login") }}</h1>
      <el-form-item prop="email">
        <el-input
          v-model="formResetPassword.email"
          prefix-icon="el-icon-message"
          :placeholder="$t('m.Reset_Password_Email')"
        >
        </el-input>
      </el-form-item>
      <el-form-item prop="captcha">
        <div id="captcha">
          <div id="captchaCode">
            <el-input
              v-model="formResetPassword.captcha"
              prefix-icon="el-icon-s-check"
              :placeholder="$t('m.Reset_Password_Captcha')"
            ></el-input>
          </div>
          <div id="captchaImg">
            <el-tooltip content="Click to refresh" placement="top">
              <img :src="captchaSrc" @click="getCaptcha" />
            </el-tooltip>
          </div>
        </div>
      </el-form-item>
      <el-form-item>
        <el-button
          type="primary"
          @click="handleResetPwd"
          :loading="btnResetPwdLoading"
          :disabled="btnResetPwdDisabled"
          style="width: 100%"
        >
          {{ resetText }}
        </el-button>
        <el-link type="primary" @click="switchMode('Login')">{{
          $t("m.Remember_Passowrd_To_Login")
        }}</el-link>
      </el-form-item>
    </el-form>
    <!--    注册-->
    <el-form
      :model="registerForm"
      :rules="rulesRegister"
      ref="registerForm"
      v-if="mode === 'Register'"
      label-position="left"
      label-width="0px"
      class="demo-ruleForm login-container"
    >
      <h1 class="title">{{ $t("m.Welcome_to_Login") }}</h1>
      <el-form-item prop="username">
        <el-input
          v-model="registerForm.username"
          prefix-icon="el-icon-user-solid"
          :placeholder="$t('m.Register_Username')"
          @keyup.enter.native="handleRegister"
          width="100%"
        ></el-input>
      </el-form-item>
      <el-form-item prop="password">
        <el-input
          v-model="registerForm.password"
          prefix-icon="el-icon-lock"
          :placeholder="$t('m.Register_Password')"
          @keyup.enter.native="handleRegister"
          type="password"
        ></el-input>
      </el-form-item>
      <el-form-item prop="passwordAgain">
        <el-input
          v-model="registerForm.passwordAgain"
          prefix-icon="el-icon-lock"
          :placeholder="$t('m.Register_Password_Again')"
          @keyup.enter.native="handleRegister"
          type="password"
        ></el-input>
      </el-form-item>
      <el-form-item prop="email">
        <el-input
          v-model="registerForm.email"
          prefix-icon="el-icon-message"
          :placeholder="$t('m.Register_Email')"
          @keyup.enter.native="handleRegister"
        >
          <el-button
            slot="append"
            icon="el-icon-message"
            type="primary"
            @click.native="sendRegisterEmail"
            :loading="btnEmailLoading"
          >
            <span v-show="btnEmailLoading">{{ countdownNum }}</span>
          </el-button>
        </el-input>
      </el-form-item>
      <el-form-item prop="code">
        <el-input
          v-model="registerForm.code"
          prefix-icon="el-icon-s-check"
          :placeholder="$t('m.Register_Email_Captcha')"
          @keyup.enter.native="handleRegister"
        ></el-input>
      </el-form-item>
      <el-form-item>
        <el-button
          type="primary"
          @click="handleRegister()"
          :loading="btnRegisterLoading"
          style="width: 100%"
        >
          {{ $t("m.Register_Btn") }}
        </el-button>
        <el-link type="primary" @click="switchMode('Login')"
          >{{ $t("m.Register_Already_Registed") }}
        </el-link>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
import api from "@/common/api";
import { default as mMessage, default as myMessage } from "@/common/message";
import { mapActions, mapGetters } from "vuex";

export default {
  name: "LoginHome",
  data() {
    const CheckEmailNotExist = (rule, value, callback) => {
      api.checkUsernameOrEmail(undefined, value).then(
        (res) => {
          if (res.data.data.email === false) {
            callback(new Error(this.$i18n.t("m.The_email_does_not_exists")));
          } else {
            callback();
          }
        },
        (_) => callback()
      );
    };
    const CheckUsernameNotExist = (rule, value, callback) => {
      api.checkUsernameOrEmail(value, undefined).then(
        (res) => {
          if (res.data.data.username === true) {
            callback(new Error(this.$i18n.t("m.The_username_already_exists")));
          } else {
            callback();
          }
        },
        (_) => callback()
      );
    };
    const unCheckEmailNotExist = (rule, value, callback) => {
      api.checkUsernameOrEmail(undefined, value).then(
        (res) => {
          if (res.data.data.email === true) {
            callback(new Error(this.$i18n.t("m.The_email_already_exists")));
          } else {
            callback();
          }
        },
        (_) => callback()
      );
    };
    const CheckPassword = (rule, value, callback) => {
      if (this.registerForm.password !== "") {
        // 对第二个密码框再次验证
        this.$refs.registerForm.validateField("passwordAgain");
      }
      callback();
    };

    const CheckAgainPassword = (rule, value, callback) => {
      if (value !== this.registerForm.password) {
        callback(new Error(this.$i18n.t("m.Password_does_not_match")));
      }
      callback();
    };
    return {
      mode: "Login",
      resetText: "Send Password Reset Email",
      btnResetPwdLoading: false,
      btnResetPwdDisabled: false,
      btnLoginLoading: false,
      btnRegisterLoading: false,
      btnEmailLoading: false,
      countdownNum: null,
      sendEmailError: false,
      formLogin: {
        username: "",
        password: "",
      },
      formResetPassword: {
        captcha: "",
        email: "",
        captchaKey: "",
      },
      registerForm: {
        username: "",
        password: "",
        passwordAgain: "",
        email: "",
        code: "",
      },
      captchaSrc: "",
      rulesLogin: {
        username: [
          {
            required: true,
            message: this.$i18n.t("m.Username_Check_Required"),
            trigger: "blur",
          },
          {
            max: 20,
            message: this.$i18n.t("m.Username_Check_Max"),
            trigger: "blur",
          },
        ],
        password: [
          {
            required: true,
            message: this.$i18n.t("m.Password_Check_Required"),
            trigger: "blur",
          },
          {
            min: 6,
            max: 20,
            message: this.$i18n.t("m.Password_Check_Between"),
            trigger: "blur",
          },
        ],
      },

      rulesResetPwd: {
        captcha: [
          {
            required: true,
            message: this.$i18n.t("m.Code_Check_Required"),
            trigger: "blur",
            min: 1,
            max: 8,
          },
        ],
        email: [
          {
            required: true,
            message: this.$i18n.t("m.Email_Check_Required"),
            type: "email",
            trigger: "blur",
          },
          { validator: CheckEmailNotExist, trigger: "blur" },
        ],
      },

      rulesRegister: {
        username: [
          {
            required: true,
            message: this.$i18n.t("m.Username_Check_Required"),
            trigger: "blur",
          },
          {
            validator: unCheckEmailNotExist,
            trigger: "blur",
            message: this.$i18n.t("m.The_username_already_exists"),
          },
          {
            max: 20,
            message: this.$i18n.t("m.Username_Check_Max"),
            trigger: "blur",
          },
        ],

        email: [
          {
            required: true,
            message: this.$i18n.t("m.Email_Check_Required"),
            trigger: "blur",
          },
          {
            type: "email",
            message: this.$i18n.t("m.Email_Check_Format"),
            trigger: "blur",
          },
          {
            validator: unCheckEmailNotExist,
            message: this.$i18n.t("m.The_email_already_exists"),
            trigger: "blur",
          },
        ],
        password: [
          {
            required: true,
            message: this.$i18n.t("m.Password_Check_Required"),
            trigger: "blur",
          },
          {
            min: 6,
            max: 20,
            message: this.$i18n.t("m.Password_Check_Between"),
            trigger: "blur",
          },
          { validator: CheckPassword, trigger: "blur" },
        ],
        passwordAgain: [
          {
            required: true,
            message: this.$i18n.t("m.Password_Again_Check_Required"),
            trigger: "blur",
          },
          { validator: CheckAgainPassword, trigger: "change" },
        ],
        code: [
          {
            required: true,
            message: this.$i18n.t("m.Code_Check_Required"),
            trigger: "blur",
          },
          {
            min: 6,
            max: 6,
            message: this.$i18n.t("m.Code_Check_Length"),
            trigger: "blur",
          },
        ],
      },
    };
  },
  methods: {
    ...mapActions([
      "changeModalStatus",
      "changeResetTimeOut",
      "startTimeOut",
      "changeRegisterTimeOut",
    ]),
    switchMode(mode) {
      // 清空规则
      if (this.mode === "Login") {
        this.$refs["formLogin"].resetFields();
      }
      if (this.mode === "ResetPwd") {
        this.$refs["formResetPassword"].resetFields();
      }
      if (this.mode === "Register") {
        this.$refs["registerForm"].resetFields();
      }
      // 切换
      this.mode = mode;
      if (this.mode === "ResetPwd") {
        this.resetText = this.$i18n.t("m.Send_Password_Reset_Email");
        this.getCaptcha();
      }
    },
    handleLogin(times) {
      this.$refs["formLogin"].validate((valid) => {
        if (valid) {
          this.btnLoginLoading = true;
          let formData = Object.assign({}, this.formLogin);
          api.login(formData).then(
            (res) => {
              this.btnLoginLoading = false;
              this.changeModalStatus({ visible: false });
              const jwt = res.headers["authorization"];
              this.$store.commit("changeUserToken", jwt);
              this.$store.dispatch("setUserInfo", res.data.data);
              this.$router.push("/home");
              mMessage.success(this.$i18n.t("m.Welcome_Back"));
            },
            (_) => {
              this.btnLoginLoading = false;
            }
          );
        }
      });
    },

    // 密码找回
    getCaptcha() {
      api.getCaptcha().then((res) => {
        this.captchaSrc = res.data.data.img;
        this.formResetPassword.captchaKey = res.data.data.captchaKey;
      });
    },
    countDown() {
      let i = this.timeReset;
      this.resetText = i + "s, " + this.$i18n.t("m.Waiting_Can_Resend_Email");
      if (i == 0) {
        this.btnResetPwdDisabled = false;
        this.resetText = this.$i18n.t("m.Send_Password_Reset_Email");
        return;
      }
      setTimeout(() => {
        this.countDown();
      }, 1000);
    },
    handleResetPwd() {
      this.$refs["formResetPassword"].validate((valid) => {
        if (valid) {
          if (
            !this.formResetPassword.password.match(
              /^(?=.*\d)(?=.*[a-zA-Z]).{6,20}$/
            )
          ) {
            myMessage.warning(this.$i18n.t("m.Pass_Strength"));
            return;
          }
          this.resetText = "Waiting...";
          mMessage.info(this.$i18n.t("m.The_system_is_processing"));
          this.btnResetPwdLoading = true;
          this.btnResetPwdDisabled = true;
          api.applyResetPassword(this.formResetPassword).then(
            (res) => {
              mMessage.message(
                "success",
                this.$i18n.t("m.ResetPwd_Send_Email_Msg"),
                10000
              );
              this.countDown();
              this.startTimeOut({ name: "resetTimeOut" });
              this.btnResetPwdLoading = false;
              this.formResetPassword.captcha = "";
              this.formResetPassword.captchaKey = "";
              this.getCaptcha();
            },
            (err) => {
              this.formResetPassword.captcha = "";
              this.formResetPassword.captchaKey = "";
              this.btnResetPwdLoading = false;
              this.btnResetPwdDisabled = false;
              this.resetText = this.$i18n.t("m.Send_Password_Reset_Email");
              this.getCaptcha();
            }
          );
        }
      });
    },

    // 注册

    countDownRegister() {
      let i = this.timeRegister;
      if (i == 0) {
        this.btnEmailLoading = false;
        return;
      }
      this.countdownNum = i;
      setTimeout(() => {
        this.countDownRegister();
      }, 1000);
    },
    sendRegisterEmail() {
      var emailReg =
        /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
      if (!emailReg.test(this.registerForm.email)) {
        mMessage.error(this.$i18n.t("m.Email_Check_Format"));
        return;
      }
      this.btnEmailLoading = true;
      this.countdownNum = "Waiting...";
      if (this.registerForm.email) {
        mMessage.info(this.$i18n.t("m.The_system_is_processing"));
        api.getRegisterEmail(this.registerForm.email).then(
          (res) => {
            if (res.data.msg != null) {
              mMessage.message(
                "success",
                this.$i18n.t("m.Register_Send_Email_Msg"),
                5000
              );
              this.$notify.success({
                title: this.$i18n.t("m.Success"),
                message: this.$i18n.t("m.Register_Send_Email_Msg"),
                duration: 5000,
                offset: 50,
              });
              this.countDownRegister();
              this.startTimeOut({ name: "registerTimeOut" });
            }
          },
          (res) => {
            this.btnEmailLoading = false;
            this.countdownNum = null;
          }
        );
      }
    },
    handleRegister() {
      this.$refs["registerForm"].validate((valid) => {
        if (valid) {
          if (
            !this.registerForm.password.match(/^(?=.*\d)(?=.*[a-zA-Z]).{6,20}$/)
          ) {
            myMessage.warning(this.$i18n.t("m.Pass_Strength"));
            return;
          }
          const _this = this;
          let formData = Object.assign({}, this.registerForm);
          delete formData["passwordAgain"];
          this.btnRegisterLoading = true;
          api.register(formData).then(
            (res) => {
              mMessage.success(this.$i18n.t("m.Thanks_for_registering"));
              this.switchMode("Login");
              this.btnRegisterLoading = false;
            },
            (res) => {
              this.registerForm.code = "";
              this.btnRegisterLoading = false;
            }
          );
        }
      });
    },
  },
  computed: {
    ...mapGetters([
      "modalStatus",
      "websiteConfig",
      "isAuthenticated",
      "resetTimeOut",
      "registerTimeOut",
    ]),
    visible: {
      get() {
        return this.modalStatus.visible;
      },
      set(value) {
        this.changeModalStatus({ visible: value });
      },
    },
    timeReset: {
      get() {
        return this.resetTimeOut;
      },
      set(value) {
        this.changeResetTimeOut({ timeReset: value });
      },
    },
    timeRegister: {
      get() {
        return this.registerTimeOut;
      },
      set(value) {
        this.changeRegisterTimeOut({ timeRegister: value });
      },
    },
  },
  created() {
    if (this.timeRegister != 60 && this.timeRegister != 0) {
      this.btnEmailLoading = true;
      this.countDownRegister();
    }
  },
};
</script>
<style scoped>
.login {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  background-image: url("~@/assets/banner.jpg");
  background-size: cover;
}
.login-container {
  -webkit-border-radius: 5px;
  border-radius: 5px;
  -moz-border-radius: 5px;
  background-clip: padding-box;
  margin: 180px auto;
  width: 375px;
  padding: 35px 35px 15px 35px;
  background: #fff;
  border: 1px solid #eaeaea;
  box-shadow: 0 0 25px #cac6c6;
}
.login-container .title {
  margin: 0px auto 40px auto;
  text-align: center;
  color: #1e9fff;
  font-size: 25px;
  font-weight: bold;
}
.login-container .remember {
  margin: 0px 0px 35px 0px;
}
#captcha {
  display: flex;
  flex-wrap: nowrap;
  justify-content: space-between;
  width: 100%;
  height: 36px;
}
#captchaCode {
  flex: auto;
}
#captchaImg {
  margin-left: 10px;
  padding: 3px;
  flex: initial;
}
</style>
