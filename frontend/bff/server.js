const express = require('express');
const { createProxyMiddleware } = require('http-proxy-middleware');
const path = require('path');

const app = express();
const PORT = process.env.PORT || 3000;
const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:8080';

// Proxy API requests to Spring Boot backend
app.use(createProxyMiddleware({ 
    target: BACKEND_URL, 
    changeOrigin: true,
    pathFilter: '/api',
    logger: console,
    on: {
        proxyReq: (proxyReq, req, res) => {
            console.log(`[PROXY] ${req.method} ${req.url} -> ${BACKEND_URL}${req.url}`);
        },
        error: (err, req, res) => {
            console.error('[PROXY ERROR]', err.message);
            res.writeHead(502, { 'Content-Type': 'application/json' });
            res.end(JSON.stringify({ error: 'Proxy error', message: err.message }));
        }
    }
}));

// Serve Angular static files
app.use(express.static(path.join(__dirname, 'public')));

// SPA fallback - all non-API, non-static requests get index.html
app.get('*', (req, res) => {
    res.sendFile(path.join(__dirname, 'public', 'index.html'));
});

app.listen(PORT, () => {
    console.log(`==============================================`);
    console.log(`BFF Server running on port ${PORT}`);
    console.log(`Backend URL: ${BACKEND_URL}`);
    console.log(`==============================================`);
});
