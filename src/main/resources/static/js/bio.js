document.addEventListener('DOMContentLoaded', async () => {
    // Extract username from /p/{username}
    const pathParts = window.location.pathname.split('/');
    const username = pathParts[pathParts.length - 1] || 'alex';

    try {
        const res = await fetch(`/api/profile/${encodeURIComponent(username)}`);
        if (!res.ok) {
            document.body.innerHTML = '<div style="text-align:center;padding:50px;"><h2>Profile Not Found</h2></div>';
            return;
        }

        const profile = await res.json();
        renderProfile(profile);
    } catch (err) {
        console.error('Failed to load profile:', err);
    }
});

function renderProfile(profile) {
    document.title = `${profile.displayName || profile.username} | LinkNest`;

    // Apply theme
    const validThemes = ['theme-midnight', 'theme-cyberpunk', 'theme-sunset', 'theme-emerald', 'theme-minimal'];
    document.body.className = `theme-${profile.theme || 'midnight'}`;

    // Elements
    document.getElementById('bioName').textContent = profile.displayName || profile.username;
    document.getElementById('bioHandle').textContent = `@${profile.username}`;
    document.getElementById('bioDescription').textContent = profile.bio || '';

    const avatar = document.getElementById('bioAvatar');
    if (profile.avatarUrl) {
        avatar.src = profile.avatarUrl;
    } else {
        avatar.src = `https://ui-avatars.com/api/?name=${encodeURIComponent(profile.displayName || profile.username)}&background=6366f1&color=fff&size=200`;
    }

    // Socials Row
    const socialsRow = document.getElementById('socialsRow');
    socialsRow.innerHTML = '';
    if (profile.socials) {
        for (const [platform, url] of Object.entries(profile.socials)) {
            if (url && url.trim()) {
                const a = document.createElement('a');
                a.href = url.startsWith('http') || url.startsWith('mailto') ? url : `https://${url}`;
                a.target = '_blank';
                a.rel = 'noopener noreferrer';
                a.className = 'social-icon-btn';
                a.title = platform.charAt(0).toUpperCase() + platform.slice(1);
                a.innerHTML = getSocialIconSvg(platform);
                socialsRow.appendChild(a);
            }
        }
    }

    // Links Stack
    const linksContainer = document.getElementById('linksContainer');
    linksContainer.innerHTML = '';

    if (profile.links && profile.links.length > 0) {
        profile.links.filter(l => l.enabled).forEach(link => {
            const card = document.createElement('a');
            // Route through click counter
            card.href = `/click/${link.id}`;
            card.target = '_blank';
            card.rel = 'noopener noreferrer';
            card.className = 'bio-link-card';

            card.innerHTML = `
                <div class="link-content">
                    <div class="link-icon-box">
                        ${getLinkIconSvg(link.icon)}
                    </div>
                    <span>${escapeHtml(link.title)}</span>
                </div>
                <svg class="arrow-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                    <line x1="7" y1="17" x2="17" y2="7"></line>
                    <polyline points="7 7 17 7 17 17"></polyline>
                </svg>
            `;
            linksContainer.appendChild(card);
        });
    } else {
        linksContainer.innerHTML = '<div style="opacity:0.6;font-size:0.9rem;text-align:center;">No links yet.</div>';
    }
}

function getLinkIconSvg(iconName) {
    const icon = (iconName || 'globe').toLowerCase();
    switch (icon) {
        case 'github':
            return '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 19c-5 1.5-5-2.5-7-3m14 6v-3.87a3.37 3.37 0 0 0-.94-2.61c3.14-.35 6.44-1.54 6.44-7A5.44 5.44 0 0 0 20 4.77 5.07 5.07 0 0 0 19.91 1S18.73.65 16 2.48a13.38 13.38 0 0 0-7 0C6.27.65 5.09 1 5.09 1A5.07 5.07 0 0 0 5 4.77a5.44 5.44 0 0 0-1.5 3.78c0 5.42 3.3 6.61 6.44 7A3.37 3.37 0 0 0 9 18.13V22"></path></svg>';
        case 'linkedin':
            return '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M16 8a6 6 0 0 1 6 6v7h-4v-7a2 2 0 0 0-2-2 2 2 0 0 0-2 2v7h-4v-7a6 6 0 0 1 6-6z"></path><rect x="2" y="9" width="4" height="12"></rect><circle cx="4" cy="4" r="2"></circle></svg>';
        case 'youtube':
            return '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22.54 6.42a2.78 2.78 0 0 0-1.94-2C18.88 4 12 4 12 4s-6.88 0-8.6.46a2.78 2.78 0 0 0-1.94 2A29 29 0 0 0 1 11.75a29 29 0 0 0 .46 5.33A2.78 2.78 0 0 0 3.4 19c1.72.46 8.6.46 8.6.46s6.88 0 8.6-.46a2.78 2.78 0 0 0 1.94-2 29 29 0 0 0 .46-5.25 29 29 0 0 0-.46-5.33z"></path><polygon points="9.75 15.02 15.5 11.75 9.75 8.48 9.75 15.02"></polygon></svg>';
        case 'newspaper':
        case 'medium':
        case 'blog':
            return '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"></path><path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"></path></svg>';
        case 'star':
            return '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon></svg>';
        default:
            return '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"></circle><line x1="2" y1="12" x2="22" y2="12"></line><path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"></path></svg>';
    }
}

function getSocialIconSvg(platform) {
    return getLinkIconSvg(platform);
}

function escapeHtml(str) {
    if (!str) return '';
    return str.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
}
