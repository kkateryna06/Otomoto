from fastapi import FastAPI

from .database import Base, engine
from .routes import router

app = FastAPI()

app.include_router(router)

Base.metadata.create_all(bind=engine)

if __name__ == '__main__':
    import uvicorn
    uvicorn.run(app, host='0.0.0.0', port=8000)