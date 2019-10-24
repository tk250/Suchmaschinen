$(document).ready(function(){
    document.getElementById("result").innerHTML = "42;"
    $("#input").keyup(function() {
        var query = $("#input").val();
        var host = window.location.hostname;
        var port = window.location.port;
        var url = "http://" + host +":" + port + "/api?query=" + query;
        console.log(url);
        $.get(url, function(response) {
            console.log(response);
            $("#result").html(response)
        })
    })
})
