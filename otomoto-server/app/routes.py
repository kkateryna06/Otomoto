import os
from typing import Optional

from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy import asc, cast, String, text, distinct, func
from sqlalchemy.orm import Session
from starlette.responses import FileResponse

from .database import get_db
from .models import Car, SpecialCar

router = APIRouter()

@router.get("/")
def read_root():
    return {"message": "Welcome to the Car API!!!!"}

@router.get("/allcars/")
def get_all_cars(
        db: Session = Depends(get_db),
        mark: Optional[str] = Query(None),
        model: Optional[str] = Query(None),
        min_price: Optional[float] = Query(None),
        max_price: Optional[float] = Query(None),
        min_year: Optional[int] = Query(None),
        max_year: Optional[int] = Query(None),
        body_type: Optional[str] = Query(None),
        min_mileage: Optional[float] = Query(None),
        max_mileage: Optional[float] = Query(None),
        fuel_type: Optional[str] = Query(None),
        min_engine_capacity: Optional[float] = Query(None),
        max_engine_capacity: Optional[float] = Query(None),
        min_urban_consumption: Optional[float] = Query(None),
        max_urban_consumption: Optional[float] = Query(None),
        min_extra_urban_consumption: Optional[float] = Query(None),
        max_extra_urban_consumption: Optional[float] = Query(None)
):
    query = db.query(
        Car.car_id, Car.mark, Car.model, Car.price, Car.mileage, Car.year, Car.body_type, Car.date, Car.urban_consumption, Car.engine_power
    )
    if mark:
        query = query.filter(Car.mark == mark)
    if model:
        query = query.filter(Car.model == model)
    if min_price:
        query = query.filter(Car.price >= min_price)
    if max_price:
        query = query.filter(Car.price <= max_price)
    if min_year:
        query = query.filter(Car.year >= min_year)
    if max_year:
        query = query.filter(Car.year <= max_year)
    if body_type:
        query = query.filter(Car.body_type == body_type)
    if min_mileage:
        query = query.filter(Car.mileage >= min_mileage)
    if max_mileage:
        query = query.filter(Car.mileage <= max_mileage)
    if fuel_type:
        query = query.filter(Car.fuel_type == fuel_type)
    if min_engine_capacity:
        query = query.filter(Car.engine_capacity >= min_engine_capacity)
    if max_engine_capacity:
        query = query.filter(Car.engine_capacity <= max_engine_capacity)
    if min_urban_consumption:
        query = query.filter(Car.urban_consumption >= min_urban_consumption)
    if max_urban_consumption:
        query = query.filter(Car.urban_consumption <= max_urban_consumption)
    if min_extra_urban_consumption:
        query = query.filter(Car.extra_urban_consumption >= min_extra_urban_consumption)
    if max_extra_urban_consumption:
        query = query.filter(Car.extra_urban_consumption <= max_extra_urban_consumption)

    cars = query.order_by(asc(Car.mark)).all()

    return [
        {
            "car_id": car.car_id,
            "mark": car.mark,
            "model": car.model,
            "price": car.price,
            "mileage": car.mileage,
            "year": car.year,
            "body_type": car.body_type,
            "date": car.date,
            "urban_consumption": car.urban_consumption,
            "engine_power": car.engine_power,
        }
        for car in cars
    ]



