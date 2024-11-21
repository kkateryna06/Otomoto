import requests
from bs4 import BeautifulSoup
import json
import pandas as pd
import os
import urllib.parse
from datetime import datetime


# Функция для загрузки HTML контента
def fetch_html(url):
    headers = {
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.212 Safari/537.36"
    }
    try:
        response = requests.get(url, headers=headers)
        response.raise_for_status()  # Проверка на успешный ответ
        return response.text
    except requests.exceptions.HTTPError as e:
        print(f"Ошибка при запросе URL: {url} - {e}")
        return None  # Возвращаем None в случае ошибки


# Функция для извлечения JSON данных из первого найденного script
def extract_json_from_html(html_content):
    soup = BeautifulSoup(html_content, 'html.parser')
    script = soup.find('script', {'type': 'application/ld+json'})
    if script:
        try:
            return json.loads(script.string)
        except json.JSONDecodeError:
            return None
    return None


# Функция для извлечения JSON данных из тега <script id="__NEXT_DATA__">
def extract_json_from_script_tag(html_content):
    soup = BeautifulSoup(html_content, 'html.parser')

    # Находим нужный тег <script id="__NEXT_DATA__" type="application/json">
    script_tag = soup.find('script', {'id': '__NEXT_DATA__', 'type': 'application/json'})

    if script_tag:
        try:
            return json.loads(script_tag.string)  # Преобразуем содержимое тега в JSON
        except json.JSONDecodeError:
            print("Ошибка при разборе JSON")
            return None
    else:
        print("Тег с нужными данными не найден")
        return None


# Функция для извлечения ссылок
def extract_links(html_content):
    soup = BeautifulSoup(html_content, 'html.parser')
    links = []

    # Ищем все теги <h1> с указанным классом
    h1_tags = soup.find_all('h1', class_='epwfahw9 ooa-1ed90th er34gjf0')
    for h1 in h1_tags:
        a_tag = h1.find('a')  # Ищем тег <a> внутри <h1>
        if a_tag and a_tag.get('href'):  # Проверяем наличие тега <a> и атрибута href
            links.append(a_tag['href'])  # Добавляем ссылку в список

    return links


# Функция для преобразования URL в безопасное имя файла
def sanitize_filename(url):
    # Преобразуем URL в строку, которая безопасна для файловой системы
    return urllib.parse.quote(url, safe='')


# Функция для создания безопасного имени папки
def create_safe_folder_name(url):
    folder_name = sanitize_filename(url)
    folder_path = os.path.join("car_photos", folder_name)
    os.makedirs(folder_path, exist_ok=True)  # Создаем папку, если ее нет
    return folder_path


# Функция для загрузки фотографий в указанную папку
def download_images(photo_links, folder_path):
    for j, photo_url in enumerate(photo_links):
        try:
            response = requests.get(photo_url, stream=True)
            status = response.raise_for_status()
            photo_path = os.path.join(folder_path, f"photo_{j + 1}.jpg")
            with open(photo_path, "wb") as photo_file:
                photo_file.write(response.content)
        except Exception as e:
            print(f"Ошибка при загрузке фото {photo_url}: {e}")


# Функция для очистки описания от HTML тегов
def clean_html_description(description_html):
    # Используем BeautifulSoup для очистки HTML
    soup = BeautifulSoup(description_html, "html.parser")
    return soup.get_text(separator=" ").strip()  # Возвращаем только текст, разделенный пробелами


