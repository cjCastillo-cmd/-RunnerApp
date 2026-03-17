/* ═══════════════════════════════════════════
   Runner App — Vista/Controlador: Home
   Dashboard, stats rapidos, historial
   ═══════════════════════════════════════════ */

const HomeView = {
    allRunsData: [],

    async load() {
        if (!App.user) return;
        document.getElementById('home-name').textContent = App.user.name.split(' ')[0].toUpperCase();
        showSkeleton('runs-list', 3);

        const [mo, ru] = await Promise.all([Api.getMonthlyStats(), Api.getRuns()]);

        if (mo.success) {
            animateCount(document.getElementById('h-km'), parseFloat(mo.data.total_km), 1);
            animateCount(document.getElementById('h-runs'), parseInt(mo.data.total_runs));
            animateCount(document.getElementById('h-cal'), parseInt(mo.data.total_calories));
            document.getElementById('h-pace').textContent = formatPace(parseFloat(mo.data.avg_pace));
        }

        const list = document.getElementById('runs-list');
        if (ru.success && ru.data.data.length) {
            this.allRunsData = ru.data.data;
            document.getElementById('view-all-btn').style.display = this.allRunsData.length > 3 ? 'block' : 'none';
            list.innerHTML = ru.data.data.slice(0, 5).map((r, i) => this.renderRunItem(r, i, false)).join('');
        } else {
            this.allRunsData = [];
            document.getElementById('view-all-btn').style.display = 'none';
            list.innerHTML = '<div class="empty">Aun no has corrido. Presiona START!</div>';
        }
    },

    renderRunItem(r, i, showDelete = false) {
        const m = Math.floor(r.duration_sec / 60);
        const s = r.duration_sec % 60;
        const d = new Date(r.created_at).toLocaleDateString('es', { day: 'numeric', month: 'short' });
        const delBtn = showDelete
            ? `<button type="button" class="run-delete" onclick="HomeView.deleteRun(${r.id}, this)" title="Eliminar"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg></button>`
            : '';
        const runData = JSON.stringify(r).replace(/'/g, "\\'").replace(/"/g, '&quot;');
        return `<div class="run-item" style="animation-delay:${i * 0.05}s;cursor:pointer" onclick="HomeView.openRunDetail(${r.id})">
            <div class="run-dot"></div>
            <div class="run-data"><div class="run-km">${parseFloat(r.distance_km).toFixed(2)} KM</div><div class="run-detail">${m}min ${s}s</div></div>
            <div class="run-right">
                <div class="run-info"><div class="run-cal">${r.calories}</div><div class="run-date">${d}</div></div>
                ${delBtn}
            </div>
        </div>`;
    },

    // ─── Modal historial completo ───
    async openAllRuns() {
        const modal = document.getElementById('modal-all-runs');
        modal.classList.add('show');
        showSkeleton('all-runs-list', 5);
        const ru = await Api.getRuns();
        const listEl = document.getElementById('all-runs-list');
        if (ru.success && ru.data.data.length) {
            this.allRunsData = ru.data.data;
            listEl.innerHTML = ru.data.data.map((r, i) => this.renderRunItem(r, i, true)).join('');
        } else {
            listEl.innerHTML = '<div class="empty">Sin carreras registradas</div>';
        }
    },

    closeAllRuns() {
        document.getElementById('modal-all-runs').classList.remove('show');
    },

    async deleteRun(id, btn) {
        if (!confirm('Eliminar esta carrera?')) return;
        btn.disabled = true;
        const r = await Api.deleteRun(id);
        if (r.success) {
            const row = btn.closest('.run-item');
            row.style.transition = 'all .3s';
            row.style.transform = 'translateX(100%)';
            row.style.opacity = '0';
            setTimeout(() => { row?.remove(); toast('Carrera eliminada'); }, 300);
            this.load();
        } else {
            btn.disabled = false;
            toast('Error al eliminar', 'error');
        }
    },

    // ─── Abrir detalle de carrera con mapa ───
    openRunDetail(runId) {
        const run = this.allRunsData.find(r => r.id === runId);
        if (run) RunningView.openDetail(run);
    },

    // ─── Pull to Refresh ───
    initPTR() {
        let startY = 0, pulling = false;
        const container = document.getElementById('screens-container');
        const indicator = document.getElementById('ptr-indicator');

        container.addEventListener('touchstart', e => {
            const homeScreen = document.getElementById('s-home');
            if (!homeScreen.classList.contains('active')) return;
            if (container.scrollTop > 5) return;
            startY = e.touches[0].clientY;
            pulling = true;
        }, { passive: true });

        container.addEventListener('touchmove', e => {
            if (!pulling) return;
            const diff = e.touches[0].clientY - startY;
            if (diff > 10 && diff < 120) {
                indicator.classList.add('pulling');
                document.getElementById('ptr-text').textContent = diff > 60 ? 'Soltar para actualizar' : 'Desliza para actualizar';
            }
        }, { passive: true });

        container.addEventListener('touchend', () => {
            if (!pulling) return;
            pulling = false;
            if (indicator.classList.contains('pulling')) {
                indicator.classList.remove('pulling');
                indicator.classList.add('refreshing');
                document.getElementById('ptr-text').textContent = 'Actualizando...';
                HomeView.load().then(() => {
                    indicator.classList.remove('refreshing');
                    toast('Actualizado', 'info');
                });
            }
        });
    }
};
