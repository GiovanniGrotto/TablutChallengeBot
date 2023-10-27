import keras
from keras.models import Sequential
from keras.layers import Dense, Dropout
import pandas as pd
import numpy as np
import tensorflow as tf
from keras_tuner import RandomSearch


def build_model(hp):
    model = keras.Sequential()
    # Tune the number of layers.
    for i in range(hp.Int("num_layers", 1, 4)):
        model.add(
            Dense(
                # Tune number of units separately.
                units=hp.Int(f"units_{i}", min_value=32, max_value=512, step=32),
                activation=hp.Choice("activation", ["relu", "tanh"]),
            )
        )
    if hp.Boolean("dropout"):
        model.add(Dropout(rate=0.25))
    model.add(Dense(1, activation="sigmoid"))
    learning_rate = hp.Float("lr", min_value=1e-4, max_value=1e-2, sampling="log")
    model.compile(
        optimizer=keras.optimizers.Adam(learning_rate=learning_rate),
        loss="mse",
        metrics=["accuracy"],
    )
    return model

# Neural network
model = Sequential()
model.add(Dense(80, input_dim=244, activation='relu'))
#model.add(Dense(500, activation='relu'))
model.add(Dense(50, activation='relu'))
model.add(Dense(10, activation='relu'))
model.add(Dense(1, activation='sigmoid'))

serialize_df = pd.read_csv("C:\\Users\\giova\\OneDrive\\Desktop\\serialized_data.csv")
serialize_data = serialize_df.to_numpy()
np.random.seed(seed=42)
np.random.shuffle(serialize_data)
#serialize_data = np.asarray(serialize_data).astype('float32')
split_train = int(len(serialize_df)*0.70)
splid_val = int(len(serialize_df)*0.80)
train_serial_data = serialize_data[:splid_val]
valid_serial_data = serialize_data[split_train:splid_val]
x_valid = valid_serial_data[:, 1:]
y_valid = valid_serial_data[:, 0]
test_serial_data = serialize_data[splid_val:]
x_test = test_serial_data[:, 1:]
y_test = test_serial_data[:, 0]
serialize_data_X = train_serial_data[:, 1:]
serialize_data_Y = train_serial_data[:, 0]

"""tuner = RandomSearch(
    build_model,
    objective='val_accuracy',
    max_trials=5,
    executions_per_trial=3,
    directory='my_dir',
    project_name='helloworld')
#build_model(keras_tuner.HyperParameters())
tuner.search(serialize_data_X, serialize_data_Y, epochs=5, validation_data=(x_valid, y_valid))
best_models = tuner.get_best_models(num_models=1)
test_loss, test_acc = best_models[0].evaluate(x_test, y_test)"""

#print('Test accuracy:', test_acc)

model.compile(loss='mse', optimizer=tf.keras.optimizers.Adam(
        learning_rate=0.002,
        beta_1=0.9,
        beta_2=0.999,
        epsilon=1e-8,), metrics=['accuracy'])

history = model.fit(serialize_data_X, serialize_data_Y, epochs=40, batch_size=128)

score, acc = model.evaluate(x_test, y_test, batch_size=128)
print(model.summary())
print('Test score:', score)
print('Test accuracy:', acc)

model.save("model.h5")
#y_pred = model.predict(tf.convert_to_tensor(serialize_data[20000][1:]))

"""
Test score: 0.2688962519168854
Test accuracy: 0.683170735836029

Test score: 0.28285324573516846
Test accuracy: 0.6931707262992859

Test score: 0.2713671028614044
Test accuracy: 0.6992682814598083

1048-500-50 learning_rate=0.001,beta_1=0.9,beta_2=0.999,epsilon=1e-8
Test score: 0.2570315897464752
Test accuracy: 0.7151219248771667

1048-500-50 learning_rate=0.001,beta_1=0.9,beta_2=0.999,epsilon=1e-8 Dropout 0.2
Test score: 0.28549084067344666
Test accuracy: 0.6875609755516052

1048-1048-500-500-50-10 learning_rate=0.003,beta_1=0.9,beta_2=0.999,epsilon=1e-8
Test score: 0.23946920037269592
Test accuracy: 0.7290244102478027

1048-1048-500-500-50-10 learning_rate=0.003,beta_1=0.9,beta_2=0.999,epsilon=1e-8 ep=10 batch=64 SHUFFLE 
Test score: 0.048595815896987915
Test accuracy: 0.9265853762626648

1048-1048-500-500-50-10 learning_rate=0.002,beta_1=0.9,beta_2=0.999,epsilon=1e-8 ep=100 batch=128 SHUFFLE 
Test score: 0.043026916682720184
Test accuracy: 0.9363414645195007

1048-500-50-10 learning_rate=0.002,beta_1=0.9,beta_2=0.999,epsilon=1e-8 ep=100 batch=128 SHUFFLE 
Test score: 0.04511101543903351
Test accuracy: 0.9353658556938171

1048-50-10 learning_rate=0.002,beta_1=0.9,beta_2=0.999,epsilon=1e-8 ep=40 batch=128 SHUFFLE 
Test score: 0.04495217278599739
Test accuracy: 0.9282926917076111
Total params: 309731 (1.18 MB)

512-50-10 learning_rate=0.002,beta_1=0.9,beta_2=0.999,epsilon=1e-8 ep=40 batch=128 SHUFFLE 
Test score: 0.046393606811761856
Test accuracy: 0.933170735836029
Total params: 151611 (592.23 KB)

50-50-10 learning_rate=0.002,beta_1=0.9,beta_2=0.999,epsilon=1e-8 ep=40 batch=128 SHUFFLE 
Test score: 0.04832370951771736
Test accuracy: 0.9243902564048767
Total params: 15321 (59.85 KB)


"""