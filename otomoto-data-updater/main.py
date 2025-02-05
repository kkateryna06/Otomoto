from update_new_ads import task, task_special
from update_relevant import task_relevant

print("1 - new ads\n2 - update relevant")
to_do = int(input())

if to_do == 1:
    print("1 - All cars\n2 - All special cars")
    to_do = int(input())
    # Parsing ads of all cars
    if to_do == 1:
        task()
    # Parsing ads of special cars
    elif to_do == 2:
        task_special()

elif to_do == 2:
    print("1 - All cars\n2 - Special cars")
    to_do = int(input())
    # Updating relevant state for all cars
    if to_do == 1:
        task_relevant("cars_info.xlsx")
    # Updating relevant state for special cars
    if to_do == 2:
        task_relevant("special_cars_info.xlsx")

