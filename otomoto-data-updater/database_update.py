import pg8000


def update_database_car_relevant(car_id, relevant=True, sell_date=""):
    insert_values = [car_id, relevant, sell_date]
    insert_car_relevant_query = """
    INSERT INTO car_relevant (
        car_id, relevant, sell_date
    ) VALUES (%s, %s, %s);
    """

    return insert_car_relevant_query, insert_values


def update_database_car_info(car_data):
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

    insert_car_info_query = """
            INSERT INTO car_info (
                mark, model, version, color, door_count, nr_seats, year, generation, fuel_type, engine_capacity,
                engine_power, body_type, gearbox, transmission, urban_consumption, extra_urban_consumption,
                mileage, has_registration, equipment, parameters_dict, price, date, description, link, car_id,
                location, photo_path, html_path, seller_type
            ) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s,
                      %s, %s, %s, %s, %s, %s, %s, %s);
            """

    return insert_car_info_query, insert_values


def update_database(data, to_do):
    try:
        connection = pg8000.connect(
            host="localhost",
            database="otomoto",
            user="postgres",
            password="990",
        )

        with connection.cursor() as cursor:
            if to_do == "car_relevant":
                query = """
                SELECT car_id
                FROM car_info
                WHERE link = %s
                """
                cursor.execute(query, (data[0],))
                car_id = cursor.fetchall()
                clean_car_id = car_id[0][0] if car_id and isinstance(car_id[0], (list, tuple)) else (
                    car_id[0] if car_id else None)

                query, values = update_database_car_relevant(clean_car_id, data[1], data[2])
                cursor.execute(query, values)

            elif to_do == "car_info":
                query, values = update_database_car_info(data)
                cursor.execute(query, values)

            connection.commit()
            print("Database updated")

    except Exception as ex:
        print(ex)
    finally:
        if connection:
            connection.close()
            print("Connection closed")



# Function to retrieve information about machines
# def update_data(url):
#     # Get the HTML content of the page
#     html = fetch_html(url)
#     if not html:
#         return
#
#     os.makedirs('car_htmls', exist_ok=True)
#
#     json_data = extract_json_from_script_tag(html)
#     if json_data:
#         car_info = extract_car_data(url, json_data)
#         print(len(car_info))
#         update_database(car_info)

        # for key, value in car_info.items():
        #     print(f"{key}: {value}")
