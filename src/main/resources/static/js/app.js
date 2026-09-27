/**
 * LinkNest Studio Application Logic
 */

const studioState = {
    profile: null,
    shortUrls: [],
    analytics: null,
    selectedTheme: 'midnight',
    currentQrText: window.location.origin + '/p/alex'
};

document.addEventListener('DOMContentLoaded', () => {
    initNavigation();
    initProfileBuilder();
    initQrStudio();
    initAnalytics();

    loadProfile();
    loadShortUrls();
    loadAnalytics();
});

// ----------------------------------------------------
// Navigation
// ----------------------------------------------------
function initNavigation() {
    const tabButtons = document.querySelectorAll('.nav-tab-btn');
    const tabPanels = document.querySelectorAll('.tab-panel');

    tabButtons.forEach(btn => {
        btn.addEventListener('click', () => {
            const targetId = btn.getAttribute('data-tab');

            tabButtons.forEach(b => b.classList.remove('active'));
            tabPanels.forEach(p => p.classList.remove('active'));

            btn.classList.add('active');
            document.getElementById(targetId).classList.add('active');

            if (targetId === 'tab-analytics') {
                loadAnalytics();
            }
        });
    });
}

// ----------------------------------------------------
// Tab 1: Bio-Page Builder & Live Phone Mockup
// ----------------------------------------------------
function initProfileBuilder() {
    // Form Inputs real-time listener to update Phone Mockup
    const inputName = document.getElementById('inputDisplayName');
    const inputBio = document.getElementById('inputBio');
    const inputAvatar = document.getElementById('inputAvatarUrl');

    inputName.addEventListener('input', () => {
        document.getElementById('mockupName').textContent = inputName.value.trim() || 'Your Name';
    });

    inputBio.addEventListener('input', () => {
        document.getElementById('mockupBio').textContent = inputBio.value.trim() || '';
    });

    inputAvatar.addEventListener('input', () => {
        updateMockupAvatar(inputAvatar.value.trim(), inputName.value.trim());
    });

    // Theme Picker
    const themeButtons = document.querySelectorAll('.theme-option');
    themeButtons.forEach(btn => {
        btn.addEventListener('click', () => {
            themeButtons.forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            studioState.selectedTheme = btn.getAttribute('data-theme');
            applyMockupTheme(studioState.selectedTheme);
        });
    });

    // Save Profile
    document.getElementById('btnSaveProfile').addEventListener('click', async () => {
        if (!studioState.profile) return;

        const updated = {
            displayName: inputName.value.trim(),
            bio: inputBio.value.trim(),
            avatarUrl: inputAvatar.value.trim(),
            theme: studioState.selectedTheme,
            socials: {
                github: document.getElementById('inputSocialGithub').value.trim(),
                linkedin: document.getElementById('inputSocialLinkedin').value.trim(),
                twitter: document.getElementById('inputSocialTwitter').value.trim(),
                email: document.getElementById('inputSocialEmail').value.trim()
            }
        };

        try {
            const res = await fetch('/api/profile', {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(updated)
            });

            if (!res.ok) throw new Error('Save failed');
            studioState.profile = await res.json();
            showToast('Profile saved successfully! ✨');
            updateMockupPreview();
        } catch (err) {
            showToast('Error saving profile: ' + err.message);
        }
    });

    // Links Modal
    const modal = document.getElementById('linkModal');
    const btnOpenAdd = document.getElementById('btnOpenAddLinkModal');
    const btnCloseModal = document.getElementById('btnCloseModal');
    const linkForm = document.getElementById('linkForm');

    btnOpenAdd.addEventListener('click', () => {
        document.getElementById('modalTitle').textContent = 'Add Bio Link';
        document.getElementById('editLinkId').value = '';
        document.getElementById('linkInputTitle').value = '';
        document.getElementById('linkInputUrl').value = '';
        document.getElementById('linkInputIcon').value = 'globe';
        modal.style.display = 'flex';
    });

    btnCloseModal.addEventListener('click', () => {
        modal.style.display = 'none';
    });

    linkForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const id = document.getElementById('editLinkId').value;
        const title = document.getElementById('linkInputTitle').value.trim();
        const url = document.getElementById('linkInputUrl').value.trim();
        const icon = document.getElementById('linkInputIcon').value;

        try {
            let res;
            if (id) {
                // Update
                res = await fetch(`/api/links/${id}`, {
                    method: 'PUT',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ title, url, icon, enabled: true })
                });
            } else {
                // Add
                res = await fetch('/api/links', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ title, url, icon, enabled: true })
                });
            }

            if (!res.ok) throw new Error('Failed to save link');
            studioState.profile = await res.json();
            modal.style.display = 'none';
            renderBuilderLinks();
            updateMockupPreview();
            showToast('Link saved! 🔗');
        } catch (err) {
            showToast('Error: ' + err.message);
        }
    });
}

