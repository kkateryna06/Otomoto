from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from .database import get_db
from .models import Car

router = APIRouter()

@router.get("/")
def read_root():
    return {"message": "Welcome to the Car API!"}

@router.get("/cars")
def get_cars(db: Session = Depends(get_db)):
    cars = db.query(Car).all()
    return cars

@router.post("/cars")
def create_car(name: str, price: float, db: Session = Depends(get_db)):
    car = Car(mark=name, price=price)
    db.add(car)
    db.commit()
    db.refresh(car)
    return {"message": "Car added successfully", "car": car}
