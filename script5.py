# -*- coding: utf-8 -*-
import re

with open('templates/admin_base.html.twig', 'r', encoding='utf-8') as f:
    content = f.read()

nav_content = '''<ul class="navbar-nav flex-column" id="navbar-sidebar">
                <!-- Menu item 1 -->
                <li class="nav-item"><a href="{{ path('app_dashboard') }}" class="nav-link"><i class="bi bi-house fa-fw me-2"></i>Dashboard</a></li>
                
                <!-- Title -->
                <li class="nav-item ms-2 my-2">Pages</li>

                <li class="nav-item"><a class="nav-link" href="{{ path('app_appel_offre_index') }}"><i class="bi bi-file-earmark-text fa-fw me-2"></i>Appels d'Offre</a></li>
                <li class="nav-item"><a class="nav-link" href="{{ path('app_candidature_index') }}"><i class="bi bi-people fa-fw me-2"></i>Candidatures</a></li>
                <li class="nav-item"><a class="nav-link" href="{{ path('app_categorie_index') }}"><i class="bi bi-tags fa-fw me-2"></i>Catégories</a></li>
                <li class="nav-item"><a class="nav-link" href="{{ path('app_user_index') }}"><i class="bi bi-person-gear fa-fw me-2"></i>Utilisateurs</a></li>
                
            </ul>'''

content = re.sub(r'<ul class="navbar-nav flex-column" id="navbar-sidebar">.*?</ul>', nav_content, content, flags=re.DOTALL)

with open('templates/admin_base.html.twig', 'w', encoding='utf-8') as f:
    f.write(content)