async function loadProfile() {
    try {
        const res = await fetch('/api/profile');
        if (!res.ok) return;
        const profile = await res.json();
        studioState.profile = profile;
        studioState.selectedTheme = profile.theme || 'midnight';

        // Populate Form
        document.getElementById('inputDisplayName').value = profile.displayName || '';
        document.getElementById('inputBio').value = profile.bio || '';
        document.getElementById('inputAvatarUrl').value = profile.avatarUrl || '';

        // Select Theme
        document.querySelectorAll('.theme-option').forEach(b => {
            b.classList.toggle('active', b.getAttribute('data-theme') === studioState.selectedTheme);
        });

        // Socials
        if (profile.socials) {
            document.getElementById('inputSocialGithub').value = profile.socials.github || '';
            document.getElementById('inputSocialLinkedin').value = profile.socials.linkedin || '';
            document.getElementById('inputSocialTwitter').value = profile.socials.twitter || '';
            document.getElementById('inputSocialEmail').value = profile.socials.email || '';
        }

        // Public button href
        document.getElementById('btnViewPublic').href = `/p/${profile.username}`;
        studioState.currentQrText = window.location.origin + `/p/${profile.username}`;
        updateQrPreview();

        renderBuilderLinks();
        updateMockupPreview();
    } catch (err) {
        console.error('Failed to load profile:', err);
    }
}

function renderBuilderLinks() {
    const list = document.getElementById('builderLinksList');
    list.innerHTML = '';

    if (!studioState.profile || !studioState.profile.links || studioState.profile.links.length === 0) {
        list.innerHTML = '<div style="color:var(--text-dim);font-size:0.85rem;padding:8px;">No links added yet. Click "+ Add New Link" above.</div>';
        return;
    }

    studioState.profile.links.forEach(link => {
        const item = document.createElement('div');
        item.className = 'builder-link-item';
        item.innerHTML = `
            <div class="link-item-left">
                <div>
                    <div class="link-title-text">${escapeHtml(link.title)}</div>
                    <div class="link-url-text">${escapeHtml(link.url)}</div>
                </div>
            </div>
            <div class="link-item-actions">
                <span class="link-clicks-badge">${link.clicks || 0} clicks</span>
                <button class="btn btn-sm btn-secondary btn-edit-link">Edit</button>
                <button class="btn btn-sm btn-danger btn-delete-link">Delete</button>
            </div>
        `;

        item.querySelector('.btn-edit-link').addEventListener('click', () => {
            document.getElementById('modalTitle').textContent = 'Edit Bio Link';
            document.getElementById('editLinkId').value = link.id;
            document.getElementById('linkInputTitle').value = link.title;
            document.getElementById('linkInputUrl').value = link.url;
            document.getElementById('linkInputIcon').value = link.icon || 'globe';
            document.getElementById('linkModal').style.display = 'flex';
        });

        item.querySelector('.btn-delete-link').addEventListener('click', async () => {
            if (!confirm(`Delete "${link.title}"?`)) return;
            try {
                const res = await fetch(`/api/links/${link.id}`, { method: 'DELETE' });
                if (!res.ok) throw new Error('Delete failed');
                studioState.profile = await res.json();
                renderBuilderLinks();
                updateMockupPreview();
                showToast('Link removed.');
            } catch (err) {
                showToast('Error: ' + err.message);
            }
        });

        list.appendChild(item);
    });
}

