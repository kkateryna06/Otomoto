import sys
from pathlib import Path

from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker, declarative_base

LEGACY_CONFIG_DIR = Path(__file__).resolve().parents[2] / "otomoto-data-updater"
sys.path.insert(0, str(LEGACY_CONFIG_DIR))

from db_config import DATABASE_URL


engine = create_engine(DATABASE_URL)
SessionLocal = sessionmaker(bind=engine, autocommit=False, autoflush=False)
Base = declarative_base()

def get_db():
    db = SessionLocal()
    try:
        yield db
    except Exception as e:
        print(e)
    finally:
        db.close()
