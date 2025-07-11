require('dotenv').config();
const express = require('express');
const fetch = require('node-fetch');
const cors = require('cors');
const app = express();
const port = 3000;

app.use(cors());
app.use(express.json());

// A more robust TTS proxy endpoint that handles both direct audio streams and URL-based responses.
app.post('/api/qwen-tts', async (req, res) => {
    console.log('--- Received TTS Request ---');
    const { text, voice = 'dylan' } = req.body; // format and sample_rate removed as they are not in the new docs
    console.log(`Text: "${text}", Voice: "${voice}"`);

    if (!text) {
        console.log('Request rejected: Text is required.');
        return res.status(400).json({ error: 'Text is required' });
    }

    const apiKey = process.env.DASHSCOPE_API_KEY;
    if (!apiKey) {
        console.error('Server Error: DASHSCOPE_API_KEY is not configured.');
        return res.status(500).json({ error: 'API key is not configured on the server.' });
    }

    const url = 'https://dashscope.aliyuncs.com/api/v1/services/aigc/multimodal-generation/generation';

    // Updated params structure according to the latest official documentation
    const params = {
        model: 'qwen-tts-latest',
        input: {
            text: text,
            voice: voice
        }
        // 'parameters' object is removed entirely.
    };

    try {
        console.log(`Sending request to DashScope with body:`, JSON.stringify(params, null, 2));
        const aliResponse = await fetch(url, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${apiKey}`
            },
            body: JSON.stringify(params)
        });

        console.log(`DashScope response status: ${aliResponse.status}`);
        const contentType = aliResponse.headers.get('content-type');
        console.log(`DashScope response Content-Type: ${contentType}`);

        // Case 1: Direct audio stream (as documented for streaming scenarios)
        if (contentType && contentType.startsWith('audio/')) {
            console.log('Received direct audio stream, streaming to client...');
            res.setHeader('Content-Type', contentType);
            aliResponse.body.pipe(res);
        }
        // Case 2: JSON response with an audio URL (as observed in logs for non-streaming)
        else if (contentType && contentType.includes('application/json')) {
            const responseData = await aliResponse.json();

            // Check if the JSON contains a URL to the audio file
            if (responseData.output && responseData.output.audio && responseData.output.audio.url) {
                console.log('Received JSON with audio URL. Fetching audio from that URL...');
                const audioUrl = responseData.output.audio.url;

                // Fetch the audio from the provided URL
                const audioResponse = await fetch(audioUrl);
                if (audioResponse.ok) {
                    console.log('Audio fetched successfully, streaming to client...');
                    const audioContentType = audioResponse.headers.get('content-type') || `audio/${format}`;
                    res.setHeader('Content-Type', audioContentType);
                    audioResponse.body.pipe(res); // Stream the final audio back to the client
                } else {
                    console.error(`Failed to download audio from URL: ${audioUrl}. Status: ${audioResponse.status}`);
                    res.status(502).json({
                        error: 'Failed to download audio file from DashScope.',
                        details: `URL: ${audioUrl}, Status: ${audioResponse.status}`
                    });
                }
            } else {
                // If the JSON does not contain a URL, it's an error message.
                console.error('DashScope API returned a JSON error:', responseData);
                res.status(aliResponse.status).json({
                    error: 'DashScope API error.',
                    details: responseData
                });
            }
        } else {
            // Handle other unexpected content types
            const responseText = await aliResponse.text();
            console.error(`Unexpected Content-Type from DashScope: ${contentType}`);
            res.status(502).json({
                error: 'Unexpected response from TTS service.',
                details: responseText
            });
        }
    } catch (error) {
        console.error('TTS proxy request failed:', error);
        res.status(500).json({
            error: 'Failed to proxy request to TTS service.',
            details: error.message
        });
    }
});

// Deprecated endpoint remains the same
app.post('/api/tts', (req, res) => {
    res.status(410).json({
        error: 'This endpoint is deprecated. Please use /api/qwen-tts instead.',
        documentation: 'https://help.aliyun.com/document_detail/2715422.html'
    });
});

app.listen(port, () => {
    console.log(`TTS proxy server is listening on http://localhost:${port}`);
});