function updateMockupPreview() {
    if (!studioState.profile) return;
    const p = studioState.profile;

    document.getElementById('mockupName').textContent = p.displayName || p.username;
    document.getElementById('mockupHandle').textContent = `@${p.username}`;
    document.getElementById('mockupBio').textContent = p.bio || '';
    updateMockupAvatar(p.avatarUrl, p.displayName || p.username);
    applyMockupTheme(studioState.selectedTheme);

    // Mockup Socials
    const socRow = document.getElementById('mockupSocials');
    socRow.innerHTML = '';
    if (p.socials) {
        for (const [platform, url] of Object.entries(p.socials)) {
            if (url && url.trim()) {
                const a = document.createElement('a');
                a.href = '#';
                a.className = 'social-icon-btn';
                a.innerHTML = getSocialIconSvg(platform);
                socRow.appendChild(a);
            }
        }
    }

    // Mockup Links
    const linksStack = document.getElementById('mockupLinks');
    linksStack.innerHTML = '';
    if (p.links) {
        p.links.filter(l => l.enabled).forEach(link => {
            const card = document.createElement('div');
            card.className = 'bio-link-card';
            card.innerHTML = `
                <div class="link-content">
                    <div class="link-icon-box">${getLinkIconSvg(link.icon)}</div>
                    <span>${escapeHtml(link.title)}</span>
                </div>
                <svg class="arrow-icon" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                    <line x1="7" y1="17" x2="17" y2="7"></line>
                    <polyline points="7 7 17 7 17 17"></polyline>
                </svg>
            `;
            linksStack.appendChild(card);
        });
    }
}

function updateMockupAvatar(url, name) {
    const img = document.getElementById('mockupAvatar');
    if (url) {
        img.src = url;
    } else {
        img.src = `https://ui-avatars.com/api/?name=${encodeURIComponent(name || 'Alex')}&background=6366f1&color=fff&size=150`;
    }
}

function applyMockupTheme(themeName) {
    const screen = document.getElementById('mockupScreen');
    screen.className = `phone-screen theme-${themeName || 'midnight'}`;
}

// ----------------------------------------------------
// Tab 2: Short Links & QR Studio
// ----------------------------------------------------
function initQrStudio() {
    const form = document.getElementById('shortUrlForm');
    const fgColor = document.getElementById('qrFgColor');
    const bgColor = document.getElementById('qrBgColor');
    const sizeSelect = document.getElementById('qrSizeSelect');

    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        const targetUrl = document.getElementById('inputTargetUrl').value.trim();
        const title = document.getElementById('inputUrlTitle').value.trim();
        const code = document.getElementById('inputCustomSlug').value.trim();

        try {
            const res = await fetch('/api/urls', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ targetUrl, title, code })
            });

            if (!res.ok) {
                const errData = await res.json();
                throw new Error(errData.error || 'Failed to create short link');
            }

            const newUrl = await res.json();
            showToast(`Created /r/${newUrl.code}! 🚀`);
            form.reset();

            studioState.currentQrText = window.location.origin + `/r/${newUrl.code}`;
            updateQrPreview();
            loadShortUrls();
        } catch (err) {
            showToast('Error: ' + err.message);
        }
    });

    fgColor.addEventListener('input', updateQrPreview);
    bgColor.addEventListener('input', updateQrPreview);
    sizeSelect.addEventListener('change', updateQrPreview);

    document.getElementById('btnCopyShortUrl').addEventListener('click', () => {
        navigator.clipboard.writeText(studioState.currentQrText).then(() => {
            showToast('Copied URL to clipboard! 📋');
        });
    });
}

function updateQrPreview() {
    const fg = document.getElementById('qrFgColor').value.replace('#', '');
    const bg = document.getElementById('qrBgColor').value.replace('#', '');
    const size = document.getElementById('qrSizeSelect').value;

    const qrUrl = `/api/qr?text=${encodeURIComponent(studioState.currentQrText)}&fg=${fg}&bg=${bg}&size=${size}`;
    const img = document.getElementById('qrPreviewImg');
    img.src = qrUrl;

    document.getElementById('qrTargetLabel').textContent = studioState.currentQrText;
    document.getElementById('btnDownloadQr').href = qrUrl;
}

