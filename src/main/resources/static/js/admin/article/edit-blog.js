function submit() {
  let aid = getQueryString("aid");
  let title = $("#title").val();
  let author = $("#author").val();
  let cid = $("#category").val();
  let content = $("#content").val();
  let htmlContent = $("#htmlContent").val();
  let deleted = $("#deleted").val();
  if (verifyTitle(title) && verifyAuthor(author) && verifyCategory(cid) && verifyContent(content)) {
    layer.confirm("确定修改并发布该篇文章吗?", function (index) {
      $.ajax({
        type: "POST",
        url: "/api/v2/article/updateArticle",
        data: {
          aid: aid,
          title: title,
          author: author,
          cid: cid,
          content: content,
          htmlContent: htmlContent,
          deleted: deleted
        },
        dataType: "json",
        success: function (res) {
          layer.msg(res.msg);
          if (res.code === 200) {
            setTimeout(function () {
              location.href = "article-management";
            }, 1000);
          }
        }
      });
      layer.close(index);
    });
  }
}

$(function () {
  editormd.emoji.path = "http://www.webpagefx.com/tools/emoji-cheat-sheet/graphics/emojis/";
  editormd("editormd", {
    emoji: true,
    width: "100%",
    height: 640,
    syncScrolling: "single",
    //你的lib目录的路径，我这边用JSP做测试的
    tocm: true, // Using [TOCM]
    tex: true, // 开启科学公式TeX语言支持，默认关闭
    flowChart: true, // 开启流程图支持，默认关闭
    path: "/plugins/editormd/lib/",
    //这个配置在simple.html中并没有，但是为了能够提交表单，使用这个配置可以让构造出来的HTML代码直接在第二个隐藏的textarea域中，方便post提交表单。
    saveHTMLToTextarea: true,
    imageUpload: true,
    imageFormats: ["jpg", "jpeg", "gif", "png", "bmp", "webp"],
    imageUploadURL: "uploadImg",
    onload: function () {
      this.width("100%");
      this.height("100%");
    }
  });
});

layui.config({
  base: '/plugins/layui/' //静态资源所在路径
}).extend({
  index: 'lib/index' //主入口模块
}).use(['index', 'form'], function () {

});
