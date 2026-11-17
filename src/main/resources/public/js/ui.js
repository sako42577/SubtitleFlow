function readVideoSettings() {
    return {
        originalUrl: $('#videoUrl').val().trim(),
        videoQuality: $('#videoQuality').val(),
        fontSize: $('#fontSize').val(),
        fontColor: $('#fontColor').val()
    };
}

function readSubtitleEdits() {
    return {
        rawText: $('#subText').val(),
        fontSize: $('#fontSize').val()
    };
}

function setProcessingButton(isProcessing) {
    const text = isProcessing
        ? 'Processing & Transcribing...'
        : 'Generate Subtitles & Open Editor';

    $('#processBtn').text(text).prop('disabled', isProcessing);
}

function updateStatus(message, colorClass) {
    $('#statusSection').removeClass('hidden');
    $('#statusText')
        .text(message)
        .attr('class', 'text-sm font-medium ' + colorClass);
}

function prepareProcessingView() {
    $('#editorSection, #finalSection').addClass('hidden');
    setProcessingButton(true);
    updateStatus(
        'Downloading video & running Whisper AI auto-detection...',
        'text-amber-400'
    );
}

function prepareRenderingView() {
    $('#editorSection').addClass('hidden');
    updateStatus('Burning subtitles via FFmpeg...', 'text-amber-400');
}

function showTranscript(result) {
    $('#videoId').text(result.id);
    setProcessingButton(false);
    updateStatus(
        'Transcription ready! Review and edit subtitles below.',
        'text-emerald-400'
    );

    if (result.draftTranscript && result.draftTranscript.rawText) {
        $('#subText').val(result.draftTranscript.rawText);
    }

    $('#editorSection').removeClass('hidden');
}

function showDownload(videoId) {
    updateStatus('Rendering completed successfully!', 'text-emerald-400');
    $('#finalSection').removeClass('hidden');
    $('#downloadBtn').attr('href', '/api/download/' + videoId);
}
