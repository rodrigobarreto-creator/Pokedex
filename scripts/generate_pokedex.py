import os
import json
import requests
import time
from tqdm import tqdm

# ---------------- CONFIGURACIÓN ----------------
BASE_DIR = "C:/Users/Victus/AndroidStudioProjects/Interfazprueba/app/src/main/assets/pokemon_sprites"
POKEDEX_PATH = "C:/Users/Victus/AndroidStudioProjects/Interfazprueba/scripts/pokedex.json"
TEMP_PATH = "C:/Users/Victus/AndroidStudioProjects/Interfazprueba/scripts/pokedex_parcial.json"
REQUEST_DELAY = 0.3  # segundos entre requests

# Carpetas de sprites
folders = ["front", "back", "shiny", "mega", "forms"]
for f in folders:
    os.makedirs(os.path.join(BASE_DIR, f), exist_ok=True)

# ------------------------------------------------

def download_image(url, path):
    if url and not os.path.exists(path):
        try:
            r = requests.get(url, timeout=10)
            if r.status_code == 200:
                with open(path, "wb") as f:
                    f.write(r.content)
        except:
            pass

def get_es(entries):
    """Obtiene texto en español de abilities, movimientos o flavor_text."""
    for e in entries:
        lang = e.get('language', {}).get('name')
        if lang == 'es':
            return e.get('name') or e.get('effect') or e.get('flavor_text')
    if entries:
        return entries[0].get('name') or entries[0].get('effect') or entries[0].get('flavor_text')
    return ""

def download_all_sprites(number):
    """Descarga los sprites de un Pokémon si no existen."""
    try:
        url = f"https://pokeapi.co/api/v2/pokemon/{number}/"
        r = requests.get(url, timeout=10).json()
        species_data = requests.get(r['species']['url'], timeout=10).json()
        time.sleep(REQUEST_DELAY)

        # Sprites principales
        front_sprite = r['sprites']['front_default']
        back_sprite = r['sprites']['back_default']
        shiny_sprite = r['sprites']['front_shiny']

        download_image(front_sprite, os.path.join(BASE_DIR, "front", f"{number:03}.png"))
        download_image(back_sprite, os.path.join(BASE_DIR, "back", f"{number:03}.png"))
        download_image(shiny_sprite, os.path.join(BASE_DIR, "shiny", f"{number:03}.png"))

        # Formas y megas
        for var in species_data['varieties']:
            name = var['pokemon']['name']
            if name == r['name']:
                continue
            var_data = requests.get(var['pokemon']['url'], timeout=10).json()
            time.sleep(REQUEST_DELAY)
            sprite_url = var_data['sprites']['front_default']

            if 'mega' in name.lower():
                path = os.path.join(BASE_DIR, "mega", f"{number:03}-{name}.png")
                download_image(sprite_url, path)
            else:
                path = os.path.join(BASE_DIR, "forms", f"{number:03}-{name}.png")
                download_image(sprite_url, path)

    except Exception as e:
        print(f"⚠️ Error descargando sprites Pokémon {number}: {e}")

