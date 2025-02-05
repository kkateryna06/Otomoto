import requests
import json
from openpyxl import load_workbook
import pandas as pd
import os
import urllib.parse
from datetime import datetime
from bs4 import BeautifulSoup

from database_update import update_database


# Function for loading HTML content
def fetch_html(url):
    headers = {
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.212 Safari/537.36"
    }
    try:
        response = requests.get(url, headers=headers)
        response.raise_for_status()  # Check for a successful response
        return response.text
    except requests.exceptions.HTTPError as e:
        print(f"Error requesting URL: {url} - {e}")
        return None  # Return None in case of error


# Function to extract JSON data from the first found script
def extract_json_from_html(html_content):
    soup = BeautifulSoup(html_content, 'html.parser')
    script = soup.find('script', {'type': 'application/ld+json'})
    if script:
        try:
            return json.loads(script.string)
        except json.JSONDecodeError:
            return None
    return None


# Function to extract JSON data from <script id="__NEXT_DATA__"> tag
def extract_json_from_script_tag(html_content):
    soup = BeautifulSoup(html_content, 'html.parser')

    # Find the required tag <script id="__NEXT_DATA__" type="application/json">
    script_tag = soup.find('script', {'id': '__NEXT_DATA__', 'type': 'application/json'})

    if script_tag:
        try:
            return json.loads(script_tag.string)  # Convert the tag content to JSON
        except json.JSONDecodeError:
            print("Error parsing JSON")
            return None
    else:
        print("Tag with required data not found")
        return None


# Function to extract links
def extract_links(html_content):
    # Разбор HTML
    soup = BeautifulSoup(html_content, 'html.parser')

    # Поиск всех тегов <a>, содержащих ссылки на объявления
    links = set()
    for a_tag in soup.find_all('a', href=True):
        href = a_tag['href']
        if "otomoto.pl" in href and "ID" in href:  # Пример фильтрации для ссылок на объявления
            links.add(href)

    return links


# Function to convert URL to safe file name
def sanitize_filename(url):
    # Convert the URL to a filesystem-safe string
    return urllib.parse.quote(url, safe='')


def create_safe_folder_name(url):
    folder_name = sanitize_filename(url)
    folder_path = os.path.join("car_photos", folder_name)
    os.makedirs(folder_path, exist_ok=True)  # Create a folder if it doesn't exist
    return folder_path


# Function to upload photos to a specified folder
def download_images(photo_links, folder_path):
    for j, photo_url in enumerate(photo_links):
        try:
            response = requests.get(photo_url, stream=True)
            photo_path = os.path.join(folder_path, f"photo_{j + 1}.jpg")
            with open(photo_path, "wb") as photo_file:
                photo_file.write(response.content)
        except Exception as e:
            print(f"Error uploading photo {photo_url}: {e}")


# Function to clear description from HTML tags
def clean_html_description(description_html):
    # Using BeautifulSoup to Clean HTML
    soup = BeautifulSoup(description_html, "html.parser")
    return soup.get_text(separator=" ").strip()  # Return only text separated by spaces


