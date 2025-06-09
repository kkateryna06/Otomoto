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

@router.get(
    "/allcars/",
    summary="Get all cars",
    description="""
        Returns a list of all cars in the database.
        You can use filters by brand, price, year, mileage, body type, fuel type, engine capacity, consumption, etc.
        
        The result can be split into pages using the `page` and `page_size` parameters.
    """,
    responses={
        200: {"description": "List of cars successfully received"},
        500: {"description": "Error while receiving list"},
    }
)
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
        max_extra_urban_consumption: Optional[float] = Query(None),
        page: int = Query(0, ge=0),
        page_size: int = Query(20, gt=0),
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

    start = page * page_size
    end = start + page_size
    cars = cars[start:end]

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



@router.get(
    "/specialcars",
    summary="Get special cars",
    description="""
        Returns a list of specially selected cars.
        Filters and pagination work similarly to the regular `/allcars/` query.
    """,
    responses={
        200: {"description": "List of cars successfully received"},
        500: {"description": "Error while receiving list"},
    }
)
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
        max_extra_urban_consumption: Optional[float] = Query(None),
        page: int = Query(0, ge=0),
        page_size: int = Query(20, gt=0),
):
    query = db.query(SpecialCar)

    if mark:
        query = query.filter(SpecialCar.mark.in_(mark))
    # if model:
    #     query = query.filter(SpecialCar.model == model)
    if min_price:
        query = query.filter(SpecialCar.price >= min_price)
    if max_price:
        query = query.filter(SpecialCar.price <= max_price)
    if min_year:
        query = query.filter(SpecialCar.year >= min_year)
    if max_year:
        query = query.filter(SpecialCar.year <= max_year)
    if body_type:
        query = query.filter(SpecialCar.body_type.in_(body_type))
    if min_mileage:
        query = query.filter(SpecialCar.mileage >= min_mileage)
    if max_mileage:
        query = query.filter(SpecialCar.mileage <= max_mileage)
    if fuel_type:
        query = query.filter(SpecialCar.fuel_type.in_(fuel_type))
    if min_engine_capacity:
        query = query.filter(SpecialCar.engine_capacity >= min_engine_capacity)
    if max_engine_capacity:
        query = query.filter(SpecialCar.engine_capacity <= max_engine_capacity)
    if min_urban_consumption:
        query = query.filter(SpecialCar.urban_consumption >= min_urban_consumption)
    if max_urban_consumption:
        query = query.filter(SpecialCar.urban_consumption <= max_urban_consumption)
    if min_extra_urban_consumption:
        query = query.filter(SpecialCar.extra_urban_consumption >= min_extra_urban_consumption)
    if max_extra_urban_consumption:
        query = query.filter(SpecialCar.extra_urban_consumption <= max_extra_urban_consumption)

    cars = query.order_by(asc(SpecialCar.mark)).all()

    start = page * page_size
    end = start + page_size
    cars = cars[start:end]

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

@router.get(
    "/allcars/{car_id}",
    summary="Get car by ID (All Cars)",
    description="Returns a single car from the 'all cars' dataset using the provided car ID.",
    responses={
        200: {"description": "Car data retrieved successfully"},
        404: {"description": "Car not found"},
    }
)
def get_car_by_id_from_all(car_id: int, db: Session = Depends(get_db)):
    car = db.query(Car).filter(Car.car_id == car_id).first()
    return car

@router.get(
    "/specialcars/{car_id}",
    summary="Get car by ID (Special Cars)",
    description="Returns a single car from the 'special cars' dataset using the provided car ID.",
    responses={
        200: {"description": "Special car data retrieved successfully"},
        404: {"description": "Special car not found"},
    }
)
def get_car_by_id_from_special(car_id: int, db: Session = Depends(get_db)):
    car = db.query(SpecialCar).filter(SpecialCar.car_id == car_id).first()
    return car

@router.get(
    "/allcars/{car_id}/photo",
    summary="Get car photo by ID (All Cars)",
    description="Returns the image (photo_1.jpg) of the car with the given ID from the 'all cars' dataset.",
    responses={
        200: {"content": {"image/jpeg": {}}},
        404: {"description": "Photo not found"},
    }
)
def get_image_by_id_from_all(car_id: int, db: Session = Depends(get_db)):
    image_path = db.query(Car.photo_path).filter(Car.car_id == car_id).scalar() + "\photo_1.jpg"
    print(image_path)
    if not os.path.exists(image_path) or not os.path.isfile(image_path):
        return {"error": "File does not exist"}

    return FileResponse(image_path, media_type="image/jpeg")

@router.get(
    "/specialcars/{car_id}/photo",
    summary="Get car photo by ID (Special Cars)",
    description="Returns the image (photo_1.jpg) of the car with the given ID from the 'special cars' dataset.",
    responses={
        200: {"content": {"image/jpeg": {}}},
        404: {"description": "Photo not found"},
    }
)
def get_image_by_id_from_special(car_id: int, db: Session = Depends(get_db)):
    image_path = db.query(SpecialCar.photo_path).filter(SpecialCar.car_id == car_id).scalar() + "\photo_1.jpg"
    if not os.path.exists(image_path) or not os.path.isfile(image_path):
        return {"error": "File does not exist"}
    return FileResponse(image_path, media_type="image/jpeg")

@router.get(
    "/allcars/search/{value}",
    summary="Get unique values (All Cars)",
    description="Returns a list of unique values for a given field from the 'all cars' dataset.",
    responses={
        200: {"description": "List of unique values"},
        400: {"description": "Invalid column name"},
    }
)
def get_unique_values_from_all(value: str, db: Session = Depends(get_db)):
    unique_values = db.query(distinct(getattr(Car, value))).all()
    return {"unique_values": [val[0] for val in unique_values]}

@router.get(
    "/specialcars/search/{value}",
    summary="Get unique values (Special Cars)",
    description="Returns a list of unique values for a given field from the 'special cars' dataset.",
    responses={
        200: {"description": "List of unique values"},
        400: {"description": "Invalid column name"},
    }
)
def get_unique_values_from_special(value: str, db: Session = Depends(get_db)):
    unique_values = db.query(distinct(getattr(SpecialCar, value))).all()
    return {"unique_values": [val[0] for val in unique_values]}

@router.get(
    "/allcars/searchminmax/{value}",
    summary="Get min and max values (All Cars)",
    description="Returns the minimum and maximum value for a numeric field from the 'all cars' dataset.",
    responses={
        200: {"description": "Min and max values"},
        400: {"description": "Invalid column name"},
    }
)
def get_min_max_values_from_all(value: str, db: Session = Depends(get_db)):
    column = getattr(SpecialCar, value, None)
    min_max_values = db.query(func.min(column), func.max(column)).first()
    return {"min_max_values": list(min_max_values)}

@router.get(
    "/specialcars/searchminmax/{value}",
    summary="Get min and max values (Special Cars)",
    description="Returns the minimum and maximum value for a numeric field from the 'special cars' dataset.",
    responses={
        200: {"description": "Min and max values"},
        400: {"description": "Invalid column name"},
    }
)
def get_min_max_values_from_all(value: str, db: Session = Depends(get_db)):
    column = getattr(SpecialCar, value, None)
    min_max_values = db.query(func.min(column), func.max(column)).first()
    return {"min_max_values": list(min_max_values)}
