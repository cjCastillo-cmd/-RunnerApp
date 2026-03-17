/* ═══════════════════════════════════════════
   Runner App — Utilidades compartidas
   Toast, animaciones, skeleton, helpers
   ═══════════════════════════════════════════ */

// ─── Toast Notifications ───
function toast(msg, type = 'success') {
    const container = document.getElementById('toast-container');
    const icons = { success: '&#10003;', error: '&#10007;', info: '&#8505;' };
    const el = document.createElement('div');
    el.className = 'toast toast-' + type;
    el.innerHTML = `<span class="toast-icon">${icons[type] || ''}</span><span>${msg}</span>`;
    el.onclick = () => removeToast(el);
    container.appendChild(el);
    setTimeout(() => removeToast(el), 3500);
}

function removeToast(el) {
    if (!el.parentNode) return;
    el.classList.add('removing');
    setTimeout(() => el.remove(), 300);
}

// ─── Animacion de conteo ───
function animateCount(el, target, decimals = 0, duration = 600, suffix = '') {
    const start = parseFloat(el.textContent) || 0;
    const diff = target - start;
    if (Math.abs(diff) < 0.01) { el.textContent = target.toFixed(decimals) + suffix; return; }
    const startTime = performance.now();
    function step(now) {
        const elapsed = now - startTime;
        const progress = Math.min(elapsed / duration, 1);
        const eased = 1 - Math.pow(1 - progress, 3);
        el.textContent = (start + diff * eased).toFixed(decimals) + suffix;
        if (progress < 1) requestAnimationFrame(step);
    }
    requestAnimationFrame(step);
}

// ─── Skeleton Loading ───
function showSkeleton(containerId, count = 3) {
    const el = document.getElementById(containerId);
    let html = '';
    for (let i = 0; i < count; i++) {
        html += `<div style="display:flex;align-items:center;gap:16px;padding:16px 28px;border-top:1px solid var(--border)">
            <div class="skeleton skeleton-circle"></div>
            <div style="flex:1"><div class="skeleton skeleton-line w80"></div><div class="skeleton skeleton-line w40"></div></div>
        </div>`;
    }
    el.innerHTML = html;
}

// ─── Alertas inline ───
function showAlert(id, msg, type = 'error') {
    const el = document.getElementById(id);
    el.textContent = typeof msg === 'string' ? msg : Object.values(msg).flat().join(', ');
    el.className = 'alert show alert-' + type;
}

// ─── Ripple en botones ───
document.addEventListener('click', e => {
    const btn = e.target.closest('.btn, .add-btn, .start-btn');
    if (!btn) return;
    const rect = btn.getBoundingClientRect();
    btn.style.setProperty('--ripple-x', ((e.clientX - rect.left) / rect.width * 100) + '%');
    btn.style.setProperty('--ripple-y', ((e.clientY - rect.top) / rect.height * 100) + '%');
    btn.classList.add('ripple');
    setTimeout(() => btn.classList.remove('ripple'), 400);
});

// ─── Toggle de visibilidad de contrasena ───
function initPasswordToggles() {
    document.querySelectorAll('.pass-toggle').forEach(btn => {
        btn.addEventListener('click', () => {
            const input = btn.parentElement.querySelector('input');
            const isPassword = input.type === 'password';
            input.type = isPassword ? 'text' : 'password';
            btn.querySelector('.eye-open').style.display = isPassword ? 'none' : '';
            btn.querySelector('.eye-closed').style.display = isPassword ? '' : 'none';
            btn.setAttribute('aria-label', isPassword ? 'Ocultar contrasena' : 'Mostrar contrasena');
        });
    });
}

// ─── Formato de pace ───
function formatPace(pace) {
    if (!pace || pace <= 0) return '-';
    return Math.floor(pace) + ':' + String(Math.round((pace % 1) * 60)).padStart(2, '0');
}

// ─── Formato de tiempo ───
function formatTime(seconds) {
    const h = Math.floor(seconds / 3600);
    const m = Math.floor((seconds % 3600) / 60);
    const s = seconds % 60;
    return String(h).padStart(2, '0') + ':' + String(m).padStart(2, '0') + ':' + String(s).padStart(2, '0');
}

function formatTimeShort(seconds) {
    const m = Math.floor(seconds / 60);
    const s = seconds % 60;
    return String(m).padStart(2, '0') + ':' + String(s).padStart(2, '0');
}
