import os
import glob
import re

def fix_badges(file_path):
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # Replace Bootstrap 4 badge classes with Bootstrap 5
    content = content.replace('badge-success', 'bg-success text-white')
    content = content.replace('badge-danger', 'bg-danger text-white')
    content = content.replace('badge-warning', 'bg-warning text-dark')
    content = content.replace('badge-info', 'bg-info text-dark')
    content = content.replace('badge-secondary', 'bg-secondary text-white')
    
    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(content)

for root, _, files in os.walk('templates'):
    for file in files:
        if file.endswith('.twig'):
            fix_badges(os.path.join(root, file))

print('Badges fixed!')
