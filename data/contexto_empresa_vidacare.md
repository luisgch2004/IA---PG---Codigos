# Contexto de la Empresa — VidaCare Peru

**Proyecto Grupal — Inteligencia Artificial — UNMSM 2026-I**

---

## 1. Datos generales

| Campo | Valor |
|---|---|
| **Nombre** | VidaCare Peru |
| **Rubro** | Healthtech / Salud digital |
| **Mercado** | Peru |
| **Servicios** | Telemedicina, atencion presencial, planes de salud preventiva, gestion de citas medicas |
| **Plataforma** | App movil y web |
| **Cobertura** | Lima Metropolitana y principales ciudades del Peru |

---

## 2. Descripcion del negocio

VidaCare Peru es una plataforma digital de salud que conecta pacientes con medicos especialistas a traves de atencion presencial y telemedicina. Ofrece tres tipos de planes:

- **Plan Basico:** Acceso a citas medicas generales via telemedicina. Cuota mensual reducida.
- **Plan Preventivo:** Incluye chequeos preventivos anuales, descuentos en farmacia y acceso a especialistas.
- **Plan Premium:** Atencion prioritaria, medicos a domicilio, cobertura de especialistas ilimitada y descuentos en laboratorios.

### Problema de negocio

El equipo de marketing de VidaCare Peru enfrenta los siguientes desafios:

1. No existe una segmentacion formal de pacientes. Todas las campanas de marketing son genericas.
2. La tasa de abandono (churn) es alta en pacientes nuevos durante los primeros 3 meses.
3. No se sabe cuales son los factores que mas influyen en el gasto anual de los pacientes.
4. No hay un sistema de prediccion de abandono para intervenir proactivamente.

### Preguntas analiticas por informe

| Informe | Pregunta analitica | Tecnica |
|---|---|---|
| **Informe 3** | ?Que factores explican el gasto total anual de un paciente? | Regresion (variable objetivo: `gasto_total_anual`) |
| **Informe 4** | ?Se puede predecir que pacientes abandonaran la plataforma? | Clasificacion (variable objetivo: `abandono`) |
| **Informe 5** | ?Cuales son los segmentos naturales de pacientes segun su comportamiento? | Clustering (K-means, DBSCAN) |
| **Informe 6** | ?Cuales son las dimensiones subyacentes en los datos de pacientes? | Reduccion de dimensionalidad (PCA, t-SNE) |

---

## 3. Dataset sintetico

### 3.1. Especificaciones generales

| Caracteristica | Valor |
|---|---|
| **Archivo** | `data/pacientes_vidacare.csv` |
| **Numero de registros** | 1500 |
| **Numero de variables** | 12 |
| **Variables numericas** | 9 |
| **Variables categoricas** | 3 (incluyendo `id_paciente`) |
| **Semilla (seed)** | 42 |
| **Ruido introducido** | 5% aleatorio en frecuencia y gasto |
| **Valores nulos** | ~4% en columnas seleccionadas |
| **Formato** | CSV con cabecera |

### 3.2. Diccionario de variables

