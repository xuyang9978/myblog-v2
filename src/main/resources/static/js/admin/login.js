layui.config({
  base: "/plugins/layui/"
}).extend({
  index: "lib/index"
}).use(["index", "user"], function () {
  let $ = layui.$,
    admin = layui.admin,
    form = layui.form,
    router = layui.router(),
    search = router.search;
  form.render();
  // 提交
/*  form.on("submit(LAY-user-login-submit)", function (obj) {
    $.ajax({
      type: "POST",
      url: "/login",
      data: obj.field,
      success: function (res) {
        if (res.code === 200) {
          // 登入成功的提示与跳转
          layer.msg(res.msg, {
            offset: "15px",
            icon: 1,
            time: 1000
          }, function () {
            location.href = "/admin/" + obj.field.username;
          });
        } else {
          layer.msg(res.msg, {
            offset: "15px",
            icon: 1,
            time: 1000
          });
        }
      }
    });
  });*/
});
