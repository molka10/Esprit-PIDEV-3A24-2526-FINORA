# -*- coding: utf-8 -*-
import re

with open('index-3.html', 'r', encoding='utf-8') as f:
    content = f.read()

start_main = content.find('<!-- **************** MAIN CONTENT START **************** -->\n<main>\n') + len('<!-- **************** MAIN CONTENT START **************** -->\n<main>\n')
end_main = content.find('<!-- =======================\nCourse slider START -->')

hero_content = content[start_main:end_main]
hero_content = re.sub(r'(href|src)="assets/([^"]+)"', r'\1="{{ asset(\'assets/\2\') }}"', hero_content)

# Modify the hero text for entreprise
hero_content = hero_content.replace('Get started with Eduport', 'Espace Entreprise')
hero_content = hero_content.replace('Develop the skillset & your\n\t\t\t\t\t<span class="position-relative d-inline-block">Bright Future', 'Gérez vos <span class="position-relative d-inline-block">Appels d\\'Offre')
hero_content = hero_content.replace('The most reliable online courses and certifications in marketing, information technology, programming, and data science.', "Publiez et gérez vos appels d'offre en toute simplicité.")

# Add the cards from old entreprise.html.twig below the hero
entreprise_cards = '''
<div class="container mt-5 mb-5">
    <div class="row g-4">
        <div class="col-md-4">
            <div class="card card-body bg-success bg-opacity-10 p-4 h-100">
                <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <h2 class="purecounter mb-0 fw-bold">{{ appelPublies }}</h2>
                        <span class="mb-0 h6 fw-light">Appels d'Offre Ouverts</span>
                    </div>
                    <div class="icon-lg rounded-circle bg-success text-white mb-0"><i class="fas fa-file-contract fa-fw"></i></div>
                </div>
            </div>
        </div>
        <div class="col-md-4">
            <div class="card card-body bg-primary bg-opacity-10 p-4 h-100">
                <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <h2 class="purecounter mb-0 fw-bold">{{ totalCandidatures }}</h2>
                        <span class="mb-0 h6 fw-light">Total Candidatures</span>
                    </div>
                    <div class="icon-lg rounded-circle bg-primary text-white mb-0"><i class="fas fa-paper-plane fa-fw"></i></div>
                </div>
            </div>
        </div>
        <div class="col-md-4">
            <div class="card card-body bg-warning bg-opacity-15 p-4 h-100">
                <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <h2 class="purecounter mb-0 fw-bold">{{ candidaturesEnAttente }}</h2>
                        <span class="mb-0 h6 fw-light">En Attente</span>
                    </div>
                    <div class="icon-lg rounded-circle bg-warning text-white mb-0"><i class="fas fa-hourglass-half fa-fw"></i></div>
                </div>
            </div>
        </div>
    </div>
    
    <div class="text-center mt-4">
        <a href="{{ path('app_appel_offre_index') }}" class="btn btn-warning btn-lg">
            <i class="fas fa-search"></i> Voir les Appels d'Offre
        </a>
        <a href="{{ path('app_candidature_index') }}" class="btn btn-primary-soft btn-lg ms-2">
            <i class="fas fa-users"></i> Mes Candidatures
        </a>
    </div>
</div>
'''

twig_content = '''{% extends 'base.html.twig' %}

{% block title %}Dashboard Entreprise{% endblock %}

{% block body %}
''' + hero_content + entreprise_cards + '''
{% endblock %}
'''

with open('templates/dashboard/entreprise.html.twig', 'w', encoding='utf-8') as f:
    f.write(twig_content)

print('Created entreprise.html.twig with index-3 content')
