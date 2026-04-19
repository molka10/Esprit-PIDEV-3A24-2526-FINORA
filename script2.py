import re

def add_blocks(file_path):
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()

    # Add block title
    content = re.sub(r'<title>.*?</title>', r'<title>{% block title %}Eduport{% endblock %}</title>', content)

    # Add block stylesheets before </head>
    content = content.replace('</head>', '\n\t{% block stylesheets %}{% endblock %}\n</head>')

    # Add block javascripts before </body>
    content = content.replace('</body>', '\n{% block javascripts %}{% endblock %}\n</body>')

    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(content)

add_blocks('templates/base.html.twig')
add_blocks('templates/admin_base.html.twig')
