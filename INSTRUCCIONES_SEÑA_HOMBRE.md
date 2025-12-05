# 🖐️ Instrucciones para Probar la Seña "Hombre"

## 📋 Implementación Completada

La seña peruana de "Hombre" ha sido implementada exitosamente en la aplicación EcoHand dentro de la categoría **"Relaciones Familiares"**.

---

## 🎯 Características de la Seña

### Descripción Visual
La seña "Hombre" se realiza de la siguiente manera:
- **Mano**: Dedo índice extendido
- **Posición**: Sobre el labio superior (indicando la zona del bigote)
- **Movimiento**: Lateral (de izquierda a derecha)

### Detalles Técnicos de Validación

La aplicación valida los siguientes aspectos:

1. **Forma de la mano** (40% de confianza)
   - Solo el dedo índice debe estar extendido
   - Los demás dedos (medio, anular, meñique) deben estar cerrados

2. **Proximidad al labio superior** (30% de confianza)
   - El dedo debe estar a menos de 0.08 unidades (normalizado) del labio superior
   - Se usa MediaPipe Face Landmark #13 (labio superior)

3. **Altura correcta** (30% de confianza)
   - El dedo debe estar posicionado entre la nariz y el labio superior
   - Zona específica del bigote

**Total requerido**: ≥ 70% de confianza para validación exitosa

---

## 📱 Pasos para Probar

### 1. Compilar la Aplicación
```bash
cd "C:\Users\herma\OneDrive\Documentos\Android proyects\ecohand"
.\gradlew.bat assembleDebug
```

### 2. Instalar en Dispositivo
```bash
.\gradlew.bat installDebug
```
O usar Android Studio para ejecutar la aplicación.

### 3. Navegar en la App

1. **Abrir EcoHand**
2. **Ir a "Validación de Señas"** (ícono 🖐️)
3. **Seleccionar "Relaciones Familiares"**
4. **Tocar en "Hombre"** (primera seña de la lista)

### 4. Realizar la Seña

#### Configuración Inicial
- Asegúrate de tener buena iluminación
- La cámara frontal debe captar tu rostro y mano
- Mantén una distancia apropiada (30-50 cm)

#### Ejecución de la Seña

**Paso 1**: Forma de la mano
```
✋ → 👆 (Solo índice extendido)
```

**Paso 2**: Posicionamiento
```
Coloca el dedo índice sobre tu labio superior
(Como si estuvieras indicando un bigote)
```

**Paso 3**: Movimiento (opcional)
```
Mueve el dedo lateralmente de izquierda a derecha
```

### 5. Mensajes de Retroalimentación

Durante la validación, verás uno de estos mensajes:

| Mensaje | Significado |
|---------|-------------|
| 🔍 Detectando... | Buscando mano y rostro |
| ⏳ Esperando seña... | Mano detectada, esperando posición |
| ❌ Extiende solo el dedo índice | Forma de mano incorrecta |
| ❌ Acerca el dedo al labio superior | Dedo muy lejos del labio |
| ❌ Coloca el dedo sobre el labio superior (zona del bigote) | Altura incorrecta |
| ✅ ¡Seña correcta! Ahora muévelo lateralmente | Posición correcta |
| ✅ ¡Correcto! | Validación exitosa |

---

## 🔧 Solución de Problemas

### Problema: "No se detecta rostro"
**Solución**: 
- Asegúrate de que tu rostro esté completamente visible
- Verifica que haya buena iluminación frontal
- Acércate o aléjate de la cámara

### Problema: "No se detecta mano"
**Solución**:
- Mantén la mano dentro del cuadro de la cámara
- Asegúrate de que la mano esté bien iluminada
- Evita fondos muy complejos o con colores de piel

### Problema: "Extiende solo el dedo índice"
**Solución**:
- Cierra completamente los dedos medio, anular y meñique
- El pulgar puede estar cerrado o ligeramente abierto
- Solo el índice debe apuntar hacia arriba

### Problema: "Acerca el dedo al labio superior"
**Solución**:
- Acerca más el dedo índice a tu labio
- El dedo debe casi tocar el labio superior
- Mantén una distancia de 1-2 cm del labio

### Problema: La validación no se completa
**Solución**:
- Mantén la posición estable por 1-2 segundos
- Asegúrate de cumplir TODOS los requisitos simultáneamente
- Verifica que el rostro y la mano estén bien iluminados

---

## 🎨 Información Adicional

### Botones Disponibles

- **ℹ️ Información**: Muestra instrucciones detalladas de la seña
- **🔄 Cambiar cámara**: Alterna entre cámara frontal/trasera (si está disponible)
- **← Volver**: Regresa a la selección de señas

### Ver Instrucciones en la App

1. Toca el botón **ℹ️** en la esquina superior derecha
2. Lee las instrucciones específicas:
   > "Con el dedo índice sobre el labio superior indicar el lugar del bigote con un movimiento lateral"
3. Toca "Entendido" para cerrar el diálogo

---

## 📊 Archivos Modificados

### 1. VowelSignValidator.kt
- Agregada función `validateSignHombre()`
- Validación con detección facial y de mano
- Sistema de confianza con retroalimentación

### 2. VowelValidationScreen.kt
- Soporte para señas con palabras completas
- Integración de detección facial en validación
- Instrucciones actualizadas

### 3. VowelSelectionScreen.kt
- Categoría "Relaciones Familiares" con seña "Hombre"
- UI actualizada para mostrar palabras

### 4. CategorySelectionScreen.kt
- Categoría "Relaciones Familiares" activada
- 1 seña disponible

---

## ✅ Lista de Verificación

Antes de considerar completa la prueba:

- [ ] La app compila sin errores
- [ ] La categoría "Relaciones Familiares" está visible
- [ ] La seña "Hombre" aparece en la lista
- [ ] Al seleccionarla, se abre la pantalla de validación
- [ ] La cámara funciona correctamente
- [ ] Se detecta el rostro (overlay visible)
- [ ] Se detecta la mano (overlay visible)
- [ ] Los mensajes de retroalimentación son claros
- [ ] La validación exitosa muestra el diálogo de éxito
- [ ] El botón de información muestra instrucciones correctas

---

## 🚀 Próximas Mejoras Sugeridas

1. **Detección de movimiento lateral**: Implementar tracking del movimiento para mayor precisión
2. **Más señas familiares**: Agregar "Mujer", "Padre", "Madre", "Hijo", "Hija", etc.
3. **Feedback háptico**: Vibración al detectar correctamente la seña
4. **Modo práctica**: Permitir repetir sin salir de la pantalla
5. **Estadísticas**: Registrar intentos y tiempo de validación

---

## 📞 Soporte

Si encuentras problemas:
1. Revisa los logs de Android Studio / Logcat
2. Verifica que MediaPipe esté correctamente configurado
3. Asegúrate de que los permisos de cámara estén otorgados
4. Comprueba que el dispositivo tenga buena capacidad de procesamiento

---

**¡Listo para probar! 🎉**

Cualquier duda o problema, revisa este documento o consulta el código implementado.

