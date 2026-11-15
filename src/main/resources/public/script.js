let currentVideoId = "";

async function processVideo() {
    const url = document.getElementById('videoUrl').value.trim();
    const quality = document.getElementById('videoQuality').value;
    const fontSize = document.getElementById('fontSize').value;
    const fontColor = document.getElementById('fontColor').value;

    if (!url) {
        alert("Please enter a valid video URL!");
        return;
    }

    document.getElementById('editorSection').classList.add('hidden');
    document.getElementById('finalSection').classList.add('hidden');

    const processBtn = document.getElementById('processBtn');
    processBtn.innerText = "Processing & Transcribing...";
    processBtn.disabled = true;

    const statusSection = document.getElementById('statusSection');
    statusSection.classList.remove('hidden');

    const statusText = document.getElementById('statusText');
    statusText.innerText = "Downloading video & running Whisper AI auto-detection...";
    statusText.className = "text-sm font-medium text-amber-400";

    try {
        const response = await fetch('/api/process', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                originalUrl: url,
                videoQuality: quality,
                fontSize: fontSize,
                fontColor: fontColor
            })
        });

        if (!response.ok) {
            throw new Error("Server failed to process the video.");
        }

        const result = await response.json();
        currentVideoId = result.id;

        document.getElementById('videoId').innerText = currentVideoId;
        processBtn.innerText = "Generate Subtitles & Open Editor";
        processBtn.disabled = false;

        statusText.innerText = "Transcription ready! Review and edit subtitles below.";
        statusText.className = "text-sm font-medium text-emerald-400";

        if (result.draftTranscript && result.draftTranscript.rawText) {
            document.getElementById('subText').value = result.draftTranscript.rawText;
        }
        document.getElementById('editorSection').classList.remove('hidden');

    } catch (err) {
        alert("Error: " + err.message);
        processBtn.innerText = "Generate Subtitles & Open Editor";
        processBtn.disabled = false;
        statusText.innerText = "Processing failed.";
        statusText.className = "text-sm font-medium text-rose-400";
    }
}

async function approveAndRender() {
    document.getElementById('editorSection').classList.add('hidden');

    const statusSection = document.getElementById('statusSection');
    statusSection.classList.remove('hidden');

    const statusText = document.getElementById('statusText');
    statusText.innerText = "Burning subtitles via FFmpeg...";
    statusText.className = "text-sm font-medium text-amber-400";

    const editedText = document.getElementById('subText').value;
    const fontSize = document.getElementById('fontSize').value;

    try {
        const response = await fetch('/api/approve?id=' + currentVideoId, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                rawText: editedText,
                fontSize: fontSize
            })
        });

        if (!response.ok) {
            throw new Error("FFmpeg rendering failed on the server.");
        }

        const result = await response.json();

        statusText.innerText = "Rendering completed successfully!";
        statusText.className = "text-sm font-medium text-emerald-400";

        document.getElementById('finalSection').classList.remove('hidden');

        const downloadBtn = document.getElementById('downloadBtn');
        downloadBtn.href = `/api/download/${currentVideoId}`;

    } catch (err) {
        alert("Rendering error: " + err.message);
        statusText.innerText = "Video build failed.";
        statusText.className = "text-sm font-medium text-rose-400";
    }
}