@router.get("/specialcars")
def get_special_cars(
        db: Session = Depends(get_db),
        mark: Optional[str] = Query(None),
        model: Optional[str] = Query(None),
        min_price: Optional[float] = Query(None),
        max_price: Optional[float] = Query(None),
        min_year: Optional[int] = Query(None),
        max_year: Optional[int] = Query(None),
        body_type: Optional[str] = Query(None),
        min_mileage: Optional[float] = Query(None),
        max_mileage: Optional[float] = Query(None),
        fuel_type: Optional[str] = Query(None),
        min_engine_capacity: Optional[float] = Query(None),
        max_engine_capacity: Optional[float] = Query(None),
        min_urban_consumption: Optional[float] = Query(None),
        max_urban_consumption: Optional[float] = Query(None),
        min_extra_urban_consumption: Optional[float] = Query(None),
        max_extra_urban_consumption: Optional[float] = Query(None)
):
    query = db.query(SpecialCar)

    if mark:
        query = query.filter(SpecialCar.mark == mark)
    if model:
        query = query.filter(SpecialCar.model == model)
    if min_price:
        query = query.filter(SpecialCar.price <= min_price)
    if max_price:
        query = query.filter(SpecialCar.price >= max_price)
    if min_year:
        query = query.filter(SpecialCar.year >= min_year)
    if max_year:
        query = query.filter(SpecialCar.year <= max_year)
    if body_type:
        query = query.filter(SpecialCar.body_type == body_type)
    if min_mileage:
        query = query.filter(SpecialCar.mileage >= min_mileage)
    if max_mileage:
        query = query.filter(SpecialCar.mileage <= max_mileage)
    if fuel_type:
        query = query.filter(SpecialCar.fuel_type == fuel_type)
    if min_engine_capacity:
        query = query.filter(SpecialCar.engine_capacity >= min_engine_capacity)
    if max_engine_capacity:
        query = query.filter(SpecialCar.engine_capacity <= max_engine_capacity)
    if min_urban_consumption:
        query = query.filter(SpecialCar.urban_consumption >= min_urban_consumption)
    if max_urban_consumption:
        query = query.filter(SpecialCar.urban_consumption <= max_urban_consumption)
    if min_extra_urban_consumption:
        query = query.filter(SpecialCar.urban_consumption >= min_extra_urban_consumption)
    if max_extra_urban_consumption:
        query = query.filter(SpecialCar.urban_consumption <= max_extra_urban_consumption)

    cars = query.order_by(asc(SpecialCar.mark)).all()

    return [
        {
            "car_id": car.car_id,
            "mark": car.mark,
            "model": car.model,
            "price": car.price,
            "mileage": car.mileage,
            "year": car.year,
            "body_type": car.body_type,
            "date": car.date,
            "urban_consumption": car.urban_consumption,
            "engine_power": car.engine_power,
        }
        for car in cars
    ]

@router.get("/allcars/{car_id}")
def get_car_by_id_from_all(car_id: int, db: Session = Depends(get_db)):
    car = db.query(Car).filter(Car.car_id == car_id).first()
    return car

@router.get("/specialcars/{spec}")
def get_car_by_id_from_special(db: Session = Depends(get_db)):
    car = db.query(SpecialCar).filter(SpecialCar.car_id == SpecialCar.car_id).first()
    return car

@router.get("/allcars/{car_id}/photo")
def get_image_by_id_from_all(car_id: int, db: Session = Depends(get_db)):
    image_path = db.query(Car.photo_path).filter(Car.car_id == car_id).scalar() + "\photo_1.jpg"
    print(image_path)
    if not os.path.exists(image_path) or not os.path.isfile(image_path):
        return {"error": "File does not exist"}

    return FileResponse(image_path, media_type="image/jpeg")

@router.get("/specialcars/{car_id}/photo")
def get_image_by_id_from_special(car_id: int, db: Session = Depends(get_db)):
    image_path = db.query(SpecialCar.photo_path).filter(SpecialCar.car_id == car_id).scalar() + "\photo_1.jpg"
    if not os.path.exists(image_path) or not os.path.isfile(image_path):
        return {"error": "File does not exist"}
    return FileResponse(image_path, media_type="image/jpeg")

@router.get("/allcars/search/{value}")
def get_unique_values_from_all(value: str, db: Session = Depends(get_db)):
    unique_values = db.query(distinct(getattr(Car, value))).all()
    return {"unique_values": [val[0] for val in unique_values]}

@router.get("/specialcars/search/{value}")
def get_unique_values_from_special(value: str, db: Session = Depends(get_db)):
    unique_values = db.query(distinct(getattr(SpecialCar, value))).all()
    return {"unique_values": [val[0] for val in unique_values]}

@router.get("/allcars/searchminmax/{value}")
def get_min_max_values_from_all(value: str, db: Session = Depends(get_db)):
    column = getattr(Car, value, None)
    min_max_values = db.query(func.min(column), func.max(column)).first()
    return {"min_max_values": list(min_max_values)}

@router.get("/specialcars/searchminmax/{value}")
def get_min_max_values_from_all(value: str, db: Session = Depends(get_db)):
    column = getattr(Car, value, None)
    min_max_values = db.query(func.min(column), func.max(column)).first()
    return {"min_max_values": list(min_max_values)}


# @router.post("/cars")
# def create_car(name: str, price: float, db: Session = Depends(get_db)):
#     car = Car(mark=name, price=price)
#     db.add(car)
#     db.commit()
#     db.refresh(car)
#     return {"message": "Car added successfully", "car": car}
