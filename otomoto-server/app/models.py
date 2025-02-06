from sqlalchemy import Column, Integer, String, Float, Boolean

from .database import Base


class BaseCar(Base):
    __abstract__ = True  # SQLAlchemy doesn't create a table for this class

    car_id = Column(Integer, primary_key=True)
    date = Column(String)
    mark = Column(String, index=True)
    model = Column(String)
    version = Column(String)
    year = Column(Integer, index=True)
    mileage = Column(Integer, index=True)
    fuel_type = Column(String)
    engine_capacity = Column(Integer, index=True)
    engine_power = Column(Integer, index=True)
    price = Column(Float)
    body_type = Column(String, index=True)
    gearbox = Column(String)
    transmission = Column(String)
    urban_consumption = Column(String)
    extra_urban_consumption = Column(String)
    color = Column(String)
    door_count = Column(Integer)
    nr_seats = Column(Integer)
    generation = Column(String, index=True)
    has_registration = Column(Boolean)
    seller_type = Column(String)
    equipment = Column(String)
    parameters_dict = Column(String)
    description = Column(String)
    link = Column(String)
    location = Column(String)
    photo_path = Column(String)
    html_path = Column(String)

class Car(BaseCar):
    __tablename__ = 'cars_info'

class SpecialCar(BaseCar):
    __tablename__ = 'special_cars_info'
