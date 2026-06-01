#!/bin/bash

# Configuración: Cambia esto por el nombre de tu rama de trabajo activa
RAMA_DESARROLLO="tu-rama-de-desarrollo"

# Colores para la consola
VERDE='\033[0;32m'
ROJO='\033[0;31m'
AZUL='\033[0;34m'
AMARILLO='\033[1;33m'
SIN_COLOR='\033[0m'

mostrar_menu() {
    clear
    echo -e "${AZUL}=============================================${SIN_COLOR}"
    echo -e "${AZUL}      SISTEMA DE GESTIÓN GIT PARA PRODUCCIÓN  ${SIN_COLOR}"
    echo -e "${AZUL}=============================================${SIN_COLOR}"
    echo -e "Rama de desarrollo actual: ${AMARILLO}$RAMA_DESARROLLO${SIN_COLOR}"
    echo "---------------------------------------------"
    echo "1) 👀 Ver archivos modificados (Git Status)"
    echo "2) 📜 Ver historial de últimos cambios (Git Log)"
    echo "3) 🔍 Verificar conflictos (Simular Merge sin guardar)"
    echo "4) 🚀 Fusionar rama de desarrollo con MAIN (Producción)"
    echo "5) ↩️  Deshacer último Commit LOCAL (Manteniendo archivos)"
    echo "6) 🛑 Cancelar un Merge trabado (Merge Abort)"
    echo "7) 🔄 Actualizar mi rama con lo último de MAIN"
    echo "8) ❌ Salir"
    echo "---------------------------------------------"
    echo -n "Selecciona una opción [1-8]: "
}

while true; do
    mostrar_menu
    read opcion
    case $opcion in
        1)
            echo -e "\n${AZUL}👀 Estado actual de tus archivos:${SIN_COLOR}"
            git status
            echo ""
            read -p "Presiona Enter para regresar al menú..."
            ;;
        2)
            echo -e "\n${AZUL}📜 Últimos 5 cambios en el repositorio:${SIN_COLOR}"
            git log --oneline -n 5
            echo ""
            read -p "Presiona Enter para regresar al menú..."
            ;;
        3)
            echo -e "\n${AZUL}🧪 Simulando fusión entre $RAMA_DESARROLLO y main...${SIN_COLOR}"
            git fetch origin main
            git merge --no-commit --no-ff origin/main
            if [ $? -eq 0 ]; then
                echo -e "\n${VERDE}✅ ¡Limpio! No hay conflictos detectados.${SIN_COLOR}"
                git merge --abort > /dev/null 2>&1
            else
                echo -e "\n${ROJO}❌ ¡ALERTA! Se detectaron conflictos en archivos.${SIN_COLOR}"
                echo "Elige la opción 6 en el menú si deseas limpiar la terminal ahora."
            fi
            read -p "Presiona Enter para regresar al menú..."
            ;;
        4)
            echo -e "\n${AZUL}🚀 Iniciando fusión definitiva en MAIN...${SIN_COLOR}"
            echo -e "${AMARILLO}Asegurándonos de que tu rama esté al día...${SIN_COLOR}"
            git checkout $RAMA_DESARROLLO && git pull origin $RAMA_DESARROLLO
            
            echo -e "${AMARILLO}Cambiando a main y trayendo cambios del servidor...${SIN_COLOR}"
            git checkout main && git pull origin main
            
            echo -e "${AMARILLO}Fusionando $RAMA_DESARROLLO en main...${SIN_COLOR}"
            git merge $RAMA_DESARROLLO
            
            if [ $? -eq 0 ]; then
                echo -e "\n${VERDE}🎉 Fusión local en main completada con éxito.${SIN_COLOR}"
                echo -e "${AMARILLO}👉 RECUERDA: Revisa que todo funcione bien y luego ejecuta manual: git push origin main${SIN_COLOR}"
            else
                echo -e "\n${ROJO}❌ La fusión falló o tiene conflictos. Resuélvelos antes de continuar.${SIN_COLOR}"
            fi
            read -p "Presiona Enter para regresar al menú..."
            ;;
        5)
            echo -e "\n${AMARILLO}⚠️ ¿Estás seguro de deshacer tu último commit local?${SIN_COLOR}"
            echo "Tus archivos modificados se quedarán guardados, solo se borrará el commit."
            echo -n "(s/n): "
            read confirmar
            if [ "$confirmar" = "s" ] || [ "$confirmar" = "S" ]; then
                git reset --soft HEAD~1
                echo -e "${VERDE}✅ Último commit eliminado. Tus archivos siguen a salvo.${SIN_COLOR}"
            else
                echo "Operación cancelada."
            fi
            read -p "Presiona Enter para regresar al menú..."
            ;;
        6)
            echo -e "\n${AZUL}🛑 Cancelando cualquier proceso de merge activo...${SIN_COLOR}"
            git merge --abort > /dev/null 2>&1
            if [ $? -eq 0 ]; then
                echo -e "${VERDE}✅ Repositorio devuelto a su estado original previo al merge.${SIN_COLOR}"
            else
                echo -e "${AMARILLO}No había ningún proceso de merge activo que cancelar.${SIN_COLOR}"
            fi
            read -p "Presiona Enter para regresar al menú..."
            ;;
        7)
            echo -e "\n${AZUL}🔄 Trayendo novedades de producción (main) hacia tu rama...${SIN_COLOR}"
            git checkout $RAMA_DESARROLLO
            git pull origin main
            echo -e "${VERDE}✅ Tu rama ha sido actualizada.${SIN_COLOR}"
            read -p "Presiona Enter para regresar al menú..."
            ;;
        8)
            echo -e "\n${VERDE}¡Hasta luego!${SIN_COLOR}"
            exit 0
            ;;
        *)
            echo -e "\n${ROJO}Opción inválida. Intenta de nuevo.${SIN_COLOR}"
            sleep 1
            ;;
    esac
done