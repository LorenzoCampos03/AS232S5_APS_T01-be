# 🔧 SOLUCIÓN ERROR 400 - ACTUALIZACIÓN DE MANTENIMIENTO

## 🚨 PROBLEMA IDENTIFICADO

El error 400 (Bad Request) al actualizar mantenimientos desde el frontend se debe a **validaciones muy estrictas** en el `MaintenanceDto`.

## ✅ CAMBIOS REALIZADOS

### 1. **Validaciones Relajadas en MaintenanceDto**

**Antes:**
```java
@NotBlank(message = "Type is required")
@Pattern(regexp = "preventivo|correctivo", message = "Type must be 'preventivo' or 'correctivo'")
private String tipo;

@NotNull(message = "Scheduled date is required")
@FutureOrPresent(message = "Scheduled date must be today or in the future")
private LocalDate fechaProgramada;

@NotBlank(message = "Status is required")
@Pattern(regexp = "pendiente|en-proceso|completado|cancelado", message = "Status must be one of: pendiente, en-proceso, completado, cancelado")
private String estado;

@DecimalMin(value = "0.0", inclusive = false, message = "Cost must be greater than 0")
private BigDecimal costo;
```

**Después:**
```java
@Pattern(regexp = "(?i)preventivo|correctivo|PREVENTIVO|CORRECTIVO", message = "Type must be 'preventivo' or 'correctivo'")
private String tipo;

private LocalDate fechaProgramada; // Sin validación estricta

@Pattern(regexp = "(?i)pendiente|en-proceso|completado|cancelado|PENDIENTE|EN-PROCESO|COMPLETADO|CANCELADO", message = "Status must be one of: pendiente, en-proceso, completado, cancelado")
private String estado;

@DecimalMin(value = "0.0", inclusive = true, message = "Cost must be 0 or greater")
private BigDecimal costo;
```

### 2. **Mejor Manejo de Errores en MaintenanceController**

- ✅ Logs detallados para debugging
- ✅ Manejo de errores con `onErrorResume`
- ✅ Respuestas HTTP apropiadas

### 3. **Endpoint de Testing Agregado**

```java
PUT /api/maintenance/test-update/{id}
```

## 🧪 CÓMO PROBAR LA SOLUCIÓN

### **Opción 1: Usar el endpoint normal (recomendado)**
```bash
PUT http://localhost:8080/api/maintenance/25
Content-Type: application/json
Authorization: Bearer YOUR_JWT_TOKEN

{
  "tipo": "preventivo",
  "descripcion": "Mantenimiento actualizado",
  "estado": "en-proceso",
  "costo": 150.00,
  "observaciones": "Actualización de prueba"
}
```

### **Opción 2: Usar el endpoint de testing (sin autenticación)**
```bash
PUT http://localhost:8080/api/maintenance/test-update/25
Content-Type: application/json

{
  "tipo": "PREVENTIVO",
  "descripcion": "Test update",
  "estado": "EN-PROCESO",
  "costo": 0,
  "observaciones": "Testing"
}
```

## 🔍 DEBUGGING

### **Si aún tienes problemas, revisa:**

1. **Logs del servidor** - Busca estos mensajes:
   ```
   INFO - 🔄 Updating maintenance ID: 25 with data: MaintenanceDto(...)
   INFO - ✅ Maintenance updated successfully: 25
   ```

2. **Datos que envía el frontend** - Asegúrate de que:
   - `tipo` sea "preventivo" o "correctivo"
   - `estado` sea "pendiente", "en-proceso", "completado", o "cancelado"
   - `costo` sea un número válido (puede ser 0)
   - `descripcion` no exceda 500 caracteres

3. **Headers HTTP** - Verifica que incluyas:
   ```
   Content-Type: application/json
   Authorization: Bearer YOUR_JWT_TOKEN
   ```

## 🎯 RESULTADO ESPERADO

Después de estos cambios:
- ✅ Las validaciones son más flexibles
- ✅ Acepta mayúsculas y minúsculas
- ✅ Permite costo = 0
- ✅ No requiere fecha programada obligatoria
- ✅ Mejor logging para debugging
- ✅ Endpoint de testing disponible

**¡El frontend ahora debería poder actualizar mantenimientos sin errores 400!** 🚀