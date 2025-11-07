from fastapi import FastAPI, File, UploadFile, HTTPException
from fastapi.middleware.cors import CORSMiddleware
import tensorflow as tf
from tensorflow import keras
import numpy as np
from PIL import Image
import io
import uvicorn
import logging

# ============================================================
# 🧩 Configuración base
# ============================================================

app = FastAPI(
    title="Profesor Oak API",
    description="API para clasificación de Pokémon (modelo EfficientNetB0)",
    version="3.0"
)

# CORS habilitado para la app Android o clientes externos
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# ============================================================
# 🧠 Carga del modelo EfficientNetB0
# ============================================================

MODEL_PATH = "profesor_oak_6.keras"

# Importar el preprocesador correcto
from tensorflow.keras.applications.efficientnet import preprocess_input

try:
    model = keras.models.load_model(
        MODEL_PATH,
        compile=False,
        custom_objects={"preprocess_input": preprocess_input}
    )
    logger.info(f"✅ Modelo EfficientNetB0 cargado correctamente desde '{MODEL_PATH}'")
except Exception as e:
    logger.error(f"❌ Error cargando el modelo: {e}")
    model = None

# ============================================================
# 🧾 Lista de clases (ordenada como durante el entrenamiento)
# ============================================================

CLASS_NAMES = [
    "1 - Bulbasaur", "25 - Pikachu", "3 - Venusaur",
    "4 - Charmander", "6 - Charizard", "7 - Squirtle", "9 - Blastoise"
]

# ============================================================
# 📡 Rutas principales
# ============================================================

@app.get("/")
def root():
    return {"message": "Profesor Oak API funcionando correctamente 🔥"}

@app.get("/health")
def health_check():
    status = model is not None
    return {"status": "ok" if status else "error", "model_loaded": status}

# ============================================================
# 🔮 Clasificación de imagen
# ============================================================

@app.post("/classify/")
async def classify_pokemon(file: UploadFile = File(...)):
    """
    Recibe una imagen y devuelve el Pokémon más probable.
    """
    try:
        if model is None:
            raise HTTPException(status_code=500, detail="El modelo no está cargado en memoria")

        contents = await file.read()
        image = Image.open(io.BytesIO(contents))

        # Convertir a RGB si no lo es
        if image.mode != "RGB":
            image = image.convert("RGB")

        # Redimensionar a 224x224 (EfficientNetB0)
        image = image.resize((224, 224))

        # Convertir a array y aplicar preprocesamiento de EfficientNet
        image_array = np.array(image, dtype=np.float32)
        image_array = preprocess_input(image_array)
        image_array = np.expand_dims(image_array, axis=0)  # [1, 224, 224, 3]

        # Predicción
        preds = model.predict(image_array)
        predicted_idx = int(np.argmax(preds[0]))
        confidence = float(preds[0][predicted_idx])

        # Respuesta
        result = {
            "pokemon": CLASS_NAMES[predicted_idx],
            "confidence": round(confidence * 100, 2),
            "predictions": {
                CLASS_NAMES[i]: round(float(preds[0][i]) * 100, 2)
                for i in range(len(CLASS_NAMES))
            }
        }

        logger.info(f"🧩 Predicción: {result['pokemon']} ({result['confidence']}%)")
        return result

    except Exception as e:
        logger.error(f"❌ Error al procesar imagen: {e}")
        raise HTTPException(status_code=500, detail=f"Error procesando imagen: {str(e)}")

# ============================================================
# 🚀 Ejecución local
# ============================================================

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8002)

