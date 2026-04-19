import os

def add_flash_messages(file_path):
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()

    flash_messages = '''
    <div class="container mt-3">
        {% for message in app.flashes('success') %}
            <div class="alert alert-success alert-dismissible fade show">
                {{ message }}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        {% endfor %}
        {% for message in app.flashes('error') %}
            <div class="alert alert-danger alert-dismissible fade show">
                {{ message }}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        {% endfor %}
        {% for message in app.flashes('info') %}
            <div class="alert alert-info alert-dismissible fade show">
                <i class="fas fa-info-circle"></i> {{ message }}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        {% endfor %}
    </div>
'''

    content = content.replace('{% block body %}', flash_messages + '{% block body %}')

    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(content)

add_flash_messages('templates/base.html.twig')
add_flash_messages('templates/admin_base.html.twig')
