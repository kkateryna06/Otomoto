import requests
from bs4 import BeautifulSoup
import json
import pandas as pd
import os
import urllib.parse


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


# Функция для извлечения информации о машинах
def update_data(url):
    html = fetch_html(url)
    if not html:
        return  # Если не удалось загрузить страницу, выходим из функции

    json_data = extract_json_from_html(html)
    links = extract_links(html)

    # Сохраняем HTML в файл с правильной кодировкой
    with open("file_url.html", "w", encoding="utf-8") as f:
        f.write(html)

    # Сохраняем JSON в текстовый файл
    if json_data:
        with open("file_json.html", "w", encoding="utf-8") as f:
            json.dump(json_data, f, ensure_ascii=False, indent=4)  # Форматируем JSON для читаемости

    with open('car_links.txt', 'w', encoding='utf-8') as file:
        for link in links:
            file.write(link + '\n')

    if json_data and "mainEntity" in json_data:
        offers = json_data["mainEntity"].get("itemListElement", [])
        car_data = []
        for i, offer in enumerate(offers):
            car = offer.get('itemOffered', {})
            car_name = car.get('name', 'Не указано')
            car_brand = car.get('brand', 'Не указано')
            car_fuel_type = car.get('fuelType', 'Не указано')
            car_mileage = car.get('mileageFromOdometer', {}).get('value', 'Не указано')
            car_price = offer.get('priceSpecification', {}).get('price', 'Не указано')
            car_link = links[i] if i < len(links) else 'Не указано'  # Привязываем ссылку к машине

            # Добавляем данные о машине в список
            car_data.append({"Car Name": car_name, "Car Brand": car_brand, "Car Fuel Type": car_fuel_type, "Car Mileage": car_mileage, "Car Price": car_price, "Car Link": car_link})

        # Преобразуем список в DataFrame
        #df = pd.DataFrame(car_data, columns=["Название", "Бренд", "Тип топлива", "Пробег (км)", "Цена (PLN)", "Ссылка"])

        # Сохраняем в Excel
        #df.to_excel("cars_data_with_links.xlsx", index=False)
        print("Данные успешно записаны в файл 'cars_data_with_links.xlsx'")

        desciption_list = []
        for i, link in enumerate(links):
            html = fetch_html(link)
            if not html:
                return  # Если не удалось загрузить страницу, выходим из функции

            # Извлекаем JSON данные из тега <script id="__NEXT_DATA__">
            json_data = extract_json_from_script_tag(html)

            if json_data:
                # Извлекаем описание (description)
                description_html = json_data.get("props", {}).get("pageProps", {}).get("advert", {}).get("description")

                car_data[i]["Car Description"] = (description_html)

        print(*car_data, sep="\n")




# Пример использования
url = 'https://www.otomoto.pl/osobowe/mitsubishi/lancer?search%5Border%5D=created_at%3Adesc'
update_data(url)