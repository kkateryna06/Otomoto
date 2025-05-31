import requests
from datetime import datetime
from database_update import update_database, get_all_car_links_for_relevant_check


# Function to check the link
def fetch_html(url):
    """
    Checks whether the page is accessible via the specified link (whether the ad link is active).
    :param url: URL to check.
    :return:
        True if the page exists (status 200).
        False if the page is not found (status 404).
        None if another error occurred (e.g. 500).
    """
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
    """
    Checks all links in the database table.
    If the link is "dead" (the page has been deleted), updates the record in the database,
    marking the ad as irrelevant.
    :param database_table: Name of the database table containing car listings.
    :param date: Current timestamp (formatted as string), used for updating the relevance field.
    """
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
