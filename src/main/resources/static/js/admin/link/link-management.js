layui.config({
  base: "/plugins/layui/"
}).extend({
  index: "lib/index"
}).use(["index", "table", "form"], function () {
  let admin = layui.admin
    , table = layui.table
    , form = layui.form;
  let tableIns = table.render({
    elem: "#link-table",
    url: "/api/v2/link/findLinksByPage",
    toolbar: "#toolbar",
    defaultToolbar: ["filter", "exports", "print"],
    title: "友链管理",
    cellMinWidth: 80,
    cols: [[
      {type: "numbers", fixed: "left"},
      {type: "checkbox", fixed: "left"},
      {field: "lid", title: "ID", width: 100, sort: true, fixed: "left", align: "center"},
      {field: "linkName", title: "友链名字", align: "center"},
      {field: "linkUrl", title: "友链地址", align: "center"},
      {field: "deleted", title: "友链状态", width: 150, templet: "#switchTpl", align: "center"},
      {fixed: "right", title: "操作", width: 180, align: "center", toolbar: "#operation-bar"}
    ]],
    page: true,
    limits: [10, 15, 20],
    response: {
      statusCode: 200
    }
  });

  form.on("switch(deleted)", function (obj) {
    let isChecked = obj.elem.checked;
    let lid = obj.value;
    // 因为在判断的时候按钮状态已经被切换了,所以判断条件要取反
    if (!isChecked) {
      layer.open({
        content: "确定关闭该友链吗?",
        btn: ["确定", "取消"],
        yes: function (index, layero) {
          $.ajax({
            type: "POST",
            url: "/api/v2/link/logicalDeleteLink/" + lid,
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
        content: "确定开启该友链吗?",
        btn: ["确定", "取消"],
        yes: function (index, layero) {
          $.ajax({
            type: "POST",
            url: "/api/v2/link/openLink/" + lid,
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
  table.on("toolbar(link-table)", function (obj) {
    let checkStatus = table.checkStatus(obj.config.id);
    switch (obj.event) {
      case "add":
        layer.open({
          type: 2,
          title: "添加友链",
          content: "add-link",
          area: ["420px", "250px"],
          btn: ["确定", "取消"],
          yes: function (index, layero) {
            let iframeWindow = window["layui-layer-iframe" + index],
              submitID = "link-submit",
              submit = layero.find("iframe").contents().find("#" + submitID);
            // 监听提交
            iframeWindow.layui.form.on("submit(" + submitID + ")", function (data) {
              let field = data.field;
              // 提交 Ajax 成功后，静态更新表格中的数据
              $.ajax({
                type: "POST",
                url: "/api/v2/link/saveLink",
                data: {
                  linkName: field.linkName,
                  linkUrl: field.linkUrl
                },
                dataType: "json",
                success: function (res) {
                  layer.msg(res.msg);
                  setTimeout(() => {
                    // 数据刷新
                    table.reload("link-table");
                    layer.close(index);
                  }, 1000);
                }
              });
            });
            submit.trigger("click");
          }
        });
        break;
      case "batch-del":
        let data = checkStatus.data;
        let batchDelIds = [];
        for (const obj of data) {
          batchDelIds.push(obj.lid);
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
              url: "/api/v2/user/verifyOperationPassword/",
              data: {
                uid: uid,
                operationPass: value
              },
              dataType: "json",
              success: function (res) {
                if (res.code === 200) {
                  layer.close(index);
                  layer.confirm("确定删除所选友链吗?删除后这些友链将被删除且无法恢复!", function (index) {
                    // 向服务端发送删除指令,执行 Ajax 后重载
                    $.ajax({
                      type: "DELETE",
                      url: "/api/v2/link/batchDeleteLink",
                      data: {
                        batchDelIdsJSON: JSON.stringify(batchDelIds)
                      },
                      dataType: "json",
                      success: function (res) {
                        if (res.code === 200) {
                          table.reload("link-table");
                        }
                        layer.msg(res.msg);
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
          layer.msg("未选中任何数据!")
        }
        break;
    }
  });

  // 监听工具条
  table.on("tool(link-table)", function (obj) {
    // 获得当前行数据
    let data = obj.data;
    let lid = data.lid;
    let linkName = data.linkName;
    let linkUrl = data.linkUrl;
    let deleted = data.deleted;
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
          url: "/api/v2/user/verifyOperationPassword/",
          data: {
            uid: uid,
            operationPass: value
          },
          dataType: "json",
          success: function (res) {
            if (res.code === 200) {
              layer.close(index);
              layer.confirm("确定删除\"" + linkName + "\"友链吗?删除后该友链将被删除且无法恢复!", function (index) {
                // 向服务端发送删除指令,执行 Ajax 后重载
                $.ajax({
                  type: "DELETE",
                  url: "/api/v2/link/deleteLink/" + lid,
                  success: function (res) {
                    if (res.code === 200) {
                      table.reload("link-table");
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
      layer.open({
        type: 2,
        title: "编辑友链",
        content: "edit-link/" + lid,
        area: ["420px", "250px"],
        btn: ["确定", "取消"],
        yes: function (index, layero) {
          let iframeWindow = window["layui-layer-iframe" + index],
            submitID = "link-submit",
            submit = layero.find("iframe").contents().find("#" + submitID);
          // 监听提交
          iframeWindow.layui.form.on("submit(" + submitID + ")", function (data) {
            let field = data.field;
            // 提交 Ajax 成功后，静态更新表格中的数据
            $.ajax({
              type: "POST",
              url: "/api/v2/link/editLink",
              data: {
                lid: lid,
                linkName: field.linkName,
                linkUrl: field.linkUrl,
                deleted: deleted
              },
              dataType: "json",
              success: function (res) {
                layer.msg(res.msg);
                setTimeout(() => {
                  // 数据刷新
                  table.reload("link-table");
                  layer.close(index);
                }, 1000);
              }
            });
          });
          submit.trigger("click");
        }
      });
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
    let str = "";
    num = String(num);
    length = length || 2;
    for (let i = num.length; i < length; i++) {
      str += "0";
    }
    return num < Math.pow(10, length) ? str + (num | 0) : num;
  };
});
