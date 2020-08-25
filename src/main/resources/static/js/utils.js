function verifyTitle(title) {
  title = title.trim();
  if (title !== "" && title != null) {
    return true;
  } else {
    layer.msg("必须填写标题");
    return false;
  }
}

function verifyAuthor(author) {
  author = author.trim();
  if (author !== "" && author != null) {
    return true;
  } else {
    layer.msg("必须填写作者");
    return false;
  }
}

function verifyCategory(category) {
  if (category !== "") {
    return true;
  } else {
    layer.msg("必须选择文章分类");
    return false;
  }
}

function verifyContent(content) {
  if (content !== "" && content != null) {
    return true;
  } else {
    layer.msg("必须填写文章内容");
    return false;
  }
}

/* 获取当前地址栏中指定名字的参数值 */
function getQueryString(name) {
  let reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
  let r = window.location.search.substr(1).match(reg);
  if (r != null) {
    return unescape(r[2]);
  }
  return null;
}
