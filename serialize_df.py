import pandas as pd
from extract_df import write_csv


def serialize_state(state):
    new_state = []
    for cell in state:
        if cell == "W":
            new_state += [1, 0, 0]
        elif cell == "B":
            new_state += [0, 1, 0]
        elif cell == "K":
            new_state += [0, 0, 1]
        else:
            new_state += [0, 0, 0]

    return new_state


def serialize_data(df):
    new_df = []

    for row in df.iterrows():
        new_state = serialize_state(row[1]['state'])
        new_state += [0] if row[1]['turn'] == "W" else [1]
        if row[1]['result'] == "WW":
            result = 1
        elif row[1]['result'] == "BW":
            result = 0
        else:
            result = 0.5
        new_df.append({'result': result, **{f'{i}': val for i, val in enumerate(new_state)}})

    return pd.DataFrame(new_df, dtype=int)


def read_csv(csv_file):
    # Read the CSV file into a DataFrame
    df = pd.read_csv(csv_file)
    return df


# La prima colonna è il risultato, e l'ultima il turno
def main():
    df = read_csv("C:\\Users\\giova\\OneDrive\\Desktop\\raw_data.csv")
    serialize_df = read_csv("C:\\Users\\giova\\OneDrive\\Desktop\\serialized_data.csv")
    serialize_df = serialize_data(df)
    serialize_df.to_csv('serialized_data.csv', index=False)


if __name__ == "__main__":
    main()
