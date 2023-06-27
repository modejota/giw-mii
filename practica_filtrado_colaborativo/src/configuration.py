import os
from dataclasses import dataclass

# Get the path of the project (parent of src)
BASE_PATH = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))


@dataclass
class Configuration:
    DATA_DIR: str = os.path.join(BASE_PATH, 'data')
    RESULTS_DIR: str = os.path.join(BASE_PATH, 'results')
    MOVIES_FILE: str = os.path.join(DATA_DIR, 'u.item')
    RATINGS_FILE: str = os.path.join(DATA_DIR, 'u.data')
    RESULTS_FILE: str = os.path.join(RESULTS_DIR, 'recomendaciones.data')
    MOVIES_TO_BE_RATED: int = 20
    NEIGHBOURS: int = 10
    MAX_RESULTS: int = 30
    USE_MAX_RESULTS: bool = True
    DELETE_IRRELEVANT_USERS: bool = True
    THRESHOLD_TO_BE_IRRELEVANT: int = 3
    SHOW_DEBUG_INFO = True
