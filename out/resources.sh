#!/bin/bash

# Script de monitoreo de recursos del servidor

# Función para verificar uso de recursos
check_resources() {
    # Uso de CPU
    CPU_USE=$(top -bn1 | grep "Cpu(s)" | awk '{print $2 + $4}')
    
    # Uso de memoria
    MEM_USE=$(free | grep Mem | awk '{print $3/$2 * 100.0}')
    
    # Espacio en disco
    DISK_USE=$(df -h / | awk '/\// {print $(NF-1)}' | sed 's/%//g')

    # Registro de métricas
    echo "$(date '+%Y-%m-%d %H:%M:%S') - CPU: $CPU_USE% - Memoria: $MEM_USE% - Disco: $DISK_USE%" >> /var/log/server_monitor.log

    # Alertas (ejemplo simplificado)
    if (( $(echo "$CPU_USE > 80" | bc -l) )); then
        send_alert "ALTA CARGA DE CPU: $CPU_USE%"
    fi

    if (( $(echo "$MEM_USE > 90" | bc -l) )); then
        send_alert "CONSUMO CRÍTICO DE MEMORIA: $MEM_USE%"
    fi
}

# Función de envío de alertas
send_alert() {
    local message=$1
    # Ejemplo de envío de alerta por correo
    echo "$message" | mail -s "ALERTA DE SERVIDOR" admin@hospital.com
    
    # Opcional: integración con sistemas de notificación
    # curl -X POST https://alertas.hospital.com/api/notify -d "message=$message"
}

# Ejecución periódica
echo "Monitor iniciado"
while true; do
    check_resources
    sleep 300  # Cada 5 minutos
done