| # | Variable | Tipo | Rango / Valores | Rol en el proyecto | Descripcion |
|---|---|---|---|---|---|
| 1 | `id_paciente` | Texto | P0001 - P1500 | Identificador | ID unico del paciente |
| 2 | `edad` | Entero | 18 - 85 | EDA | Edad del paciente en anos. Distribucion normal con media 45 y desviacion 15. |
| 3 | `ingreso_mensual` | Entero | 1000 - 8000 | EDA | Ingreso mensual estimado en soles. Correlacionado positivamente con edad. |
| 4 | `frecuencia_citas` | Entero | 0 - 36 | **Clustering (Inf 5)** | Numero de citas medicas en el ultimo ano. Poisson segun plan: Basico=4, Preventivo=8, Premium=14. |
| 5 | `gasto_promedio_cita` | Float | 30 - 350 | **Clustering (Inf 5)** | Gasto promedio por cita en soles. Dependiente del tipo de plan e ingreso mensual. |
| 6 | `antiguedad_meses` | Entero | 1 - 60 | **Clustering (Inf 5)** | Meses desde el registro del paciente. Distribucion uniforme. |
| 7 | `recencia_dias` | Entero | 1 - 200 | **Clustering (Inf 5)** | Dias desde la ultima cita. Inversamente correlacionada con antiguedad y frecuencia. |
| 8 | `tipo_plan` | Categorica | Basico, Preventivo, Premium | Interpretacion post-clustering | Plan contratado. Distribucion: Basico 50%, Preventivo 30%, Premium 20%. |
| 9 | `canal_atencion` | Categorica | Presencial, Telemedicina | EDA | Canal de atencion preferido. Distribucion: Presencial 55%, Telemedicina 45%. |
| 10 | `gasto_total_anual` | Float | 0 - 50000 | **Regresion (Inf 3)** | Gasto total anual estimado. Calculado como frecuencia x gasto_promedio x 0.95 + ruido gaussiano. |
| 11 | `abandono` | Binaria | 0 (No) / 1 (Si) | **Clasificacion (Inf 4)** | Indicador de abandono de la plataforma. Dependiente de baja frecuencia y alta recencia. |
| 12 | `num_condiciones_cronicas` | Entero | 0 - 5 | EDA | Numero de condiciones cronicas del paciente. Relacionado con la edad. |

### 3.3. Reglas de generacion

#### 3.3.1. Distribuciones

```
edad ~ Normal(media=45, std=15), clip(18, 85)
ingreso_mensual = 1500 + edad * 60 + Normal(0, 500), clip(1000, 8000)
tipo_plan ~ Categorical(Basico=50%, Preventivo=30%, Premium=20%)
canal_atencion ~ Categorical(Presencial=55%, Telemedicina=45%)

# Parametros de Poisson segun plan
frecuencia_citas ~ Poisson(lambda_plan) + (ingreso // 2000), clip(0, 36)
  lambda = 4  para Basico
  lambda = 8  para Preventivo
  lambda = 14 para Premium

antiguedad_meses ~ Uniforme(1, 60)
recencia_dias = 60 - antiguedad * 0.6 + (20 - frecuencia) * 2 + Normal(0, 20), clip(1, 200)

# Gasto segun plan
gasto_promedio_cita = base_plan + ingreso * 0.015 + Normal(0, 20), clip(30, 350)
  base = 60  para Basico
  base = 90  para Preventivo
  base = 150 para Premium

gasto_total_anual = frecuencia * gasto_promedio * 0.95 + Normal(0, 100), clip(0, 50000)

# Abandono (churn) - modelo logistico
P(abandono=1) = sigmoid(-0.5 - 0.3 * frecuencia/10 + 0.02 * recencia/10)
abandono ~ Bernoulli(P)

num_condiciones_cronicas = edad // 20 - 1 + Poisson(0.3), clip(0, 5)
```

#### 3.3.2. Correlaciones introducidas

| Variable 1 | Variable 2 | Tipo de correlacion | Razon |
|---|---|---|---|
| `edad` | `ingreso_mensual` | Positiva | A mayor edad, mayor experiencia laboral e ingresos |
| `tipo_plan` | `frecuencia_citas` | Positiva | Planes mas caros incentivan mayor uso del servicio |
| `tipo_plan` | `gasto_promedio_cita` | Positiva | Planes premium acceden a especialistas mas costosos |
| `ingreso_mensual` | `gasto_promedio_cita` | Positiva | Mayor poder adquisitivo permite mayor gasto en salud |
| `antiguedad_meses` | `recencia_dias` | Negativa | Pacientes mas antiguos tienden a tener citas mas recientes |
| `frecuencia_citas` | `recencia_dias` | Negativa | Mayor frecuencia implica menor recencia |
| `frecuencia_citas` | `abandono` | Negativa | Pacientes frecuentes abandonan menos |
| `recencia_dias` | `abandono` | Positiva | Pacientes con alta recencia tienen mayor riesgo de abandono |
| `edad` | `num_condiciones_cronicas` | Positiva | Pacientes mayores tienen mas condiciones cronicas |

