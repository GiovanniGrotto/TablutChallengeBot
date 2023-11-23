import pandas as pd
from extract_df import write_csv
import time

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


def basic_label(row):
    if row[1]['result'] == "WW":
        result = 1.0
    elif row[1]['result'] == "BW":
        result = 0.0
    else:
        result = 0.5
    return result


def each_turn_label(row):
    if row[1]['result'] == "WW":
        if row[1]['turn'] == "W":
            result = 1.0
        else:
            result = -1.0
    elif row[1]['result'] == "BW":
        if row[1]['turn'] == "B":
            result = 1.0
        else:
            result = -1.0
    else:
        result = 0.0
    return result


#TODO
def discount_basic_label(row):
    if row[1]['result'] == "WW":
        result = 1.0
    elif row[1]['result'] == "BW":
        result = 0.0
    else:
        result = 0.5
    return result


def serialize_data(df, label_type):
    new_df = []
    for row in df.iterrows():
        if row[0]%50000==0:
            print(row[0])
        row[1]['state'] = row[1]['index'][:-1].replace("\n","").replace("-","")
        row[1]['turn'] = row[1]['index'][-1]
        row[1]['result'] = row[1][0]
        new_state = serialize_state(row[1]['state'])
        new_state += [0] if row[1]['turn'] == "W" else [1]
        result = row[1]['result']
        if label_type == "basic_label":
            result = basic_label(row)
        elif label_type == "each_turn_label":
            result = each_turn_label(row)
        new_df.append({'result': result, **{f'{i}': val for i, val in enumerate(new_state)}})

    return pd.DataFrame(new_df, dtype=float)


def read_csv(csv_file):
    # Read the CSV file into a DataFrame
    df = pd.read_csv(csv_file)
    return df


LABEL_TYPE = "fromStateEvalMap1mln" #basic_label, each_turn_label, discounted_label
# La prima colonna è il risultato, e l'ultima il turno
def main():
    start = time.time()
    df = pd.read_json("C:\\Users\\giova\\OneDrive\\Desktop\\stateEvaluation.json", typ='series').reset_index()
    df = read_csv("C:\\Users\\giova\\OneDrive\\Desktop\\TablutChallengeBot\\python materials\\serialized_data_fromStateEvalMap.csv")
    #serialize_df = read_csv(f"C:\\Users\\giova\\OneDrive\\Desktop\\TablutChallengeBot\\python materials\\serialized_data_{LABEL_TYPE}.csv")
    df = df[:1000000]
    serialize_df = serialize_data(df, LABEL_TYPE)
    print("Writing the df")
    serialize_df.to_csv(f'serialized_data_{LABEL_TYPE}.csv', index=False)
    print(f"Time needed: {time.time()-start}")


if __name__ == "__main__":
    main()
