var Transformer = {
    post: function() {
        var content = $("[name='content']").val()
        var mediaType = $("[name='mediaType']").val()
        $.ajax({
            type: "POST",
            url: window.location,
            data: content,
            contentType: mediaType,
            success: function(r) {alert("Result: "+r)},
            error: function(http,status, error) {alert(error+": "+http.responseText)}
        });
    }
}