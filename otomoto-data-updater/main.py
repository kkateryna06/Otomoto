import time

from update_relevant import task_relevant
from update_new_ads import load_links_from_file, run_links


print("1 - new ads\n2 - update relevant\n3 - auto mod")
to_do = int(input())

all_links, special_links = load_links_from_file()

if to_do == 1:
    print("1 - All cars\n2 - Special cars")
    dataset_type = int(input())

    if dataset_type == 1:
        run_links(all_links, "cars_info", "cars_info.xlsx")
    elif dataset_type == 2:
        run_links(special_links, "special_cars_info", "special_cars_info.xlsx")

elif to_do == 2:
    print("1 - All cars\n2 - Special cars")
    dataset_type = int(input())

    if dataset_type == 1:
        task_relevant("cars_info")
    elif dataset_type == 2:
        task_relevant("special_cars_info")

elif to_do == 3:
    count = 0
    while True:
        run_links(all_links, "cars_info", "cars_info.xlsx")
        run_links(special_links, "special_cars_info", "special_cars_info.xlsx")
        count += 1

        if count == 24:
            count = 0
            task_relevant("cars_info")
            task_relevant("special_cars_info")

        time.sleep(60*60)