def generate_pokedex(start=1, max_number=1025):
    """Genera la pokedex completa en JSON."""
    pokedex = []

    # Cargar progreso parcial si existe
    if os.path.exists(TEMP_PATH):
        with open(TEMP_PATH, "r", encoding="utf-8") as f:
            pokedex = json.load(f)
        print(f"Progreso previo encontrado ({len(pokedex)} Pokémon cargados).")

    for n in tqdm(range(start, max_number + 1), desc="Generando JSON"):
        if any(p["number"] == n for p in pokedex):
            continue

        try:
            # Datos principales
            r = requests.get(f"https://pokeapi.co/api/v2/pokemon/{n}/", timeout=10).json()
            species_data = requests.get(r['species']['url'], timeout=10).json()
            time.sleep(REQUEST_DELAY)

            # Stats y tipos
            stats = {s['stat']['name']: s['base_stat'] for s in r['stats']}
            types = [t['type']['name'] for t in r['types']]

            # Habilidades
            abilities = []
            for ab in r['abilities']:
                ab_data = requests.get(ab['ability']['url'], timeout=10).json()
                time.sleep(REQUEST_DELAY)
                abilities.append({
                    "name": ab['ability']['name'],
                    "hidden": ab['is_hidden'],
                    "description": get_es(ab_data['effect_entries'])
                })

            # Movimientos
            moves = []
            for m in r['moves']:
                try:
                    move_data = requests.get(m['move']['url'], timeout=10).json()
                    time.sleep(REQUEST_DELAY)
                    damage_class = move_data.get('damage_class', {}).get('name', "desconocido")
                    effect = get_es(move_data.get('effect_entries', []))
                    move_type = move_data.get('type', {}).get('name', None)
                    power = move_data.get('power', None)
                    accuracy = move_data.get('accuracy', None)
                    pp = move_data.get('pp', None)

                    for v in m['version_group_details']:
                        learn_method = v['move_learn_method']['name']
                        level = v['level_learned_at'] if learn_method == 'level-up' else None
                        moves.append({
                            "name": m['move']['name'],
                            "type": move_type,
                            "power": power,
                            "accuracy": accuracy,
                            "pp": pp,
                            "damage_class": damage_class,
                            "effect": effect,
                            "learn_method": learn_method,
                            "level": level
                        })
                except Exception as me:
                    print(f"⚠️ Error en movimiento {m['move']['name']} de Pokémon {r['name']}: {me}")

            # Evoluciones
            evo_chain_data = requests.get(species_data['evolution_chain']['url'], timeout=10).json()
            time.sleep(REQUEST_DELAY)
            def parse_chain(chain):
                evo_list = []
                def recurse(c):
                    evo_list.append(c['species']['name'])
                    for e in c['evolves_to']:
                        recurse(e)
                recurse(chain)
                return evo_list
            evolutions = parse_chain(evo_chain_data['chain'])

            # Descripción
            description = ""
            for entry in species_data['flavor_text_entries']:
                if entry['language']['name'] == 'es':
                    description = entry['flavor_text'].replace("\n", " ").replace("\f", " ")
                    break

            # Formas y Megas
            forms_list = []
            mega_list = []
            for var in species_data['varieties']:
                name = var['pokemon']['name']
                if name == r['name']:
                    continue
                var_data = requests.get(var['pokemon']['url'], timeout=10).json()
                time.sleep(REQUEST_DELAY)

                if 'mega' in name.lower():
                    sprite_path = f"mega/{n:03}-{name}.png"
                    stats_var = {s['stat']['name']: s['base_stat'] for s in var_data['stats']}
                    abilities_var = [
                        {
                            "name": ab['ability']['name'],
                            "hidden": ab['is_hidden'],
                            "description": get_es(requests.get(ab['ability']['url'], timeout=10).json()['effect_entries'])
                        } for ab in var_data['abilities']
                    ]
                    mega_list.append({
                        "name": name,
                        "sprite": sprite_path,
                        "stats": stats_var,
                        "abilities": abilities_var
                    })
                else:
                    forms_list.append({
                        "name": name,
                        "sprite": f"forms/{n:03}-{name}.png"
                    })

            # Pokémon final
            pokemon = {
                "number": n,
                "name": r['name'],
                "types": types,
                "stats": stats,
                "weight": r['weight'],
                "height": r['height'],
                "abilities": abilities,
                "moves": moves,
                "evolutions": evolutions,
                "description": description,
                "forms": forms_list,
                "mega_forms": mega_list,
                "sprites": {
                    "front": f"front/{n:03}.png",
                    "back": f"back/{n:03}.png",
                    "shiny": f"shiny/{n:03}.png"
                }
            }

            pokedex.append(pokemon)

            # Guardar progreso parcial cada 50 Pokémon
            if n % 50 == 0:
                with open(TEMP_PATH, "w", encoding="utf-8") as f:
                    json.dump(pokedex, f, ensure_ascii=False, indent=2)
                print(f"💾 Progreso guardado hasta Pokémon {n}")

        except Exception as e:
            print(f"⚠️ Error con Pokémon {n}: {e}")
            time.sleep(1)

    # Guardar archivo final
    with open(POKEDEX_PATH, "w", encoding="utf-8") as f:
        json.dump(pokedex, f, ensure_ascii=False, indent=2)
    print("✅ ¡Pokedex generada con éxito!")

# ------------------------------------------------
if __name__ == "__main__":
    # Generar desde un número específico si se desea reanudar
    generate_pokedex(start=1, max_number=1025)
