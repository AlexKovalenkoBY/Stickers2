@echo off
chcp 1251 >nul
setlocal enabledelayedexpansion

:: Способ 1: Получаем IPv4 через ipconfig (более надежно для русской Windows)
for /f "tokens=2 delims=:" %%A in ('ipconfig ^| findstr /C:"IPv4"') do (
    for /f "tokens=* delims= " %%B in ("%%A") do (
        set SERVER_IP=%%B
        goto :IP_FOUND
    )
)

:: Способ 2: Если первый способ не сработал, используем wmic (для англоязычных систем)
for /f "tokens=2 delims=," %%A in (
    'wmic nicconfig where "IPEnabled=true" get IPAddress /value ^| findstr "IPAddress"'
) do (
    set SERVER_IP=%%~A
    set SERVER_IP=!SERVER_IP:~1,-1!
    goto :IP_FOUND
)

:IP_FOUND

:: Проверяем, что IP найден
if "%SERVER_IP%"=="" (
    echo Ошибка: не удалось определить IP-адрес!
    echo Попробуйте ввести его вручную:
    set /p SERVER_IP="Введите IP-адрес сервера: "
    if "!SERVER_IP!"=="" (
        echo IP-адрес не введен. Выход.
        pause
        exit /b 1
    )
)

:: Выводим полученный IP для проверки
echo Используемый IP-адрес: %SERVER_IP%

:: Запускаем приложение
cd /d "%~dp0"
echo Запуск приложения...
java -jar "Stickers-0.0.1.jar" --wb.products.api.url="http://%SERVER_IP%:8080/api/wb/all-cards-mapped-short"

:: Ждём 5 секунд и открываем браузер
timeout /t 5 >nul
start "" "http://127.0.0.1:8081"
exit