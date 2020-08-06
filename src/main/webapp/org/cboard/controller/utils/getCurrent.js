var loginName = "";
$(function () {
    loginName = sessionStorage.getItem("currentLoginName");
    $.post("commons/setCurrentLoginName.do",{loginName:loginName,flag:"1"},function (data) {
        $("#shouSuo").click();
        $("#header").hide();
        $("#aside").hide();
        $("#footer").hide();
        if(loginName != 'admin' && loginName != ""){

        }else {
            $("#header").show();
            $("#aside").show();
            $("#footer").show();
        }
    });
});
