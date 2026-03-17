<!DOCTYPE html>
<html>
<body style="font-family: Arial; max-width: 600px; margin: 0 auto;">
    <div style="background: #EF4444; padding: 30px; text-align: center;">
        <h1 style="color: white; margin: 0;">Runner App</h1>
    </div>
    <div style="padding: 40px;">
        <h2>Hola, {{ $name }}!</h2>
        <p>Recibimos una solicitud para restablecer tu contrasena.</p>
        <p>Tu token de restablecimiento es:</p>
        <div style="background: #f5f5f5; padding: 20px; text-align: center; border-radius: 8px; word-break: break-all;">
            <code style="font-size: 14px; color: #EF4444;">{{ $token }}</code>
        </div>
        <p style="color: #666; margin-top: 20px;">Si no solicitaste esto, ignora este correo.</p>
    </div>
</body>
</html>
