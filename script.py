import re

def process_admin():
    with open('admin-dashboard.html', 'r', encoding='utf-8') as f:
        content = f.read()

    start_main = content.find('<!-- Page main content START -->')
    end_main = content.find('<!-- Page main content END -->')
    
    if start_main != -1 and end_main != -1:
        header = content[:start_main + len('<!-- Page main content START -->\n\t<div class="page-content-wrapper border">\n')]
        footer = content[end_main:]
        
        twig_content = header + '\n{% block body %}{% endblock %}\n' + footer
        
        # Replace assets
        twig_content = re.sub(r'(href|src)="assets/([^"]+)"', r'\1="{{ asset(\'assets/\2\') }}"', twig_content)
        
        with open('templates/admin_base.html.twig', 'w', encoding='utf-8') as f:
            f.write(twig_content)
            
        print('Created admin_base.html.twig')

process_admin()
