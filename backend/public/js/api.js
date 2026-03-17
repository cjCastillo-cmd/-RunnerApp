/* ═══════════════════════════════════════════
   Runner App — Capa Modelo (API)
   Todas las llamadas al backend
   ═══════════════════════════════════════════ */

const API_BASE = location.origin + '/runner_backend/public/api';

const Api = {
    // Token de autenticacion
    _token: localStorage.getItem('runner_token') || '',

    setToken(t) {
        this._token = t;
        if (t) localStorage.setItem('runner_token', t);
        else localStorage.removeItem('runner_token');
    },

    getToken() {
        return this._token;
    },

    // Peticion generica JSON
    async request(path, method = 'GET', body = null) {
        const headers = { 'Accept': 'application/json', 'Content-Type': 'application/json' };
        if (this._token) headers['Authorization'] = 'Bearer ' + this._token;
        const options = { method, headers };
        if (body && method !== 'GET') options.body = JSON.stringify(body);
        const response = await fetch(API_BASE + path, options);
        return { status: response.status, ...await response.json() };
    },

    // Peticion multipart (subida de archivos)
    async uploadFile(path, formData) {
        const headers = {};
        if (this._token) headers['Authorization'] = 'Bearer ' + this._token;
        const response = await fetch(API_BASE + path, { method: 'POST', headers, body: formData });
        return { status: response.status, ...await response.json() };
    },

    // ─── Auth ───
    login: (email, password) => Api.request('/auth/login', 'POST', { email, password }),
    register: (data) => Api.request('/auth/register', 'POST', data),
    verifyEmail: (email, code) => Api.request('/auth/verify-email', 'POST', { email, code }),
    resendCode: (email) => Api.request('/auth/resend-code', 'POST', { email }),
    forgotPassword: (email) => Api.request('/auth/forgot-password', 'POST', { email }),
    resetPassword: (data) => Api.request('/auth/reset-password', 'POST', data),
    logout: () => Api.request('/auth/logout', 'POST'),

    // ─── Runs ───
    getRuns: () => Api.request('/runs'),
    saveRun: (data) => Api.request('/runs', 'POST', data),
    deleteRun: (id) => Api.request('/runs/' + id, 'DELETE'),

    // ─── Stats ───
    getMonthlyStats: () => Api.request('/stats/monthly'),
    getWeeklyCompare: () => Api.request('/stats/weekly-compare'),
    getMonthlyCompare: () => Api.request('/stats/monthly-compare'),
    getLeaderboard: () => Api.request('/stats/leaderboard'),

    // ─── Friends ───
    getFriends: () => Api.request('/friends'),
    getPending: () => Api.request('/friends/pending'),
    sendRequest: (friendId) => Api.request('/friends/request', 'POST', { friend_id: friendId }),
    respondRequest: (id, status) => Api.request('/friends/' + id + '/respond', 'POST', { status }),

    // ─── Users ───
    getByCountry: () => Api.request('/users/by-country'),

    // ─── Profile ───
    getProfile: () => Api.request('/user/profile'),
    updateProfile: (data) => Api.request('/user/update', 'POST', data),
    uploadPhoto: (formData) => Api.uploadFile('/user/upload-photo', formData),
};
