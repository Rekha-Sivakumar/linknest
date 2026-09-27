/**
 * LinkNest URL Shortener & Dynamic QR Code Studio
 */

const state = {
    urls: [],
    currentUrl: null
};

document.addEventListener('DOMContentLoaded', () => {
    initForm();
    initQrCustomizer();
    loadUrls();
});

function initForm() {
    const form = document.getElementById('urlForm');
    const targetInput = document.getElementById('targetUrlInput');
    const slugInput = document.getElementById('customSlugInput');
    const titleInput = document.getElementById('titleInput');

    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        const targetUrl = targetInput.value.trim();
        const code = slugInput.value.trim();
        const title = titleInput.value.trim();

        if (!targetUrl) return;

        try {
            const res = await fetch('/api/urls', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ targetUrl, code, title })
            });

            if (!res.ok) {
                const errData = await res.json();
                throw new Error(errData.error || 'Failed to shorten URL');
            }

            const created = await res.json();
            showToast('Short link and QR code generated! 🚀');

            form.reset();
            displayResult(created);
            loadUrls();
        } catch (err) {
            showToast('Error: ' + err.message);
        }
    });

    // Copy Short URL button
    document.getElementById('btnCopyShortUrl').addEventListener('click', () => {
        const urlInput = document.getElementById('resultShortUrl');
        navigator.clipboard.writeText(urlInput.value).then(() => {
            const btn = document.getElementById('btnCopyShortUrl');
            const originalHtml = btn.innerHTML;
            btn.innerHTML = '✓ Copied!';
            showToast('Short URL copied to clipboard! 📋');
            setTimeout(() => { btn.innerHTML = originalHtml; }, 2000);
        });
    });

    // Copy QR Image to Clipboard
    document.getElementById('btnCopyQrImage').addEventListener('click', async () => {
        const img = document.getElementById('qrImagePreview');
        try {
            const res = await fetch(img.src);
            const blob = await res.blob();
            await navigator.clipboard.write([
                new ClipboardItem({ 'image/png': blob })
            ]);
            showToast('QR code image copied to clipboard! 🖼️');
        } catch (err) {
            showToast('Could not copy image directly. Use download button instead.');
        }
    });
}

function initQrCustomizer() {
    const fg = document.getElementById('pickerFg');
    const bg = document.getElementById('pickerBg');
    const size = document.getElementById('selectSize');

    fg.addEventListener('input', updateQrDisplay);
    bg.addEventListener('input', updateQrDisplay);
    size.addEventListener('change', updateQrDisplay);
}

function displayResult(item) {
    state.currentUrl = item;

    const resultCard = document.getElementById('resultCard');
    resultCard.style.display = 'block';
    resultCard.scrollIntoView({ behavior: 'smooth', block: 'nearest' });

    const fullShortUrl = window.location.origin + `/r/${item.code}`;

    document.getElementById('resultTitle').textContent = item.title || `Link /r/${item.code}`;
    document.getElementById('resultShortUrl').value = fullShortUrl;
    document.getElementById('resultClicksBadge').textContent = `${item.totalClicks || 0} clicks`;

    const destLink = document.getElementById('resultTargetUrl');
    destLink.textContent = item.targetUrl;
    destLink.href = item.targetUrl;

    document.getElementById('btnTestUrl').href = fullShortUrl;

    updateQrDisplay();
}

function updateQrDisplay() {
    if (!state.currentUrl) return;

    const fullShortUrl = window.location.origin + `/r/${state.currentUrl.code}`;
    const fg = document.getElementById('pickerFg').value.replace('#', '');
    const bg = document.getElementById('pickerBg').value.replace('#', '');
    const size = document.getElementById('selectSize').value;

    const qrApiUrl = `/api/qr?text=${encodeURIComponent(fullShortUrl)}&fg=${fg}&bg=${bg}&size=${size}`;

    const qrImg = document.getElementById('qrImagePreview');
    qrImg.src = qrApiUrl;

    const downloadBtn = document.getElementById('btnDownloadQr');
    downloadBtn.href = qrApiUrl;
    downloadBtn.download = `linknest-qr-${state.currentUrl.code}.png`;
}

async function loadUrls() {
    try {
        const res = await fetch('/api/urls');
        if (!res.ok) return;
        state.urls = await res.json();

        document.getElementById('headerTotalCount').textContent = state.urls.length;

        const tbody = document.getElementById('linksTableBody');
        tbody.innerHTML = '';

        if (state.urls.length === 0) {
            tbody.innerHTML = '<tr><td colspan="5" style="text-align:center;color:var(--text-dim);padding:24px;">No short links created yet. Create one above!</td></tr>';
            return;
        }

        state.urls.forEach(item => {
            const tr = document.createElement('tr');
            const fullShortUrl = window.location.origin + `/r/${item.code}`;

            tr.innerHTML = `
                <td>
                    <a href="${fullShortUrl}" target="_blank" class="short-code-link">/r/${escapeHtml(item.code)}</a>
                    <div style="font-size:0.75rem;color:var(--text-dim);">${escapeHtml(item.title || '')}</div>
                </td>
                <td class="dest-url-cell">
                    <a href="${escapeHtml(item.targetUrl)}" target="_blank" style="color:var(--text-secondary);text-decoration:none;">${escapeHtml(item.targetUrl)}</a>
                </td>
                <td><span class="clicks-badge">${item.totalClicks}</span></td>
                <td style="color:var(--text-dim);font-size:0.78rem;">${new Date(item.createdAt).toLocaleDateString()}</td>
                <td>
                    <div class="action-cell">
                        <button class="btn-table-action btn-show-qr" title="Show QR">Show QR</button>
                        <button class="btn-table-action btn-copy-link" title="Copy Short Link">Copy</button>
                        <button class="btn btn-danger btn-delete-link" title="Delete">✕</button>
                    </div>
                </td>
            `;

            tr.querySelector('.btn-show-qr').addEventListener('click', () => {
                displayResult(item);
                showToast(`Viewing QR for /r/${item.code}`);
            });

            tr.querySelector('.btn-copy-link').addEventListener('click', () => {
                navigator.clipboard.writeText(fullShortUrl).then(() => {
                    showToast(`Copied /r/${item.code} to clipboard! 📋`);
                });
            });

            tr.querySelector('.btn-delete-link').addEventListener('click', async () => {
                if (!confirm(`Delete short link /r/${item.code}?`)) return;
                try {
                    const dRes = await fetch(`/api/urls/${item.code}`, { method: 'DELETE' });
                    if (!dRes.ok) throw new Error('Delete failed');
                    showToast('Link deleted.');
                    if (state.currentUrl && state.currentUrl.code === item.code) {
                        document.getElementById('resultCard').style.display = 'none';
                        state.currentUrl = null;
                    }
                    loadUrls();
                } catch (err) {
                    showToast('Error: ' + err.message);
                }
            });

            tbody.appendChild(tr);
        });

        // If no current selection, display first one as example
        if (!state.currentUrl && state.urls.length > 0) {
            displayResult(state.urls[0]);
        }
    } catch (err) {
        console.error('Failed to load URLs:', err);
    }
}

function showToast(msg) {
    const toast = document.getElementById('toast');
    toast.textContent = msg;
    toast.classList.add('show');
    setTimeout(() => {
        toast.classList.remove('show');
    }, 3000);
}

function escapeHtml(str) {
    if (!str) return '';
    return str.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
}
