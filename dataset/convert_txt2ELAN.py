import pandas as pd
import numpy as np

def read_csv(file_path):
    column_names = ['id','label','start_frm', 'end_frm','NaN']
    data = pd.read_csv(file_path,header = None, names = column_names, delimiter=";")
    return data

data=read_csv("Pat_15_23_9/Recorder_2019_10_22_15_41/GH010375.txt")
print(data)
rs=[]
t=0.0
for idx in range(len(data["id"])):
    start_frm=data["start_frm"][idx]/30
    end_frm=data["end_frm"][idx]/30
    label=data["label"][idx]

    if t<start_frm:
        raw=[]
        raw.append(data["id"][idx])
        raw.append(t)
        raw.append(start_frm)
        raw.append("0")
        raw.append(np.nan)
        rs.append(raw)
        t=start_frm

    raw=[]
    raw.append(data["id"][idx])
    raw.append(t)
    raw.append(end_frm)
    raw.append(label)
    raw.append(data["NaN"][idx])
    rs.append(raw)
    t=end_frm

df = pd.DataFrame(rs,columns=['id','Begin Time - ss.msec','End Time - ss.msec','default','NaN'])
print(df)
df.to_csv("Pat_15_23_9/Recorder_2019_10_22_15_41/label.txt", sep="\t", encoding='utf-8', index=False, float_format='%.6f')