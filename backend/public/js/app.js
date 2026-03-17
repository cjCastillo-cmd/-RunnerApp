/* ═══════════════════════════════════════════
   Runner App — Controlador Principal
   Router, estado global, inicializacion
   ═══════════════════════════════════════════ */

const App = {
    user: JSON.parse(localStorage.getItem('runner_user') || 'null'),
    verifyEmail: '',
    forgotEmail: '',

    // ─── Estado del usuario ───
    setUser(u) {
        this.user = u;
        if (u) localStorage.setItem('runner_user', JSON.stringify(u));
        else localStorage.removeItem('runner_user');
    },

    // ─── Navegacion entre pantallas ───
    showScreen(id, anim = 'fade-in') {
        const prev = document.querySelector('.screen.active');
        if (prev) prev.classList.remove('active', 'slide-in-right', 'slide-in-left', 'fade-in');
        const next = document.getElementById(id);
        next.classList.remove('slide-in-right', 'slide-in-left', 'fade-in');
        next.classList.add('active', anim);
    },

    showApp() {
        document.getElementById('bnav').classList.add('show');
        this.nav('s-home');
        HomeView.load();
    },

    nav(screen) {
        this.showScreen(screen, 'fade-in');
        document.querySelectorAll('.bnav-item').forEach(b => b.classList.toggle('active', b.dataset.s === screen));
        if (screen === 's-home') HomeView.load();
        else if (screen === 's-stats') StatsView.load();
        else if (screen === 's-friends') FriendsView.load();
        else if (screen === 's-profile') ProfileView.load();
    },

    // ─── Splash Screen ───
    showSplash() {
        // Splash animado de 2.2 segundos
        setTimeout(() => {
            const splash = document.getElementById('s-splash');
            splash.style.transition = 'opacity .5s, transform .5s';
            splash.style.opacity = '0';
            splash.style.transform = 'scale(1.1)';
            setTimeout(() => {
                splash.classList.remove('active');
                splash.style.display = 'none';
                // Decidir: si hay sesion ir a app, si no ir a login
                if (Api.getToken() && this.user) {
                    this.showApp();
                } else {
                    this.showScreen('s-login', 'fade-in');
                }
            }, 500);
        }, 2200);
    },

    // ─── Inicializacion ───
    init() {
        // Mostrar splash primero
        this.showSplash();

        // Init sub-vistas
        AuthView.init();
        HomeView.initPTR();

        // Cerrar modal con click afuera
        document.getElementById('modal-all-runs').addEventListener('click', e => {
            if (e.target === e.currentTarget) HomeView.closeAllRuns();
        });
    }
};

// Arrancar al cargar
App.init();
