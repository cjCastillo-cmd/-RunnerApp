/* ═══════════════════════════════════════════
   Runner App — Vista/Controlador: Auth
   Login, Register, Verify, Forgot, Reset
   Con validacion por campo
   ═══════════════════════════════════════════ */

const AuthView = {

    // ─── Validacion por campo ───
    clearFieldErrors(prefix) {
        document.querySelectorAll(`#s-${prefix} .input`).forEach(inp => inp.classList.remove('error'));
        document.querySelectorAll(`#s-${prefix} .field-error`).forEach(el => { el.classList.remove('show'); el.textContent = ''; });
    },

    setFieldError(inputId, msg) {
        const inp = document.getElementById(inputId);
        if (!inp) return;
        inp.classList.add('error');
        let errEl = inp.parentElement.querySelector('.field-error');
        if (!errEl) {
            errEl = document.createElement('div');
            errEl.className = 'field-error';
            inp.parentElement.appendChild(errEl);
        }
        errEl.textContent = msg;
        errEl.classList.add('show');
    },

    validateLogin() {
        this.clearFieldErrors('login');
        let valid = true;
        const email = document.getElementById('login-email').value.trim();
        const pass = document.getElementById('login-pass').value;
        if (!email) { this.setFieldError('login-email', 'Email es requerido'); valid = false; }
        else if (!email.includes('@')) { this.setFieldError('login-email', 'Email no valido'); valid = false; }
        if (!pass) { this.setFieldError('login-pass', 'Contrasena es requerida'); valid = false; }
        else if (pass.length < 6) { this.setFieldError('login-pass', 'Minimo 6 caracteres'); valid = false; }
        return valid;
    },

    validateRegister() {
        this.clearFieldErrors('register');
        let valid = true;
        const name = document.getElementById('reg-name').value.trim();
        const email = document.getElementById('reg-email').value.trim();
        const pass = document.getElementById('reg-pass').value;
        const pass2 = document.getElementById('reg-pass2').value;
        const country = document.getElementById('reg-country').value.trim();

        if (!name) { this.setFieldError('reg-name', 'Nombre es requerido'); valid = false; }
        if (!email) { this.setFieldError('reg-email', 'Email es requerido'); valid = false; }
        else if (!email.includes('@')) { this.setFieldError('reg-email', 'Email no valido'); valid = false; }
        if (!country) { this.setFieldError('reg-country', 'Pais es requerido'); valid = false; }
        if (!pass) { this.setFieldError('reg-pass', 'Contrasena es requerida'); valid = false; }
        else if (pass.length < 6) { this.setFieldError('reg-pass', 'Minimo 6 caracteres'); valid = false; }
        if (pass !== pass2) { this.setFieldError('reg-pass2', 'Las contrasenas no coinciden'); valid = false; }
        return valid;
    },

    // ─── Login ───
    async doLogin() {
        if (!this.validateLogin()) return;

        const btn = document.getElementById('login-btn');
        btn.disabled = true;
        btn.innerHTML = '<span class="spinner"></span>';

        const r = await Api.login(
            document.getElementById('login-email').value.trim(),
            document.getElementById('login-pass').value
        );

        btn.disabled = false;
        btn.textContent = 'INICIAR SESION';

        if (!r.success) return showAlert('login-alert', r.message);

        Api.setToken(r.data.token);
        App.setUser(r.data.user);
        toast('Bienvenido, ' + (r.data.user.name || ''));
        App.showApp();
    },

    // ─── Register ───
    async doRegister() {
        if (!this.validateRegister()) return;

        const btn = document.getElementById('reg-btn');
        btn.disabled = true;
        btn.innerHTML = '<span class="spinner"></span>';

        const r = await Api.register({
            name: document.getElementById('reg-name').value.trim(),
            email: document.getElementById('reg-email').value.trim(),
            password: document.getElementById('reg-pass').value,
            password_confirmation: document.getElementById('reg-pass2').value,
            country: document.getElementById('reg-country').value.trim()
        });

        btn.disabled = false;
        btn.textContent = 'CREAR CUENTA';

        if (!r.success) return showAlert('reg-alert', r.message);

        App.verifyEmail = document.getElementById('reg-email').value.trim();
        App.showScreen('s-verify', 'slide-in-right');

        if (r.data.debug_code) {
            document.getElementById('debug-wrap').style.display = 'block';
            document.getElementById('debug-code').textContent = r.data.debug_code;
        }
        toast('Cuenta creada! Verifica tu email', 'info');
    },

    // ─── Verify Email ───
    async doVerify() {
        const code = Array.from(document.querySelectorAll('#code-row input')).map(i => i.value).join('');
        if (code.length < 6) return showAlert('verify-alert', 'Ingresa los 6 digitos');

        const r = await Api.verifyEmail(App.verifyEmail, code);
        if (!r.success) return showAlert('verify-alert', r.message);

        Api.setToken(r.data.token);
        App.setUser(r.data.user);
        toast('Email verificado!');
        App.showApp();
    },

    // ─── Resend Code ───
    async doResend() {
        const r = await Api.resendCode(App.verifyEmail);
        if (r.data?.debug_code) {
            document.getElementById('debug-wrap').style.display = 'block';
            document.getElementById('debug-code').textContent = r.data.debug_code;
        }
        toast('Codigo reenviado', 'info');
    },

    // ─── Forgot Password ───
    async doForgotPassword() {
        const btn = document.getElementById('forgot-btn');
        const email = document.getElementById('forgot-email').value.trim();
        if (!email) return showAlert('forgot-alert', 'Ingresa tu email');

        btn.disabled = true;
        btn.innerHTML = '<span class="spinner"></span>';
        const r = await Api.forgotPassword(email);
        btn.disabled = false;
        btn.textContent = 'ENVIAR CODIGO';

        if (!r.success) return showAlert('forgot-alert', r.message);

        App.forgotEmail = email;
        showAlert('forgot-alert', r.message || 'Codigo enviado a tu email', 'success');

        if (r.data?.debug_token) {
            document.getElementById('reset-token').value = r.data.debug_token;
        }
        setTimeout(() => App.showScreen('s-reset', 'slide-in-right'), 1500);
    },

    // ─── Reset Password ───
    async doResetPassword() {
        const btn = document.getElementById('reset-btn');
        const token = document.getElementById('reset-token').value;
        const pass = document.getElementById('reset-pass').value;
        const pass2 = document.getElementById('reset-pass2').value;

        if (!token || !pass) return showAlert('reset-alert', 'Completa todos los campos');
        if (pass !== pass2) return showAlert('reset-alert', 'Las contrasenas no coinciden');

        btn.disabled = true;
        btn.innerHTML = '<span class="spinner"></span>';
        const r = await Api.resetPassword({
            email: App.forgotEmail,
            token,
            password: pass,
            password_confirmation: pass2
        });
        btn.disabled = false;
        btn.textContent = 'CAMBIAR CONTRASENA';

        if (!r.success) return showAlert('reset-alert', r.message);
        toast('Contrasena actualizada!');
        App.showScreen('s-login', 'slide-in-left');
    },

    // ─── Logout ───
    async doLogout() {
        await Api.logout();
        Api.setToken('');
        App.setUser(null);
        document.getElementById('bnav').classList.remove('show');
        App.showScreen('s-login', 'fade-in');
        toast('Sesion cerrada', 'info');
    },

    // ─── Init ───
    init() {
        // Toggle de visibilidad de contrasenas
        initPasswordToggles();

        // Navegacion de codigo de verificacion
        document.querySelectorAll('#code-row input').forEach((inp, i, all) => {
            inp.addEventListener('input', () => { if (inp.value && i < 5) all[i + 1].focus(); });
            inp.addEventListener('keydown', e => { if (e.key === 'Backspace' && !inp.value && i > 0) all[i - 1].focus(); });
        });

        // Limpiar errores al escribir
        document.querySelectorAll('.input').forEach(inp => {
            inp.addEventListener('input', () => {
                inp.classList.remove('error');
                const errEl = inp.parentElement.querySelector('.field-error');
                if (errEl) errEl.classList.remove('show');
            });
        });
    }
};