# Function to retrieve data about the car
def extract_car_data(link, json_data):
    advert = json_data.get("props", {}).get("pageProps", {}).get("advert", {})

    # BASIC INFORMATION
    details = advert.get("details", {})

    mark = next((i["value"] for i in details if i["key"] == "make"), None)
    model = next((i["value"] for i in details if i["key"] == "model"), None)
    version = next((i["value"] for i in details if i["key"] == "version"), None)
    color = next((i["value"] for i in details if i["key"] == "color"), None)
    door_count = next((i["value"] for i in details if i["key"] == "door_count"), None)
    nr_seats = next((i["value"] for i in details if i["key"] == "nr_seats"), None)
    year = next(((i["value"] for i in details if i["key"] == "year")), None)
    generation = next((i["value"] for i in details if i["key"] == "generation"), None)

    # TECHNICAL SPECS
    fuel_type = next((i["value"] for i in details if i["key"] == "fuel_type"), None)
    engine_capacity = next((i["value"] for i in details if i["key"] == "engine_capacity"), None)
    engine_capacity = engine_capacity[:-4].replace(" ", "")
    engine_power = next((i["value"] for i in details if i["key"] == "engine_power"), None)
    engine_power = engine_power[:-3]
    body_type = next((i["value"] for i in details if i["key"] == "body_type"), None)
    gearbox = next((i["value"] for i in details if i["key"] == "gearbox"), None)
    transmission = next((i["value"] for i in details if i["key"] == "transmission"), None)
    urban_consumption = next((i["value"] for i in details if i["key"] == "urban_consumption"), None)
    extra_urban_consumption = next((i["value"] for i in details if i["key"] == "extra_urban_consumption"),
                                   None)
    mileage = next((i["value"] for i in details if i["key"] == "mileage"), None)
    mileage = mileage[:-3].replace(" ", "")

    # CONDITION HISTORY
    has_registration = next((i["value"] for i in details if i["key"] == "has_registration"), None)
    has_registration = True if has_registration == "Tak" else False

    # EQUIPMENT
    equipment = advert.get("equipment", {})
    equipment = json.dumps(equipment)

    # PARAMETRS
    parameters_dict = advert.get("parametersDict", {})
    parameters_dict = json.dumps(parameters_dict)

    # ADVERT INFO
    price = advert.get("price", {}).get("value", None)
    date = advert.get("createdAt", None)
    id = advert.get("id")
    description = clean_html_description(advert.get("description", None))

    seller = advert.get("seller", {})
    seller_type = seller.get("type", None)

    # LOCATION
    location = advert.get("seller", {}).get("location", {}).get("map", {})
    location = json.dumps(location)

    # PATH
    base_folder_photo = r"C:\Users\katya\Desktop\otomoto\otomoto-data-updater\car_photos"
    photo_folder = urllib.parse.quote(link, safe='')
    photo_path = os.path.join(base_folder_photo, photo_folder)

    base_folder_html = r"C:\Users\katya\Desktop\otomoto\otomoto-data-updater\car_htmls"
    html_folder = urllib.parse.quote(link, safe='')
    html_path = os.path.join(base_folder_html, html_folder)

    #Download photos
    photos = advert.get("images", {}).get("photos", [])
    photo_links = [photo.get("url") for photo in photos]
    folder_path = create_safe_folder_name(link)
    download_images(photo_links, folder_path)

    dict_result = {
        "mark": mark, "model": model, "version": version, "color": color,
        "door_count": door_count, "nr_seats": nr_seats, "year": year, "generation": generation,
        "fuel_type": fuel_type, "engine_capacity": engine_capacity, "engine_power": engine_power,
        "body_type": body_type, "gearbox": gearbox,
        "transmission": transmission, "urban_consumption": urban_consumption,
        "extra_urban_consumption": extra_urban_consumption, "mileage": mileage,
        "has_registration": has_registration, "equipment": equipment,
        "parameters_dict": parameters_dict, "price": price, "date": date, "description": description,
        "link": link, "car_id": id, "location": location, "photo_path": photo_path,
        "html_path": html_path, "seller_type": seller_type
    }
    return dict_result


# The function updates the Excel file while preserving the formats.
def update_excel_with_styles(existing_file, updated_df):
    try:
        # Load an existing Excel file
        workbook = load_workbook(existing_file)
        sheet_name = workbook.sheetnames[0]  # Sheet name (default first)
        sheet = workbook[sheet_name]

        # Clear data but keep styles
        for row in sheet.iter_rows(min_row=2):  # Start with line 2 (the first one is the headings)
            for cell in row:
                cell.value = None  # Delete data, but styles remain

        # Write updated data
        for i, row in updated_df.iterrows():
            for j, value in enumerate(row):
                sheet.cell(row=i + 2, column=j + 1, value=value)

        # Save the file
        workbook.save(existing_file)
    except Exception as e:
        print(f"Error updating excel file: {e}")


