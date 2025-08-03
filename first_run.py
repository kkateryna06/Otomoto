"""
run before using the application to make sure everything is set up and working correctly.
if there are no errors you can continue :)

1st step. checking local connection to db
2nd step. checking remote connection to db
3rd step. creating two tables in the db
4th step. checking the recorded car links (fill in the links_config.txt before checking)
5th step. checking server connection

if you see the inscription "✅Everything is okay!✅", then everything is working correctly
"""
import os
import signal
import subprocess
import time

import psycopg2
import requests
from sqlalchemy import create_engine, text
from sqlalchemy.orm import sessionmaker

import db_config

def check_db_local_connection():
    try:
        conn = psycopg2.connect(**db_config.DB_SETTINGS)
        conn.close()
        print('✅ Database local connection established')
    except Exception as e:
        print(f"❌ Database local connection failed: {e}")
        return True


def check_db_remote_connection():
    try:
        engine = create_engine(db_config.DATABASE_URL)
        SessionLocal = sessionmaker(bind=engine, autocommit=False, autoflush=False)

        db = SessionLocal()
        db.execute(text('select 1'))
        db.close()

        print('✅ Database connection established')

    except Exception as e:
        print(f"❌ Database connection failed: {e}")
        return True


def create_table_query(db_table):
    query = """
                create table if not exists {}(
        car_id bigint primary key,
        date text,
        sell_date text,
        mark text,
        model text,
        version text,
        year int,
        mileage bigint,
        fuel_type text,
        engine_capacity int,
        engine_power int,
        price int,
        body_type text,
        gearbox text,
        transmission text,
        urban_consumption double precision,
        extra_urban_consumption double precision,
        color text,
        door_count int,
        nr_seats int,
        generation text,
        has_registration boolean,
        seller_type text,
        link text,
        location jsonb,
        photo_path text,
        html_path text,
        description text
    )
                """.format(db_table)

    return query


def create_database_table():
    conn = None
    try:

        conn = psycopg2.connect(**db_config.DB_SETTINGS)
        cursor = conn.cursor()
        cursor.execute(
            create_table_query("cars_info")
        )
        cursor.execute(
            create_table_query("special_cars_info")
        )

        conn.commit()
        print('✅ Database created successfully')

    except (Exception, psycopg2.DatabaseError) as error:
        print(f'❌ {error}')
        return True

    finally:
        if conn is not None:
            conn.close()


def check_links(filepath="links_config.txt"):
    all_links = []
    special_links = []

    with open(filepath, encoding="utf-8") as f:
        lines = f.readlines()

    current_type = None
    for line in lines:
        line = line.strip()
        if not line:
            continue
        if line.startswith("#"):
            if "ALL" in line.upper():
                current_type = "all"
            elif "SPECIAL" in line.upper():
                current_type = "special"
            continue
        if current_type == "all":
            all_links.append((line, None))  # no label
        elif current_type == "special":
            label = None
            if "|" in line:
                url, label = line.split("|", 1)
                line = url.strip()
                label = label.strip()
            special_links.append((line, label or "Special ad"))

    links = {'All links': all_links, 'Special links': special_links}
    for key, value in links.items():
        print(f"{key}:")
        if value:
            for line in value:
                print(line[0])
        else:
            print("No links")


def check_server_connection(url="http://127.0.0.1:8000/"):
    try:
        response = requests.get(url)
        return response.status_code == 200
    except requests.RequestException:
        return False

def start_and_check_server():
    print("🚀 Starting server...")
    process = subprocess.Popen(
        ["uvicorn", "otomoto-server.app.main:app", "--host", "127.0.0.1", "--port", "8000"],
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,
        text=True,
        creationflags=subprocess.CREATE_NEW_PROCESS_GROUP
    )

    for line in iter(process.stdout.readline, ''):
        print("[uvicorn]", line.strip())
        if "Application startup complete" in line:
            break

    try:
        for _ in range(10):
            if check_server_connection():
                print("✅ Server is up and healthy")
                break
            time.sleep(1)
        else:
            print("❌ Server failed to respond in time")
            return True

        time.sleep(2)

    finally:
        print("Shutting down server...")
        if os.name == 'nt':
            process.send_signal(signal.CTRL_BREAK_EVENT)
        else:
            process.terminate()
        process.wait()
        print("Server stopped")


def main():
    for step in [
        ("Checking database connection...", check_db_local_connection),
        ("Checking database remote connection...", check_db_remote_connection),
        ("Checking database creation...", create_database_table),
        ("Checking links...", check_links),
        ("Checking server connection...", start_and_check_server)
    ]:
        print(step[0])
        if step[1]():
            break
        print("---------")

    print("✅Everything is okay!✅")


main()
