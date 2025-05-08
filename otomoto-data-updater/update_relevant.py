import pandas as pd
import requests
from openpyxl import load_workbook
from datetime import datetime
import os

from database_update import update_database, get_all_car_links_for_relevant_check


# Function to check the link
def fetch_html(url):
    headers = {
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.212 Safari/537.36"
    }
    try:
        response = requests.get(url, headers=headers)
        response.raise_for_status()  # Check for a successful response
        return True
    except requests.exceptions.HTTPError as e:
        if e.response.status_code == 404:
            return False
        return None


# Function to update the "Relevant" column
def update_relevant_column_with_openpyxl(database_table, date):
    try:
        links = get_all_car_links_for_relevant_check(database_table)
        # Process each line
        for link in links:
            print(f"Checking {link}")
            is_active = fetch_html(link)  # Check if the URL is available
            if not is_active:
                update_database([link, date], "car_relevant", database_table)
        print("Db has been successfully updated!")
    except Exception as e:
        print(f"Error updating db file: {e}")


def task_relevant(database_table):
    date = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    update_relevant_column_with_openpyxl(database_table, date)
    with open("logs.txt", "a", encoding="utf-8") as f:  # "a" to append, "w" to overwrite
        f.write(f'Relevant info in {database_table} was updated {date}\n')  # Log