#### 3.3.3. Valores nulos

Se introdujeron nulos aleatorios en ~4% de las filas para las siguientes columnas:
- `frecuencia_citas`
- `gasto_promedio_cita`
- `antiguedad_meses`
- `recencia_dias`
- `tipo_plan`

#### 3.3.4. Ruido

Se anadio ruido gaussiano en el 5% de los registros de `frecuencia_citas` (+/- 3) y `gasto_promedio_cita` (+/- 30 soles) para simular errores de captura realistas.

---

## 4. Estructura de clustering esperada

Los datos fueron disenados para que emerjan **4 segmentos naturales** de pacientes:

| Segmento | Frecuencia | Gasto prom. | Antiguedad | Recencia | Tamaño aprox. |
|---|---|---|---|---|---|
| Fidelizado Premium | Alta (20-36) | Alto (200-350) | Alta (36-60) | Baja (1-30) | ~20% |
| Regular Preventivo | Media (10-20) | Medio (100-200) | Media (18-36) | Media (20-60) | ~30% |
| Nuevo Exploratorio | Baja (0-10) | Bajo (30-120) | Baja (1-12) | Baja-Media (10-60) | ~25% |
| Inactivo / En Riesgo | Baja (0-8) | Bajo (30-100) | Alta (24-60) | Alta (60-200) | ~25% |

---

## 5. Notebooks asociados

| Notebook | Ruta | Proposito |
|---|---|---|
| Generador del dataset | `notebooks/generar_dataset_vidacare.ipynb` | Generar el dataset sintetico (ejecutar una vez) |
| Informe 5 — Agrupamiento | `notebooks/PG_IA_GrupoXX_Informe5_Agrupamiento.ipynb` | Clustering K-means, DBSCAN, perfilado |
| (Futuro) Informe 3 — Regresion | `notebooks/PG_IA_GrupoXX_Informe3_Regresion.ipynb` | Prediccion de gasto_total_anual |
| (Futuro) Informe 4 — Clasificacion | `notebooks/PG_IA_GrupoXX_Informe4_Clasificacion.ipynb` | Prediccion de abandono |
| (Futuro) Informe 6 — Reduccion | `notebooks/PG_IA_GrupoXX_Informe6_Reduccion.ipynb` | PCA, t-SNE |

---

## 6. Estructura del dataset (primeras 5 filas)

| id_paciente | edad | ingreso_mensual | frecuencia_citas | gasto_promedio_cita | antiguedad_meses | recencia_dias | tipo_plan | canal_atencion | gasto_total_anual | abandono | num_condiciones_cronicas |
|---|---|---|---|---|---|---|---|---|---|---|---|
| P0001 | 49 | 4440 | 18 | 131.42 | 33 | 19 | Preventivo | Telemedicina | 2280.29 | 0 | 1 |
| P0002 | 29 | 3240 | 5 | 100.15 | 52 | 39 | Basico | Presencial | 441.42 | 1 | 0 |
| P0003 | 56 | 4860 | 16 | 142.78 | 12 | 47 | Preventivo | Telemedicina | 2180.85 | 0 | 1 |

---

## 7. Reproducibilidad

Para regenerar el dataset exactamente igual:

```python
import numpy as np
import pandas as pd

SEED = 42
rng = np.random.default_rng(SEED)
N = 1500
# ... (ver notebooks/generar_dataset_vidacare.ipynb para el codigo completo)
```

El notebook `generar_dataset_vidacare.ipynb` contiene el script completo y documentado.