async function loadShortUrls() {
    try {
        const res = await fetch('/api/urls');
        if (!res.ok) return;
        studioState.shortUrls = await res.json();

        const tbody = document.getElementById('shortUrlsTableBody');
        tbody.innerHTML = '';

        if (studioState.shortUrls.length === 0) {
            tbody.innerHTML = '<tr><td colspan="5" style="color:var(--text-dim);text-align:center;padding:16px;">No short links created yet.</td></tr>';
            return;
        }

        studioState.shortUrls.forEach(item => {
            const tr = document.createElement('tr');
            const fullShortUrl = window.location.origin + `/r/${item.code}`;

            tr.innerHTML = `
                <td>
                    <a href="${fullShortUrl}" target="_blank" class="short-link-badge">/r/${escapeHtml(item.code)}</a>
                    <div style="font-size:0.75rem;color:var(--text-dim);">${escapeHtml(item.title || '')}</div>
                </td>
                <td style="max-width:240px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;">
                    <a href="${escapeHtml(item.targetUrl)}" target="_blank" style="color:var(--text-muted);text-decoration:none;">${escapeHtml(item.targetUrl)}</a>
                </td>
                <td><strong style="color:var(--cyan);font-family:'Fira Code',monospace;">${item.totalClicks}</strong></td>
                <td style="color:var(--text-dim);font-size:0.78rem;">${new Date(item.createdAt).toLocaleDateString()}</td>
                <td>
                    <button class="btn btn-sm btn-secondary btn-qr-this" title="Show QR">QR</button>
                    <button class="btn btn-sm btn-danger btn-delete-url" title="Delete">✕</button>
                </td>
            `;

            tr.querySelector('.btn-qr-this').addEventListener('click', () => {
                studioState.currentQrText = fullShortUrl;
                updateQrPreview();
                showToast(`Loaded QR for /r/${item.code}`);
            });

            tr.querySelector('.btn-delete-url').addEventListener('click', async () => {
                if (!confirm(`Delete short link /r/${item.code}?`)) return;
                try {
                    const dRes = await fetch(`/api/urls/${item.code}`, { method: 'DELETE' });
                    if (!dRes.ok) throw new Error('Delete failed');
                    loadShortUrls();
                    showToast('Short link deleted.');
                } catch (err) {
                    showToast('Error: ' + err.message);
                }
            });

            tbody.appendChild(tr);
        });
    } catch (err) {
        console.error('Failed to load short URLs:', err);
    }
}

// ----------------------------------------------------
// Tab 3: Live Analytics Dashboard
// ----------------------------------------------------
function initAnalytics() {
    document.getElementById('btnRefreshAnalytics').addEventListener('click', () => {
        loadAnalytics();
        showToast('Analytics refreshed! 📊');
    });
}

async function loadAnalytics() {
    try {
        const res = await fetch('/api/analytics');
        if (!res.ok) return;
        const data = await res.json();
        studioState.analytics = data;

        // KPI values
        document.getElementById('valPageViews').textContent = data.totalPageViews.toLocaleString();
        document.getElementById('valBioClicks').textContent = data.totalBioLinkClicks.toLocaleString();
        document.getElementById('valShortClicks').textContent = data.totalShortUrlClicks.toLocaleString();
        document.getElementById('valGrandTotal').textContent = data.grandTotalInteractions.toLocaleString();

        // Device distribution
        const devs = data.deviceDistribution || {};
        const mobileClicks = devs['Mobile'] || 0;
        const desktopClicks = devs['Desktop'] || 0;
        const tabletClicks = devs['Tablet'] || 0;
        const totalDevClicks = Math.max(1, mobileClicks + desktopClicks + tabletClicks);

        const mobPct = Math.round((mobileClicks / totalDevClicks) * 100);
        const deskPct = Math.round((desktopClicks / totalDevClicks) * 100);
        const tabPct = Math.round((tabletClicks / totalDevClicks) * 100);

        document.getElementById('devMobilePercent').textContent = `${mobPct}% (${mobileClicks} clicks)`;
        document.getElementById('barMobile').style.width = `${mobPct}%`;

        document.getElementById('devDesktopPercent').textContent = `${deskPct}% (${desktopClicks} clicks)`;
        document.getElementById('barDesktop').style.width = `${deskPct}%`;

        document.getElementById('devTabletPercent').textContent = `${tabPct}% (${tabletClicks} clicks)`;
        document.getElementById('barTablet').style.width = `${tabPct}%`;

        // Top Performing Links
        const topList = document.getElementById('topLinksList');
        topList.innerHTML = '';
        if (data.topLinks && data.topLinks.length > 0) {
            data.topLinks.forEach((link, idx) => {
                const li = document.createElement('li');
                li.className = 'top-item-row';
                li.innerHTML = `
                    <div style="display:flex;align-items:center;gap:10px;">
                        <span style="font-weight:700;color:var(--text-dim);width:16px;">#${idx + 1}</span>
                        <span>${escapeHtml(link.title)}</span>
                    </div>
                    <strong style="color:var(--primary);font-family:'Fira Code',monospace;">${link.clicks} clicks</strong>
                `;
                topList.appendChild(li);
            });
        } else {
            topList.innerHTML = '<li style="color:var(--text-dim);font-size:0.85rem;">No link clicks recorded yet.</li>';
        }

        // Live Feed Table
        const feedBody = document.getElementById('activityFeedBody');
        feedBody.innerHTML = '';
        if (data.recentActivity && data.recentActivity.length > 0) {
            data.recentActivity.forEach(ev => {
                const tr = document.createElement('tr');
                const badgeColor = ev.targetType === 'PROFILE_VIEW' ? '#818cf8' : (ev.targetType === 'BIO_LINK' ? '#34d399' : '#22d3ee');
                tr.innerHTML = `
                    <td><span style="color:${badgeColor};font-weight:600;font-size:0.75rem;">${ev.targetType}</span></td>
                    <td><strong>${escapeHtml(ev.targetTitle || ev.targetId)}</strong></td>
                    <td>${ev.deviceType === 'Mobile' ? '📱 Mobile' : (ev.deviceType === 'Desktop' ? '💻 Desktop' : '📟 Tablet')}</td>
                    <td style="color:var(--text-dim);">${escapeHtml(ev.referrer || 'Direct')}</td>
                    <td style="color:var(--text-dim);font-size:0.75rem;">${formatTimeAgo(ev.timestamp)}</td>
                `;
                feedBody.appendChild(tr);
            });
        } else {
            feedBody.innerHTML = '<tr><td colspan="5" style="color:var(--text-dim);text-align:center;padding:16px;">No recent interactions.</td></tr>';
        }
    } catch (err) {
        console.error('Failed to load analytics:', err);
    }
}

