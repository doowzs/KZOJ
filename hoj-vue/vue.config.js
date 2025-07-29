const UglifyJsPlugin = require("uglifyjs-webpack-plugin"); // 清除注释
const CompressionWebpackPlugin = require("compression-webpack-plugin"); // 开启压缩

// 是否为生产环境
const isProduction = process.env.NODE_ENV === "production";

// 本地环境是否需要使用cdn
const devNeedCdn = true;

// cdn链接
const cdn = {
  // cdn：模块名称和模块作用域命名（对应window里面挂载的变量名称）
  externals: {
    vue: "Vue",
    "vue-router": "VueRouter",
    axios: "axios",
    vuex: "Vuex",
    "element-ui": "ELEMENT",
    "highlight.js": "hljs",
    moment: "moment",
    "vue-echarts": "VueECharts",
    echarts: "echarts",
    katex: "katex",
    "muse-ui": "MuseUI",
    jquery: "$",
  },
  // cdn的css链接
  css: [
    "https://kzoj.cn/cdn/element-ui/2.15.14/lib/theme-chalk/index.min.css",
    "https://kzoj.cn/cdn/github-markdown-css/5.8.1/github-markdown-light.min.css",
    "https://kzoj.cn/cdn/katex/0.16.22/dist/katex.min.css",
    "https://kzoj.cn/cdn/muse-ui/3.0.2/dist/muse-ui.min.css",
  ],
  // cdn的js链接
  js: [
    "https://kzoj.cn/cdn/axios/1.11.0/dist/axios.min.js",
    "https://kzoj.cn/cdn/highlight.js/11.11.1/highlight.min.js",
    "https://kzoj.cn/cdn/echarts/4.9.0/dist/echarts.min.js",
    "https://kzoj.cn/cdn/jquery/3.7.1/dist/jquery.min.js",
    "https://kzoj.cn/cdn/katex/0.16.22/dist/katex.min.js",
    "https://kzoj.cn/cdn/katex/0.16.22/dist/contrib/auto-render.min.js",
    "https://kzoj.cn/cdn/moment/2.30.1/dist/moment.min.js",
    "https://kzoj.cn/cdn/moment/2.30.1/dist/locale/zh-cn.min.js",
    "https://kzoj.cn/cdn/moment/2.30.1/dist/locale/en-gb.min.js",
    "https://kzoj.cn/cdn/vue/2.7.16/dist/vue.min.js",
    "https://kzoj.cn/cdn/vuex/3.6.2/dist/vuex.min.js",
    "https://kzoj.cn/cdn/vue-echarts/5.0.0-beta.0/vue-echarts.min.js",
    "https://kzoj.cn/cdn/vue-router/3.6.5/dist/vue-router.min.js",
    "https://kzoj.cn/cdn/element-ui/2.15.14/lib/index.min.js", // must be after vue@2
    "https://kzoj.cn/cdn/muse-ui/3.0.2/dist/muse-ui.min.js", // must be after vue@2
  ],
};

module.exports = {
  publicPath: "/",
  assetsDir: "assets",
  devServer: {
    open: true, // npm run serve后自动打开页面
    host: "0.0.0.0", // 匹配本机IP地址(默认是0.0.0.0)
    port: 8066, // 开发服务器运行端口号
    proxy: {
      "/api": {
        //   以'/api'开头的请求会被代理进行转发
        target: "http://localhost:6688", //   要发向的后台服务器地址  如果后台服务跑在后台开发人员的机器上，就写成 `http://ip:port` 如 `http:192.168.12.213:8081`   ip为后台服务器的ip
        changeOrigin: true,
      },
    },
    disableHostCheck: true,
  },
  //去除生产环境的productionSourceMap
  productionSourceMap: false,

  chainWebpack: (config) => {
    // ============注入cdn start============
    config.plugin("html").tap((args) => {
      // 生产环境或本地需要cdn时，才注入cdn
      if (isProduction || devNeedCdn) args[0].cdn = cdn;
      return args;
    });
    config
      .plugin("webpack-bundle-analyzer") // 查看打包文件体积大小
      .use(require("webpack-bundle-analyzer").BundleAnalyzerPlugin);
    // ============注入cdn end============
  },
  configureWebpack: (config) => {
    // 用cdn方式引入，则构建时要忽略相关资源
    const plugins = [];
    if (isProduction || devNeedCdn) {
      config.externals = cdn.externals;
      config.mode = "production";
      config["performance"] = {
        //打包文件大小配置
        maxEntrypointSize: 10000000,
        maxAssetSize: 30000000,
      };
      config.plugins.push(
        new UglifyJsPlugin({
          uglifyOptions: {
            output: {
              comments: false, // 去掉注释
            },
            warnings: false,
            compress: {
              drop_console: false,
              drop_debugger: false,
              // pure_funcs: ['console.log']//移除console
            },
          },
        })
      );
      // 服务器也要相应开启gzip
      config.plugins.push(
        new CompressionWebpackPlugin({
          filename: "[path].gz[query]",
          algorithm: "gzip",
          test: /\.(js|css)$/, // 匹配文件名
          threshold: 10000, // 对超过10k的数据压缩
          deleteOriginalAssets: false, // 不删除源文件
          minRatio: 0.8, // 压缩比
        })
      );
    }
  },
};
