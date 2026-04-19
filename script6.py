# -*- coding: utf-8 -*-
with open('index-3.html', 'r', encoding='utf-8') as f:
    content = f.read()

start_main = content.find('<!-- **************** MAIN CONTENT START **************** -->\n<main>\n') + len('<!-- **************** MAIN CONTENT START **************** -->\n<main>\n')
end_main = content.find('<!-- **************** MAIN CONTENT END **************** -->')

main_content = content[start_main:end_main]

import re
main_content = re.sub(r'(href|src)="assets/([^"]+)"', r'\1="{{ asset(\'assets/\2\') }}"', main_content)

twig_content = '''{% extends 'base.html.twig' %}

{% block title %}Plateforme des Appels d'Offre{% endblock %}

{% block body %}
''' + main_content + '''
{% endblock %}
'''

with open('templates/dashboard/visiteur.html.twig', 'w', encoding='utf-8') as f:
    f.write(twig_content)

print('Created visiteur.html.twig with index-3 content')
