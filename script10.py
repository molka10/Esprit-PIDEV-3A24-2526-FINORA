# -*- coding: utf-8 -*-
import re

def fix_logo_links(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # Replace href="index.html" with href="{{ path('app_dashboard') }}"
    content = content.replace('href="index.html"', 'href="{{ path(\'app_dashboard\') }}"')
    
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)

fix_logo_links('templates/base.html.twig')
fix_logo_links('templates/admin_base.html.twig')
