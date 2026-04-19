# -*- coding: utf-8 -*-

def fix_slashes(file_path):
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # Replace \'{ with ' and \'} with ' inside {{ asset(...) }}
    content = content.replace("asset(\\'", "asset('")
    content = content.replace("\\')", "')")
    
    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(content)

fix_slashes('templates/base.html.twig')
fix_slashes('templates/admin_base.html.twig')
fix_slashes('templates/dashboard/entreprise.html.twig')
fix_slashes('templates/dashboard/visiteur.html.twig')
