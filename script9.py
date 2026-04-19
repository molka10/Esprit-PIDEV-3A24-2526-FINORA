import os
import glob

folders = ['user', 'categorie', 'candidature', 'appel_offre', 'rating']

for folder in folders:
    for filepath in glob.glob(f'templates/{folder}/*.html.twig'):
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()
        
        # Replace the hardcoded base.html.twig extension
        content = content.replace(
            "{% extends 'base.html.twig' %}", 
            "{% extends app.session.get('role') == 'admin' ? 'admin_base.html.twig' : 'base.html.twig' %}"
        )
        
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(content)

print('Updated Twig extensions.')
