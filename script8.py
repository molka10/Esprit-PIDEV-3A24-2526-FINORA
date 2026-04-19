# -*- coding: utf-8 -*-
with open('templates/dashboard/admin.html.twig', 'r', encoding='utf-8') as f:
    content = f.read()

# I will replace the custom stat boxes with the template's counter boxes
import re

# Remove the old HERO
content = re.sub(r'{# HERO #}.*?</div>\s*</div>\s*</div>', '', content, flags=re.DOTALL)

# Add the title at the top
title_html = '''
<!-- Title -->
<div class="row">
    <div class="col-12 mb-3">
        <h1 class="h3 mb-2 mb-sm-0">Dashboard Admin</h1>
    </div>
</div>
'''

content = content.replace('{% block body %}', '{% block body %}' + title_html)

# Replace the row of cards for Appels d'Offre with the new layout
old_appels_row = re.search(r'{# ROW 1 - Appels d\'offre #}.*?</div>\s*</div>\s*</div>\s*</div>\s*</div>', content, re.DOTALL)
if old_appels_row:
    new_appels_row = '''
<h5 class="mb-3 mt-4"><i class="fas fa-file-contract text-warning"></i> Appels d'Offre</h5>
<div class="row g-4 mb-4">
    <!-- Counter item -->
    <div class="col-md-6 col-xxl-3">
        <div class="card card-body bg-primary bg-opacity-10 p-4 h-100">
            <div class="d-flex justify-content-between align-items-center">
                <div>
                    <h2 class="purecounter mb-0 fw-bold">{{ totalAppels }}</h2>
                    <span class="mb-0 h6 fw-light">Total</span>
                </div>
                <div class="icon-lg rounded-circle bg-primary text-white mb-0"><i class="fas fa-file-contract fa-fw"></i></div>
            </div>
        </div>
    </div>
    <!-- Counter item -->
    <div class="col-md-6 col-xxl-3">
        <div class="card card-body bg-success bg-opacity-10 p-4 h-100">
            <div class="d-flex justify-content-between align-items-center">
                <div>
                    <h2 class="purecounter mb-0 fw-bold">{{ appelPublies }}</h2>
                    <span class="mb-0 h6 fw-light">Publiés</span>
                </div>
                <div class="icon-lg rounded-circle bg-success text-white mb-0"><i class="fas fa-check-circle fa-fw"></i></div>
            </div>
        </div>
    </div>
    <!-- Counter item -->
    <div class="col-md-6 col-xxl-3">
        <div class="card card-body bg-danger bg-opacity-10 p-4 h-100">
            <div class="d-flex justify-content-between align-items-center">
                <div>
                    <h2 class="purecounter mb-0 fw-bold">{{ appelClotures }}</h2>
                    <span class="mb-0 h6 fw-light">Clôturés</span>
                </div>
                <div class="icon-lg rounded-circle bg-danger text-white mb-0"><i class="fas fa-lock fa-fw"></i></div>
            </div>
        </div>
    </div>
    <!-- Counter item -->
    <div class="col-md-6 col-xxl-3">
        <div class="card card-body bg-secondary bg-opacity-10 p-4 h-100">
            <div class="d-flex justify-content-between align-items-center">
                <div>
                    <h2 class="purecounter mb-0 fw-bold">{{ appelBrouillons }}</h2>
                    <span class="mb-0 h6 fw-light">Brouillons</span>
                </div>
                <div class="icon-lg rounded-circle bg-secondary text-white mb-0"><i class="fas fa-edit fa-fw"></i></div>
            </div>
        </div>
    </div>
</div>
'''
    content = content[:old_appels_row.start()] + new_appels_row + content[old_appels_row.end():]

# Replace the row of cards for Candidatures
old_cand_row = re.search(r'{# ROW 2 - Candidatures #}.*?</div>\s*</div>\s*</div>\s*</div>\s*</div>', content, re.DOTALL)
if old_cand_row:
    new_cand_row = '''
<h5 class="mb-3"><i class="fas fa-users text-warning"></i> Candidatures</h5>
<div class="row g-4 mb-4">
    <!-- Counter item -->
    <div class="col-md-6 col-xxl-3">
        <div class="card card-body bg-primary bg-opacity-10 p-4 h-100">
            <div class="d-flex justify-content-between align-items-center">
                <div>
                    <h2 class="purecounter mb-0 fw-bold">{{ totalCandidatures }}</h2>
                    <span class="mb-0 h6 fw-light">Total</span>
                </div>
                <div class="icon-lg rounded-circle bg-primary text-white mb-0"><i class="fas fa-users fa-fw"></i></div>
            </div>
        </div>
    </div>
    <!-- Counter item -->
    <div class="col-md-6 col-xxl-3">
        <div class="card card-body bg-success bg-opacity-10 p-4 h-100">
            <div class="d-flex justify-content-between align-items-center">
                <div>
                    <h2 class="purecounter mb-0 fw-bold">{{ candidaturesAcceptees }}</h2>
                    <span class="mb-0 h6 fw-light">Acceptées</span>
                </div>
                <div class="icon-lg rounded-circle bg-success text-white mb-0"><i class="fas fa-thumbs-up fa-fw"></i></div>
            </div>
        </div>
    </div>
    <!-- Counter item -->
    <div class="col-md-6 col-xxl-3">
        <div class="card card-body bg-danger bg-opacity-10 p-4 h-100">
            <div class="d-flex justify-content-between align-items-center">
                <div>
                    <h2 class="purecounter mb-0 fw-bold">{{ candidaturesRejetes }}</h2>
                    <span class="mb-0 h6 fw-light">Rejetées</span>
                </div>
                <div class="icon-lg rounded-circle bg-danger text-white mb-0"><i class="fas fa-thumbs-down fa-fw"></i></div>
            </div>
        </div>
    </div>
    <!-- Counter item -->
    <div class="col-md-6 col-xxl-3">
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
'''
    content = content[:old_cand_row.start()] + new_cand_row + content[old_cand_row.end():]

# Replace ROW 3 Autres
old_autres = re.search(r'{# ROW 3 - Autres #}.*?</div>\s*</div>\s*</div>\s*</div>', content, re.DOTALL)
if old_autres:
    new_autres = '''
<div class="row g-4 mb-4">
    <!-- Counter item -->
    <div class="col-md-6">
        <div class="card card-body bg-info bg-opacity-10 p-4 h-100">
            <div class="d-flex justify-content-between align-items-center">
                <div>
                    <h2 class="purecounter mb-0 fw-bold">{{ totalCategories }}</h2>
                    <span class="mb-0 h6 fw-light">Catégories</span>
                </div>
                <div class="icon-lg rounded-circle bg-info text-white mb-0"><i class="fas fa-tags fa-fw"></i></div>
            </div>
        </div>
    </div>
    <!-- Counter item -->
    <div class="col-md-6">
        <div class="card card-body bg-purple bg-opacity-10 p-4 h-100">
            <div class="d-flex justify-content-between align-items-center">
                <div>
                    <h2 class="purecounter mb-0 fw-bold">{{ totalUsers }}</h2>
                    <span class="mb-0 h6 fw-light">Utilisateurs</span>
                </div>
                <div class="icon-lg rounded-circle bg-purple text-white mb-0"><i class="fas fa-user-cog fa-fw"></i></div>
            </div>
        </div>
    </div>
</div>
'''
    content = content[:old_autres.start()] + new_autres + content[old_autres.end():]

with open('templates/dashboard/admin.html.twig', 'w', encoding='utf-8') as f:
    f.write(content)

