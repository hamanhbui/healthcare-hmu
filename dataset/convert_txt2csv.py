import pandas as pd
from datetime import datetime

def read_csv(file_path):
    column_names = ['timestamp','x-axis', 'y-axis', 'z-axis','x1-axis', 'y1-axis', 'z1-axis']
    data = pd.read_csv(file_path,header = None, names = column_names, delimiter=r"\s+")
    return data

folder_root="Pat_15_23_9/Recorder_2019_10_22_15_41/"
data=read_csv(folder_root+'/data_device_2.txt')

times=[0]
for idx in range(len(data["timestamp"])-1):
    raw_1=data["timestamp"][idx]
    raw_2=data["timestamp"][idx+1]
    datetime_object_0 = datetime.strptime(raw_1, '%Y-%m-%d;%H:%M:%S.%f')
    datetime_object_1 = datetime.strptime(raw_2, '%Y-%m-%d;%H:%M:%S.%f')
    diff=datetime_object_1-datetime_object_0
    times.append(times[-1]+diff.total_seconds())

data["timestamp"]=times

print(data)
data.to_csv(folder_root+"/data_device_2.csv",header=None, sep='\t', encoding='utf-8', index=False, float_format='%.6f')