# Function to retrieve information about machines
def update_data(url, database_table, excel_table):
    # Load an existing table if it exists
    if os.path.exists(excel_table):
        try:
            existing_df = pd.read_excel(excel_table)
            if "link" in existing_df.columns:
                existing_links = set(existing_df["link"].dropna().str.strip())
            else:
                print("The 'link' column is missing from the file.")
                existing_df = pd.DataFrame()
                existing_links = set()
        except Exception as e:
            print(f"Error reading Excel file: {e}")
            existing_df = pd.DataFrame()
            existing_links = set()
    else:
        existing_df = pd.DataFrame()
        existing_links = set()

    # Get the HTML content of the page
    html = fetch_html(url)
    if not html:
        return

    # Extract links to ads
    links = extract_links(html)
    new_car_data = []
    count = 0

    for link in links:
        if link in existing_links:
            print(f"Skip the ad (already in the table): {link}")
            continue

        count+= 1

        # Load the HTML content of the ad
        html = fetch_html(link)
        if not html:
            continue


        html_filename = sanitize_filename(link) + ".html"
        html_path = os.path.join('car_htmls', html_filename)

        os.makedirs('car_htmls', exist_ok=True)

        with open(html_path, "w", encoding="utf-8") as file:
            file.write(html)

        print(f"Processing a new ad: {link}")
        json_data = extract_json_from_script_tag(html)
        if json_data:
            # Define a new column order
            new_column_order = [
                'relevant', 'date', 'sell_date', 'mark', 'model', 'version', 'year', 'fuel_type', 'mileage',
                'engine_capacity', 'price', 'body_type', 'gearbox', 'transmission',
                'seller_type', 'location', 'link', 'photo_path', 'html_path', 'description'
            ]

            car_info = extract_car_data(link, json_data)
            update_database(car_info, "new_ads", database_table)
            filtered_dict = {key: car_info[key] for key in car_info if key in new_column_order}
            filtered_dict["year"] = int(filtered_dict["year"])
            filtered_dict["price"] = int(filtered_dict["price"])
            filtered_dict["mileage"] = int(filtered_dict["mileage"])
            filtered_dict["engine_capacity"] = filtered_dict["engine_capacity"] + " cm3"
            filtered_dict["relevant"] = "yes"
            new_car_data.append(filtered_dict)

    # Save updated date
    if new_car_data:
        new_df = pd.DataFrame(new_car_data)
        updated_df = pd.concat([existing_df, new_df], ignore_index=True)
        updated_df = updated_df[new_column_order]

        # Instead of pandas.to_excel we use openpyxl to save styles
        if os.path.exists(excel_table):
            update_excel_with_styles(excel_table, updated_df)
        else:
            updated_df.to_excel(excel_table, index=False)  # If the file does not exist, create it
        print(f"Table successfully updated! {count} new ads")

    else:
        print("No new ads found.")


