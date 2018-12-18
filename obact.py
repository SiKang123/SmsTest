import json
import string
import random
import glob, os

used_names = []
activity_mangle_setting = {}

def process_file_content(path, file_name):
    print("processing content: %s..."%path)
    with open(path, "rt") as fin:
        file_content = fin.read()
        
        # replace the file content based on mangle settings
        for (src, dst) in activity_mangle_setting.items():
            file_content = file_content.replace(src, dst)

        ext = os.path.splitext(file_name)[1]   
        if ext == '.java':
            comment = "// machine renamed: %s\n"%file_name
            file_content = "%s%s"%(comment, file_content)
        with open(path, "wt") as fout:
            fout.write(file_content)
        print("done\n")
    return

# rand string for class names
def gen_rand_str():
    rand_str = ''.join(random.choices(string.ascii_uppercase + string.ascii_lowercase, k=8))
    while rand_str in used_names:
        rand_str = ''.join(random.choices(string.ascii_uppercase + string.ascii_lowercase, k=8))
    
    used_names.append(rand_str)
    return rand_str

# read configs
with open('obact.json', "rt") as f:
    json_root = json.load(f)
    activities = json_root["activities"]
    source_root = json_root["root"]
    manifest = json_root["manifest"]

# mangle activity names
for act_name in activities:
    activity_mangle_setting[act_name] = gen_rand_str()

print("activity mapping %s"%activity_mangle_setting)
    
root_path = os.path.join(os.getcwd(), source_root)
# perform content replace for the files
print("processing file content...")
for root, dirs, files in os.walk(source_root):
    directory = os.path.join(os.getcwd(), root)
    for file in files:
        if file.endswith(".java"):
            # get the path of the java file
            path = os.path.join(directory, file)
            
            process_file_content(path, file)
print("processing file content done")

print("renaming files")
for root, dirs, files in os.walk(source_root):
    directory = os.path.join(os.getcwd(), root)
    for file in files:
        if file.endswith(".java"):            
            name = os.path.splitext(file)[0]
            if name in activity_mangle_setting:
                # get the path of the java file
                src = os.path.join(directory, file)
                dst = os.path.join(directory, "%s.java"%activity_mangle_setting[name])
                
                print("%s -> %s"%(src, dst))
                os.rename(src, dst)
print("renaming files done")

manifest_path = os.path.join(os.getcwd(), manifest)
process_file_content(manifest_path, "AndroidManifest.xml")


