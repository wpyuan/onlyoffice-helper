var config;
function loadJs(jsUrl, callback) {
    const s = document.createElement('script');
    s.type = 'text/javascript';
    s.src = jsUrl;
    document.head.appendChild(s);

    if(typeof(callback) != "undefined"){
        if(s.readyState){
            s.onreadystatechange = function(){
                if(s.readyState == "loaded" || s.readyState == "complete"){
                    s.onreadystatechange = null;
                    callback();
                }
            }
        } else {
            s.onload = function(){
                callback();
            }
        }
    }
}

function initData() {
    var fileModel;
    var history;
    var historyData;

    $.ajax({
        url: '/editor/loadData?fileName=' + queryString("fileName") + '&uid=' + queryString("uid") +
            '&uname=' + queryString("uname") + '&actionLink=' + queryString("actionLink") +
            '&mode=' + queryString("mode") + '&type=' + queryString("type") + '&lang=' + queryString("language"),
        type: 'get',
        async: false,
        success: function (res) {
            if (res == null) {
                return;
            }
            fileModel = res.model;
            history = res.history;
            historyData = res.historyData;
            loadJs(res.apiJsUrl, function () {
                initConfig(fileModel, history, historyData);
            });

        }
    });
}

function initConfig(fileModel, history, historyData) {
    config = JSON.parse(fileModel);
    config.width = "100%";
    config.height = "100%";
    config.events = {
        "onAppReady": onAppReady,
        "onDocumentStateChange": onDocumentStateChange,
        'onRequestEditRights': onRequestEditRights,
        "onError": onError,
        "onOutdatedVersion": onOutdatedVersion,
        "onMakeActionLink": onMakeActionLink,
    };

    if ((history && history.trim() != '') && (historyData && historyData.trim() != '')) {
        config.events['onRequestHistory'] = function () {
            docEditor.refreshHistory(JSON.parse(history));
        };
        config.events['onRequestHistoryData'] = function (event) {
            var ver = event.data;
            var histData = JSON.parse(historyData);
            docEditor.setHistoryData(histData[ver]);
        };
        config.events['onRequestHistoryClose'] = function () {
            document.location.reload();
        };
    }
}

function queryString(name) {
    var result = location.search.match(new RegExp("[\?\&]" + name + "=([^\&]+)", "i"));
    if (result == null || result.length < 1) {
        return "";
    }
    return result[1];
}

initData();