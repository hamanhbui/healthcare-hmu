import pandas as pd
from datetime import datetime
import os
def read_watch_csv(file_path):
    column_names = ['id','timestamp','x-axis', 'y-axis', 'z-axis','x1-axis', 'y1-axis', 'z1-axis','activity','description']
    data = pd.read_csv(file_path,header = None, names = column_names, delimiter=r"\t")
    return data

def read_shoe_csv(file_path):
    column_names = ['id','timestamp','x-axis', 'y-axis', 'z-axis','activity','description']
    data = pd.read_csv(file_path,header = None, names = column_names, delimiter=r"\t")
    return data

def convert_watch_csv(file_path, exprorted_file_path):
    data=read_watch_csv(file_path)
    times=[0]
    for idx in range(len(data["timestamp"])-1):
        raw_1=data["timestamp"][idx]
        raw_2=data["timestamp"][idx+1]
        datetime_object_0 = datetime.strptime(raw_1, '%Y-%m-%d;%H:%M:%S.%f')
        datetime_object_1 = datetime.strptime(raw_2, '%Y-%m-%d;%H:%M:%S.%f')
        diff=datetime_object_1-datetime_object_0
        times.append(times[-1]+diff.total_seconds())

    data["timestamp"]=times
    data.to_csv(exprorted_file_path,header=None, sep='\t', encoding='utf-8', index=False, float_format='%.6f')

def convert_shoe_csv(file_path, exprorted_file_path):
    data=read_watch_csv(file_path)
    times=[0]
    for idx in range(len(data["timestamp"])-1):
        raw_1=data["timestamp"][idx]
        raw_2=data["timestamp"][idx+1]
        datetime_object_0 = datetime.strptime(raw_1, '%Y-%m-%d %H:%M:%S.%f')
        datetime_object_1 = datetime.strptime(raw_2, '%Y-%m-%d %H:%M:%S.%f')
        diff=datetime_object_1-datetime_object_0
        times.append(times[-1]+diff.total_seconds())

    data["timestamp"]=times
    data.to_csv(exprorted_file_path,header=None, sep='\t', encoding='utf-8', index=False, float_format='%.7f')

def main():
    folder_root="pat_03/"
    for subdir, dirs, files in os.walk(folder_root):
        for subdir in dirs:
            dir_name=folder_root+subdir
            left_watch_dir_name=dir_name+"/left_watch.txt"
            right_watch_dir_name=dir_name+"/right_watch.txt"
            right_shoe_dir_name=dir_name+"/right_shoe.txt"
            if os.path.exists(left_watch_dir_name):
                if not os.path.exists("out/"+dir_name):
                    os.makedirs("out/"+dir_name)
                convert_watch_csv(left_watch_dir_name, "out/"+left_watch_dir_name)
            if os.path.exists(right_watch_dir_name):
                if not os.path.exists("out/"+dir_name):
                    os.makedirs("out/"+dir_name)
                convert_watch_csv(right_watch_dir_name, "out/"+right_watch_dir_name)
            if os.path.exists(right_shoe_dir_name):
                if not os.path.exists("out/"+dir_name):
                    os.makedirs("out/"+dir_name)
                convert_shoe_csv(right_shoe_dir_name, "out/"+right_shoe_dir_name)

main()