def task_special():
    database_table = "special_cars_info"
    excel_table = "special_cars_info.xlsx"

    """MITSUBISHI LANCER"""
    url = 'https://www.otomoto.pl/osobowe/mitsubishi/lancer?search%5Bfilter_float_price%3Ato%5D=27000&search%5Border%5D=created_at_first%3Adesc'
    update_data(url, database_table, excel_table)
    with open("logs.txt", "a", encoding="utf-8") as f:  # "a" to append, "w" to overwrite
        f.write(f'Mitsubishi Lancer ads were updated {datetime.now().strftime("%Y-%m-%d %H:%M:%S")}\n')  # Add line break

    """KIA RIO"""
    url = 'https://www.otomoto.pl/osobowe/kia/rio?search%5Bfilter_enum_damaged%5D=0&search%5Bfilter_float_price%3Ato%5D=27000&search%5Border%5D=created_at_first%3Adesc&search%5Badvanced_search_expanded%5D=true'
    update_data(url, database_table, excel_table)
    with open("logs.txt", "a", encoding="utf-8") as f:  # "a" to append, "w" to overwrite
        f.write(f'Kia Rio ads were updated {datetime.now().strftime("%Y-%m-%d %H:%M:%S")}\n')  # Add line break

    """HONDA CIVIC"""
    url = 'https://www.otomoto.pl/osobowe/honda/civic?search%5Bfilter_enum_damaged%5D=0&search%5Bfilter_float_price%3Ato%5D=27000&search%5Badvanced_search_expanded%5D=true'
    update_data(url, database_table, excel_table)
    with open("logs.txt", "a", encoding="utf-8") as f:  # "a" to append, "w" to overwrite
        f.write(f'Honda Civic ads were updated {datetime.now().strftime("%Y-%m-%d %H:%M:%S")}\n')  # Add line break

    """OPEL ASTRA"""
    url = 'https://www.otomoto.pl/osobowe/opel/astra?search%5Bfilter_enum_damaged%5D=0&search%5Bfilter_float_price%3Afrom%5D=15000&search%5Bfilter_float_price%3Ato%5D=27000&search%5Border%5D=created_at_first%3Adesc&search%5Badvanced_search_expanded%5D=true'
    update_data(url, database_table, excel_table)
    with open("logs.txt", "a", encoding="utf-8") as f:  # "a" to append, "w" to overwrite
        f.write(f'Opel Astra ads were updated {datetime.now().strftime("%Y-%m-%d %H:%M:%S")}\n')  # Add line break

    """FORD FOCUS"""
    url = 'https://www.otomoto.pl/osobowe/ford/focus?search%5Bfilter_enum_damaged%5D=0&search%5Bfilter_float_price%3Afrom%5D=15000&search%5Bfilter_float_price%3Ato%5D=27000&search%5Border%5D=created_at_first%3Adesc&search%5Badvanced_search_expanded%5D=true'
    update_data(url, database_table, excel_table)
    with open("logs.txt", "a", encoding="utf-8") as f:  # "a" to append, "w" to overwrite
        f.write(f'Opel Astra ads were updated {datetime.now().strftime("%Y-%m-%d %H:%M:%S")}\n')  # Add line break

    """CITROEN C5"""
    url = 'https://www.otomoto.pl/osobowe/citroen/c5?search%5Bfilter_enum_damaged%5D=0&search%5Bfilter_float_price%3Afrom%5D=15000&search%5Bfilter_float_price%3Ato%5D=27000&search%5Border%5D=created_at_first%3Adesc&search%5Badvanced_search_expanded%5D=true'
    update_data(url, database_table, excel_table)
    with open("logs.txt", "a", encoding="utf-8") as f:  # "a" to append, "w" to overwrite
        f.write(f'Opel Astra ads were updated {datetime.now().strftime("%Y-%m-%d %H:%M:%S")}\n')  # Add line break

    """RENAULT MEGANE"""
    url = 'https://www.otomoto.pl/osobowe/renault/megane?search%5Bfilter_enum_damaged%5D=0&search%5Bfilter_float_price%3Afrom%5D=15000&search%5Bfilter_float_price%3Ato%5D=27000&search%5Border%5D=created_at_first%3Adesc&search%5Badvanced_search_expanded%5D=true'
    update_data(url, database_table, excel_table)
    with open("logs.txt", "a", encoding="utf-8") as f:  # "a" to append, "w" to overwrite
        f.write(f'Opel Astra ads were updated {datetime.now().strftime("%Y-%m-%d %H:%M:%S")}\n')  # Add line break


def task():
    url = 'https://www.otomoto.pl/osobowe/seg-cabrio--seg-city-car--seg-combi--seg-compact--seg-coupe--seg-mini--seg-sedan/od-2010/dolnoslaskie?search%5Bfilter_enum_damaged%5D=0&search%5Bfilter_float_mileage%3Ato%5D=200000&search%5Bfilter_float_price%3Afrom%5D=15000&search%5Bfilter_float_price%3Ato%5D=25000&search%5Badvanced_search_expanded%5D=true'
    database_table = "cars_info"
    excel_table = "cars_info.xlsx"
    update_data(url, database_table, excel_table)
    with open("logs.txt", "a", encoding="utf-8") as f:  # "a" to append, "w" to overwrite
        f.write(f'Ads were updated {datetime.now().strftime("%Y-%m-%d %H:%M:%S")}\n')  # Add line break
