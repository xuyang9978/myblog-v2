layui.config({
  base: "/plugins/layui/"
}).extend({
  index: "lib/index"
}).use(["index", "form"], function () {
  let $ = layui.$,
    form = layui.form;
})
