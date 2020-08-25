layui.config({
  base: "/plugins/layui/"
}).extend({
  index: "lib/index"
}).use(["index", "table", "form"], function () {
  let admin = layui.admin
    , table = layui.table
    , form = layui.form;
  let tableIns = table.render({
    elem: "#article-table",
    url: "/api/v2/article/findArticlesByPageAndCondition",
    toolbar: "#toolbar",
    defaultToolbar: ["filter", "exports", "print"],
    title: "文章管理",
    cellMinWidth: 80,
    cols: [[
      {type: "numbers", fixed: "left"},
      {type: "checkbox", fixed: "left"},
      {field: "aid", title: "ID", width: 100, sort: true, fixed: "left", align: "center"},
      {field: "title", title: "标题", align: "center"},
      {field: "htmlContent", title: "内容", align: "center"},
      {field: "author", title: "作者", align: "center"},
      {
        field: "createdTime",
        title: "发布时间",
        align: "center",
        sort: true,
        templet: "<div>{{ layui.laytpl.toDateString(d.createdTime) }}</div>"
      },
      {
        field: "updatedTime",
        title: "更新时间",
        align: "center",
        sort: true,
        templet: "<div>{{ layui.laytpl.toDateString(d.updatedTime) }}</div>"
      },
      {
        field: "cid",
        title: "分类名",
        width: 100,
        align: "center",
        templet: "<div>{{ layui.laytpl.categoryNameMappedCId(d.cid) }}</div>"
      },
      {field: "deleted", width: 100, title: "文章状态", templet: "#switchTpl", align: "center"},
      {fixed: "right", title: "操作", width: 180, align: "center", toolbar: "#operation-bar"}
    ]],
    page: true,
    limits: [10, 15, 20],
    response: {
      statusCode: 200
    }
  });

  // 监听搜索
  form.on("submit(article-search)", function (data) {
    let field = data.field;
    // 根据搜索条件来渲染表格
    tableIns.reload({
      // 设定异步数据接口的额外参数
      where: {
        author: field.author,
        title: field.title,
        cid: field.category,
        deleted: field.deleted
      },
      page: {
        // 重新从第 1 页开始
        curr: 1
      }
    });
  });

  form.on("switch(deleted)", function (obj) {
    let isChecked = obj.elem.checked;
    let aid = obj.value;
    // 因为在判断的时候按钮状态已经被切换了,所以判断条件要取反
    if (!isChecked) {
      layer.open({
        content: "确定关闭该文章吗?关闭后博客将无法查看该文章!",
        btn: ["确定", "取消"],
        yes: function (index, layero) {
          $.ajax({
            type: "POST",
            url: "/api/v2/article/logicalDeleteArticle/" + aid,
            success: function (res) {
              obj.elem.checked = res.code !== 200;
              form.render();
              layer.msg(res.msg);
            }
          });
          layer.close(index);
        },
        btn2: function (index, layero) {
          obj.elem.checked = !isChecked;
          form.render();
          layer.close(index);
        },
        cancel: function () {
          obj.elem.checked = !isChecked;
          form.render();
        }
      });
    } else {
      layer.open({
        content: "确定开启该文章吗?开启后博客将可以查看该文章!",
        btn: ["确定", "取消"],
        yes: function (index, layero) {
          $.ajax({
            type: "POST",
            url: "/api/v2/article/openArticle/" + aid,
            success: function (res) {
              obj.elem.checked = res.code === 200;
              form.render();
              layer.msg(res.msg);
            }
          });
          layer.close(index);
        },
        btn2: function (index, layero) {
          obj.elem.checked = !isChecked;
          form.render();
          layer.close(index);
        },
        cancel: function () {
          obj.elem.checked = !isChecked;
          form.render();
        }
      });
    }

  });

  // 头工具栏事件
  table.on("toolbar(article-table)", function (obj) {
    let checkStatus = table.checkStatus(obj.config.id);
    switch (obj.event) {
      case "add":
        location.href = "write-blog";
        break;
      case "batch-del":
        let data = checkStatus.data;
        let batchDelIds = [];
        for (const obj of data) {
          batchDelIds.push(obj.aid);
        }
        if (batchDelIds.length > 0) {
          layer.prompt({
            formType: 1,
            title: "敏感操作,请验证口令"
          }, function (value, index) {
            let uid = $("#uid").val();
            // 验证口令
            $.ajax({
              type: "POST",
              url: "/api/v2/user/verifyOperationPassword",
              data: {
                uid: uid,
                operationPass: value
              },
              dataType: "json",
              success: function (res) {
                if (res.code === 200) {
                  layer.close(index);
                  layer.confirm("确定删除所选文章吗?删除后将无法恢复这些文章!", function (index) {
                    // 向服务端发送删除指令,执行 Ajax 后重载
                    $.ajax({
                      type: "DELETE",
                      url: "/api/v2/article/batchDeleteArticle",
                      data: {
                        batchDelIdsJSON: JSON.stringify(batchDelIds)
                      },
                      dataType: "json",
                      success: function (res) {
                        if (res.code === 200) {
                          table.reload("article-table");
                          layer.msg("已删除");
                        } else {
                          layer.msg(res.msg);
                        }
                      }
                    });
                  });
                } else {
                  layer.msg(res.msg);
                }
              }
            });
          });
        } else {
          layer.msg("未选中任何数据!");
        }
        break;
    }
  });

  // 监听工具条
  table.on("tool(article-table)", function (obj) {
    // 获得当前行数据
    let data = obj.data;
    let aid = data.aid;
    // 事件名字
    let layEvent = obj.event;
    if (layEvent === "del") {
      layer.prompt({
        formType: 1,
        title: "敏感操作,请验证口令"
      }, function (value, index) {
        let uid = $("#uid").val();
        // 验证口令
        $.ajax({
          type: "POST",
          url: "/api/v2/user/verifyOperationPassword",
          data: {
            uid: uid,
            operationPass: value
          },
          dataType: "json",
          success: function (res) {
            if (res.code === 200) {
              layer.close(index);
              layer.confirm("确定删除该篇文章吗?删除后将无法恢复该文章!", function (index) {
                // 向服务端发送删除指令,执行 Ajax 后重载
                $.ajax({
                  type: "DELETE",
                  url: "/api/v2/article/deleteArticle/" + aid,
                  success: function (res) {
                    if (res.code === 200) {
                      table.reload("article-table");
                      layer.msg("已删除");
                    } else {
                      layer.msg(res.msg);
                    }
                  }
                });
              });
            } else {
              layer.msg(res.msg);
            }
          }
        });
      });
    } else if (layEvent === "edit") {
      location.href = "edit-blog?aid=" + aid;
    }
  });

  // 时间戳的处理
  layui.laytpl.toDateString = function (d, format) {
    let date = new Date(d || new Date()),
      ymd = [
        this.digit(date.getFullYear(), 4),
        this.digit(date.getMonth() + 1),
        this.digit(date.getDate())
      ],
      hms = [
        this.digit(date.getHours()),
        this.digit(date.getMinutes()),
        this.digit(date.getSeconds())
      ];
    format = format || "yyyy-MM-dd HH:mm:ss";
    return format.replace(/yyyy/g, ymd[0])
      .replace(/MM/g, ymd[1])
      .replace(/dd/g, ymd[2])
      .replace(/HH/g, hms[0])
      .replace(/mm/g, hms[1])
      .replace(/ss/g, hms[2]);
  };
  // 数字前置补零
  layui.laytpl.digit = function (num, length, end) {
    let str = '';
    num = String(num);
    length = length || 2;
    for (let i = num.length; i < length; i++) {
      str += "0";
    }
    return num < Math.pow(10, length) ? str + (num | 0) : num;
  };

  let isFirst = true;
  let categoryMap;
  layui.laytpl.categoryNameMappedCId = function (d) {
    if (isFirst) {
      $.ajax({
        async: false,
        type: "GET",
        url: "/api/v2/category/findAllCategoriesMap",
        timeout: 5000,
        success: function (res) {
          categoryMap = res;
        }
      });
      isFirst = false;
    }
    return categoryMap[d];
  }
});