// ----------------------------------------------------
// Helpers
// ----------------------------------------------------
function showToast(msg) {
    const toast = document.getElementById('toast');
    toast.textContent = msg;
    toast.classList.add('show');
    setTimeout(() => {
        toast.classList.remove('show');
    }, 3000);
}

function formatTimeAgo(isoString) {
    if (!isoString) return '-';
    const date = new Date(isoString);
    const now = new Date();
    const sec = Math.floor((now - date) / 1000);
    if (sec < 60) return `${sec}s ago`;
    const min = Math.floor(sec / 60);
    if (min < 60) return `${min}m ago`;
    const hr = Math.floor(min / 60);
    if (hr < 24) return `${hr}h ago`;
    return date.toLocaleDateString();
}

function escapeHtml(str) {
    if (!str) return '';
    return str.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
}

function getLinkIconSvg(iconName) {
    const icon = (iconName || 'globe').toLowerCase();
    switch (icon) {
        case 'github':
            return '<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 19c-5 1.5-5-2.5-7-3m14 6v-3.87a3.37 3.37 0 0 0-.94-2.61c3.14-.35 6.44-1.54 6.44-7A5.44 5.44 0 0 0 20 4.77 5.07 5.07 0 0 0 19.91 1S18.73.65 16 2.48a13.38 13.38 0 0 0-7 0C6.27.65 5.09 1 5.09 1A5.07 5.07 0 0 0 5 4.77a5.44 5.44 0 0 0-1.5 3.78c0 5.42 3.3 6.61 6.44 7A3.37 3.37 0 0 0 9 18.13V22"></path></svg>';
        case 'linkedin':
            return '<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M16 8a6 6 0 0 1 6 6v7h-4v-7a2 2 0 0 0-2-2 2 2 0 0 0-2 2v7h-4v-7a6 6 0 0 1 6-6z"></path><rect x="2" y="9" width="4" height="12"></rect><circle cx="4" cy="4" r="2"></circle></svg>';
        case 'youtube':
            return '<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22.54 6.42a2.78 2.78 0 0 0-1.94-2C18.88 4 12 4 12 4s-6.88 0-8.6.46a2.78 2.78 0 0 0-1.94 2A29 29 0 0 0 1 11.75a29 29 0 0 0 .46 5.33A2.78 2.78 0 0 0 3.4 19c1.72.46 8.6.46 8.6.46s6.88 0 8.6-.46a2.78 2.78 0 0 0 1.94-2 29 29 0 0 0 .46-5.25 29 29 0 0 0-.46-5.33z"></path><polygon points="9.75 15.02 15.5 11.75 9.75 8.48 9.75 15.02"></polygon></svg>';
        case 'newspaper':
        case 'medium':
        case 'blog':
            return '<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"></path><path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"></path></svg>';
        case 'star':
            return '<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon></svg>';
        default:
            return '<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"></circle><line x1="2" y1="12" x2="22" y2="12"></line><path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"></path></svg>';
    }
}

function getSocialIconSvg(platform) {
    return getLinkIconSvg(platform);
}
