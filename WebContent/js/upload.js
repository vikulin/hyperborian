function upload(){
	var file = $('input[name="files"]').get(0).files[0];
    if(file.size>5368709120){
    	alert("file size should be less than 5GB.");
    	return;
    }
    var formData = new FormData();
    formData.append('files', file);
    //alert(file.name);
    //formData.append('id', id);
    $.ajax({
        url: '/bt-streaming-service/rest/upload/files',
        type: 'POST',
        data: formData,
        cache: false,
        contentType: false,
        processData: false,
        beforeSend: function (jqXHR) {
            // set request headers here rather than in the ajax 'headers' object
            //jqXHR.setRequestHeader('Accept-Encoding', 'UTF-8');
        },
        success: function(data, textStatus, jqXHR){
        	var torrentFileLocation = jqXHR.getResponseHeader('Location');
        	var infoHash = jqXHR.getResponseHeader('Content-InfoHash');
        	var size = jqXHR.getResponseHeader('Data-Length');
            //alert(torrentFileLocation);
        	//$("#progress").detach();
        	//$(".upload_btn").detach();
        	//$('#torrentLink').attr({href:torrentFileLocation})
        	//$(".hidden").toggleClass("visible");
        	//update db
        	zAu.send(new zk.Event(zk.Widget.$('$upload'), 'onUploadedFile', {'location': torrentFileLocation, 'infoHash': infoHash, 'size': size}, {toServer:true}));
        },
        error: function(response){
            var error = "error";
            if (response.status === 409){
                error = response.responseText;
            }
            alert(response.responseText);
        },
        xhr: function() {
            var myXhr = $.ajaxSettings.xhr();
            if (myXhr.upload) {
                myXhr.upload.addEventListener('progress', progress, false);
            } else {
                console.log('Upload progress is not supported.');
            }
            return myXhr;
        }
    });
}

function progress(e) {
    if (e.lengthComputable) {
        //$('.progress_percent').text(Math.floor((e.loaded * 100) / e.total));
        $('#progress').attr({value:e.loaded,max:e.total});
    }
}