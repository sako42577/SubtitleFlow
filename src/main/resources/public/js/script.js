let currentVideoId = '';

async function processVideo() {
    const settings = readVideoSettings();

    if (!settings.originalUrl) {
        alert('Please enter a valid video URL!');
        return;
    }

    prepareProcessingView();

    try {
        const result = await requestProcessing(settings);
        currentVideoId = result.id;
        showTranscript(result);
    } catch (err) {
        alert('Error: ' + err.message);
        setProcessingButton(false);
        updateStatus('Processing failed.', 'text-rose-400');
    }
}

async function approveAndRender() {
    prepareRenderingView();
    const subtitles = readSubtitleEdits();

    try {
        await requestRendering(currentVideoId, subtitles);
        showDownload(currentVideoId);
    } catch (err) {
        alert('Rendering error: ' + err.message);
        updateStatus('Video build failed.', 'text-rose-400');
    }
}
