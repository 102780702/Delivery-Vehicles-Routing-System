import tkinter as tk
from tkinter import simpledialog, messagebox

# Define the coordinates of the dots (ignoring the capacity values provided)
coordinates = [
    (1.535546, 110.358202),
    (1.520725, 110.354431),
    (1.526855, 110.369588),
    (1.511337, 110.352239),
    (1.487123, 110.341599),
    (1.507103, 110.360874),
    (1.555485, 110.342379),
    (1.588484, 110.360216),
    (1.46668, 110.425148),
    (1.543655, 110.338852),
    (1.550192, 110.341737),
    (1.454789, 110.45877),
    (1.559603, 110.3448),
    (1.558697, 110.3445),
    (1.557889, 110.353534),
    (1.562618, 110.405601),
    (1.555303, 110.351251),
    (1.5615714, 110.3476374)
]

def on_dot_click(event, dot_id, coord):
    # Prompt user to enter capacity
    capacity = simpledialog.askinteger("Input", "Enter the capacity:", parent=root)
    if capacity is not None:  # Check if the user entered a value
        # Append the data to the file
        with open("coordinates_capacity.txt", "a") as file:
            file.write(f"{coord[0]},{coord[1]},{capacity}\n")
        # Change the dot color to red
        canvas.itemconfig(dot_id, fill='red')

def reset_canvas():
    # Reset all dots to blue and clear the text file
    for dot in dot_ids:
        canvas.itemconfig(dot, fill='blue')
    open("coordinates_capacity.txt", "w").close()  # Clear the content of the file

def finalize():
    # Confirm and close the application
    response = messagebox.askokcancel("Confirm", "Are you sure you want to finalize?")
    if response:
        root.destroy()

root = tk.Tk()
canvas_width, canvas_height = 600, 600
canvas = tk.Canvas(root, width=canvas_width, height=canvas_height)
canvas.pack()

# Buttons
ok_button = tk.Button(root, text="Okay", command=finalize)
ok_button.pack(side=tk.LEFT, padx=20, pady=20)

delete_button = tk.Button(root, text="Delete", command=reset_canvas)
delete_button.pack(side=tk.RIGHT, padx=20, pady=20)

# Normalize and scale coordinates
latitudes = [coord[0] for coord in coordinates]
longitudes = [coord[1] for coord in coordinates]

min_lat, max_lat = min(latitudes), max(latitudes)
min_long, max_long = min(longitudes), max(longitudes)

lat_scale = (canvas_height - 20) / (max_lat - min_lat)
long_scale = (canvas_width - 20) / (max_long - min_long)

def get_canvas_coords(lat, long):
    x = (long - min_long) * long_scale + 10
    y = (max_lat - lat) * lat_scale + 10
    return x, y

dot_ids = []  # Store canvas dot IDs to reset them later
for coord in coordinates:
    x, y = get_canvas_coords(coord[0], coord[1])
    dot = canvas.create_oval(x-5, y-5, x+5, y+5, fill='blue')
    dot_ids.append(dot)
    canvas.tag_bind(dot, "<Button-1>", lambda e, dot_id=dot, coord=coord: on_dot_click(e, dot_id, coord))

root.mainloop()
