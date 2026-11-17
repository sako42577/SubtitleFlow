async function postJson(url, data, errorMessage) {
    try {
        return await $.ajax({
            url: url,
            method: 'POST',
            contentType: 'application/json',
            dataType: 'json',
            data: JSON.stringify(data)
        });
    } catch (error) {
        throw new Error(errorMessage);
    }
}

function requestProcessing(settings) {
    return postJson(
        '/api/process',
        settings,
        'Server failed to process the video.'
    );
}

function requestRendering(videoId, subtitles) {
    return postJson(
        '/api/approve?id=' + videoId,
        subtitles,
        'FFmpeg rendering failed on the server.'
    );
}
