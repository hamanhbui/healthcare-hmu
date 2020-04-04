import pandas as pd
from datetime import datetime

def read_csv(file_path):
    column_names = ['ACEL','timestamp','155', 'id','x-axis', 'y-axis', 'z-axis']
    data = pd.read_csv(file_path,header = None, names = column_names, delimiter=r",")
    return data

folder_root="Pat_14_23_9/Recorder_2019_10_22_15_16/"
data=read_csv(folder_root+'/wax-2019-10-22-15-16-40.csv')
times=[0]
for idx in range(len(data["timestamp"])-1):
    raw_1=data["timestamp"][idx]
    raw_2=data["timestamp"][idx+1]
    datetime_object_0 = datetime.strptime(raw_1, '%Y-%m-%d %H:%M:%S.%f')
    datetime_object_1 = datetime.strptime(raw_2, '%Y-%m-%d %H:%M:%S.%f')
    diff=datetime_object_1-datetime_object_0
    times.append(times[-1]+diff.total_seconds())

data["timestamp"]=times
del data['ACEL']
del data['155']
del data['id']
print(data)
data.to_csv(folder_root+"/elan_shoes.csv",header=None, sep='\t', encoding='utf-8', index=False, float_format='%.7f')