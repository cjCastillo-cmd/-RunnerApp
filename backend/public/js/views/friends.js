/* ═══════════════════════════════════════════
   Runner App — Vista/Controlador: Friends
   Social, leaderboard, solicitudes, descubrir
   ═══════════════════════════════════════════ */

const FriendsView = {
    async load() {
        showSkeleton('leaderboard', 3);
        showSkeleton('friends-list', 2);
        showSkeleton('discover-list', 2);

        const [lb, fr, ds, pn] = await Promise.all([
            Api.getLeaderboard(),
            Api.getFriends(),
            Api.getByCountry(),
            Api.getPending()
        ]);

        // Solicitudes pendientes
        const pendingSection = document.getElementById('pending-section');
        const pendingList = document.getElementById('pending-list');
        if (pn.success && pn.data.length) {
            pendingSection.style.display = 'block';
            pendingList.innerHTML = pn.data.map(req =>
                `<div class="friend-item">
                    <div class="lb-avatar">${(req.user?.name || '?')[0]}</div>
                    <div><div class="lb-name">${req.user?.name || 'Desconocido'}</div><div class="lb-country">Quiere ser tu amigo</div></div>
                    <div class="pending-actions">
                        <button type="button" class="accept-btn" onclick="FriendsView.respond(${req.id},'accepted',this)">Aceptar</button>
                        <button type="button" class="reject-btn" onclick="FriendsView.respond(${req.id},'rejected',this)">Rechazar</button>
                    </div>
                </div>`).join('');
        } else {
            pendingSection.style.display = 'none';
        }

        // Leaderboard
        const lbEl = document.getElementById('leaderboard');
        if (lb.success && lb.data.length) {
            lbEl.innerHTML = lb.data.map(u =>
                `<div class="lb-item ${u.is_me ? 'is-me' : ''}">
                    <span class="lb-pos">${u.position}</span>
                    <div class="lb-avatar">${u.photo_url ? `<img src="${u.photo_url}" alt="${u.name}">` : (u.name || '?')[0]}</div>
                    <div><div class="lb-name">${u.name}${u.is_me ? ' (Tu)' : ''}</div><div class="lb-country">${u.country}</div></div>
                    <span class="lb-km">${u.km_this_month} km</span>
                </div>`).join('');
        } else {
            lbEl.innerHTML = '<div class="empty">Sin datos</div>';
        }

        // Mis amigos
        const fEl = document.getElementById('friends-list');
        if (fr.success && fr.data.length) {
            fEl.innerHTML = fr.data.map(f =>
                `<div class="friend-item">
                    <div class="lb-avatar">${(f.friend?.name || '?')[0]}</div>
                    <div><div class="lb-name">${f.friend?.name || 'N/A'}</div><div class="lb-country">${f.friend?.total_km || 0} km</div></div>
                </div>`).join('');
        } else {
            fEl.innerHTML = '<div class="empty">Sin amigos aun</div>';
        }

        // Descubrir
        const dEl = document.getElementById('discover-list');
        if (ds.success && ds.data.length) {
            dEl.innerHTML = ds.data.map(u =>
                `<div class="friend-item">
                    <div class="lb-avatar">${(u.name || '?')[0]}</div>
                    <div><div class="lb-name">${u.name}</div><div class="lb-country">${u.total_km} km · ${u.country}</div></div>
                    <button type="button" class="add-btn" onclick="FriendsView.addFriend(${u.id},this)">AGREGAR</button>
                </div>`).join('');
        } else {
            dEl.innerHTML = '<div class="empty">No hay corredores</div>';
        }
    },

    async addFriend(id, btn) {
        btn.disabled = true;
        btn.textContent = '...';
        const r = await Api.sendRequest(id);
        btn.textContent = 'ENVIADO';
        if (r.success) toast('Solicitud enviada', 'info');
        else toast('Error al enviar solicitud', 'error');
    },

    async respond(id, status, btn) {
        const row = btn.closest('.friend-item');
        row.style.opacity = '.5';
        const r = await Api.respondRequest(id, status);
        if (r.success) {
            row.style.transition = 'all .3s';
            row.style.transform = 'translateX(100%)';
            row.style.opacity = '0';
            setTimeout(() => row.remove(), 300);
            toast(status === 'accepted' ? 'Amigo aceptado!' : 'Solicitud rechazada', status === 'accepted' ? 'success' : 'info');
            setTimeout(() => this.load(), 500);
        } else {
            row.style.opacity = '1';
            toast('Error al responder', 'error');
        }
    }
};
