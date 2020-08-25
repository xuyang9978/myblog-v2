$(function () {
  editormd.emoji.path = "http://www.webpagefx.com/tools/emoji-cheat-sheet/graphics/emojis/";
  editormd("my-editormd", {
    emoji: true,
    width: "100%",
    height: 640,
    syncScrolling: "single",
    tocm: true,
    tex: true,
    flowChart: true,
    path: "/plugins/editormd/lib/",
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

function submit() {
  let title = $("#title").val();
  let author = $("#author").val();
  let cid = $("#category").val();
  let content = $("#content").val();
  let htmlContent = $("#htmlContent").val();
  if (verifyTitle(title) && verifyAuthor(author) && verifyCategory(cid) && verifyContent(content)) {
    layer.confirm("确定发布该篇文章吗?", function (index) {
      $.ajax({
        type: "POST",
        url: "/api/v2/article/saveArticle",
        data: {
          title: title,
          author: author,
          cid: cid,
          content: content,
          htmlContent: htmlContent,
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

layui.config({
  base: '/plugins/layui/' //静态资源所在路径
}).extend({
  index: 'lib/index' //主入口模块
}).use(['index', 'form'], function () {

});
