<!DOCTYPE html>
<html>
<body style="font-family: Arial; max-width: 600px; margin: 0 auto;">
    <div style="background: #EF4444; padding: 30px; text-align: center;">
        <h1 style="color: white; margin: 0;">Runner App</h1>
    </div>
    <div style="padding: 40px;">
        <h2>Hola, {{ $name }}!</h2>
        <p>Tu codigo de verificacion es:</p>
        <div style="background: #f5f5f5; padding: 20px; text-align: center; border-radius: 8px;">
            <span style="font-size: 48px; font-weight: bold; color: #EF4444; letter-spacing: 8px;">
                {{ $code }}
            </span>
        </div>
        <p style="color: #666; margin-top: 20px;">Este codigo expira en 30 minutos.</p>
    </div>
</body>
</html>
