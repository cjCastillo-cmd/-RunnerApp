# RunnerApp API — PHP puro

API backend para RunnerApp. Sin framework, solo PHP + MySQL.

## Requisitos

- PHP >= 8.0 con extensiones: pdo_mysql, mbstring
- MySQL 8.0+
- Apache con mod_rewrite habilitado

## Despliegue en servidor

1. **Instalar dependencias** (Ubuntu/Debian):

```bash
sudo apt update
sudo apt install apache2 php php-mysql mysql-server -y
sudo a2enmod rewrite
```

2. **Copiar archivos** al servidor:

```bash
sudo mkdir -p /var/www/html/api
sudo cp -r * /var/www/html/api/
sudo chown -R www-data:www-data /var/www/html/api/uploads
sudo chmod 775 /var/www/html/api/uploads
```

3. **Crear la base de datos**:

```bash
sudo mysql < /var/www/html/api/setup.sql
```

4. **Configurar Apache** — editar `/etc/apache2/sites-available/000-default.conf`:

```apache
<VirtualHost *:80>
    DocumentRoot /var/www/html
    <Directory /var/www/html/api>
        AllowOverride All
        Require all granted
    </Directory>
</VirtualHost>
```

5. **Reiniciar Apache**:

```bash
sudo systemctl restart apache2
```

6. **Editar config.php** si el password de MySQL es diferente.

## URL base

```
http://TU_IP/api/
```

## Endpoints

Mismos endpoints que el backend Laravel original. Ver `index.php` para la lista completa.

## Testing rapido

```bash
# Registro
curl -X POST http://localhost/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Test","email":"test@test.com","password":"123456","password_confirmation":"123456","country":"Honduras"}'

# Login
curl -X POST http://localhost/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"123456"}'
```