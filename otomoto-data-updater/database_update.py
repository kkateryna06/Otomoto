import psycopg2
from psycopg2.sql import SQL, Identifier


def update_database_car_relevant(car_id, sell_date, database_table):
    insert_values = [sell_date, car_id]
    insert_car_relevant_query = SQL("""
    UPDATE {}
    SET sell_date = %s
    WHERE car_id = %s;
    """).format(Identifier(database_table))

    return insert_car_relevant_query, insert_values


def update_database_car_info(car_data, database_table):
    insert_values = [
        car_data['mark'], car_data['model'], car_data['version'], car_data['color'], car_data['door_count'],
        car_data['nr_seats'], car_data['year'], car_data['generation'], car_data['fuel_type'],
        car_data['engine_capacity'], car_data['engine_power'], car_data['body_type'], car_data['gearbox'],
        car_data['transmission'], car_data['urban_consumption'], car_data['extra_urban_consumption'],
        car_data['mileage'], car_data['has_registration'], car_data['equipment'], car_data['parameters_dict'],
        car_data['price'], car_data['date'], car_data['description'], car_data['link'], car_data['car_id'],
        car_data['location'], car_data['photo_path'], car_data['html_path'], car_data['seller_type'],
    ]

    print(insert_values)

    insert_car_info_query = SQL("""
            INSERT INTO {} (
                mark, model, version, color, door_count, nr_seats, year, generation, fuel_type, engine_capacity,
                engine_power, body_type, gearbox, transmission, urban_consumption, extra_urban_consumption,
                mileage, has_registration, equipment, parameters_dict, price, date, description, link, car_id,
                location, photo_path, html_path, seller_type
            ) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s,
                      %s, %s, %s, %s, %s, %s, %s, %s);
            """).format(Identifier(database_table))

    return insert_car_info_query, insert_values

def update_database(data, to_do, database_table):

    try:
        # Connecting to the database
        connection = psycopg2.connect(
            host="localhost",
            database="otomoto",
            user="postgres",
            password="990",
        )

        with connection.cursor() as cursor:
            if to_do == "car_relevant":
                # Getting car_id by the link
                query = SQL("""
                SELECT car_id
                FROM {}
                WHERE link = %s
                """).format(Identifier(database_table))
                cursor.execute(query, (data[0],))
                car_id = cursor.fetchall()

                # Extracting car_id
                clean_car_id = car_id[0][0] if car_id and isinstance(car_id[0], (list, tuple)) else (
                    car_id[0] if car_id else None)

                # Updating data
                query, values = update_database_car_relevant(clean_car_id, data[1], database_table)
                cursor.execute(query, values)

            elif to_do == "new_ads":
                # Inserting new car data
                query, values = update_database_car_info(data, database_table)
                cursor.execute(query, values)

            # Committing the transaction
            connection.commit()
            print("Database updated")

    except Exception as ex:
        print(f"Error: {ex}")
    finally:
        # Closing the connection
        if connection:
            connection.close()
            print("Connection closed")



def get_all_car_links(database_table):
    try:
        connection = psycopg2.connect(
            host="localhost",
            database="otomoto",
            user="postgres",
            password="990",
        )

        with connection.cursor() as cursor:
            query = SQL("SELECT link FROM {}").format(Identifier(database_table))
            cursor.execute(query)
            result = cursor.fetchall()
            # Преобразуем список кортежей [(1,), (2,), (3,)] в [1, 2, 3]
            car_ids = [row[0] for row in result]
            return car_ids

    except Exception as ex:
        print(f"Error: {ex}")
        return []

    finally:
        if connection:
            connection.close()


def get_all_car_links_for_relevant_check(database_table):
    try:
        connection = psycopg2.connect(
            host="localhost",
            database="otomoto",
            user="postgres",
            password="990",
        )

        with connection.cursor() as cursor:
            query = SQL("SELECT link FROM {} where sell_date is null").format(Identifier(database_table))
            cursor.execute(query)
            result = cursor.fetchall()
            # Преобразуем список кортежей [(1,), (2,), (3,)] в [1, 2, 3]
            car_ids = [row[0] for row in result]
            return car_ids

    except Exception as ex:
        print(f"Error: {ex}")
        return []

    finally:
        if connection:
            connection.close()

