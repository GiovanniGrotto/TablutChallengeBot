import os
import csv
import pandas as pd

def extract_game_info(file_path):
    game_info_list = []
    with open(file_path, 'r') as file:
        file_lines = file.readlines()
        current_result = file_lines[-1].strip()
        if len(current_result) != 2:
            return None
        for num, line in enumerate(file_lines, 1):
            if "Stato:" in line:
                current_board_state = str(file_lines[num:num+9]).replace("[", "").replace("]", "").replace("'", "").replace("\\n", "").replace(" ", "").replace(",", "")
                current_turn = str(file_lines[num+10]).replace("\n", "")
                game_info_list.append({"state": current_board_state, "turn":current_turn, "result": current_result})
    # Non ritorno l'ultima board perchè è quella vincente e sarà controllata dall'evaluation con le regole de gioco
    return game_info_list[:-1]


def fix_file(file_path):
    replaced_content = ""
    new_line = ""
    with open(file_path, 'r') as file:
        lines = file.readlines()
        for line in lines:
            # stripping line break
            line = line.strip()
            # replacing the texts
            if 'FINE: Stato:' in line and len(line) > len("FINE: Stato:  "):
                # Add a newline character after 'Stato:' if it exists in the line
                line = line.replace(' ', '')
                line = line.replace('FINE:Stato:', 'FINE:Stato:\n')
            # concatenate the new string and add an end-line break
            replaced_content = replaced_content + line + "\n"
    # Open the file in write mode and write the modified content back
    with open(file_path, 'w') as file:
        file.write(replaced_content)


def write_csv(csv_file, fieldnames, data):
    # Open the CSV file in write mode
    with open(csv_file, mode='w', newline='') as file:
        # Create a CSV writer object
        writer = csv.DictWriter(file, fieldnames=fieldnames)
        # Write the header row
        writer.writeheader()
        # Write the data rows
        for row in data:
            writer.writerow(row)

    print(f"Data written to {csv_file}")


def main():
    # Example usage:
    folder_path = 'C:\\Users\\giova\\OneDrive\\Desktop\\games'  # Replace with the path to your folder
    # Get a list of all files in the folder
    file_paths = [os.path.join(folder_path, filename) for filename in os.listdir(folder_path) if os.path.isfile(os.path.join(folder_path, filename))]

    games_info = []
    # Print the list of file paths
    for file_path in file_paths:
        #fix_file(file_path)
        games_info.append(extract_game_info(file_path))
    final_games_info = list(filter(lambda item: item is not None, games_info))
    final_games_info = list(filter(lambda item: len(item) > 4, final_games_info))
    flat_list = [item for sublist in final_games_info for item in sublist]
    write_csv("C:\\Users\\giova\\OneDrive\\Desktop\\raw_data.csv", ["result", "state", "turn"], flat_list)
    print()


if __name__ == "__main__":
    main()
