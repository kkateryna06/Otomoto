import ast
import os
from typing import Optional, List

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
        mark: Optional[List[str]] = Query(None),
        # model: Optional[List[str]] = Query(None),
        min_price: Optional[float] = Query(None),
        max_price: Optional[float] = Query(None),
        min_year: Optional[int] = Query(None),
        max_year: Optional[int] = Query(None),
        body_type: Optional[List[str]] = Query(None),
        min_mileage: Optional[float] = Query(None),
        max_mileage: Optional[float] = Query(None),
        fuel_type: Optional[List[str]] = Query(None),
        min_engine_capacity: Optional[float] = Query(None),
        max_engine_capacity: Optional[float] = Query(None),
        min_urban_consumption: Optional[float] = Query(None),
        max_urban_consumption: Optional[float] = Query(None),
        min_extra_urban_consumption: Optional[float] = Query(None),
        max_extra_urban_consumption: Optional[float] = Query(None)
):
    print(f"Raw mark param: {mark}")
    query = db.query(
        Car.car_id, Car.mark, Car.model, Car.price, Car.mileage, Car.year, Car.body_type, Car.date, Car.sell_date, Car.urban_consumption, Car.engine_power
    )

    if mark:
        query = query.filter(Car.mark.in_(mark))
    # if model:
    #     query = query.filter(Car.model == model)
    if min_price:
        query = query.filter(Car.price >= min_price)
    if max_price:
        query = query.filter(Car.price <= max_price)
    if min_year:
        query = query.filter(Car.year >= min_year)
    if max_year:
        query = query.filter(Car.year <= max_year)
    if body_type:
        query = query.filter(Car.body_type.in_(body_type))
    if min_mileage:
        query = query.filter(Car.mileage >= min_mileage)
    if max_mileage:
        query = query.filter(Car.mileage <= max_mileage)
    if fuel_type:
        query = query.filter(Car.fuel_type.in_(fuel_type))
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
            "sell_date": car.sell_date,
            "urban_consumption": car.urban_consumption,
            "engine_power": car.engine_power,
        }
        for car in cars
    ]



@router.get("/specialcars")
def get_special_cars(
        db: Session = Depends(get_db),
        mark: Optional[List[str]] = Query(None),
        # model: Optional[List[str]] = Query(None),
        min_price: Optional[float] = Query(None),
        max_price: Optional[float] = Query(None),
        min_year: Optional[int] = Query(None),
        max_year: Optional[int] = Query(None),
        body_type: Optional[List[str]] = Query(None),
        min_mileage: Optional[float] = Query(None),
        max_mileage: Optional[float] = Query(None),
        fuel_type: Optional[List[str]] = Query(None),
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
    # if model:
    #     query = query.filter(SpecialCar.model == model)
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
            "sell_date": car.sell_date,
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

# /allcars/?mark=%5BHonda%2C%20Ford%2C%20Smart%2C%20Dodge%2C%20Skoda%2C%20Chevrolet%2C%20MINI%2C%20Peugeot%2C%20Da
# cia%2C%20Alfa%20Romeo%2C%20Audi%2C%20Lexus%2C%20Renault%2C%20Kia%2C%20Mercedes-Benz%2C%20Fiat%2C%20Volvo%2C%20Opel%2C%20Lancia%2C%20Nissan%2C%20Citr
# o%C3%ABn%2C%20Suzuki%2C%20Hyundai%2C%20Mitsubishi%2C%20Subaru%2C%20Mazda%2C%20BMW%2C%20DS%20Automobiles%2C%20Volkswagen%2C%20Seat%2C%20Toyota%5D&mod
# el=%5BCruze%2C%20Aveo%2C%20Zafira%2C%202%2C%20S40%2C%20Exeo%2C%20Karl%2C%20Punto%2C%20Leon%2C%20Golf%2C%20DS4%2C%20Octavia%2C%20KA%2C%20C-MAX%2C%20D
# elta%2C%20Superb%2C%20Civic%2C%20Mii%2C%20Logan%2C%20C-Elys%C3%A9e%2C%20Scenic%2C%20Sedici%2C%20RAPID%2C%20Mondeo%2C%20C1%2C%20i20%2C%20Grande%20Pun
# to%2C%20i30%2C%20508%2C%20Fluence%2C%20Clubman%2C%20iQ%2C%20Mito%2C%20DS%203%2C%20ONE%2C%20Megane%2C%20Klasa%20B%2C%20Lancer%2C%20Laguna%2C%20Fiesta
# %2C%20Yaris%2C%20Fabia%2C%20Roomster%2C%20Polo%2C%20Ibiza%2C%20i10%2C%20Splash%2C%20C3%2C%20New%20Beetle%2C%20500%2C%20Giulietta%2C%20Golf%20Plus%2C
# %20B-MAX%2C%20207%20CC%2C%20IONIQ%2C%20Dokker%2C%203%2C%20SX4%2C%20Passat%2C%20DS3%2C%20Tiida%2C%20Fusion%2C%20C4%2C%20Sandero%2C%20Aygo%2C%20Klasa%
# 20A%2C%20Panda%2C%20208%2C%20Swift%2C%20Astra%2C%20up%21%2C%20Ceed%2C%206%2C%20Soul%2C%20301%2C%20Dart%2C%203008%2C%20500L%2C%20ix20%2C%20Sandero%20
# Stepway%2C%20Focus%2C%20Legacy%2C%20Jetta%2C%20Cooper%2C%20Twingo%2C%20Space%20Star%2C%20ProCeed%2C%20C5%2C%20Insignia%2C%20Bravo%2C%20Note%2C%20Mic
# ra%2C%20Spark%2C%20A3%2C%20Meriva%2C%20Clio%2C%205%2C%20Citigo%2C%20Auris%2C%20Punto%20Evo%2C%20Tipo%2C%20Seria%201%2C%20Venga%2C%20i40%2C%20Fortwo%
# 2C%20308%2C%20Corsa%2C%20Picanto%2C%20Thalia%2C%20Rio%2C%20C3%20Picasso%2C%20Adam%2C%20IS%2C%20Punto%202012%2C%20Fiorino%2C%20Toledo%2C%20V50%2C%20G
# rand%20Scenic%2C%20Seria%203%2C%20C30%2C%20Jazz%2C%20A1%5D&min_price=0.0&max_price=25000.0&min_year=2010&max_year=2021&body_type=%5BKompakt%2C%20Sed
# an%2C%20Kabriolet%2C%20Auta%20miejskie%2C%20Coupe%2C%20Auta%20ma%C5%82e%2C%20Kombi%5D&min_mileage=170.0&max_mileage=200000.0&fuel_type=%5BHybryda%2C
# %20Diesel%2C%20Benzyna%2C%20Benzyna%2BLPG%5D&min_engine_capacity=875.0&max_engine_capacity=2231.0&min_urban_consumption=3.0&max_urban_consumption=11.0