# Функция для извлечения данных о машине
def extract_car_data(link, json_data, data):
    advert = json_data.get("props", {}).get("pageProps", {}).get("advert", {})
    # Основная информация о машине
    car_name = advert.get("title", "Не указано")
    car_price = advert.get("price", {}).get("value", "Не указано")
    car_description = clean_html_description(advert.get("description", "Описание отсутствует"))
    car_mileage = advert.get("mainFeatures", ["Не указано"])[1] if len(
        advert.get("mainFeatures", [])) > 1 else "Не указано"
    car_mileage = car_mileage[:len(car_mileage) - 3]
    car_fuel_type = advert.get("mainFeatures", ["Не указано"])[3] if len(
        advert.get("mainFeatures", [])) > 3 else "Не указано"
    car_year = advert.get("mainFeatures", ["Не указано"])[0] if len(
        advert.get("mainFeatures", [])) > 0 else "Не указан год"
    car_engine_capacity = advert.get("mainFeatures", ["Не указано"])[2] if len(
        advert.get("mainFeatures", [])) > 2 else "Не указана объем двигателя"

    # Информация о продавце
    seller = advert.get("seller", {})
    seller_type = seller.get("type", "Не указан тип продавца")
    seller_location = seller.get("location", {}).get("shortAddress", "Не указано местоположение")

    # Дополнительная информация
    is_accident_free = advert.get("no_accident", "Не указано")

    photos = advert.get("images", {}).get("photos", [])
    photo_links = [photo.get("url") for photo in photos]

    # Указываем базовый путь, где будут сохраняться фотографии
    base_folder = r"C:\Users\katya\Desktop\otomoto\car_photos"
    photo_folder = sanitize_filename(link)

    # Создаём пути для фотографий
    photo_path = os.path.join(base_folder, photo_folder)

    return {
        "Data": data,
        "Car Name": car_name,
        "Car Year": int(car_year),
        "Car Fuel Type": car_fuel_type,
        "Car Mileage": int(car_mileage.replace(" ", "")),
        "Car Engine Capacity": car_engine_capacity,
        "Car Price": int(car_price),
        "Seller Type": seller_type,
        "Seller Location": seller_location,
        "Car Link": link,
        "Photo Folder": photo_path,
        "Car Description": car_description,
        "Photo Links": photo_links
    }


# Функция для извлечения информации о машинах
def update_data(url):
    data = datetime.now().strftime("%Y-%m-%d %H:%M:%S")

    # Загружаем существующую таблицу, если она есть
    if os.path.exists("cars_data.xlsx"):
        try:
            existing_df = pd.read_excel("cars_data.xlsx")
            if "Car Link" in existing_df.columns:
                existing_links = set(existing_df["Car Link"].dropna().str.strip())
            else:
                print("Колонка 'Car Link' отсутствует в файле.")
                existing_df = pd.DataFrame()
                existing_links = set()
        except Exception as e:
            print(f"Ошибка при чтении файла Excel: {e}")
            existing_df = pd.DataFrame()
            existing_links = set()
    else:
        existing_df = pd.DataFrame()
        existing_links = set()

    # Получаем HTML содержимое страницы
    html = fetch_html(url)
    if not html:
        return

    # Извлекаем ссылки на объявления
    links = extract_links(html)
    new_car_data = []

    for link in links:
        if link in existing_links:
            print(f"Пропускаем объявление (уже есть в таблице): {link}")
            continue

        # Загружаем HTML содержимое объявления
        html = fetch_html(link)
        if not html:
            continue

        print(f"Обрабатываем новое объявление: {link}")
        json_data = extract_json_from_script_tag(html)
        if json_data:
            car_info = extract_car_data(link, json_data, data)
            new_car_data.append(car_info)

            folder_path = create_safe_folder_name(link)
            download_images(car_info["Photo Links"], folder_path)

    # Объединяем новые данные с существующими
    if new_car_data:
        new_df = pd.DataFrame(new_car_data)
        # Объединяем старые и новые данные
        updated_df = pd.concat([existing_df, new_df], ignore_index=True)

        # Определяем новый порядок колонок
        new_column_order = [
            'Data', 'Car Name', 'Car Year', 'Car Fuel Type', 'Car Mileage', 'Car Engine Capacity',
            'Car Price', 'Seller Type', 'Seller Location', 'Car Link', 'Photo Folder', 'Car Description'
        ]

        # Переставляем колонки в новый порядок
        updated_df = updated_df[new_column_order]

        # Сохраняем обновленный DataFrame обратно в исходный файл
        updated_df.to_excel("cars_data.xlsx", index=False)
        print("Таблица успешно обновлена!")
    else:
        print("Новых объявлений не найдено.")



# Пример использования
url = 'https://www.otomoto.pl/osobowe/mitsubishi/lancer?search%5Bfilter_float_price%3Ato%5D=27000&search%5Border%5D=created_at_first%3Adesc'
update_data(url)

with open("logs.txt", "a", encoding="utf-8") as f:  # "a" для добавления, "w" для перезаписи
    f.write(f'Задание было выполнено {datetime.now().strftime("%Y-%m-%d %H:%M:%S")}\n')  # Добавляем перевод строки

